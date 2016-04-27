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
import com.qwazr.search.analysis.CustomAnalyzer;
import com.qwazr.search.analysis.UpdatableAnalyzer;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.json.JsonMapper;
import com.qwazr.utils.server.ServerException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.replicator.LocalReplicator;
import org.apache.lucene.replicator.ReplicationClient;
import org.apache.lucene.replicator.Replicator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Semaphore;

final public class IndexInstance implements Closeable {

	private static final Logger logger = LoggerFactory.getLogger(IndexInstance.class);

	private final IndexInstanceBuilder.FileSet fileSet;

	private final SchemaInstance schema;
	private final Directory dataDirectory;
	private final LiveIndexWriterConfig indexWriterConfig;
	private final SnapshotDeletionPolicy snapshotDeletionPolicy;
	private final IndexWriter indexWriter;
	private final SearcherManager searcherManager;
	private final IndexSettingsDefinition settings;

	private final LocalReplicator replicator;
	private final ReplicationClient replicationClient;

	private final UpdatableAnalyzer indexAnalyzer;
	private final UpdatableAnalyzer queryAnalyzer;
	private volatile LinkedHashMap<String, FieldDefinition> fieldMap;
	private volatile LinkedHashMap<String, AnalyzerDefinition> analyzerMap;

	private volatile Pair<IndexReader, SortedSetDocValuesReaderState> facetsReaderStateCache;

	IndexInstance(IndexInstanceBuilder builder) {
		this.schema = builder.schema;
		this.fileSet = builder.fileSet;
		this.dataDirectory = builder.dataDirectory;
		this.analyzerMap = builder.analyzerMap;
		this.fieldMap = builder.fieldMap;
		this.indexWriter = builder.indexWriter;
		this.indexWriterConfig = indexWriter.getConfig();
		this.indexAnalyzer = (UpdatableAnalyzer) indexWriterConfig.getAnalyzer();
		this.queryAnalyzer = builder.queryAnalyzer;
		this.snapshotDeletionPolicy = (SnapshotDeletionPolicy) indexWriterConfig.getIndexDeletionPolicy();
		this.settings = builder.settings;
		this.searcherManager = builder.searcherManager;
		this.replicator = builder.replicator;
		this.replicationClient = builder.replicationClient;
		this.facetsReaderStateCache = null;
	}

	public IndexSettingsDefinition getSettings() {
		return settings;
	}

	@Override
	public void close() {
		IOUtils.closeQuietly(replicationClient, searcherManager, indexAnalyzer, queryAnalyzer, replicator);
		if (indexWriter.isOpen())
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
			return new IndexStatus(indexSearcher.getIndexReader(), settings, analyzerMap.keySet(), fieldMap.keySet());
		} finally {
			searcherManager.release(indexSearcher);
		}
	}

	LinkedHashMap<String, FieldDefinition> getFields() {
		return fieldMap;
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

	synchronized void setFields(LinkedHashMap<String, FieldDefinition> fields) throws ServerException, IOException {
		AnalyzerContext analyzerContext = new AnalyzerContext(analyzerMap, fields);
		indexAnalyzer.update(analyzerContext, analyzerContext.indexAnalyzerMap);
		queryAnalyzer.update(analyzerContext, analyzerContext.queryAnalyzerMap);
		JsonMapper.MAPPER.writeValue(fileSet.fieldMapFile, fields);
		fieldMap = fields;
	}

	void setField(String field_name, FieldDefinition field) throws IOException, ServerException {
		LinkedHashMap<String, FieldDefinition> fields = (LinkedHashMap<String, FieldDefinition>) fieldMap.clone();
		fields.put(field_name, field);
		setFields(fields);
	}

	void deleteField(String field_name) throws IOException, ServerException {
		LinkedHashMap<String, FieldDefinition> fields = (LinkedHashMap<String, FieldDefinition>) fieldMap.clone();
		if (fields.remove(field_name) == null)
			throw new ServerException(Response.Status.NOT_FOUND, "Field not found: " + field_name);
		setFields(fields);
	}

	LinkedHashMap<String, AnalyzerDefinition> getAnalyzers() {
		return analyzerMap;
	}

	synchronized void setAnalyzers(LinkedHashMap<String, AnalyzerDefinition> analyzers)
			throws ServerException, IOException {
		AnalyzerContext analyzerContext = new AnalyzerContext(analyzers, fieldMap);
		indexAnalyzer.update(analyzerContext, analyzerContext.indexAnalyzerMap);
		queryAnalyzer.update(analyzerContext, analyzerContext.queryAnalyzerMap);
		JsonMapper.MAPPER.writeValue(fileSet.analyzerMapFile, analyzers);
		analyzerMap = analyzers;
	}

	void setAnalyzer(String analyzerName, AnalyzerDefinition analyzer) throws IOException, ServerException {
		LinkedHashMap<String, AnalyzerDefinition> analyzers =
				(LinkedHashMap<String, AnalyzerDefinition>) analyzerMap.clone();
		analyzers.put(analyzerName, analyzer);
		setAnalyzers(analyzers);
	}

	List<TermDefinition> testAnalyzer(String analyzerName, String text)
			throws ServerException, InterruptedException, ReflectiveOperationException, IOException {
		AnalyzerDefinition analyzerDefinition = analyzerMap.get(analyzerName);
		if (analyzerDefinition == null)
			throw new ServerException(Response.Status.NOT_FOUND, "Analyzer not found: " + analyzerName);
		Analyzer analyzer = new CustomAnalyzer(analyzerDefinition);
		try {
			return TermDefinition.buildTermList(analyzer, StringUtils.EMPTY, text);
		} finally {
			analyzer.close();
		}
	}

	void deleteAnalyzer(String analyzerName) throws IOException, ServerException {
		LinkedHashMap<String, AnalyzerDefinition> analyzers =
				(LinkedHashMap<String, AnalyzerDefinition>) analyzerMap.clone();
		if (analyzers.remove(analyzerName) == null)
			throw new ServerException(Response.Status.NOT_FOUND, "Analyzer not found: " + analyzerName);
		setAnalyzers(analyzers);
	}

	public Analyzer getIndexAnalyzer(String field) throws ServerException, IOException {
		return indexAnalyzer.getWrappedAnalyzer(field);
	}

	public Analyzer getQueryAnalyzer(String field) throws ServerException, IOException {
		return queryAnalyzer.getWrappedAnalyzer(field);
	}

	private void nrtCommit() throws IOException, ServerException {
		indexWriter.commit();
		searcherManager.maybeRefresh();
		schema.mayBeRefresh();
	}

	final synchronized BackupStatus backup(Integer keepLastCount) throws IOException, InterruptedException {
		Semaphore sem = schema.acquireReadSemaphore();
		try {
			File backupdir = null;
			final IndexCommit commit = snapshotDeletionPolicy.snapshot();
			try {
				int files_count = 0;
				long bytes_size = 0;
				if (!fileSet.backupDirectory.exists())
					fileSet.backupDirectory.mkdir();
				backupdir = new File(fileSet.backupDirectory, Long.toString(commit.getGeneration()));
				if (!backupdir.exists())
					backupdir.mkdir();
				if (!backupdir.exists())
					throw new IOException("Cannot create the backup directory: " + backupdir);
				for (String fileName : commit.getFileNames()) {
					File sourceFile = new File(fileSet.dataDirectory, fileName);
					File targetFile = new File(backupdir, fileName);
					files_count++;
					bytes_size += sourceFile.length();
					if (targetFile.exists() && targetFile.length() == sourceFile.length()
							&& targetFile.lastModified() == sourceFile.lastModified())
						continue;
					FileUtils.copyFile(sourceFile, targetFile, true);
				}
				purgeBackups(keepLastCount);
				return new BackupStatus(commit.getGeneration(), backupdir.lastModified(), bytes_size, files_count);
			} catch (IOException e) {
				if (backupdir != null)
					FileUtils.deleteQuietly(backupdir);
				throw e;
			} finally {
				snapshotDeletionPolicy.release(commit);
			}
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	private void purgeBackups(Integer keepLastCount) {
		if (keepLastCount == null)
			return;
		if (keepLastCount == 0)
			return;
		List<BackupStatus> backups = backups();
		if (backups.size() <= keepLastCount)
			return;
		for (int i = keepLastCount; i < backups.size(); i++) {
			File backupDir = new File(fileSet.backupDirectory, Long.toString(backups.get(i).generation));
			FileUtils.deleteQuietly(backupDir);
		}
	}

	private List<BackupStatus> backups() {
		List<BackupStatus> list = new ArrayList<BackupStatus>();
		if (!fileSet.backupDirectory.exists())
			return list;
		File[] dirs = fileSet.backupDirectory.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
		if (dirs == null)
			return list;
		for (File dir : dirs) {
			BackupStatus status = BackupStatus.newBackupStatus(dir);
			if (status != null)
				list.add(status);
		}
		list.sort(new Comparator<BackupStatus>() {
			@Override
			public int compare(BackupStatus o1, BackupStatus o2) {
				return o2.generation.compareTo(o1.generation);
			}
		});
		return list;
	}

	final List<BackupStatus> getBackups() throws InterruptedException {
		final Semaphore sem = schema.acquireReadSemaphore();
		try {
			return backups();
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	final Replicator getReplicator() {
		return replicator;
	}

	void replicationCheck() throws IOException, InterruptedException {
		if (replicationClient == null)
			throw new UnsupportedOperationException("No replication master has been setup.");
		final Semaphore sem = schema.acquireWriteSemaphore();
		try {
			replicationClient.updateNow();
			searcherManager.maybeRefresh();
			schema.mayBeRefresh();
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	final void deleteAll() throws IOException, InterruptedException, ServerException {
		final Semaphore sem = schema.acquireWriteSemaphore();
		try {
			indexWriter.deleteAll();
			nrtCommit();
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	private final RecordsPoster.UpdateObjectDocument getDocumentPoster(final Map<String, Field> fields) {
		return new RecordsPoster.UpdateObjectDocument(fields, indexAnalyzer.getContext(), indexWriter);
	}

	private final RecordsPoster.UpdateMapDocument getDocumentPoster() {
		return new RecordsPoster.UpdateMapDocument(indexAnalyzer.getContext(), indexWriter);
	}

	private final RecordsPoster.UpdateObjectDocValues getDocValuesPoster(final Map<String, Field> fields) {
		return new RecordsPoster.UpdateObjectDocValues(fields, indexAnalyzer.getContext(), indexWriter);
	}

	private final RecordsPoster.UpdateMapDocValues getDocValuesPoster() {
		return new RecordsPoster.UpdateMapDocValues(indexAnalyzer.getContext(), indexWriter);
	}

	final <T> Object postDocument(final Map<String, Field> fields, final T document)
			throws IOException, InterruptedException {
		if (document == null)
			return null;
		final Semaphore sem = schema.acquireWriteSemaphore();
		try {
			schema.checkSize(1);
			RecordsPoster.UpdateObjectDocument poster = getDocumentPoster(fields);
			poster.accept(document);
			Object id = poster.ids.isEmpty() ? null : poster.ids.iterator().next();
			nrtCommit();
			return id;
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	final Object postMappedDocument(final Map<String, Object> document) throws IOException, InterruptedException {
		if (document == null || document.isEmpty())
			return null;
		final Semaphore sem = schema.acquireWriteSemaphore();
		try {
			schema.checkSize(1);
			RecordsPoster.UpdateMapDocument poster = getDocumentPoster();
			poster.accept(document);
			Object id = poster.ids.isEmpty() ? null : poster.ids.iterator().next();
			nrtCommit();
			return id;
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	final Collection<Object> postMappedDocuments(final Collection<Map<String, Object>> documents)
			throws IOException, InterruptedException {
		if (documents == null || documents.isEmpty())
			return null;
		final Semaphore sem = schema.acquireWriteSemaphore();
		try {
			schema.checkSize(documents.size());
			RecordsPoster.UpdateMapDocument poster = getDocumentPoster();
			documents.forEach(poster);
			nrtCommit();
			return poster.ids;
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	final <T> Collection<Object> postDocuments(final Map<String, Field> fields, final Collection<T> documents)
			throws IOException, InterruptedException {
		if (documents == null || documents.isEmpty())
			return null;
		final Semaphore sem = schema.acquireWriteSemaphore();
		try {
			schema.checkSize(documents.size());
			RecordsPoster.UpdateObjectDocument poster = getDocumentPoster(fields);
			documents.forEach(poster);
			nrtCommit();
			return poster.ids;
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	final <T> void updateDocValues(final Map<String, Field> fields, final T document)
			throws InterruptedException, IOException {
		if (document == null)
			return;
		final Semaphore sem = schema.acquireWriteSemaphore();
		try {
			RecordsPoster.UpdateObjectDocValues poster = getDocValuesPoster(fields);
			poster.accept(document);
			nrtCommit();
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	final void updateMappedDocValues(final Map<String, Object> document) throws IOException, InterruptedException {
		if (document == null || document.isEmpty())
			return;
		final Semaphore sem = schema.acquireWriteSemaphore();
		try {
			RecordsPoster.UpdateMapDocValues poster = getDocValuesPoster();
			poster.accept(document);
			nrtCommit();
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	final <T> void updateDocsValues(final Map<String, Field> fields, final Collection<T> documents)
			throws IOException, InterruptedException {
		if (documents == null || documents.isEmpty())
			return;
		final Semaphore sem = schema.acquireWriteSemaphore();
		try {
			RecordsPoster.UpdateObjectDocValues poster = getDocValuesPoster(fields);
			documents.forEach(poster);
			nrtCommit();
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	final void updateMappedDocsValues(final Collection<Map<String, Object>> documents)
			throws IOException, ServerException, InterruptedException {
		if (documents == null || documents.isEmpty())
			return;
		final Semaphore sem = schema.acquireWriteSemaphore();
		try {
			RecordsPoster.UpdateMapDocValues poster = getDocValuesPoster();
			documents.forEach(poster);
			nrtCommit();
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	final ResultDefinition.WithMap deleteByQuery(final QueryDefinition queryDefinition)
			throws IOException, InterruptedException, QueryNodeException, ParseException, ServerException,
			ReflectiveOperationException {
		final Semaphore sem = schema.acquireWriteSemaphore();
		try {
			final QueryContext queryContext = new QueryContext(null, queryAnalyzer, null, queryDefinition);
			final Query query = QueryUtils.getLuceneQuery(queryContext);
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
		indexSearcher.setSimilarity(indexWriterConfig.getSimilarity());
		final SortedSetDocValuesReaderState facetsState = getFacetsState(indexSearcher.getIndexReader());
		return new QueryContext(indexSearcher, queryAnalyzer, facetsState, queryDefinition);
	}

	final ResultDefinition search(final QueryDefinition queryDefinition,
			ResultDocumentBuilder.BuilderFactory<?> documentBuilderFactory)
			throws IOException, InterruptedException, ParseException, ReflectiveOperationException, QueryNodeException {
		final Semaphore sem = schema.acquireReadSemaphore();
		try {
			final IndexSearcher indexSearcher = searcherManager.acquire();
			try {
				return QueryUtils.search(buildQueryContext(indexSearcher, queryDefinition), documentBuilderFactory);
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
		this.fieldMap.forEach((name, fieldDef) -> {
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

}
