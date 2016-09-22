/**
 * Copyright 2015-2016 Emmanuel Keller / QWAZR
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.index;

import com.qwazr.search.analysis.AnalyzerContext;
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.analysis.UpdatableAnalyzer;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.json.JsonMapper;
import com.qwazr.utils.server.ServerException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.SnapshotDeletionPolicy;
import org.apache.lucene.replicator.*;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;

class IndexInstanceBuilder {

	final static String INDEX_DATA = "data";
	final static String INDEX_BACKUP = "backup";
	final static String REPL_WORK = "repl_work";
	final static String SETTINGS_FILE = "settings.json";
	final static String FIELDS_FILE = "fields.json";
	final static String ANALYZERS_FILE = "analyzers.json";
	final static String RESOURCES_DIR = "resources";

	static class FileSet {

		final File settingsFile;
		final File indexDirectory;
		final File backupDirectory;
		final File dataDirectory;
		final File analyzerMapFile;
		final File resourcesDirectory;
		final File fieldMapFile;
		final Path replWorkPath;

		private FileSet(File indexDirectory) {
			this.indexDirectory = indexDirectory;
			this.backupDirectory = new File(indexDirectory, INDEX_BACKUP);
			this.dataDirectory = new File(indexDirectory, INDEX_DATA);
			this.analyzerMapFile = new File(indexDirectory, ANALYZERS_FILE);
			this.resourcesDirectory = new File(indexDirectory, RESOURCES_DIR);
			this.fieldMapFile = new File(indexDirectory, FIELDS_FILE);
			this.settingsFile = new File(indexDirectory, SETTINGS_FILE);
			this.replWorkPath = indexDirectory.toPath().resolve(REPL_WORK);
		}
	}

	final SchemaInstance schema;
	final File indexDirectory;
	final FileSet fileSet;

	IndexSettingsDefinition settings;

	Directory dataDirectory = null;

	LinkedHashMap<String, AnalyzerDefinition> analyzerMap = null;
	LinkedHashMap<String, FieldDefinition> fieldMap = null;

	IndexWriter indexWriter = null;
	SearcherManager searcherManager = null;
	UpdatableAnalyzer indexAnalyzer = null;
	UpdatableAnalyzer queryAnalyzer = null;

	FileResourceLoader fileResourceLoader = null;

	LocalReplicator replicator = null;
	ReplicationClient replicationClient = null;
	IndexReplicator indexReplicator = null;

	private IndexInstanceBuilder(final SchemaInstance schema, final File indexDirectory,
			final IndexSettingsDefinition settings) {
		this.schema = schema;
		this.indexDirectory = indexDirectory;
		this.settings = settings;
		this.fileSet = new FileSet(indexDirectory);
	}

	private void buildCommon()
			throws IOException, ReflectiveOperationException, InterruptedException, URISyntaxException {

		if (!indexDirectory.exists())
			indexDirectory.mkdir();
		if (!indexDirectory.isDirectory())
			throw new IOException("This name is not valid. No directory exists for this location: " +
					indexDirectory.getAbsolutePath());

		//Loading the settings
		if (settings == null)
			settings = fileSet.settingsFile.exists() ?
					JsonMapper.MAPPER.readValue(fileSet.settingsFile, IndexSettingsDefinition.class) :
					IndexSettingsDefinition.EMPTY;
		else
			JsonMapper.MAPPER.writeValue(fileSet.settingsFile, settings);

		//Loading the fields
		final File fieldMapFile = new File(indexDirectory, FIELDS_FILE);
		fieldMap = fieldMapFile.exists() ?
				JsonMapper.MAPPER.readValue(fieldMapFile, FieldDefinition.MapStringFieldTypeRef) :
				new LinkedHashMap<>();

		//Loading the fields
		final File analyzerMapFile = new File(indexDirectory, ANALYZERS_FILE);
		analyzerMap = analyzerMapFile.exists() ?
				JsonMapper.MAPPER.readValue(analyzerMapFile, AnalyzerDefinition.MapStringAnalyzerTypeRef) :
				new LinkedHashMap<>();

		// Set the resource loader
		fileResourceLoader = new FileResourceLoader(null, fileSet.resourcesDirectory);

		final AnalyzerContext context = new AnalyzerContext(fileResourceLoader, analyzerMap, fieldMap, false);
		indexAnalyzer = new UpdatableAnalyzer(context.indexAnalyzerMap);
		queryAnalyzer = new UpdatableAnalyzer(context.queryAnalyzerMap);

		// Open and lock the data directory
		dataDirectory = FSDirectory.open(fileSet.dataDirectory.toPath());

	}

	private void openOrCreateIndex() throws InterruptedException, ReflectiveOperationException, IOException {
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(indexAnalyzer);
		if (settings != null && settings.similarity_class != null && !settings.similarity_class.isEmpty())
			indexWriterConfig.setSimilarity(IndexUtils.findSimilarity(settings.similarity_class));
		indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
		SnapshotDeletionPolicy snapshotDeletionPolicy =
				new SnapshotDeletionPolicy(indexWriterConfig.getIndexDeletionPolicy());
		indexWriterConfig.setIndexDeletionPolicy(snapshotDeletionPolicy);
		indexWriter = new IndexWriter(dataDirectory, indexWriterConfig);
		if (indexWriter.hasUncommittedChanges())
			indexWriter.commit();
	}

	private void buildSlave()
			throws IOException, URISyntaxException, ReflectiveOperationException, InterruptedException {

		// We just want to be sure the index exists.
		openOrCreateIndex();
		indexWriter.close();
		indexWriter = null;

		Callable<Boolean> callback = () -> {
			searcherManager.maybeRefresh();
			schema.mayBeRefresh(false);
			return true;
		};
		ReplicationClient.ReplicationHandler handler = new IndexReplicationHandler(dataDirectory, callback);
		ReplicationClient.SourceDirectoryFactory factory = new PerSessionDirectoryFactory(fileSet.replWorkPath);
		indexReplicator = new IndexReplicator(settings.master);
		replicationClient = new ReplicationClient(indexReplicator, handler, factory);

		// Finally we build the SearcherManager
		searcherManager = new SearcherManager(dataDirectory, null);
	}

	private void buildMaster() throws IOException, ReflectiveOperationException, InterruptedException {

		openOrCreateIndex();

		// Manage the master replication (revision publishing)
		replicator = new LocalReplicator();
		replicator.publish(new IndexRevision(indexWriter));

		// Finally we build the SearcherManager
		searcherManager = new SearcherManager(indexWriter, null);
	}

	private IndexInstance build()
			throws InterruptedException, ReflectiveOperationException, URISyntaxException, IOException {
		buildCommon();
		if (settings.master != null && settings.master.length > 0)
			buildSlave();
		else
			buildMaster();
		return new IndexInstance(this);
	}

	private void abort() {
		IOUtils.closeQuietly(replicationClient, searcherManager, indexAnalyzer, queryAnalyzer, replicator);
		if (indexWriter != null && indexWriter.isOpen())
			IOUtils.closeQuietly(indexWriter);
		IOUtils.closeQuietly(dataDirectory);
	}

	static IndexInstance build(final SchemaInstance schema, final File indexDirectory,
			final IndexSettingsDefinition settings)
			throws InterruptedException, ReflectiveOperationException, IOException, URISyntaxException {
		final IndexInstanceBuilder builder = new IndexInstanceBuilder(schema, indexDirectory, settings);
		try {
			return builder.build();
		} catch (IOException | ReflectiveOperationException | InterruptedException | URISyntaxException e) {
			builder.abort();
			throw e;
		} catch (Exception e) {
			builder.abort();
			throw new ServerException(e);
		}
	}
}
