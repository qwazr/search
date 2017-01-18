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

import com.qwazr.classloader.ClassLoaderManager;
import com.qwazr.search.analysis.AnalyzerContext;
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.analysis.UpdatableAnalyzer;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.server.ServerException;
import com.qwazr.utils.IOUtils;
import org.apache.lucene.facet.taxonomy.SearcherTaxonomyManager;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.SerialMergeScheduler;
import org.apache.lucene.index.SnapshotDeletionPolicy;
import org.apache.lucene.index.TieredMergePolicy;
import org.apache.lucene.replicator.IndexReplicationHandler;
import org.apache.lucene.replicator.IndexRevision;
import org.apache.lucene.replicator.LocalReplicator;
import org.apache.lucene.replicator.PerSessionDirectoryFactory;
import org.apache.lucene.replicator.ReplicationClient;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

class IndexInstanceBuilder {

	final SchemaInstance schema;
	final IndexFileSet fileSet;

	final ClassLoaderManager classLoaderManager;
	final ExecutorService executorService;

	private final IndexServiceInterface indexService;
	private final SearcherFactory searcherFactory;

	final IndexSettingsDefinition settings;
	final FileResourceLoader fileResourceLoader;
	final UUID indexUuid;

	Directory dataDirectory = null;
	Directory taxonomyDirectory = null;

	LinkedHashMap<String, AnalyzerDefinition> analyzerMap = null;
	LinkedHashMap<String, FieldDefinition> fieldMap = null;

	IndexWriter indexWriter = null;
	DirectoryTaxonomyWriter taxonomyWriter = null;

	SearcherTaxonomyManager searcherTaxonomyManager = null;

	UpdatableAnalyzer indexAnalyzer = null;
	UpdatableAnalyzer queryAnalyzer = null;

	LocalReplicator replicator = null;
	ReplicationClient replicationClient = null;
	IndexReplicator indexReplicator = null;

	IndexInstanceBuilder(final SchemaInstance schema, final IndexFileSet fileSet,
			final IndexSettingsDefinition settings, UUID indexUuid) {
		this.schema = schema;
		this.fileSet = fileSet;
		this.classLoaderManager = schema.getClassLoaderManager();
		this.executorService = schema.getExecutorService();
		this.settings = settings;
		this.searcherFactory = schema.getSearcherFactory();
		this.indexService = schema.getService();
		this.fileResourceLoader = new FileResourceLoader(classLoaderManager, null, fileSet.resourcesDirectory);
		this.indexUuid = indexUuid;
	}

	private void buildCommon() throws IOException, ReflectiveOperationException, URISyntaxException {

		analyzerMap = fileSet.loadAnalyzerMap();
		fieldMap = fileSet.loadFieldMap();

		final AnalyzerContext context =
				new AnalyzerContext(classLoaderManager, fileResourceLoader, analyzerMap, fieldMap, false);
		indexAnalyzer = new UpdatableAnalyzer(context.indexAnalyzerMap);
		queryAnalyzer = new UpdatableAnalyzer(context.queryAnalyzerMap);

		// Open and lock the index directories
		dataDirectory = getDirectory(settings, fileSet.dataDirectory);
		taxonomyDirectory = getDirectory(settings, fileSet.taxonomyDirectory);
	}

	static Directory getDirectory(IndexSettingsDefinition settings, File dataDirectory) throws IOException {
		return settings.directoryType == null || settings.directoryType == IndexSettingsDefinition.Type.FSDirectory ?
				FSDirectory.open(dataDirectory.toPath()) :
				new RAMDirectory();
	}

	private void openOrCreateDataIndex() throws ReflectiveOperationException, IOException {

		final IndexWriterConfig indexWriterConfig = new IndexWriterConfig(indexAnalyzer);
		indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
		if (settings != null) {
			if (settings.similarityClass != null && !settings.similarityClass.isEmpty())
				indexWriterConfig.setSimilarity(
						IndexUtils.findSimilarity(classLoaderManager, settings.similarityClass));
			if (settings.ramBufferSize != null)
				indexWriterConfig.setRAMBufferSizeMB(settings.ramBufferSize);

			final TieredMergePolicy mergePolicy = new TieredMergePolicy();
			if (settings.maxMergeAtOnce != null)
				mergePolicy.setMaxMergeAtOnce(settings.maxMergeAtOnce);
			if (settings.maxMergedSegmentMB != null)
				mergePolicy.setMaxMergedSegmentMB(settings.maxMergedSegmentMB);
			if (settings.segmentsPerTier != null)
				mergePolicy.setSegmentsPerTier(settings.segmentsPerTier);
			indexWriterConfig.setMergePolicy(mergePolicy);
		}

		final SerialMergeScheduler serialMergeScheduler = new SerialMergeScheduler();
		indexWriterConfig.setMergeScheduler(serialMergeScheduler);

		final SnapshotDeletionPolicy snapshotDeletionPolicy =
				new SnapshotDeletionPolicy(indexWriterConfig.getIndexDeletionPolicy());
		indexWriterConfig.setIndexDeletionPolicy(snapshotDeletionPolicy);

		indexWriter = new IndexWriter(dataDirectory, indexWriterConfig);
		if (indexWriter.hasUncommittedChanges())
			indexWriter.commit();
	}

	private void openOrCreateTaxonomyIndex() throws IOException {
		taxonomyWriter = new DirectoryTaxonomyWriter(taxonomyDirectory, IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
	}

	private void buildSlave() throws IOException, URISyntaxException, ReflectiveOperationException {

		// We just want to be sure the index exists.
		openOrCreateDataIndex();
		indexWriter.close();
		indexWriter = null;

		openOrCreateTaxonomyIndex();
		taxonomyWriter.close();
		taxonomyWriter = null;

		Callable<Boolean> callback = () -> {
			searcherTaxonomyManager.maybeRefresh();
			return true;
		};
		ReplicationClient.ReplicationHandler handler = new IndexReplicationHandler(dataDirectory, callback);
		ReplicationClient.SourceDirectoryFactory factory = new PerSessionDirectoryFactory(fileSet.replWorkPath);
		indexReplicator = new IndexReplicator(indexService, settings.master, fileSet.uuidMasterFile);
		replicationClient = new ReplicationClient(indexReplicator, handler, factory);

		// we build the SearcherManager
		searcherTaxonomyManager = new SearcherTaxonomyManager(dataDirectory, taxonomyDirectory, searcherFactory);
	}

	private void buildMaster() throws IOException, ReflectiveOperationException {

		openOrCreateDataIndex();
		openOrCreateTaxonomyIndex();

		// Manage the master replication (revision publishing)
		replicator = new LocalReplicator();
		replicator.publish(new IndexRevision(indexWriter));

		// Finally we build the SearcherManager
		searcherTaxonomyManager = new SearcherTaxonomyManager(indexWriter, searcherFactory, taxonomyWriter);
	}

	private void abort() {
		IOUtils.closeQuietly(replicationClient, searcherTaxonomyManager, indexAnalyzer, queryAnalyzer, replicator);

		if (taxonomyWriter != null)
			IOUtils.closeQuietly(taxonomyWriter);
		IOUtils.closeQuietly(taxonomyDirectory);
		taxonomyDirectory = null;

		if (indexWriter != null && indexWriter.isOpen())
			IOUtils.closeQuietly(indexWriter);
		IOUtils.closeQuietly(dataDirectory);
		dataDirectory = null;
	}

	IndexInstance build() throws ReflectiveOperationException, IOException, URISyntaxException {
		try {
			buildCommon();
			if (fileSet.uuidMasterFile.exists() || settings.master != null)
				buildSlave();
			else
				buildMaster();
			return new IndexInstance(this);
		} catch (IOException | ReflectiveOperationException | URISyntaxException e) {
			abort();
			throw e;
		} catch (Exception e) {
			abort();
			throw new ServerException(e);
		}
	}
}
