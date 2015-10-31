/**
 * Copyright 2015 Emmanuel Keller / QWAZR
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.index;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.qwazr.search.SearchServer;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.TimeTracker;
import com.qwazr.utils.json.JsonMapper;
import com.qwazr.utils.server.ServerException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.index.*;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.NumericUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.function.BiConsumer;

public class IndexInstance implements Closeable {

	private static final Logger logger = LoggerFactory.getLogger(IndexInstance.class);

	private final static String INDEX_DATA = "data";
	private final static String INDEX_BACKUP = "backup";
	private final static String FIELDS_FILE = "fields.json";

	private final FileSet fileSet;

	private final IndexSchema schema;
	private final Directory luceneDirectory;
	private final LiveIndexWriterConfig indexWriterConfig;
	private final SnapshotDeletionPolicy snapshotDeletionPolicy;
	private final IndexWriter indexWriter;
	private final SearcherManager searcherManager;
	private final PerFieldAnalyzer perFieldAnalyzer;
	private volatile Map<String, FieldDefinition> fieldMap;

	private IndexInstance(IndexSchema schema, Directory luceneDirectory, Map<String, FieldDefinition> fieldMap,
					FileSet fileSet, IndexWriter indexWriter, SearcherManager searcherManager) {
		this.schema = schema;
		this.fileSet = fileSet;
		this.luceneDirectory = luceneDirectory;
		this.fieldMap = fieldMap;
		this.indexWriter = indexWriter;
		this.indexWriterConfig = indexWriter.getConfig();
		this.perFieldAnalyzer = (PerFieldAnalyzer) indexWriterConfig.getAnalyzer();
		this.snapshotDeletionPolicy = (SnapshotDeletionPolicy) indexWriterConfig.getIndexDeletionPolicy();
		this.searcherManager = searcherManager;
	}

	private static class FileSet {

		private final File indexDirectory;
		private final File backupDirectory;
		private final File dataDirectory;
		private final File fieldMapFile;

		private FileSet(File indexDirectory) {
			this.indexDirectory = indexDirectory;
			this.backupDirectory = new File(indexDirectory, INDEX_BACKUP);
			this.dataDirectory = new File(indexDirectory, INDEX_DATA);
			this.fieldMapFile = new File(indexDirectory, FIELDS_FILE);
		}
	}

	/**
	 * @param schema
	 * @param indexDirectory
	 * @return
	 */
	final static IndexInstance newInstance(IndexSchema schema, File indexDirectory)
					throws ServerException, IOException {
		PerFieldAnalyzer perFieldAnalyzer = null;
		IndexWriter indexWriter = null;
		Directory luceneDirectory = null;
		try {

			SearchServer.checkDirectoryExists(indexDirectory);
			FileSet fileSet = new FileSet(indexDirectory);

			//Loading the fields
			File fieldMapFile = new File(indexDirectory, FIELDS_FILE);
			Map<String, FieldDefinition> fieldMap = fieldMapFile.exists() ?
							JsonMapper.MAPPER.readValue(fieldMapFile, FieldDefinition.MapStringFieldTypeRef) :
							null;
			perFieldAnalyzer = new PerFieldAnalyzer(schema.getFileClassCompilerLoader(), fieldMap);

			// Open and lock the data directory
			luceneDirectory = FSDirectory.open(fileSet.dataDirectory.toPath());

			// Set
			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(perFieldAnalyzer);
			indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			SnapshotDeletionPolicy snapshotDeletionPolicy = new SnapshotDeletionPolicy(
							indexWriterConfig.getIndexDeletionPolicy());
			indexWriterConfig.setIndexDeletionPolicy(snapshotDeletionPolicy);
			indexWriter = new IndexWriter(luceneDirectory, indexWriterConfig);
			if (indexWriter.hasUncommittedChanges())
				indexWriter.commit();

			// Finally we build the SearchSercherManger
			SearcherManager searcherManager = new SearcherManager(indexWriter, true, null);

			return new IndexInstance(schema, luceneDirectory, fieldMap, fileSet, indexWriter, searcherManager);
		} catch (IOException | ServerException e) {
			// We failed in opening the index. We close everything we can
			if (perFieldAnalyzer != null)
				IOUtils.closeQuietly(perFieldAnalyzer);
			if (indexWriter != null)
				IOUtils.closeQuietly(indexWriter);
			if (luceneDirectory != null)
				IOUtils.closeQuietly(luceneDirectory);
			throw e;
		}
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
		IndexSearcher indexSearcher = searcherManager.acquire();
		try {
			return new IndexStatus(indexSearcher.getIndexReader(), fieldMap);
		} finally {
			searcherManager.release(indexSearcher);
		}
	}

	IndexStatus getStatus() throws IOException, InterruptedException {
		Semaphore sem = schema.acquireReadSemaphore();
		try {
			return getIndexStatus();
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	public synchronized void setFields(IndexSchema schema, Map<String, FieldDefinition> fields)
					throws ServerException, IOException {
		perFieldAnalyzer.update(schema.getFileClassCompilerLoader(), fields);
		JsonMapper.MAPPER.writeValue(fileSet.fieldMapFile, fields);
		fieldMap = fields;
	}

	private BytesRef objectToBytesRef(Object object) throws IOException {
		if (object instanceof String)
			return new BytesRef((String) object);
		BytesRefBuilder bytesBuilder = new BytesRefBuilder();
		if (object instanceof Integer)
			NumericUtils.longToPrefixCodedBytes(((Integer) object).longValue(), 0, bytesBuilder);
		else if (object instanceof Double)
			NumericUtils.longToPrefixCodedBytes(NumericUtils.doubleToSortableLong((Double) object), 0, bytesBuilder);
		else
			throw new IOException("Type not supported: " + object.getClass());
		return bytesBuilder.get();
	}

	private final static String FIELD_ID = "$id$";

	private void addNewLuceneField(String fieldName, Object value, Document doc) throws IOException {
		FieldDefinition fieldDef = fieldMap == null ? null : fieldMap.get(fieldName);
		if (fieldDef == null)
			throw new IOException("No field definition for the field: " + fieldName);
		fieldDef.putNewField(fieldName, value, doc);
	}

	private Object addNewLuceneDocument(Map<String, Object> document) throws IOException {
		Document doc = new Document();

		Term termId = null;

		Object id = document.get(FIELD_ID);
		if (id == null)
			id = UUIDs.timeBased();
		String id_string = id.toString();
		doc.add(new StringField(FIELD_ID, id_string, Field.Store.NO));
		termId = new Term(FIELD_ID, id_string);

		for (Map.Entry<String, Object> field : document.entrySet()) {
			String fieldName = field.getKey();
			if (FIELD_ID.equals(fieldName))
				continue;
			Object fieldValue = field.getValue();
			if (fieldValue instanceof Map<?, ?>)
				fieldValue = ((Map<?, Object>) fieldValue).values();
			if (fieldValue instanceof Collection<?>) {
				for (Object val : ((Collection<Object>) fieldValue))
					addNewLuceneField(fieldName, val, doc);
			} else
				addNewLuceneField(fieldName, fieldValue, doc);
		}

		Document facetedDoc = perFieldAnalyzer.getFacetsConfig().build(doc);
		if (termId == null)
			indexWriter.addDocument(facetedDoc);
		else
			indexWriter.updateDocument(termId, facetedDoc);
		return facetedDoc.hashCode();
	}

	private void nrtCommit() throws IOException, ServerException {
		indexWriter.commit();
		searcherManager.maybeRefresh();
		schema.mayBeRefresh();
	}

	synchronized BackupStatus backup(Integer keepLastCount) throws IOException, InterruptedException {
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

	List<BackupStatus> getBackups() throws InterruptedException {
		Semaphore sem = schema.acquireReadSemaphore();
		try {
			return backups();
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	void deleteAll() throws IOException, InterruptedException, ServerException {
		Semaphore sem = schema.acquireWriteSemaphore();
		try {
			indexWriter.deleteAll();
			nrtCommit();
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	Object postDocument(Map<String, Object> document) throws IOException, ServerException, InterruptedException {
		if (document == null)
			return null;
		Semaphore sem = schema.acquireWriteSemaphore();
		try {
			schema.checkSize(1);
			Object id = addNewLuceneDocument(document);
			nrtCommit();
			return id;
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	List<Object> postDocuments(List<Map<String, Object>> documents)
					throws IOException, ServerException, InterruptedException {
		if (documents == null)
			return null;
		Semaphore sem = schema.acquireWriteSemaphore();
		try {
			schema.checkSize(documents.size());
			List<Object> ids = new ArrayList<Object>(documents.size());
			for (Map<String, Object> document : documents)
				ids.add(addNewLuceneDocument(document));
			nrtCommit();
			return ids;
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	ResultDefinition deleteByQuery(QueryDefinition queryDef)
					throws IOException, InterruptedException, QueryNodeException, ParseException, ServerException {
		final Semaphore sem = schema.acquireWriteSemaphore();
		try {
			final Query query = QueryUtils.getLuceneQuery(queryDef, perFieldAnalyzer);
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

	ResultDefinition search(QueryDefinition queryDef)
					throws ServerException, IOException, QueryNodeException, InterruptedException, ParseException {
		final Semaphore sem = schema.acquireReadSemaphore();
		try {
			final IndexSearcher indexSearcher = searcherManager.acquire();
			try {
				return QueryUtils.search(fieldMap, indexSearcher, queryDef, perFieldAnalyzer);
			} finally {
				searcherManager.release(indexSearcher);
			}
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	private MoreLikeThis getMoreLikeThis(MltQueryDefinition mltQueryDef, IndexReader reader) throws IOException {

		MoreLikeThis mlt = new MoreLikeThis(reader);
		if (mltQueryDef.boost != null)
			mlt.setBoost(mltQueryDef.boost);
		if (mltQueryDef.boost_factor != null)
			mlt.setBoostFactor(mltQueryDef.boost_factor);
		if (mltQueryDef.fieldnames != null)
			mlt.setFieldNames(mltQueryDef.fieldnames);
		if (mltQueryDef.max_doc_freq != null)
			mlt.setMaxDocFreq(mltQueryDef.max_doc_freq);
		if (mltQueryDef.max_doc_freq_pct != null)
			mlt.setMaxDocFreqPct(mltQueryDef.max_doc_freq_pct);
		if (mltQueryDef.max_num_tokens_parsed != null)
			mlt.setMaxNumTokensParsed(mltQueryDef.max_num_tokens_parsed);
		if (mltQueryDef.max_query_terms != null)
			mlt.setMaxQueryTerms(mltQueryDef.max_query_terms);
		if (mltQueryDef.max_word_len != null)
			mlt.setMaxWordLen(mltQueryDef.max_word_len);
		if (mltQueryDef.min_doc_freq != null)
			mlt.setMinDocFreq(mltQueryDef.min_doc_freq);
		if (mltQueryDef.min_term_freq != null)
			mlt.setMinTermFreq(mltQueryDef.min_term_freq);
		if (mltQueryDef.min_word_len != null)
			mlt.setMinWordLen(mltQueryDef.min_word_len);
		if (mltQueryDef.stop_words != null)
			mlt.setStopWords(mltQueryDef.stop_words);
		mlt.setAnalyzer(perFieldAnalyzer);
		return mlt;
	}

	ResultDefinition mlt(MltQueryDefinition mltQueryDef)
					throws ServerException, IOException, QueryNodeException, InterruptedException {
		Semaphore sem = schema.acquireReadSemaphore();
		try {
			final IndexSearcher indexSearcher = searcherManager.acquire();
			try {
				final IndexReader indexReader = indexSearcher.getIndexReader();
				final TimeTracker timeTracker = new TimeTracker();
				final Query filterQuery = new StandardQueryParser(perFieldAnalyzer)
								.parse(mltQueryDef.document_query, mltQueryDef.query_default_field);
				final TopDocs filterTopDocs = indexSearcher.search(filterQuery, 1, Sort.INDEXORDER);
				if (filterTopDocs.totalHits == 0)
					return new ResultDefinition(timeTracker);
				final TopDocs topDocs;
				final Query query = getMoreLikeThis(mltQueryDef, indexReader).like(filterTopDocs.scoreDocs[0].doc);
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

	void fillFields(final Map<String, FieldDefinition> fieldMap) {
		if (fieldMap == null || this.fieldMap == null)
			return;
		this.fieldMap.forEach(new BiConsumer<String, FieldDefinition>() {
			@Override
			public void accept(String name, FieldDefinition fieldDefinition) {
				if (!fieldMap.containsKey(name))
					fieldMap.put(name, fieldDefinition);
			}
		});
	}

}
