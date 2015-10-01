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
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
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

public class IndexInstance implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(IndexInstance.class);

    private final static String INDEX_DATA = "data";
    private final static String FIELDS_FILE = "fields.json";

    private final File indexDirectory;
    private final File dataDirectory;
    private final File fieldMapFile;
    private final Directory luceneDirectory;
    private final FacetsConfig facetsConfig;
    private final IndexWriterConfig indexWriterConfig;
    private final IndexWriter indexWriter;
    private final SearcherManager searcherManager;
    private volatile PerFieldAnalyzerWrapper perFieldAnalyzer;
    private volatile Map<String, FieldDefinition> fieldMap;

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
	fieldMap = fieldMapFile.exists()
		? JsonMapper.MAPPER.readValue(fieldMapFile, FieldDefinition.MapStringFieldTypeRef) : null;
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

    IndexStatus getStatus() throws IOException {
	IndexSearcher indexSearchser = searcherManager.acquire();
	try {
	    return new IndexStatus(indexSearchser.getIndexReader(), fieldMap);
	} finally {
	    searcherManager.release(indexSearchser);
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
		throw new ServerException(Response.Status.NOT_ACCEPTABLE,
			"Class " + fieldDef.analyzer + " not known for the field " + fieldName);
	    }
	}
	return new PerFieldAnalyzerWrapper(new KeywordAnalyzer(), analyzerMap);
    }

    public synchronized void setFields(Map<String, FieldDefinition> fields) throws ServerException, IOException {
	perFieldAnalyzer = buildFieldAnalyzer(fields);
	JsonMapper.MAPPER.writeValue(fieldMapFile, fields);
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

    public void deleteAll() throws IOException {
	indexWriter.deleteAll();
	nrtCommit();
    }

    public Object postDocument(Map<String, Object> document) throws IOException {
	Object id = addNewLuceneDocument(document);
	nrtCommit();
	return id;
    }

    public List<Object> postDocuments(List<Map<String, Object>> documents) throws IOException {
	if (documents == null)
	    return null;
	List<Object> ids = new ArrayList<Object>(documents.size());
	for (Map<String, Object> document : documents)
	    ids.add(addNewLuceneDocument(document));
	nrtCommit();
	return ids;
    }

    private Query getLuceneQuery(QueryDefinition queryDef) throws ParseException {
	final QueryParser parser;
	if (queryDef.multi_field != null && !queryDef.multi_field.isEmpty()) {
	    Set<String> fieldSet = queryDef.multi_field.keySet();
	    String[] fieldArray = fieldSet.toArray(new String[fieldSet.size()]);
	    parser = new MultiFieldQueryParser(fieldArray, perFieldAnalyzer, queryDef.multi_field);
	} else
	    parser = new QueryParser(queryDef.default_field, perFieldAnalyzer);
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
	return parser.parse(qs);
    }

    public void deleteByQuery(QueryDefinition queryDef) throws ParseException, IOException {
	Query query = getLuceneQuery(queryDef);
	indexWriter.deleteDocuments(query);
	nrtCommit();
    }

    public ResultDefinition search(QueryDefinition queryDef) throws ServerException, IOException, ParseException {

	Query query = getLuceneQuery(queryDef);

	final IndexSearcher indexSearcher = searcherManager.acquire();
	final IndexReader indexReader = indexSearcher.getIndexReader();
	final TimeTracker timeTracker = new TimeTracker();
	try {
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

	    return new ResultDefinition(timeTracker, indexSearcher, topDocs, queryDef, facets, postingsHighlightersMap);
	} finally {
	    searcherManager.release(indexSearcher);
	}
    }

}
