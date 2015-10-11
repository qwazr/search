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

import com.datastax.driver.core.utils.UUIDs;
import com.qwazr.search.SearchServer;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.TimeTracker;
import com.qwazr.utils.json.JsonMapper;
import com.qwazr.utils.server.ServerException;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.facet.DrillDownQuery;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.sortedset.DefaultSortedSetDocValuesReaderState;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetCounts;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.postingshighlight.PostingsHighlighter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.NumericUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Semaphore;

public class IndexInstance implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(IndexInstance.class);

    private final static String INDEX_DATA = "data";
    private final static String FIELDS_FILE = "fields.json";
    private final static String SETTINGS_FILE = "settings.json";

    private final File indexDirectory;
    private final File dataDirectory;
    private final File fieldMapFile;
    private final File settingsFile;
    private final Directory luceneDirectory;
    private final FacetsConfig facetsConfig;
    private final IndexWriterConfig indexWriterConfig;
    private final IndexWriter indexWriter;
    private final SearcherManager searcherManager;
    private volatile PerFieldAnalyzerWrapper perFieldAnalyzer;
    private volatile Map<String, FieldDefinition> fieldMap;
    private volatile SettingsDefinition settingsDefinition;
    private volatile Semaphore readSemaphore;
    private volatile Semaphore writeSemaphore;

    /**
     * Create an index directory
     *
     * @param indexDirectory
     *            the root location of the directory
     * @throws IOException
     * @throws ServerException
     */
    IndexInstance(File indexDirectory) throws IOException, ServerException {

	this.indexDirectory = indexDirectory;
	dataDirectory = new File(indexDirectory, INDEX_DATA);
	SearchServer.checkDirectoryExists(indexDirectory);
	luceneDirectory = FSDirectory.open(dataDirectory.toPath());
	facetsConfig = new FacetsConfig();
	fieldMapFile = new File(indexDirectory, FIELDS_FILE);
	fieldMap = fieldMapFile.exists() ? JsonMapper.MAPPER.readValue(fieldMapFile,
		FieldDefinition.MapStringFieldTypeRef) : null;
	settingsFile = new File(indexDirectory, SETTINGS_FILE);
	settingsDefinition = settingsFile.exists() ? JsonMapper.MAPPER
		.readValue(settingsFile, SettingsDefinition.class) : null;
	checkSettings();
	perFieldAnalyzer = buildFieldAnalyzer(fieldMap);
	indexWriterConfig = new IndexWriterConfig(perFieldAnalyzer);
	indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
	indexWriter = new IndexWriter(luceneDirectory, indexWriterConfig);
	searcherManager = new SearcherManager(indexWriter, true, null);
    }

    @Override
    public void close() {
	IOUtils.close(searcherManager);
	if (indexWriter.isOpen())
	    IOUtils.close(indexWriter);
	IOUtils.close(luceneDirectory);
    }

    /**
     * Delete the index. The directory is deleted from the local file system.
     */
    void delete() {
	close();
	if (indexDirectory.exists())
	    FileUtils.deleteQuietly(indexDirectory);
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
	Semaphore sem = checkReadLimit();
	try {
	    return getIndexStatus();
	} finally {
	    if (sem != null)
		sem.release();
	}
    }

    private Class<?> findAnalyzer(String analyzer) throws ClassNotFoundException {
	try {
	    return Class.forName(analyzer);
	} catch (ClassNotFoundException e1) {
	    try {
		return Class.forName("com.qwazr.search.analysis." + analyzer);
	    } catch (ClassNotFoundException e2) {
		try {
		    return Class.forName("org.apache.lucene.analysis." + analyzer);
		} catch (ClassNotFoundException e3) {
		    throw e1;
		}
	    }
	}
    }

    private PerFieldAnalyzerWrapper buildFieldAnalyzer(Map<String, FieldDefinition> fields) throws ServerException {
	if (fields == null || fields.size() == 0)
	    return new PerFieldAnalyzerWrapper(new KeywordAnalyzer());
	Map<String, Analyzer> analyzerMap = new HashMap<String, Analyzer>();
	for (Map.Entry<String, FieldDefinition> field : fields.entrySet()) {
	    String fieldName = field.getKey();
	    FieldDefinition fieldDef = field.getValue();
	    if (fieldDef.template == FieldDefinition.Template.SortedSetMultiDocValuesFacetField)
		facetsConfig.setMultiValued(fieldName, true);
	    else if (fieldDef.template == FieldDefinition.Template.SortedSetDocValuesFacetField)
		facetsConfig.setMultiValued(fieldName, false);
	    try {
		if (!StringUtils.isEmpty(fieldDef.analyzer))
		    analyzerMap.put(field.getKey(), (Analyzer) findAnalyzer(fieldDef.analyzer).newInstance());
	    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
		throw new ServerException(Response.Status.NOT_ACCEPTABLE, "Class " + fieldDef.analyzer
			+ " not known for the field " + fieldName);
	    }
	}
	return new PerFieldAnalyzerWrapper(new KeywordAnalyzer(), analyzerMap);
    }

    public synchronized void setFields(Map<String, FieldDefinition> fields) throws ServerException, IOException {
	perFieldAnalyzer = buildFieldAnalyzer(fields);
	JsonMapper.MAPPER.writeValue(fieldMapFile, fields);
	fieldMap = fields;
    }

    public synchronized void setSettings(SettingsDefinition settings) throws IOException {
	if (settings == null)
	    settingsFile.delete();
	else
	    JsonMapper.MAPPER.writeValue(settingsFile, settings);
	this.settingsDefinition = settings;
	checkSettings();
    }

    public SettingsDefinition getSettings() {
	return settingsDefinition;
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

	Document facetedDoc = facetsConfig.build(doc);
	if (termId == null)
	    indexWriter.addDocument(facetedDoc);
	else
	    indexWriter.updateDocument(termId, facetedDoc);
	return facetedDoc.hashCode();
    }

    private void nrtCommit() throws IOException {
	indexWriter.commit();
	searcherManager.maybeRefresh();
    }

    public void deleteAll() throws IOException, InterruptedException {
	Semaphore sem = checkWriteLimit();
	try {
	    indexWriter.deleteAll();
	    nrtCommit();
	} finally {
	    if (sem != null)
		sem.release();
	}
    }

    private Semaphore checkReadLimit() throws InterruptedException {
	if (readSemaphore == null)
	    return null;
	readSemaphore.acquire();
	return readSemaphore;
    }

    private Semaphore checkWriteLimit() throws InterruptedException {
	if (writeSemaphore == null)
	    return null;
	writeSemaphore.acquire();
	return writeSemaphore;
    }

    private synchronized void checkSettings() {
	if (settingsDefinition == null) {
	    readSemaphore = null;
	    writeSemaphore = null;
	    return;
	}
	if (settingsDefinition.max_simultaneous_read != null)
	    readSemaphore = new Semaphore(settingsDefinition.max_simultaneous_read);
	else
	    readSemaphore = null;
	if (settingsDefinition.max_simultaneous_write != null)
	    writeSemaphore = new Semaphore(settingsDefinition.max_simultaneous_write);
	else
	    writeSemaphore = null;
    }

    private void checkSize(int size) throws IOException, ServerException {
	if (settingsDefinition == null)
	    return;
	if (settingsDefinition.max_size == null)
	    return;
	if (getIndexStatus().num_docs + size > settingsDefinition.max_size)
	    throw new ServerException(Response.Status.NOT_ACCEPTABLE, "This index is limited to "
		    + settingsDefinition.max_size + " documents");
    }

    public Object postDocument(Map<String, Object> document) throws IOException, ServerException, InterruptedException {
	if (document == null)
	    return null;
	Semaphore sem = checkWriteLimit();
	try {
	    checkSize(1);
	    Object id = addNewLuceneDocument(document);
	    nrtCommit();
	    return id;
	} finally {
	    if (sem != null)
		sem.release();
	}
    }

    public List<Object> postDocuments(List<Map<String, Object>> documents) throws IOException, ServerException,
	    InterruptedException {
	if (documents == null)
	    return null;
	Semaphore sem = checkWriteLimit();
	try {
	    checkSize(documents.size());
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

    private Query getLuceneQuery(QueryDefinition queryDef) throws QueryNodeException {
	final StandardQueryParser parser = new StandardQueryParser(perFieldAnalyzer);
	if (queryDef.multi_field != null && !queryDef.multi_field.isEmpty()) {
	    Set<String> fieldSet = queryDef.multi_field.keySet();
	    String[] fieldArray = fieldSet.toArray(new String[fieldSet.size()]);
	    parser.setMultiFields(fieldArray);
	}
	if (queryDef.allow_leading_wildcard != null)
	    parser.setAllowLeadingWildcard(queryDef.allow_leading_wildcard);
	if (queryDef.default_operator != null)
	    parser.setDefaultOperator(queryDef.default_operator);
	final String qs;
	// Check if we have to escape some characters
	if (queryDef.escape_query != null && queryDef.escape_query) {
	    if (queryDef.escaped_chars != null && queryDef.escaped_chars.length > 0)
		qs = StringUtils.escape_chars(queryDef.query_string, queryDef.escaped_chars);
	    else
		qs = QueryParser.escape(queryDef.query_string);
	} else
	    qs = queryDef.query_string;
	return parser.parse(qs, queryDef.default_field);
    }

    public void deleteByQuery(QueryDefinition queryDef) throws IOException, InterruptedException, QueryNodeException {
	Semaphore sem = checkWriteLimit();
	try {
	    Query query = getLuceneQuery(queryDef);
	    indexWriter.deleteDocuments(query);
	    nrtCommit();
	} finally {
	    if (sem != null)
		sem.release();
	}
    }

    public ResultDefinition search(QueryDefinition queryDef) throws ServerException, IOException, QueryNodeException,
	    InterruptedException {
	Semaphore sem = checkReadLimit();
	try {

	    Query query = getLuceneQuery(queryDef);

	    final IndexSearcher indexSearcher = searcherManager.acquire();
	    try {
		final IndexReader indexReader = indexSearcher.getIndexReader();
		final TimeTracker timeTracker = new TimeTracker();
		final TopDocs topDocs;
		final Facets facets;

		// Overload query with filters
		if (queryDef.filters != null && !queryDef.filters.isEmpty()) {
		    DrillDownQuery drillDownQuery = new DrillDownQuery(facetsConfig, query);
		    for (Map.Entry<String, Set<String>> entry : queryDef.filters.entrySet()) {
			Set<String> filter_terms = entry.getValue();
			if (filter_terms == null)
			    continue;
			String filter_field = entry.getKey();
			for (String filter_term : filter_terms)
			    drillDownQuery.add(filter_field, filter_term);
		    }
		    query = drillDownQuery;
		}
		if (queryDef.facets != null && queryDef.facets.size() > 0) {
		    FacetsCollector facetsCollector = new FacetsCollector();
		    SortedSetDocValuesReaderState state = new DefaultSortedSetDocValuesReaderState(indexReader);
		    topDocs = FacetsCollector.search(indexSearcher, query, queryDef.getEnd(), facetsCollector);
		    timeTracker.next("search_query");
		    facets = new SortedSetDocValuesFacetCounts(state, facetsCollector);
		    timeTracker.next("facet_count");
		} else {
		    topDocs = indexSearcher.search(query, queryDef.getEnd());
		    timeTracker.next("search_query");
		    facets = null;
		}
		Map<String, String[]> postingsHighlightersMap = null;
		if (queryDef.postings_highlighter != null) {
		    postingsHighlightersMap = new LinkedHashMap<String, String[]>();
		    for (Map.Entry<String, Integer> entry : queryDef.postings_highlighter.entrySet()) {
			String field = entry.getKey();
			PostingsHighlighter highlighter = new PostingsHighlighter(entry.getValue());
			String highlights[] = highlighter.highlight(field, query, indexSearcher, topDocs);
			if (highlights != null) {
			    postingsHighlightersMap.put(field, highlights);
			}
		    }
		    timeTracker.next("postings_highlighters");
		}

		return new ResultDefinition(timeTracker, indexSearcher, topDocs, queryDef, facets,
			postingsHighlightersMap);
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

    public ResultDefinition mlt(MltQueryDefinition mltQueryDef) throws ServerException, IOException,
	    QueryNodeException, InterruptedException {
	Semaphore sem = checkReadLimit();
	try {
	    final IndexSearcher indexSearcher = searcherManager.acquire();
	    try {
		final IndexReader indexReader = indexSearcher.getIndexReader();
		final TimeTracker timeTracker = new TimeTracker();
		final Query filterQuery = new StandardQueryParser(perFieldAnalyzer).parse(mltQueryDef.document_query,
			mltQueryDef.query_default_field);
		final TopDocs filterTopDocs = indexSearcher.search(filterQuery, 1, Sort.INDEXORDER);
		if (filterTopDocs.totalHits == 0)
		    return new ResultDefinition(timeTracker);
		final TopDocs topDocs;
		final Query query = getMoreLikeThis(mltQueryDef, indexReader).like(filterTopDocs.scoreDocs[0].doc);
		topDocs = indexSearcher.search(query, mltQueryDef.getEnd());
		return new ResultDefinition(timeTracker, indexSearcher, topDocs, mltQueryDef);
	    } finally {
		searcherManager.release(indexSearcher);
	    }
	} finally {
	    if (sem != null)
		sem.release();
	}
    }

}
