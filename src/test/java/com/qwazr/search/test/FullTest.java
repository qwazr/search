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
package com.qwazr.search.test;

import com.google.common.io.Files;
import com.qwazr.search.SearchServer;
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.*;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.json.JsonMapper;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FullTest {

	private static volatile boolean started;

	public static final String BASE_URL = "http://localhost:9091";
	public static final String SCHEMA_NAME = "schema-test";
	public static final String INDEX_NAME = "index-test";
	public static final LinkedHashMap<String, FieldDefinition> FIELDS_JSON = getFieldMap("fields.json");
	public static final FieldDefinition FIELD_NAME_JSON = getField("field_name.json");
	public static final LinkedHashMap<String, AnalyzerDefinition> ANALYZERS_JSON = getAnalyzerMap("analyzers.json");
	public static final AnalyzerDefinition ANALYZER_FRENCH_JSON = getAnalyzer("analyzer_french.json");
	public static final QueryDefinition MATCH_ALL_QUERY = getQuery("query_match_all.json");
	public static final IndexSettingsDefinition INDEX_SETTINGS = getIndexSettings("index_settings.json");
	public static final QueryDefinition FACETS_ROWS_QUERY = getQuery("query_facets_rows.json");
	public static final QueryDefinition FACETS_FILTERS_QUERY = getQuery("query_facets_filters.json");
	public static final QueryDefinition QUERY_SORTFIELD = getQuery("query_sortfield.json");
	public static final QueryDefinition QUERY_SORTFIELDS = getQuery("query_sortfields.json");
	public static final QueryDefinition QUERY_HIGHLIGHT = getQuery("query_highlight.json");
	public static final QueryDefinition QUERY_CHECK_RETURNED = getQuery("query_check_returned.json");
	public static final QueryDefinition QUERY_CHECK_FUNCTIONS = getQuery("query_check_functions.json");
	public static final QueryDefinition DELETE_QUERY = getQuery("query_delete.json");
	public static final Map<String, Object> UPDATE_DOC = getDoc("update_doc.json");
	public static final List<Map<String, Object>> UPDATE_DOCS = getDocs("update_docs.json");
	public static final Map<String, Object> UPDATE_DOC_VALUE = getDoc("update_doc_value.json");
	public static final List<Map<String, Object>> UPDATE_DOCS_VALUES = getDocs("update_docs_values.json");

	private IndexServiceInterface getClient() throws URISyntaxException {
		return new IndexSingleClient(BASE_URL, 60000);
	}

	@BeforeClass
	public static void startSearchServer() throws Exception {
		final File dataDir = Files.createTempDir();
		final String[] parameters = { "-d", dataDir.getAbsolutePath() };
		SearchServer.main(parameters);
	}

	@Test
	public void test000CreateSchema() throws URISyntaxException {
		IndexServiceInterface client = getClient();
		SchemaSettingsDefinition settings = client.createUpdateSchema(SCHEMA_NAME, null);
		Assert.assertNotNull(settings);
	}

	@Test
	public void test001UpdateSchema() throws URISyntaxException {
		IndexServiceInterface client = getClient();
		SchemaSettingsDefinition settings = client.createUpdateSchema(SCHEMA_NAME, null);
		Assert.assertNotNull(settings);
	}

	@Test
	public void test002ListSchema() throws URISyntaxException {
		IndexServiceInterface client = getClient();
		Set<String> schemas = client.getSchemas();
		Assert.assertNotNull(schemas);
		Assert.assertEquals(schemas.size(), 1);
		Assert.assertTrue(schemas.contains(SCHEMA_NAME));
	}

	private static LinkedHashMap<String, FieldDefinition> getFieldMap(String res) {
		InputStream is = FullTest.class.getResourceAsStream(res);
		try {
			return FieldDefinition.newFieldMap(IOUtils.toString(is));
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.close(is);
		}
	}

	private static FieldDefinition getField(String res) {
		InputStream is = FullTest.class.getResourceAsStream(res);
		try {
			return FieldDefinition.newField(IOUtils.toString(is));
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.close(is);
		}
	}

	private static LinkedHashMap<String, AnalyzerDefinition> getAnalyzerMap(String res) {
		InputStream is = FullTest.class.getResourceAsStream(res);
		try {
			return AnalyzerDefinition.newAnalyzerMap(IOUtils.toString(is));
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.close(is);
		}
	}

	private static AnalyzerDefinition getAnalyzer(String res) {
		InputStream is = FullTest.class.getResourceAsStream(res);
		try {
			return AnalyzerDefinition.newAnalyzer(IOUtils.toString(is));
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.close(is);
		}
	}

	private static IndexSettingsDefinition getIndexSettings(String res) {
		InputStream is = FullTest.class.getResourceAsStream(res);
		try {
			return JsonMapper.MAPPER.readValue(is, IndexSettingsDefinition.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.close(is);
		}
	}

	@Test
	public void test100CreateIndexWithoutSettings() throws URISyntaxException, IOException {
		IndexServiceInterface client = getClient();
		IndexStatus indexStatus = client.createUpdateIndex(SCHEMA_NAME, INDEX_NAME, null);
		Assert.assertNotNull(indexStatus);
		indexStatus = client.getIndex(SCHEMA_NAME, INDEX_NAME);
		Assert.assertNotNull(indexStatus);
		Assert.assertEquals(new Long(0), indexStatus.num_docs);
		checkAllSizes(client, 0);
		client.deleteIndex(SCHEMA_NAME, INDEX_NAME);
	}

	@Test
	public void test110CreateIndexWithSettings() throws URISyntaxException, IOException {
		IndexServiceInterface client = getClient();
		IndexStatus indexStatus = client.createUpdateIndex(SCHEMA_NAME, INDEX_NAME, INDEX_SETTINGS);
		Assert.assertNotNull(indexStatus);
		Assert.assertNotNull(indexStatus.settings);
		Assert.assertNotNull(indexStatus.settings.similarity_class);
		checkAllSizes(client, 0);
	}

	@Test
	public void test120SetAnalyzers() throws URISyntaxException, IOException {
		IndexServiceInterface client = getClient();
		LinkedHashMap<String, AnalyzerDefinition> analyzers = client
				.setAnalyzers(SCHEMA_NAME, INDEX_NAME, ANALYZERS_JSON);
		Assert.assertEquals(analyzers.size(), ANALYZERS_JSON.size());
		IndexStatus indexStatus = client.getIndex(SCHEMA_NAME, INDEX_NAME);
		Assert.assertNotNull(indexStatus.analyzers);
		Assert.assertEquals(indexStatus.analyzers.size(), ANALYZERS_JSON.size());
		checkAllSizes(client, 0);
	}

	@Test
	public void test122DeleteFrenchAnalyzer() throws URISyntaxException {
		IndexServiceInterface client = getClient();
		client.deleteAnalyzer(SCHEMA_NAME, INDEX_NAME, "FrenchAnalyzer");
		Map<String, AnalyzerDefinition> analyzers = client.getAnalyzers(SCHEMA_NAME, INDEX_NAME);
		Assert.assertNotNull(analyzers);
		Assert.assertNull(analyzers.get("FrenchAnalyzer"));
	}

	@Test
	public void test124SetFrenchAnalyzer() throws URISyntaxException {
		IndexServiceInterface client = getClient();
		client.setAnalyzer(SCHEMA_NAME, INDEX_NAME, "FrenchAnalyzer", ANALYZER_FRENCH_JSON);
		Map<String, AnalyzerDefinition> analyzers = client.getAnalyzers(SCHEMA_NAME, INDEX_NAME);
		Assert.assertNotNull(analyzers);
		Assert.assertNotNull(analyzers.get("FrenchAnalyzer"));
	}

	@Test
	public void test130SetFields() throws URISyntaxException, IOException {
		IndexServiceInterface client = getClient();
		LinkedHashMap<String, FieldDefinition> fields = client.setFields(SCHEMA_NAME, INDEX_NAME, FIELDS_JSON);
		Assert.assertEquals(fields.size(), FIELDS_JSON.size());
		IndexStatus indexStatus = client.getIndex(SCHEMA_NAME, INDEX_NAME);
		Assert.assertNotNull(indexStatus.fields);
		Assert.assertEquals(indexStatus.fields.size(), FIELDS_JSON.size());
		checkAllSizes(client, 0);
	}

	@Test
	public void test132DeleteNameField() throws URISyntaxException {
		IndexServiceInterface client = getClient();
		client.deleteField(SCHEMA_NAME, INDEX_NAME, "name");
		Map<String, FieldDefinition> fields = client.getFields(SCHEMA_NAME, INDEX_NAME);
		Assert.assertNotNull(fields);
		Assert.assertNull(fields.get("name"));
	}

	@Test
	public void test134SetNameField() throws URISyntaxException {
		IndexServiceInterface client = getClient();
		client.setField(SCHEMA_NAME, INDEX_NAME, "name", FIELD_NAME_JSON);
		Map<String, FieldDefinition> fields = client.getFields(SCHEMA_NAME, INDEX_NAME);
		Assert.assertNotNull(fields);
		Assert.assertNotNull(fields.get("name"));
	}

	private static QueryDefinition getQuery(String res) {
		InputStream is = FullTest.class.getResourceAsStream(res);
		try {
			return QueryDefinition.newQuery(IOUtils.toString(is));
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.close(is);
		}
	}

	private ResultDefinition checkQuerySchema(IndexServiceInterface client, QueryDefinition queryDef, int expectedCount)
			throws IOException {
		ResultDefinition result = client.searchQuery(SCHEMA_NAME, "*", queryDef, null);
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.total_hits);
		Assert.assertEquals(expectedCount, result.total_hits.intValue());
		return result;
	}

	private ResultDefinition checkQueryIndex(IndexServiceInterface client, QueryDefinition queryDef, int expectedCount)
			throws IOException {
		ResultDefinition result = client.searchQuery(SCHEMA_NAME, INDEX_NAME, queryDef, null);
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.total_hits);
		Assert.assertEquals(expectedCount, result.total_hits.intValue());
		return result;
	}

	private void checkIndexSize(IndexServiceInterface client, long expectedCount) throws IOException {
		IndexStatus status = client.getIndex(SCHEMA_NAME, INDEX_NAME);
		Assert.assertNotNull(status);
		Assert.assertNotNull(status.num_docs);
		Assert.assertEquals(expectedCount, status.num_docs.longValue());
	}

	private void checkAllSizes(IndexServiceInterface client, int expectedSize) throws IOException {
		checkQuerySchema(client, MATCH_ALL_QUERY, expectedSize);
		checkQueryIndex(client, MATCH_ALL_QUERY, expectedSize);
		checkIndexSize(client, expectedSize);
	}

	private static List<Map<String, Object>> getDocs(String res) {
		InputStream is = FullTest.class.getResourceAsStream(res);
		try {
			return JsonMapper.MAPPER.readValue(is, IndexSingleClient.ListMapStringObjectTypeRef);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.close(is);
		}
	}

	private static Map<String, Object> getDoc(String res) {
		InputStream is = FullTest.class.getResourceAsStream(res);
		try {
			return JsonMapper.MAPPER.readValue(is, IndexSingleClient.MapStringObjectTypeRef);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.close(is);
		}
	}

	private Map<String, Number> checkFacetSize(ResultDefinition result, String dimName, int size) {
		Assert.assertTrue(result.facets.containsKey(dimName));
		final Map<String, Number> facetCounts = result.facets.get(dimName);
		Assert.assertNotNull(facetCounts);
		Assert.assertEquals(size, result.facets.get(dimName).size());
		return facetCounts;
	}

	private void checkEmptyFacets(ResultDefinition result) {
		Assert.assertNotNull(result.facets);
		checkFacetSize(result, "category", 0);
		checkFacetSize(result, "format", 0);
	}

	@Test
	public void test190EmptyQueryFacetFilterDoc() throws URISyntaxException, IOException {
		IndexServiceInterface client = getClient();
		checkEmptyFacets(checkQueryIndex(client, FACETS_FILTERS_QUERY, 0));
		checkEmptyFacets(checkQuerySchema(client, FACETS_FILTERS_QUERY, 0));
	}

	@Test
	public void test200UpdateDocs() throws URISyntaxException, IOException {
		IndexServiceInterface client = getClient();
		for (int i = 0; i < 6; i++) { // Yes, six times: we said "testing" !
			Response response = client.postDocuments(SCHEMA_NAME, INDEX_NAME, UPDATE_DOCS);
			Assert.assertNotNull(response);
			Assert.assertEquals(200, response.getStatusInfo().getStatusCode());
			checkAllSizes(client, 4);
		}
	}

	private BackupStatus doBackup(IndexServiceInterface client) {
		BackupStatus status = client.doBackup(SCHEMA_NAME, INDEX_NAME, null);
		Assert.assertNotNull(status);
		Assert.assertNotNull(status.date);
		Assert.assertNotNull(status.bytes_size);
		Assert.assertTrue(status.bytes_size > 0);
		Assert.assertNotNull(status.files_count);
		Assert.assertTrue(status.files_count > 0);
		return status;
	}

	private List<BackupStatus> getBackups(IndexServiceInterface client, int expectedSize) {
		List<BackupStatus> backups = client.getBackups(SCHEMA_NAME, INDEX_NAME);
		Assert.assertNotNull(backups);
		Assert.assertEquals(expectedSize, backups.size());
		return backups;
	}

	@Test
	public void test250FirstBackup() throws URISyntaxException, IOException {
		IndexServiceInterface client = getClient();
		List<BackupStatus> backups = client.getBackups(SCHEMA_NAME, INDEX_NAME);
		Assert.assertNotNull(backups);
		Assert.assertTrue(backups.isEmpty());
		BackupStatus status = doBackup(client);
		Assert.assertEquals(status, getBackups(client, 1).get(0));
	}

	@Test
	public void test300UpdateDoc() throws URISyntaxException, IOException {
		IndexServiceInterface client = getClient();
		for (int i = 0; i < 7; i++) { // Seven times: we said "testing" !
			Response response = client.postDocument(SCHEMA_NAME, INDEX_NAME, UPDATE_DOC);
			Assert.assertNotNull(response);
			Assert.assertEquals(200, response.getStatusInfo().getStatusCode());
			checkAllSizes(client, 5);
		}
	}

	@Test
	public void test350UpdateDocValue() throws URISyntaxException, IOException {
		IndexServiceInterface client = getClient();

		// Check that the initial stock is 0 for all documents
		ResultDefinition result = checkQueryIndex(client, QUERY_CHECK_RETURNED, 5);
		Assert.assertNotNull(result.documents);
		for (ResultDocument document : result.documents) {
			Integer stock = (Integer) document.fields.get("stock");
			Assert.assertNotNull(stock);
			Assert.assertEquals(0, (int) stock);
		}

		// Update one document value
		Response response = client.updateDocumentValues(SCHEMA_NAME, INDEX_NAME, UPDATE_DOC_VALUE);
		Assert.assertNotNull(response);
		Assert.assertEquals(200, response.getStatusInfo().getStatusCode());
		checkAllSizes(client, 5);

		// Update a list of documents values
		response = client.updateDocumentsValues(SCHEMA_NAME, INDEX_NAME, UPDATE_DOCS_VALUES);
		Assert.assertNotNull(response);
		Assert.assertEquals(200, response.getStatusInfo().getStatusCode());
		checkAllSizes(client, 5);

		// Check the result
		result = checkQueryIndex(client, QUERY_CHECK_RETURNED, 5);
		Assert.assertNotNull(result.documents);
		for (ResultDocument document : result.documents) {
			// Check that the price is still here
			Assert.assertNotNull(document.fields);
			Double price = (Double) document.fields.get("price");
			Assert.assertNotNull(price);
			Assert.assertNotEquals(0, price);
			// The stock should be change to another value than 0
			Integer stock = (Integer) document.fields.get("stock");
			Assert.assertNotNull(stock);
			Assert.assertNotEquals(0, (double) stock);
		}
	}

	private void checkFacetRowsQuery(ResultDefinition result) {
		Assert.assertNotNull(result.documents);
		Assert.assertEquals(3, result.documents.size());
		for (ResultDocument doc : result.documents) {
			Assert.assertNotNull(doc.fields);
			Assert.assertEquals(2, doc.fields.size());
			Assert.assertTrue(doc.fields.containsKey("name"));
			Assert.assertTrue(doc.fields.get("name") instanceof String);
			Assert.assertTrue(doc.fields.containsKey("price"));
			Assert.assertTrue(doc.fields.get("price") instanceof Double);
		}
		Assert.assertNotNull(result.facets);
		checkFacetSize(result, "category", 5);
		checkFacetSize(result, "format", 2);
		Map<String, Number> facetCounts = checkFacetSize(result, "FacetQueries", 2);
		Assert.assertEquals(5, facetCounts.get("AllDocs"));
		Assert.assertEquals(2, facetCounts.get("2016,January"));
	}

	@Test
	public void test400QueryDoc() throws URISyntaxException, IOException {
		IndexServiceInterface client = getClient();
		checkFacetRowsQuery(checkQueryIndex(client, FACETS_ROWS_QUERY, 5));
		checkFacetRowsQuery(checkQuerySchema(client, FACETS_ROWS_QUERY, 5));
	}

	private void checkFacetFiltersResult(ResultDefinition result) {
		Assert.assertNotNull(result.documents);
		Assert.assertEquals(2, result.documents.size());
		Assert.assertNotNull(result.documents.get(0).fields);
		Assert.assertNotNull(result.documents.get(1).fields);
		Assert.assertEquals("Fourth name", result.documents.get(0).fields.get("name"));
		Assert.assertEquals("Fifth name", result.documents.get(1).fields.get("name"));
		Assert.assertNotNull(result.facets);
		checkFacetSize(result, "category", 5);
		checkFacetSize(result, "format", 2);
	}

	@Test
	public void test410QueryFacetFilterDoc() throws URISyntaxException, IOException {
		IndexServiceInterface client = getClient();
		checkFacetFiltersResult(checkQueryIndex(client, FACETS_FILTERS_QUERY, 2));
		checkFacetFiltersResult(checkQuerySchema(client, FACETS_FILTERS_QUERY, 2));
	}

	private <T extends Comparable> void checkDescending(T startValue, String field,
			Collection<ResultDocument> documents) {
		T old = startValue;
		for (ResultDocument document : documents) {
			Assert.assertNotNull(document.fields);
			T val = (T) document.fields.get(field);
			Assert.assertNotNull(val);
			Assert.assertTrue(val.compareTo(old) <= 0);
			old = val;
		}
	}

	private <T extends Comparable> void checkAscending(T startValue, String field,
			Collection<ResultDocument> documents) {
		T old = startValue;
		for (ResultDocument document : documents) {
			Assert.assertNotNull(document.fields);
			T val = (T) document.fields.get(field);
			Assert.assertNotNull(val);
			Assert.assertTrue(val.compareTo(old) >= 0);
			old = val;
		}
	}

	@Test
	public void test420QuerySortFieldDoc() throws URISyntaxException, IOException {
		IndexServiceInterface client = getClient();
		ResultDefinition result = checkQueryIndex(client, QUERY_SORTFIELD, 5);
		Assert.assertNotNull(result.documents);
		checkDescending(Double.MAX_VALUE, "price", result.documents);
		result = checkQueryIndex(client, QUERY_SORTFIELDS, 5);
		checkAscending(Double.MIN_VALUE, "price", result.documents);
	}

	@Test
	public void test430QueryFunctionsDoc() throws URISyntaxException, IOException {
		Object[] results = new Object[] { 1.1D, 10.5D, 10, 14 };
		IndexServiceInterface client = getClient();
		ResultDefinition result = checkQueryIndex(client, QUERY_CHECK_FUNCTIONS, 5);
		Assert.assertNotNull(result.functions);
		Assert.assertEquals(results.length, result.functions.size());
		for (int i = 0; i < result.functions.size(); i++) {
			Assert.assertEquals(QUERY_CHECK_FUNCTIONS.functions.get(i).field, result.functions.get(i).field);
			Assert.assertEquals(QUERY_CHECK_FUNCTIONS.functions.get(i).function, result.functions.get(i).function);
			Assert.assertNotNull(result.functions.get(i).value);
			Assert.assertEquals(results[i], result.functions.get(i).value);
		}
	}

	@Test
	public void test440QueryHighlight() throws URISyntaxException, IOException {
		IndexServiceInterface client = getClient();
		ResultDefinition result = checkQueryIndex(client, QUERY_HIGHLIGHT, 1);
		ResultDocument document = result.getDocuments().get(0);
		String snippet = document.getHighlights().get("my_custom_snippet");
		Assert.assertNotNull(snippet);
		Assert.assertTrue(snippet.contains("<strong>search</strong>"));
		Assert.assertTrue(snippet.contains("<strong>engine</strong>"));
		snippet = document.getHighlights().get("my_default_snippet");
		Assert.assertNotNull(snippet);
		Assert.assertTrue(snippet.contains("<b>search</b>"));
		Assert.assertTrue(snippet.contains("<b>engine</b>"));
	}

	@Test
	public void test500SecondBackup() throws URISyntaxException, IOException {
		IndexServiceInterface client = getClient();
		BackupStatus status = doBackup(client);
		Assert.assertEquals(status, getBackups(client, 2).get(0));
		// Same backup again
		status = doBackup(client);
		Assert.assertEquals(status, getBackups(client, 2).get(0));
	}

	private void checkAnalyzerResult(String[] term_results, List<TermDefinition> termList) {
		Assert.assertNotNull(termList);
		Assert.assertEquals(term_results.length, termList.size());
		int i = 0;
		for (String term : term_results)
			Assert.assertEquals(term, termList.get(i++).char_term);
	}

	@Test
	public void test600FieldAnalyzer() throws URISyntaxException {
		final String[] term_results = { "there", "are", "few", "parts", "of", "texts" };
		IndexServiceInterface client = getClient();
		checkAnalyzerResult(term_results,
				client.doAnalyzeIndex(SCHEMA_NAME, INDEX_NAME, "name", "There are few parts of texts"));

		checkAnalyzerResult(term_results,
				client.doAnalyzeQuery(SCHEMA_NAME, INDEX_NAME, "name", "There are few parts of texts"));
	}

	@Test
	public void test700DeleteDoc() throws URISyntaxException, IOException {
		IndexServiceInterface client = getClient();
		ResultDefinition result = client.searchQuery(SCHEMA_NAME, INDEX_NAME, DELETE_QUERY, true);
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.total_hits);
		Assert.assertEquals(2L, (long) result.total_hits);
		checkAllSizes(client, 3);
	}

	@Test
	public void test800ThirdBackup() throws URISyntaxException, IOException {
		IndexServiceInterface client = getClient();
		BackupStatus status = doBackup(client);
		Assert.assertEquals(status, getBackups(client, 3).get(0));
		client.doBackup("*", "*", 2);
		Assert.assertEquals(status, getBackups(client, 2).get(0));
	}

	@Test
	public void test980DeleteIndex() throws URISyntaxException {
		IndexServiceInterface client = getClient();
		Response response = client.deleteIndex(SCHEMA_NAME, INDEX_NAME);
		Assert.assertNotNull(response);
		Assert.assertEquals(200, response.getStatusInfo().getStatusCode());
	}

	@Test
	public void test981EmptyIndex() throws URISyntaxException {
		IndexServiceInterface client = getClient();
		Set<String> indexes = client.getIndexes(SCHEMA_NAME);
		Assert.assertNotNull(indexes);
		Assert.assertTrue(indexes.isEmpty());
	}

	@Test
	public void test990DeleteSchema() throws URISyntaxException {
		IndexServiceInterface client = getClient();
		Response response = client.deleteSchema(SCHEMA_NAME);
		Assert.assertNotNull(response);
		Assert.assertEquals(200, response.getStatusInfo().getStatusCode());

	}

	@Test
	public void test991EmptySchema() throws URISyntaxException {
		IndexServiceInterface client = getClient();
		Set<String> schemas = client.getSchemas();
		Assert.assertNotNull(schemas);
		Assert.assertTrue(schemas.isEmpty());
	}

}
