/**
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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
import com.qwazr.search.analysis.AnalyzerFactory;
import com.qwazr.search.analysis.CustomAnalyzer;
import com.qwazr.search.analysis.UpdatableAnalyzer;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.server.ServerException;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.concurrent.ReadWriteSemaphores;
import org.apache.lucene.index.ConcurrentMergeScheduler;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MergeScheduler;
import org.apache.lucene.index.NoMergeScheduler;
import org.apache.lucene.index.SegmentInfos;
import org.apache.lucene.index.SerialMergeScheduler;
import org.apache.lucene.index.SnapshotDeletionPolicy;
import org.apache.lucene.index.TieredMergePolicy;
import org.apache.lucene.replicator.LocalReplicator;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static org.apache.lucene.replicator.IndexAndTaxonomyRevision.SnapshotDirectoryTaxonomyWriter;

class IndexInstanceBuilder {

	final IndexFileSet fileSet;
	final ExecutorService executorService;
	final ReadWriteSemaphores readWriteSemaphores;
	final IndexInstance.Provider indexProvider;

	private final IndexServiceInterface indexService;

	final IndexSettingsDefinition settings;
	final FileResourceLoader fileResourceLoader;
	final UUID indexUuid;

	Directory dataDirectory = null;
	Directory taxonomyDirectory = null;

	private IndexWriter indexWriter = null;
	private SnapshotDirectoryTaxonomyWriter taxonomyWriter = null;

	final Map<String, AnalyzerFactory> globalAnalyzerFactoryMap;
	LinkedHashMap<String, CustomAnalyzer.Factory> localAnalyzerFactoryMap = null;
	LinkedHashMap<String, FieldDefinition> fieldMap = null;

	WriterAndSearcher writerAndSearcher = null;

	UpdatableAnalyzer indexAnalyzer = null;
	UpdatableAnalyzer queryAnalyzer = null;

	LocalReplicator localReplicator = null;
	IndexReplicator indexReplicator = null;

	Similarity similarity = null;
	SearcherFactory searcherFactory = null;

	IndexInstanceBuilder(final IndexInstance.Provider indexProvider,
			final Map<String, AnalyzerFactory> globalAnalyzerFactoryMap, final ReadWriteSemaphores readWriteSemaphores,
			final ExecutorService executorService, final IndexServiceInterface indexService, final IndexFileSet fileSet,
			final IndexSettingsDefinition settings, UUID indexUuid) {
		this.fileSet = fileSet;
		this.executorService = executorService;
		this.readWriteSemaphores = readWriteSemaphores;
		this.indexProvider = indexProvider;
		this.settings = settings;
		this.globalAnalyzerFactoryMap = globalAnalyzerFactoryMap;
		this.indexService = indexService;
		this.fileResourceLoader = new FileResourceLoader(null, fileSet.resourcesDirectory);
		this.indexUuid = indexUuid;
	}

	private void buildCommon() throws IOException, ReflectiveOperationException, URISyntaxException {

		if (settings.similarityClass != null && !settings.similarityClass.isEmpty())
			similarity = IndexUtils.findSimilarity(settings.similarityClass);

		searcherFactory = MultiThreadSearcherFactory.of(executorService, similarity);

		localAnalyzerFactoryMap = fileSet.loadAnalyzerDefinitionMap();
		fieldMap = fileSet.loadFieldMap();

		final AnalyzerContext context =
				new AnalyzerContext(fileResourceLoader, fieldMap, false, globalAnalyzerFactoryMap,
						localAnalyzerFactoryMap);
		indexAnalyzer = new UpdatableAnalyzer(context.indexAnalyzerMap);
		queryAnalyzer = new UpdatableAnalyzer(context.queryAnalyzerMap);

		// Open and lock the index directories
		dataDirectory = getDirectory(settings, fileSet.dataDirectory);
		taxonomyDirectory = IndexSettingsDefinition.useTaxonomyIndex(settings) ?
				getDirectory(settings, fileSet.taxonomyDirectory) :
				null;
	}

	static Directory getDirectory(IndexSettingsDefinition settings, File dataDirectory) throws IOException {
		return settings == null || settings.directoryType == null
				|| settings.directoryType == IndexSettingsDefinition.Type.FSDirectory ?
				FSDirectory.open(dataDirectory.toPath()) :
				new RAMDirectory();
	}

	private final static int MERGE_SCHEDULER_SSD_THREADS =
			Math.max(1, Math.min(4, Runtime.getRuntime().availableProcessors() / 2));

	private void openOrCreateDataIndex() throws IOException {

		final IndexWriterConfig indexWriterConfig = new IndexWriterConfig(indexAnalyzer);
		indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
		if (settings != null) {
			if (similarity != null)
				indexWriterConfig.setSimilarity(similarity);
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

			final MergeScheduler mergeScheduler;
			if (settings.mergeScheduler != null) {
				switch (settings.mergeScheduler) {
				case NO:
					mergeScheduler = NoMergeScheduler.INSTANCE;
					break;
				default:
				case CONCURRENT:
					mergeScheduler = new ConcurrentMergeScheduler();
					((ConcurrentMergeScheduler) mergeScheduler).setMaxMergesAndThreads(MERGE_SCHEDULER_SSD_THREADS,
							MERGE_SCHEDULER_SSD_THREADS);
					break;
				case SERIAL:
					mergeScheduler = new SerialMergeScheduler();
					break;
				}
				indexWriterConfig.setMergeScheduler(mergeScheduler);
			}
		}

		final SnapshotDeletionPolicy snapshotDeletionPolicy =
				new SnapshotDeletionPolicy(indexWriterConfig.getIndexDeletionPolicy());
		indexWriterConfig.setIndexDeletionPolicy(snapshotDeletionPolicy);

		indexWriter = checkCommit(new IndexWriter(dataDirectory, indexWriterConfig));
	}

	private IndexWriter checkCommit(final IndexWriter indexWriter) throws IOException {
		if (indexWriter.hasUncommittedChanges())
			indexWriter.commit();
		return indexWriter;
	}

	private void openOrCreateTaxonomyIndex() throws IOException {
		taxonomyWriter = new SnapshotDirectoryTaxonomyWriter(taxonomyDirectory);
		checkCommit(taxonomyWriter.getIndexWriter());
	}

	private void buildSlave() throws IOException, URISyntaxException {

		indexReplicator = new IndexReplicator(indexService, settings.master, fileSet.uuidMasterFile, dataDirectory,
				taxonomyDirectory, fileSet.replWorkPath, () -> false);

		if (taxonomyDirectory != null) {
			if (SegmentInfos.getLastCommitGeneration(dataDirectory) < 0
					|| SegmentInfos.getLastCommitGeneration(taxonomyDirectory) < 0)
				indexReplicator.updateNow();
		} else {
			if (SegmentInfos.getLastCommitGeneration(dataDirectory) < 0) {
				openOrCreateDataIndex();
				IOUtils.closeQuietly(indexWriter);
				indexWriter = null;
			}
		}

		writerAndSearcher = WriterAndSearcher.of(dataDirectory, taxonomyDirectory, searcherFactory);
	}

	private void buildMaster() throws IOException {

		openOrCreateDataIndex();
		if (IndexSettingsDefinition.useTaxonomyIndex(settings))
			openOrCreateTaxonomyIndex();

		// Finally we build the SearcherManager
		writerAndSearcher = WriterAndSearcher.of(indexWriter, taxonomyWriter, searcherFactory);

		// Manage the master replication (revision publishing)
		localReplicator = new LocalReplicator();
		localReplicator.publish(writerAndSearcher.newRevision());
	}

	private void abort() {
		IOUtils.closeQuietly(indexReplicator, writerAndSearcher, indexAnalyzer, queryAnalyzer, localReplicator);

		if (taxonomyWriter != null) {
			IOUtils.closeQuietly(taxonomyWriter);
			taxonomyWriter = null;
		}

		if (taxonomyDirectory != null) {
			IOUtils.closeQuietly(taxonomyDirectory);
			taxonomyDirectory = null;
		}

		if (indexWriter != null && indexWriter.isOpen()) {
			IOUtils.closeQuietly(indexWriter);
			indexWriter = null;
		}

		if (dataDirectory != null) {
			IOUtils.closeQuietly(dataDirectory);
			dataDirectory = null;
		}
	}

	IndexInstance build() throws ReflectiveOperationException, IOException, URISyntaxException {
		try {
			buildCommon();
			if (settings.master != null && settings.master.schema != null && settings.master.index != null)
				buildSlave();
			else
				buildMaster();
			return new IndexInstance(this);
		} catch (IOException | ReflectiveOperationException | URISyntaxException e) {
			abort();
			throw e;
		} catch (Exception e) {
			abort();
			throw ServerException.getServerException(e);
		}
	}

}
