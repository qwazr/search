/**
 * Copyright 2015 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.index;

import com.qwazr.search.SearchServer;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.json.JsonMapper;
import com.qwazr.utils.server.ServerException;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexInstance implements Closeable {

	private static final Logger logger = LoggerFactory
			.getLogger(IndexInstance.class);

	private final static String INDEX_DATA = "data";
	private final static String FIELDS_FILE = "fields.json";

	private final File indexDirectory;
	private final File dataDirectory;
	private final File fieldMapFile;
	private final Directory luceneDirectory;
	private final IndexWriterConfig indexWriterConfig;
	private final IndexWriter indexWriter;
	private final SearcherManager searcherManager;
	private volatile IndexSearcher indexSearcher;
	private volatile PerFieldAnalyzerWrapper perFieldAnalyzer;
	private volatile Map<String, FieldDefinition> fieldMap;

	/**
	 * Create an index directory
	 *
	 * @param indexDirectory the root location of the directory
	 * @throws IOException
	 * @throws ServerException
	 */
	IndexInstance(File indexDirectory)
			throws IOException, ServerException {

		this.indexDirectory = indexDirectory;
		dataDirectory = new File(indexDirectory, INDEX_DATA);
		SearchServer.checkDirectoryExists(indexDirectory);
		luceneDirectory = FSDirectory.open(dataDirectory.toPath());

		fieldMapFile = new File(indexDirectory, FIELDS_FILE);
		fieldMap = fieldMapFile.exists() ?
				JsonMapper.MAPPER.readValue(fieldMapFile, IndexSingleClient.MapStringFieldTypeRef) : null;
		perFieldAnalyzer = buildFieldAnalyzer(fieldMap);
		indexWriterConfig = new IndexWriterConfig(perFieldAnalyzer);
		indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
		indexWriter = new IndexWriter(luceneDirectory, indexWriterConfig);
		searcherManager = new SearcherManager(indexWriter, true, null);
		indexSearcher = searcherManager.acquire();
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
		return new IndexStatus(indexSearcher.getIndexReader(), fieldMap);
	}


	private Class<?> findAnalyzer(String analyzer) throws ClassNotFoundException {
		try {
			return Class.forName(analyzer);
		} catch (ClassNotFoundException e1) {
			try {
				return Class.forName("org.apache.lucene.analysis." + analyzer);
			} catch (ClassNotFoundException e2) {
				throw e1;
			}
		}
	}

	private PerFieldAnalyzerWrapper buildFieldAnalyzer(Map<String, FieldDefinition> fields)
			throws ServerException {
		if (fields == null || fields.size() == 0)
			return new PerFieldAnalyzerWrapper(new StandardAnalyzer(CharArraySet.EMPTY_SET));
		Map<String, Analyzer> analyzerMap = new HashMap<String, Analyzer>();
		for (Map.Entry<String, FieldDefinition> field : fields.entrySet()) {
			String fieldName = field.getKey();
			FieldDefinition fieldDef = field.getValue();
			try {
				if (!StringUtils.isEmpty(fieldDef.analyzer))
					analyzerMap.put(field.getKey(), (Analyzer) findAnalyzer(fieldDef.analyzer).newInstance());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				throw new ServerException(Response.Status.NOT_ACCEPTABLE,
						"Class " + fieldDef.analyzer + " not known for the field " + fieldName);
			}
		}
		return new PerFieldAnalyzerWrapper(new StandardAnalyzer(CharArraySet.EMPTY_SET), analyzerMap);
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
		else throw new IOException("Type not supported: " + object.getClass());
		return bytesBuilder.get();
	}

	private Object addNewLuceneDocument(Map<String, Object> document) throws IOException {
		Document doc = new Document();

		Term termId = null;

		for (Map.Entry<String, Object> field : document.entrySet()) {
			String fieldName = field.getKey();
			FieldDefinition fieldDef = fieldMap == null ? null : fieldMap.get(fieldName);
			if (fieldDef == null) throw new IOException("No field definition for the field: " + fieldName);
			doc.add(fieldDef.getNewField(fieldName, field.getValue()));
		}

		if (termId == null)
			indexWriter.addDocument(doc);
		else
			indexWriter.updateDocument(termId, doc);
		return doc.hashCode();
	}

	private void nrtCommit() throws IOException {
		indexWriter.commit();
		searcherManager.maybeRefresh();
		indexSearcher = searcherManager.acquire();
	}

	public Object postDocument(Map<String, Object> document) throws IOException {
		Object id = addNewLuceneDocument(document);
		nrtCommit();
		return id;
	}

	public List<Object> postDocuments(List<Map<String, Object>> documents) throws IOException {
		if (documents == null) return null;
		List<Object> ids = new ArrayList<Object>(documents.size());
		for (Map<String, Object> document : documents)
			ids.add(addNewLuceneDocument(document));
		nrtCommit();
		return ids;
	}

	public ResultDefinition search(QueryDefinition queryDef) throws ServerException, IOException {
		QueryParser parser = new QueryParser(queryDef.default_field, perFieldAnalyzer);
		try {
			TopDocs topDocs;
			FacetsCollector facetsCollector = null;
			Query query = parser.parse(queryDef.query_string);
			if (queryDef.facet_fields != null) {
				facetsCollector = new FacetsCollector();
				topDocs = FacetsCollector.search(indexSearcher, query, queryDef.getEnd(), facetsCollector);
			} else
				topDocs = indexSearcher.search(query, queryDef.getEnd());
			return new ResultDefinition(indexSearcher, topDocs, queryDef, facetsCollector);
		} catch (ParseException e) {
			throw new ServerException(e);
		}
	}
}
