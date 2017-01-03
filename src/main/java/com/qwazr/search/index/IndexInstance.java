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
import com.qwazr.search.analysis.CustomAnalyzer;
import com.qwazr.search.analysis.UpdatableAnalyzer;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.field.FieldTypeInterface;
import com.qwazr.search.query.JoinQuery;
import com.qwazr.server.ServerException;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.json.JsonMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.LiveIndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.SnapshotDeletionPolicy;
import org.apache.lucene.index.Terms;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.replicator.IndexRevision;
import org.apache.lucene.replicator.LocalReplicator;
import org.apache.lucene.replicator.ReplicationClient;
import org.apache.lucene.replicator.Replicator;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.join.JoinUtil;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.lucene.store.Directory;

import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

final public class IndexInstance implements Closeable {

	private final IndexInstanceBuilder.FileSet fileSet;
	private final UUID indexUuid;
	private final String indexName;

	private final SchemaInstance schema;
	private final Directory dataDirectory;
	private final LiveIndexWriterConfig indexWriterConfig;
	private final SnapshotDeletionPolicy snapshotDeletionPolicy;
	private final IndexWriter indexWriter;

	private final SearcherManager searcherManager;
	private final ExecutorService executorService;
	private final IndexSettingsDefinition settings;
	private final FileResourceLoader fileResourceLoader;
	private final ClassLoaderManager classLoaderManager;

	private final LocalReplicator replicator;
	private final ReplicationClient replicationClient;
	private final IndexReplicator indexReplicator;
	private final ReentrantLock replicationLock;

	private final UpdatableAnalyzer indexAnalyzer;
	private final UpdatableAnalyzer queryAnalyzer;

	private volatile FieldMap fieldMap;
	private volatile LinkedHashMap<String, AnalyzerDefinition> analyzerMap;

	private volatile Pair<IndexReader, SortedSetDocValuesReaderState> facetsReaderStateCache;

	IndexInstance(final ClassLoaderManager classLoaderManager, final IndexInstanceBuilder builder) {
		this.classLoaderManager = classLoaderManager;
		this.schema = builder.schema;
		this.fileSet = builder.fileSet;
		this.indexName = builder.fileSet.indexDirectory.getName();
		this.indexUuid = builder.indexUuid;
		this.dataDirectory = builder.dataDirectory;
		this.analyzerMap = builder.analyzerMap;
		this.fieldMap = builder.fieldMap == null ? null : new FieldMap(builder.fieldMap);
		this.indexWriter = builder.indexWriter;
		if (builder.indexWriter != null) { // We are a master
			this.indexWriterConfig = indexWriter.getConfig();
			this.snapshotDeletionPolicy = (SnapshotDeletionPolicy) indexWriterConfig.getIndexDeletionPolicy();
		} else { // We are a slave (no write)
			this.indexWriterConfig = null;
			this.snapshotDeletionPolicy = null;
		}
		this.indexAnalyzer = builder.indexAnalyzer;
		this.queryAnalyzer = builder.queryAnalyzer;
		this.settings = builder.settings;
		this.searcherManager = builder.searcherManager;
		this.executorService = builder.executorService;
		this.fileResourceLoader = builder.fileResourceLoader;
		this.replicator = builder.replicator;
		this.replicationClient = builder.replicationClient;
		this.indexReplicator = builder.indexReplicator;
		this.replicationLock = new ReentrantLock(true);
		this.facetsReaderStateCache = null;
	}

	public IndexSettingsDefinition getSettings() {
		return settings;
	}

	boolean isIndexWriterOpen() {
		return indexWriter != null && indexWriter.isOpen();
	}

	@Override
	public void close() {
		IOUtils.closeQuietly(replicationClient, searcherManager, indexAnalyzer, queryAnalyzer, replicator);
		if (indexWriter != null && indexWriter.isOpen())
			IOUtils.closeQuietly(indexWriter);
		IOUtils.closeQuietly(dataDirectory);
	}

	/**
	 * Delete the index. The directory is deleted from the local file system.
	 */
	void delete() {
		close();
		if (fileSet.indexDirectory.exists())
			FileUtils.deleteQuietly(fileSet.indexDirectory);
	}

	private IndexStatus getIndexStatus() throws IOException {
		final IndexSearcher indexSearcher = searcherManager.acquire();
		try {
			return new IndexStatus(indexUuid, indexReplicator != null ? indexReplicator.getMasterUuid() : null,
					indexSearcher.getIndexReader(), indexWriter, snapshotDeletionPolicy, settings, analyzerMap.keySet(),
					fieldMap.getFieldDefinitionMap().keySet());
		} finally {
			searcherManager.release(indexSearcher);
		}
	}

	LinkedHashMap<String, FieldDefinition> getFields() {
		return fieldMap.getFieldDefinitionMap();
	}

	IndexStatus getStatus() throws IOException, InterruptedException {
		final Semaphore sem = schema.acquireReadSemaphore();
		try {
			return getIndexStatus();
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	private void refreshFieldsAnalyzers(final LinkedHashMap<String, AnalyzerDefinition> analyzers,
			final LinkedHashMap<String, FieldDefinition> fields) throws IOException {
		final AnalyzerContext analyzerContext =
				new AnalyzerContext(classLoaderManager, fileResourceLoader, analyzers, fields, true);
		indexAnalyzer.update(analyzerContext.indexAnalyzerMap);
		queryAnalyzer.update(analyzerContext.queryAnalyzerMap);
	}

	synchronized void setFields(final LinkedHashMap<String, FieldDefinition> fields)
			throws ServerException, IOException {
		JsonMapper.MAPPER.writeValue(fileSet.fieldMapFile, fields);
		fieldMap = new FieldMap(fields);
		refreshFieldsAnalyzers(analyzerMap, fields);
		schema.mayBeRefresh(true);
	}

	void setField(final String field_name, final FieldDefinition field) throws IOException, ServerException {
		final LinkedHashMap<String, FieldDefinition> fields =
				(LinkedHashMap<String, FieldDefinition>) fieldMap.getFieldDefinitionMap().clone();
		fields.put(field_name, field);
		setFields(fields);
	}

	void deleteField(final String field_name) throws IOException, ServerException {
		final LinkedHashMap<String, FieldDefinition> fields =
				(LinkedHashMap<String, FieldDefinition>) fieldMap.getFieldDefinitionMap().clone();
		if (fields.remove(field_name) == null)
			throw new ServerException(Response.Status.NOT_FOUND,
					"Field not found: " + field_name + " - Index: " + indexName);
		setFields(fields);
	}

	LinkedHashMap<String, AnalyzerDefinition> getAnalyzers() {
		return analyzerMap;
	}

	synchronized void setAnalyzers(final LinkedHashMap<String, AnalyzerDefinition> analyzers)
			throws ServerException, IOException {
		refreshFieldsAnalyzers(analyzerMap, fieldMap.getFieldDefinitionMap());
		JsonMapper.MAPPER.writeValue(fileSet.analyzerMapFile, analyzers);
		analyzerMap = analyzers;
		schema.mayBeRefresh(true);
	}

	void setAnalyzer(final String analyzerName, final AnalyzerDefinition analyzer) throws IOException, ServerException {
		final LinkedHashMap<String, AnalyzerDefinition> analyzers =
				(LinkedHashMap<String, AnalyzerDefinition>) analyzerMap.clone();
		analyzers.put(analyzerName, analyzer);
		setAnalyzers(analyzers);
	}

	List<TermDefinition> testAnalyzer(final String analyzerName, final String inputText)
			throws ServerException, InterruptedException, ReflectiveOperationException, IOException {
		final AnalyzerDefinition analyzerDefinition = analyzerMap.get(analyzerName);
		if (analyzerDefinition == null)
			throw new ServerException(Response.Status.NOT_FOUND,
					"Analyzer not found: " + analyzerName + " - Index: " + indexName);
		try (final Analyzer analyzer = new CustomAnalyzer(classLoaderManager, fileResourceLoader, analyzerDefinition)) {
			return TermDefinition.buildTermList(analyzer, StringUtils.EMPTY, inputText);
		}
	}

	void deleteAnalyzer(final String analyzerName) throws IOException, ServerException {
		final LinkedHashMap<String, AnalyzerDefinition> analyzers =
				(LinkedHashMap<String, AnalyzerDefinition>) analyzerMap.clone();
		if (analyzers.remove(analyzerName) == null)
			throw new ServerException(Response.Status.NOT_FOUND,
					"Analyzer not found: " + analyzerName + " - Index: " + indexName);
		setAnalyzers(analyzers);
	}

	Analyzer getIndexAnalyzer(final String field) throws ServerException, IOException {
		return indexAnalyzer.getWrappedAnalyzer(field);
	}

	Analyzer getQueryAnalyzer(final String field) throws ServerException, IOException {
		return queryAnalyzer.getWrappedAnalyzer(field);
	}

	public Query createJoinQuery(final JoinQuery joinQuery)
			throws IOException, ParseException, ReflectiveOperationException, QueryNodeException {
		final Semaphore sem = schema.acquireReadSemaphore();
		try {
			final IndexSearcher indexSearcher = searcherManager.acquire();
			try {
				final Query fromQuery = joinQuery.from_query == null ?
						new MatchAllDocsQuery() :
						joinQuery.from_query.getQuery(buildQueryContext(indexSearcher, null));
				return JoinUtil.createJoinQuery(joinQuery.from_field, joinQuery.multiple_values_per_document,
						joinQuery.to_field, fromQuery, indexSearcher,
						joinQuery.score_mode == null ? ScoreMode.None : joinQuery.score_mode);
			} finally {
				searcherManager.release(indexSearcher);
			}
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	private void nrtCommit() throws IOException {
		indexWriter.flush();
		indexWriter.commit();
		replicator.publish(new IndexRevision(indexWriter));
		searcherManager.maybeRefresh();
		schema.mayBeRefresh(true);
	}

	final synchronized BackupStatus backup(final File backupIndexDirectory) throws IOException {
		checkIsMaster();
		final Semaphore sem = schema.acquireReadSemaphore();
		try {
			if (!backupIndexDirectory.exists())
				backupIndexDirectory.mkdir();
			if (!backupIndexDirectory.exists() || !backupIndexDirectory.isDirectory())
				throw new IOException(
						"Cannot create the backup directory: " + backupIndexDirectory.getAbsolutePath() + " - Index: "
								+ indexName);
			// Get the existing files
			final Map<String, File> fileMap = new HashMap<>();
			for (File file : backupIndexDirectory.listFiles((FileFilter) FileFileFilter.FILE))
				fileMap.put(file.getName(), file);
			final IndexCommit commit = snapshotDeletionPolicy.snapshot();
			try {
				for (String fileName : commit.getFileNames()) {
					fileMap.remove(fileName);
					final File sourceFile = new File(fileSet.dataDirectory, fileName);
					final File targetFile = new File(backupIndexDirectory, fileName);
					if (targetFile.exists() && targetFile.length() == sourceFile.length()
							&& targetFile.lastModified() == sourceFile.lastModified())
						continue;
					FileUtils.copyFile(sourceFile, targetFile, true);
				}
				// Delete files from previous backup
				fileMap.forEach((name, file) -> file.delete());
				return BackupStatus.newBackupStatus(backupIndexDirectory);
			} catch (IOException e) {
				FileUtils.deleteQuietly(backupIndexDirectory);
				throw e;
			} finally {
				snapshotDeletionPolicy.release(commit);
			}
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	final BackupStatus getBackup(final File backupIndexDirectory) throws IOException {
		checkIsMaster();
		final Semaphore sem = schema.acquireReadSemaphore();
		try {
			return BackupStatus.newBackupStatus(backupIndexDirectory);
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	final void checkIsMaster() {
		if (indexWriter == null)
			throw new UnsupportedOperationException(
					"Writing in a read only index (slave) is not allowed: " + indexName);
	}

	final UUID checkRemoteMasterUUID(final String remoteMasterUuid, final UUID localUuid) {
		final UUID uuid = UUID.fromString(remoteMasterUuid);
		if (!Objects.equals(uuid, localUuid))
			throw new ServerException(Response.Status.NOT_ACCEPTABLE,
					"The UUID of the local index and the remote index does not match: " + localUuid + " <> "
							+ remoteMasterUuid + " - " + indexName);
		return uuid;
	}

	final Replicator getReplicator(final String remoteMasterUuid) {
		checkRemoteMasterUUID(remoteMasterUuid, indexUuid);
		return replicator;
	}

	void replicationCheck() throws IOException {
		if (replicationClient == null)
			throw new ServerException(Response.Status.NOT_ACCEPTABLE,
					"No replication master has been setup - Index: " + indexName);

		final Semaphore sem = schema.acquireWriteSemaphore();

		try {

			// We only want one replication at a time
			replicationLock.lock();
			try {

				// Check that the master is the right one
				indexReplicator.checkRemoteMasterUuid();

				//Sync resources
				final Map<String, ResourceInfo> localResources = getResources();
				indexReplicator.getMasterResources().forEach((remoteName, remoteInfo) -> {
					final ResourceInfo localInfo = localResources.remove(remoteName);
					if (localInfo != null && localInfo.equals(remoteInfo))
						return;
					try (final InputStream input = indexReplicator.getResource(remoteName)) {
						postResource(remoteName, remoteInfo.lastModified, input);
					} catch (IOException e) {
						throw new ServerException(
								"Cannot replicate the resource " + remoteName + " - Index: " + indexName, e);
					}
				});
				localResources.forEach((resourceName, resourceInfo) -> deleteResource(resourceName));

				//Sync analyzer and fields
				setAnalyzers(indexReplicator.getMasterAnalyzers());
				setFields(indexReplicator.getMasterFields());

				// Lucene index replication
				long masterVersion = indexReplicator.getMasterStatus().version;
				long slaveVersion = getIndexStatus().version;
				if (masterVersion == slaveVersion) // same version, nothing to do
					return;
				if (slaveVersion > masterVersion)
					throw new ServerException(Response.Status.NOT_ACCEPTABLE,
							"The slave version is greater than the master version: " + slaveVersion + " / "
									+ masterVersion + " - Index: " + indexName);
				replicationClient.updateNow();

			} finally {
				replicationLock.unlock();
			}
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	final void deleteAll() throws IOException {
		checkIsMaster();
		final Semaphore sem = schema.acquireWriteSemaphore();
		try {
			indexWriter.deleteAll();
			nrtCommit();
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	private RecordsPoster.UpdateObjectDocument getDocumentPoster(final Map<String, Field> fields) {
		return new RecordsPoster.UpdateObjectDocument(fields, fieldMap, indexWriter);
	}

	private RecordsPoster.UpdateMapDocument getDocumentPoster() {
		return new RecordsPoster.UpdateMapDocument(fieldMap, indexWriter);
	}

	private RecordsPoster.UpdateObjectDocValues getDocValuesPoster(final Map<String, Field> fields) {
		return new RecordsPoster.UpdateObjectDocValues(fields, fieldMap, indexWriter);
	}

	private RecordsPoster.UpdateMapDocValues getDocValuesPoster() {
		return new RecordsPoster.UpdateMapDocValues(fieldMap, indexWriter);
	}

	final <T> int postDocument(final Map<String, Field> fields, final T document)
			throws IOException, InterruptedException {
		if (document == null)
			return 0;
		checkIsMaster();
		final Semaphore sem = schema.acquireWriteSemaphore();
		try {
			schema.checkSize(1);
			final RecordsPoster.UpdateObjectDocument poster = getDocumentPoster(fields);
			poster.accept(document);
			nrtCommit();
			return poster.counter;
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	final int postMappedDocument(final Map<String, Object> document) throws IOException, InterruptedException {
		if (document == null || document.isEmpty())
			return 0;
		checkIsMaster();
		final Semaphore sem = schema.acquireWriteSemaphore();
		try {
			schema.checkSize(1);
			final RecordsPoster.UpdateMapDocument poster = getDocumentPoster();
			poster.accept(document);
			nrtCommit();
			return poster.counter;
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	final int postMappedDocuments(final Collection<Map<String, Object>> documents)
			throws IOException, InterruptedException {
		if (documents == null || documents.isEmpty())
			return 0;
		checkIsMaster();
		final Semaphore sem = schema.acquireWriteSemaphore();
		try {
			schema.checkSize(documents.size());
			final RecordsPoster.UpdateMapDocument poster = getDocumentPoster();
			documents.forEach(poster);
			nrtCommit();
			return poster.counter;
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	final <T> int postDocuments(final Map<String, Field> fields, final Collection<T> documents)
			throws IOException, InterruptedException {
		if (documents == null || documents.isEmpty())
			return 0;
		checkIsMaster();
		final Semaphore sem = schema.acquireWriteSemaphore();
		try {
			schema.checkSize(documents.size());
			final RecordsPoster.UpdateObjectDocument poster = getDocumentPoster(fields);
			documents.forEach(poster);
			nrtCommit();
			return poster.counter;
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	final <T> int postDocuments(final Map<String, Field> fields, final T... documents)
			throws IOException, InterruptedException {
		if (documents == null || documents.length == 0)
			return 0;
		checkIsMaster();
		final Semaphore sem = schema.acquireWriteSemaphore();
		try {
			schema.checkSize(documents.length);
			final RecordsPoster.UpdateObjectDocument poster = getDocumentPoster(fields);
			for (T document : documents)
				poster.accept(document);
			nrtCommit();
			return poster.counter;
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	final <T> int updateDocValues(final Map<String, Field> fields, final T document)
			throws InterruptedException, IOException {
		if (document == null)
			return 0;
		checkIsMaster();
		final Semaphore sem = schema.acquireWriteSemaphore();
		try {
			final RecordsPoster.UpdateObjectDocValues poster = getDocValuesPoster(fields);
			poster.accept(document);
			nrtCommit();
			return poster.counter;
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	final int updateMappedDocValues(final Map<String, Object> document) throws IOException, InterruptedException {
		if (document == null || document.isEmpty())
			return 0;
		checkIsMaster();
		final Semaphore sem = schema.acquireWriteSemaphore();
		try {
			final RecordsPoster.UpdateMapDocValues poster = getDocValuesPoster();
			poster.accept(document);
			nrtCommit();
			return poster.counter;
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	final <T> int updateDocsValues(final Map<String, Field> fields, final Collection<T> documents)
			throws IOException, InterruptedException {
		if (documents == null || documents.isEmpty())
			return 0;
		checkIsMaster();
		final Semaphore sem = schema.acquireWriteSemaphore();
		try {
			final RecordsPoster.UpdateObjectDocValues poster = getDocValuesPoster(fields);
			documents.forEach(poster);
			nrtCommit();
			return poster.counter;
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	final <T> int updateDocsValues(final Map<String, Field> fields, final T... documents)
			throws IOException, InterruptedException {
		if (documents == null || documents.length == 0)
			return 0;
		checkIsMaster();
		final Semaphore sem = schema.acquireWriteSemaphore();
		try {
			final RecordsPoster.UpdateObjectDocValues poster = getDocValuesPoster(fields);
			for (T document : documents)
				poster.accept(document);
			nrtCommit();
			return poster.counter;
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	final int updateMappedDocsValues(final Collection<Map<String, Object>> documents)
			throws IOException, ServerException, InterruptedException {
		if (documents == null || documents.isEmpty())
			return 0;
		checkIsMaster();
		final Semaphore sem = schema.acquireWriteSemaphore();
		try {
			RecordsPoster.UpdateMapDocValues poster = getDocValuesPoster();
			documents.forEach(poster);
			nrtCommit();
			return poster.counter;
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	final ResultDefinition.WithMap deleteByQuery(final QueryDefinition queryDefinition)
			throws IOException, InterruptedException, QueryNodeException, ParseException, ServerException,
			ReflectiveOperationException {
		checkIsMaster();
		Objects.requireNonNull(queryDefinition, "The queryDefinition is missing - Index: " + indexName);
		Objects.requireNonNull(queryDefinition.query, "The query is missing - Index: " + indexName);
		final Semaphore sem = schema.acquireWriteSemaphore();
		try {
			final QueryContext queryContext =
					new QueryContext(schema, fileResourceLoader, null, executorService, indexAnalyzer, queryAnalyzer,
							fieldMap, null, queryDefinition);
			final Query query = queryDefinition.query.getQuery(queryContext);
			int docs = indexWriter.numDocs();
			indexWriter.deleteDocuments(query);
			nrtCommit();
			docs -= indexWriter.numDocs();
			return new ResultDefinition.WithMap(docs);
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	final List<TermEnumDefinition> getTermsEnum(final String fieldName, final String prefix, final Integer start,
			final Integer rows) throws InterruptedException, IOException {
		Objects.requireNonNull(fieldName, "The field name is missing - Index: " + indexName);
		final Semaphore sem = schema.acquireReadSemaphore();
		try {
			final IndexSearcher indexSearcher = searcherManager.acquire();
			try {
				FieldMap.Item fieldMapItem = fieldMap.find(fieldName);
				if (fieldMapItem == null)
					throw new ServerException(Response.Status.NOT_FOUND,
							"Field not found: " + fieldName + " - Index: " + indexName);
				FieldTypeInterface fieldType = FieldTypeInterface.getInstance(fieldMapItem);
				Terms terms = MultiFields.getTerms(indexSearcher.getIndexReader(), fieldName);
				if (terms == null)
					return Collections.emptyList();
				return TermEnumDefinition.buildTermList(fieldType, terms.iterator(), prefix, start == null ? 0 : start,
						rows == null ? 20 : rows);
			} finally {
				searcherManager.release(indexSearcher);
			}
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	private synchronized SortedSetDocValuesReaderState getFacetsState(final IndexReader indexReader)
			throws IOException {
		Pair<IndexReader, SortedSetDocValuesReaderState> current = facetsReaderStateCache;
		if (current != null && current.getLeft() == indexReader)
			return current.getRight();
		SortedSetDocValuesReaderState newState = IndexUtils.getNewFacetsState(indexReader);
		facetsReaderStateCache = Pair.of(indexReader, newState);
		return newState;
	}

	final private QueryContext buildQueryContext(final IndexSearcher indexSearcher,
			final QueryDefinition queryDefinition) throws IOException {
		if (indexWriterConfig != null)
			indexSearcher.setSimilarity(indexWriterConfig.getSimilarity());
		final SortedSetDocValuesReaderState facetsState = getFacetsState(indexSearcher.getIndexReader());
		return new QueryContext(schema, fileResourceLoader, indexSearcher, executorService, indexAnalyzer,
				queryAnalyzer, fieldMap, facetsState, queryDefinition);
	}

	final ResultDefinition search(final QueryDefinition queryDefinition,
			final ResultDocumentBuilder.BuilderFactory<?> documentBuilderFactory)
			throws IOException, InterruptedException, ParseException, ReflectiveOperationException, QueryNodeException {
		final Semaphore sem = schema.acquireReadSemaphore();
		try {
			final IndexSearcher indexSearcher = searcherManager.acquire();
			try {
				return new QueryExecution(buildQueryContext(indexSearcher, queryDefinition)).execute(
						documentBuilderFactory);
			} finally {
				searcherManager.release(indexSearcher);
			}
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	final Explanation explain(final QueryDefinition queryDefinition, final int docId)
			throws IOException, ParseException, ReflectiveOperationException, QueryNodeException {
		final Semaphore sem = schema.acquireReadSemaphore();
		try {
			final IndexSearcher indexSearcher = searcherManager.acquire();
			try {
				return new QueryExecution(buildQueryContext(indexSearcher, queryDefinition)).explain(docId);
			} finally {
				searcherManager.release(indexSearcher);
			}
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	Directory getDataDirectory() {
		return dataDirectory;
	}

	void fillFields(final Map<String, FieldDefinition> fields) {
		if (fields == null)
			return;
		this.fieldMap.getFieldDefinitionMap().forEach((name, fieldDef) -> {
			if (!fields.containsKey(name))
				fields.put(name, fieldDef);
		});
	}

	void fillAnalyzers(final Map<String, AnalyzerDefinition> analyzers) {
		if (analyzers == null)
			return;
		this.analyzerMap.forEach((name, analyzerDef) -> {
			if (!analyzers.containsKey(name))
				analyzers.put(name, analyzerDef);
		});
	}

	public static class ResourceInfo {

		public final long lastModified;
		public final long length;

		public ResourceInfo() {
			lastModified = 0;
			length = 0;
		}

		private ResourceInfo(final File file) {
			lastModified = file.lastModified();
			length = file.length();
		}

		@Override
		public boolean equals(final Object o) {
			if (o == null || !(o instanceof ResourceInfo))
				return false;
			final ResourceInfo info = (ResourceInfo) o;
			return lastModified == info.lastModified && length == info.length;
		}

	}

	final void postResource(final String resourceName, final long lastModified, final InputStream inputStream)
			throws IOException {
		if (!fileSet.resourcesDirectory.exists())
			fileSet.resourcesDirectory.mkdir();
		final File resourceFile = fileResourceLoader.checkResourceName(resourceName);
		IOUtils.copy(inputStream, resourceFile);
		resourceFile.setLastModified(lastModified);
		refreshFieldsAnalyzers((LinkedHashMap<String, AnalyzerDefinition>) analyzerMap.clone(),
				fieldMap.getFieldDefinitionMap());
		schema.mayBeRefresh(true);
	}

	final LinkedHashMap<String, ResourceInfo> getResources() {
		final LinkedHashMap<String, ResourceInfo> map = new LinkedHashMap<>();
		if (!fileSet.resourcesDirectory.exists())
			return map;
		final File[] files = fileSet.resourcesDirectory.listFiles();
		if (files == null)
			return map;
		for (File file : files)
			map.put(file.getName(), new ResourceInfo(file));
		return map;
	}

	final InputStream getResource(final String resourceName) throws IOException {
		if (!fileSet.resourcesDirectory.exists())
			throw new ServerException(Response.Status.NOT_FOUND,
					"Resource not found : " + resourceName + " - Index: " + indexName);
		return fileResourceLoader.openResource(resourceName);
	}

	final void deleteResource(final String resourceName) {
		if (!fileSet.resourcesDirectory.exists())
			throw new ServerException(Response.Status.NOT_FOUND,
					"Resource not found : " + resourceName + " - Index: " + indexName);
		final File resourceFile = fileResourceLoader.checkResourceName(resourceName);
		if (!resourceFile.exists())
			throw new ServerException(Response.Status.NOT_FOUND,
					"Resource not found : " + resourceName + " - Index: " + indexName);
		resourceFile.delete();
	}

	final FileResourceLoader newResourceLoader(final FileResourceLoader resourceLoader) {
		return new FileResourceLoader(classLoaderManager, resourceLoader, fileSet.resourcesDirectory);
	}

}
