/**
 * Copyright 2015 Emmanuel Keller / QWAZR
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

import com.qwazr.search.SearchServer;
import com.qwazr.search.analysis.AnalyzerContext;
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.analysis.UpdatableAnalyzer;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.TimeTracker;
import com.qwazr.utils.json.JsonMapper;
import com.qwazr.utils.server.ServerException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.function.BiConsumer;

final public class IndexInstance implements Closeable {

	private static final Logger logger = LoggerFactory.getLogger(IndexInstance.class);

	private final static String INDEX_DATA = "data";
	private final static String INDEX_BACKUP = "backup";
	private final static String FIELDS_FILE = "fields.json";
	private final static String ANALYZERS_FILE = "analyzers.json";
	private final static String SETTINGS_FILE = "settings.json";

	private final FileSet fileSet;

	private final SchemaInstance schema;
	private final Directory luceneDirectory;
	private final LiveIndexWriterConfig indexWriterConfig;
	private final SnapshotDeletionPolicy snapshotDeletionPolicy;
	private final IndexWriter indexWriter;
	private final SearcherManager searcherManager;
	private final IndexSettingsDefinition settings;

	private final UpdatableAnalyzer indexAnalyzer;
	private final UpdatableAnalyzer queryAnalyzer;
	private volatile LinkedHashMap<String, FieldDefinition> fieldMap;
	private volatile LinkedHashMap<String, AnalyzerDefinition> analyzerMap;

	private IndexInstance(SchemaInstance schema, Directory luceneDirectory, IndexSettingsDefinition settings,
					LinkedHashMap<String, AnalyzerDefinition> analyzerMap,
					LinkedHashMap<String, FieldDefinition> fieldMap, FileSet fileSet, IndexWriter indexWriter,
					SearcherManager searcherManager, UpdatableAnalyzer queryAnalyzer) {
		this.schema = schema;
		this.fileSet = fileSet;
		this.luceneDirectory = luceneDirectory;
		this.analyzerMap = analyzerMap;
		this.fieldMap = fieldMap;
		this.indexWriter = indexWriter;
		this.indexWriterConfig = indexWriter.getConfig();
		this.indexAnalyzer = (UpdatableAnalyzer) indexWriterConfig.getAnalyzer();
		this.queryAnalyzer = queryAnalyzer;
		this.snapshotDeletionPolicy = (SnapshotDeletionPolicy) indexWriterConfig.getIndexDeletionPolicy();
		this.settings = settings;
		this.searcherManager = searcherManager;
	}

	private static class FileSet {

		private final File settingsFile;
		private final File indexDirectory;
		private final File backupDirectory;
		private final File dataDirectory;
		private final File analyzerMapFile;
		private final File fieldMapFile;

		private FileSet(File indexDirectory) {
			this.indexDirectory = indexDirectory;
			this.backupDirectory = new File(indexDirectory, INDEX_BACKUP);
			this.dataDirectory = new File(indexDirectory, INDEX_DATA);
			this.analyzerMapFile = new File(indexDirectory, ANALYZERS_FILE);
			this.fieldMapFile = new File(indexDirectory, FIELDS_FILE);
			this.settingsFile = new File(indexDirectory, SETTINGS_FILE);
		}
	}

	/**
	 * @param schema
	 * @param indexDirectory
	 * @return
	 */
	final static IndexInstance newInstance(SchemaInstance schema, File indexDirectory, IndexSettingsDefinition settings)
					throws ServerException, IOException, ReflectiveOperationException, InterruptedException {
		UpdatableAnalyzer indexAnalyzer = null;
		UpdatableAnalyzer queryAnalyzer = null;
		IndexWriter indexWriter = null;
		Directory luceneDirectory = null;
		try {

			SearchServer.checkDirectoryExists(indexDirectory);
			FileSet fileSet = new FileSet(indexDirectory);

			//Loading the settings
			if (settings == null) {
				settings = fileSet.settingsFile.exists() ?
								JsonMapper.MAPPER.readValue(fileSet.settingsFile, IndexSettingsDefinition.class) :
								IndexSettingsDefinition.EMPTY;
			} else {
				JsonMapper.MAPPER.writeValue(fileSet.settingsFile, settings);
			}

			//Loading the fields
			File fieldMapFile = new File(indexDirectory, FIELDS_FILE);
			LinkedHashMap<String, FieldDefinition> fieldMap = fieldMapFile.exists() ?
							JsonMapper.MAPPER.readValue(fieldMapFile, FieldDefinition.MapStringFieldTypeRef) :
							null;

			//Loading the fields
			File analyzerMapFile = new File(indexDirectory, ANALYZERS_FILE);
			LinkedHashMap<String, AnalyzerDefinition> analyzerMap = analyzerMapFile.exists() ?
							JsonMapper.MAPPER.readValue(analyzerMapFile, AnalyzerDefinition.MapStringAnalyzerTypeRef) :
							null;

			AnalyzerContext context = new AnalyzerContext(schema.getFileClassCompilerLoader(), analyzerMap, fieldMap);
			indexAnalyzer = new UpdatableAnalyzer(context, context.indexAnalyzerMap);
			queryAnalyzer = new UpdatableAnalyzer(context, context.queryAnalyzerMap);

			// Open and lock the data directory
			luceneDirectory = FSDirectory.open(fileSet.dataDirectory.toPath());

			// Set
			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(indexAnalyzer);
			if (settings != null && settings.similarity_class != null)
				indexWriterConfig.setSimilarity(IndexUtils
								.findSimilarity(schema.getFileClassCompilerLoader(), settings.similarity_class));
			indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			SnapshotDeletionPolicy snapshotDeletionPolicy = new SnapshotDeletionPolicy(
							indexWriterConfig.getIndexDeletionPolicy());
			indexWriterConfig.setIndexDeletionPolicy(snapshotDeletionPolicy);
			indexWriter = new IndexWriter(luceneDirectory, indexWriterConfig);
			if (indexWriter.hasUncommittedChanges())
				indexWriter.commit();

			// Finally we build the SearchSercherManger
			SearcherManager searcherManager = new SearcherManager(indexWriter, true, null);

			return new IndexInstance(schema, luceneDirectory, settings, analyzerMap, fieldMap, fileSet, indexWriter,
							searcherManager, queryAnalyzer);
		} catch (IOException | ServerException | ReflectiveOperationException | InterruptedException e) {
			// We failed in opening the index. We close everything we can
			if (queryAnalyzer != null)
				IOUtils.closeQuietly(queryAnalyzer);
			if (indexAnalyzer != null)
				IOUtils.closeQuietly(indexAnalyzer);
			if (indexWriter != null)
				IOUtils.closeQuietly(indexWriter);
			if (luceneDirectory != null)
				IOUtils.closeQuietly(luceneDirectory);
			throw e;
		}
	}

	public IndexSettingsDefinition getSettings() {
		return settings;
	}

	@Override
	public void close() {
		IOUtils.closeQuietly(searcherManager);
		if (indexWriter.isOpen())
			IOUtils.closeQuietly(indexWriter);
		IOUtils.closeQuietly(luceneDirectory);
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
			return new IndexStatus(indexSearcher.getIndexReader(), settings,
							analyzerMap == null ? null : analyzerMap.keySet(),
							fieldMap == null ? null : fieldMap.keySet());
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
		AnalyzerContext analyzerContext = new AnalyzerContext(schema.getFileClassCompilerLoader(), analyzerMap, fields);
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
		AnalyzerContext analyzerContext = new AnalyzerContext(schema.getFileClassCompilerLoader(), analyzers, fieldMap);
		indexAnalyzer.update(analyzerContext, analyzerContext.indexAnalyzerMap);
		queryAnalyzer.update(analyzerContext, analyzerContext.queryAnalyzerMap);
		JsonMapper.MAPPER.writeValue(fileSet.analyzerMapFile, analyzers);
		analyzerMap = analyzers;
	}

	void setAnalyzer(String analyzer_name, AnalyzerDefinition analyzer) throws IOException, ServerException {
		LinkedHashMap<String, AnalyzerDefinition> analyzers = (LinkedHashMap<String, AnalyzerDefinition>) analyzerMap
						.clone();
		analyzers.put(analyzer_name, analyzer);
		setAnalyzers(analyzers);
	}

	void deleteAnalyzer(String analyzer_name) throws IOException, ServerException {
		LinkedHashMap<String, AnalyzerDefinition> analyzers = (LinkedHashMap<String, AnalyzerDefinition>) analyzerMap
						.clone();
		if (analyzers.remove(analyzer_name) == null)
			throw new ServerException(Response.Status.NOT_FOUND, "Analyzer not found: " + analyzer_name);
		setAnalyzers(analyzers);
	}

	public Analyzer getIndexAnalyzer(String field) {
		return indexAnalyzer.getWrappedAnalyzer(field);
	}

	public Analyzer getQueryAnalyzer(String field) {
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
			final IndexCommit commit = snapshotDeletionPolicy.snapshot();
			File backupdir = null;
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

	final Object postDocument(Map<String, Object> document) throws IOException, ServerException, InterruptedException {
		if (document == null || document.isEmpty())
			return null;
		final Semaphore sem = schema.acquireWriteSemaphore();
		try {
			schema.checkSize(1);
			Object id = IndexUtils.addNewLuceneDocument(indexAnalyzer.getContext(), document, indexWriter);
			nrtCommit();
			return id;
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	final List<Object> postDocuments(List<Map<String, Object>> documents)
					throws IOException, ServerException, InterruptedException {
		if (documents == null || documents.isEmpty())
			return null;
		final Semaphore sem = schema.acquireWriteSemaphore();
		try {
			schema.checkSize(documents.size());
			final AnalyzerContext context = indexAnalyzer.getContext();
			List<Object> ids = new ArrayList<Object>(documents.size());
			for (Map<String, Object> document : documents)
				ids.add(IndexUtils.addNewLuceneDocument(context, document, indexWriter));
			nrtCommit();
			return ids;
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	final void updateDocumentValues(Map<String, Object> document)
					throws IOException, ServerException, InterruptedException {
		if (document == null || document.isEmpty())
			return;
		final Semaphore sem = schema.acquireWriteSemaphore();
		try {
			IndexUtils.updateDocValues(indexAnalyzer.getContext(), document, indexWriter);
			nrtCommit();
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	final void updateDocumentsValues(List<Map<String, Object>> documents)
					throws IOException, ServerException, InterruptedException {
		if (documents == null || documents.isEmpty())
			return;
		final Semaphore sem = schema.acquireWriteSemaphore();
		try {
			final AnalyzerContext context = indexAnalyzer.getContext();
			for (Map<String, Object> document : documents)
				IndexUtils.updateDocValues(context, document, indexWriter);
			nrtCommit();
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	final ResultDefinition deleteByQuery(QueryDefinition queryDef)
					throws IOException, InterruptedException, QueryNodeException, ParseException, ServerException {
		final Semaphore sem = schema.acquireWriteSemaphore();
		try {
			final Query query = QueryUtils.getLuceneQuery(queryDef, queryAnalyzer);
			int docs = indexWriter.numDocs();
			indexWriter.deleteDocuments(query);
			nrtCommit();
			docs -= indexWriter.numDocs();
			return new ResultDefinition(docs);
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	final ResultDefinition search(QueryDefinition queryDef)
					throws ServerException, IOException, QueryNodeException, InterruptedException, ParseException {
		final Semaphore sem = schema.acquireReadSemaphore();
		try {
			final IndexSearcher indexSearcher = searcherManager.acquire();
			try {
				indexSearcher.setSimilarity(indexWriterConfig.getSimilarity());
				return QueryUtils.search(indexSearcher, queryDef, queryAnalyzer);
			} finally {
				searcherManager.release(indexSearcher);
			}
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	ResultDefinition mlt(MltQueryDefinition mltQueryDef)
					throws ServerException, IOException, QueryNodeException, InterruptedException {
		final Semaphore sem = schema.acquireReadSemaphore();
		try {
			final IndexSearcher indexSearcher = searcherManager.acquire();
			try {
				indexSearcher.setSimilarity(indexWriterConfig.getSimilarity());
				final IndexReader indexReader = indexSearcher.getIndexReader();
				final TimeTracker timeTracker = new TimeTracker();
				final Query filterQuery = new StandardQueryParser(queryAnalyzer)
								.parse(mltQueryDef.document_query, mltQueryDef.query_default_field);
				final TopDocs filterTopDocs = indexSearcher.search(filterQuery, 1, Sort.INDEXORDER);
				if (filterTopDocs.totalHits == 0)
					return new ResultDefinition(timeTracker);
				final TopDocs topDocs;
				final Query query = QueryUtils.getMoreLikeThis(mltQueryDef, indexReader, queryAnalyzer)
								.like(filterTopDocs.scoreDocs[0].doc);
				topDocs = indexSearcher.search(query, mltQueryDef.getEnd());
				return new ResultDefinition(fieldMap, timeTracker, indexSearcher, topDocs, mltQueryDef, query);
			} finally {
				searcherManager.release(indexSearcher);
			}
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	Directory getLuceneDirectory() {
		return luceneDirectory;
	}

	void fillFields(final Map<String, FieldDefinition> fields) {
		if (fields == null || this.fieldMap == null)
			return;
		this.fieldMap.forEach(new BiConsumer<String, FieldDefinition>() {
			@Override
			public void accept(String name, FieldDefinition fieldDefinition) {
				if (!fields.containsKey(name))
					fields.put(name, fieldDefinition);
			}
		});
	}

	void fillAnalyzers(final Map<String, AnalyzerDefinition> analyzers) {
		if (analyzers == null || this.analyzerMap == null)
			return;
		this.analyzerMap.forEach(new BiConsumer<String, AnalyzerDefinition>() {
			@Override
			public void accept(String name, AnalyzerDefinition analyzerDefinition) {
				if (!analyzers.containsKey(name))
					analyzers.put(name, analyzerDefinition);
			}
		});
	}

}
