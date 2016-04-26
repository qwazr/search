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
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.SnapshotDeletionPolicy;
import org.apache.lucene.replicator.IndexRevision;
import org.apache.lucene.replicator.LocalReplicator;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

class IndexInstanceBuilder {

	final static String INDEX_DATA = "data";
	final static String INDEX_BACKUP = "backup";
	final static String SETTINGS_FILE = "settings.json";
	final static String FIELDS_FILE = "fields.json";
	final static String ANALYZERS_FILE = "analyzers.json";

	static class FileSet {

		final File settingsFile;
		final File indexDirectory;
		final File backupDirectory;
		final File dataDirectory;
		final File analyzerMapFile;
		final File fieldMapFile;

		private FileSet(File indexDirectory) {
			this.indexDirectory = indexDirectory;
			this.backupDirectory = new File(indexDirectory, INDEX_BACKUP);
			this.dataDirectory = new File(indexDirectory, INDEX_DATA);
			this.analyzerMapFile = new File(indexDirectory, ANALYZERS_FILE);
			this.fieldMapFile = new File(indexDirectory, FIELDS_FILE);
			this.settingsFile = new File(indexDirectory, SETTINGS_FILE);
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

	LocalReplicator replicator = null;

	private IndexInstanceBuilder(final SchemaInstance schema, final File indexDirectory,
			final IndexSettingsDefinition settings) {
		this.schema = schema;
		this.indexDirectory = indexDirectory;
		this.settings = settings;
		this.fileSet = new FileSet(indexDirectory);
	}

	private IndexInstance build() throws IOException, ReflectiveOperationException, InterruptedException {

		if (!indexDirectory.exists())
			indexDirectory.mkdir();
		if (!indexDirectory.isDirectory())
			throw new IOException("This name is not valid. No directory exists for this location: " + indexDirectory);

		//Loading the settings
		if (settings == null)
			settings = fileSet.settingsFile.exists() ?
					JsonMapper.MAPPER.readValue(fileSet.settingsFile, IndexSettingsDefinition.class) :
					IndexSettingsDefinition.EMPTY;
		else
			JsonMapper.MAPPER.writeValue(fileSet.settingsFile, settings);

		//Loading the fields
		File fieldMapFile = new File(indexDirectory, FIELDS_FILE);
		fieldMap = fieldMapFile.exists() ?
				JsonMapper.MAPPER.readValue(fieldMapFile, FieldDefinition.MapStringFieldTypeRef) :
				new LinkedHashMap<>();

		//Loading the fields
		File analyzerMapFile = new File(indexDirectory, ANALYZERS_FILE);
		analyzerMap = analyzerMapFile.exists() ?
				JsonMapper.MAPPER.readValue(analyzerMapFile, AnalyzerDefinition.MapStringAnalyzerTypeRef) :
				new LinkedHashMap<>();

		AnalyzerContext context = new AnalyzerContext(analyzerMap, fieldMap);
		indexAnalyzer = new UpdatableAnalyzer(context, context.indexAnalyzerMap);
		queryAnalyzer = new UpdatableAnalyzer(context, context.queryAnalyzerMap);

		// Open and lock the data directory
		dataDirectory = FSDirectory.open(fileSet.dataDirectory.toPath());

		// Set
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(indexAnalyzer);
		if (settings != null && settings.similarity_class != null)
			indexWriterConfig.setSimilarity(IndexUtils.findSimilarity(settings.similarity_class));
		indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
		SnapshotDeletionPolicy snapshotDeletionPolicy = new SnapshotDeletionPolicy(
				indexWriterConfig.getIndexDeletionPolicy());
		indexWriterConfig.setIndexDeletionPolicy(snapshotDeletionPolicy);
		indexWriter = new IndexWriter(dataDirectory, indexWriterConfig);
		if (indexWriter.hasUncommittedChanges())
			indexWriter.commit();

		// Finally we build the SearchSearcherManger
		searcherManager = new SearcherManager(indexWriter, null);

		replicator = new LocalReplicator();
		replicator.publish(new IndexRevision(indexWriter));

		return new IndexInstance(this);
	}

	private void abort() {
		IOUtils.closeQuietly(searcherManager, queryAnalyzer, indexAnalyzer, replicator, indexWriter, dataDirectory);
	}

	static IndexInstance build(final SchemaInstance schema, final File indexDirectory,
			final IndexSettingsDefinition settings)
			throws InterruptedException, ReflectiveOperationException, IOException {
		final IndexInstanceBuilder builder = new IndexInstanceBuilder(schema, indexDirectory, settings);
		try {
			return builder.build();
		} catch (IOException | ReflectiveOperationException | InterruptedException e) {
			builder.abort();
			throw e;
		}
	}
}
