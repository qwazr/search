/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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

import com.fasterxml.jackson.databind.JsonNode;
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.field.CustomFieldDefinition;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.BackupStatus;
import com.qwazr.search.index.ExplainDefinition;
import com.qwazr.search.index.IndexInstance;
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.IndexStatus;
import com.qwazr.search.index.PostDefinition;
import com.qwazr.search.index.QueryBuilder;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ReplicationStatus;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.index.ResultDocumentMap;
import com.qwazr.search.index.TermDefinition;
import com.qwazr.search.query.MatchAllDocs;
import com.qwazr.search.query.QueryParser;
import com.qwazr.search.query.QueryParserOperator;
import com.qwazr.search.similarity.CustomSimilarity;
import static com.qwazr.search.test.JavaAbstractTest.checkCollector;
import com.qwazr.server.ServerException;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.ObjectMappers;
import com.qwazr.utils.concurrent.RunnableEx;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class JsonAbstractTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    public static final String INDEX_DUMMY_NAME = "index-dummy";
    public static final String INDEX_ERROR_NAME = "test_error_index";
    public static final String INDEX_MASTER_NAME = "index-test-master-json";
    public static final String INDEX_TEST_NAME = "index-test-json";
    public static final String INDEX_BACKUP_NAME1 = "my_backup-1";
    public static final String INDEX_BACKUP_NAME2 = "my_backup-2";
    public static final String INDEX_BACKUP_NAME3 = "my_backup-3";
    public static final String INDEX_SLAVE_NAME = "index-test-slave-json";
    public static final String DUMMY_DOC_ID = "sflkjsdlksjdlkj";
    public static final String DUMMY_FIELD_NAME = "sflkjsdlksjdlkj";
    public static final String DUMMY_ANALYZER_NAME = "sflkjsdlksjdlkj";
    public static final String SYNONYMS_TXT = "synonyms.txt";
    public static final String SYNONYMS2_TXT = "synonyms2.txt";
    public static final long SYNONYM_LAST_MODIFIED = System.currentTimeMillis();
    public static final Map<String, FieldDefinition> FIELDS_JSON = getFieldMap("fields.json");
    public static final FieldDefinition FIELD_NAME_JSON = getField("field_name.json");
    public static final FieldDefinition FIELD_UPDATE_JSON = getField("field_update.json");
    public static final Map<String, AnalyzerDefinition> ANALYZERS_JSON = getAnalyzerMap("analyzers.json");
    public static final AnalyzerDefinition ANALYZER_FRENCH_JSON = getAnalyzer("analyzer_french.json");
    public static final QueryDefinition MATCH_ALL_QUERY = getQuery("query_match_all.json");
    public static final IndexSettingsDefinition INDEX_MASTER_SETTINGS = getIndexSettings("index_master_settings.json");
    public static final IndexSettingsDefinition INDEX_SLAVE_SETTINGS = getIndexSettings("index_slave_settings.json");
    public static final IndexSettingsDefinition INDEX_TEST_SETTINGS = getIndexSettings("index_test_settings.json");
    public static final QueryDefinition FACETS_ROWS_QUERY = getQuery("query_facets_rows.json");
    public static final QueryDefinition FACETS_FILTERS_QUERY = getQuery("query_facets_filters.json");
    public static final QueryDefinition FACETS_DRILLDOWN_QUERY = getQuery("query_facets_drilldown.json");
    public static final QueryDefinition QUERY_SORTFIELD_PRICE = getQuery("query_sortfield_price.json");
    public static final QueryDefinition QUERY_SORTFIELDS = getQuery("query_sortfields.json");
    public static final QueryDefinition QUERY_HIGHLIGHT = getQuery("query_highlight.json");
    public static final QueryDefinition QUERY_PAYLOAD_FILTER = getQuery("query_payload_filter.json");
    public static final QueryDefinition QUERY_CHECK_RETURNED = getQuery("query_check_returned.json");
    public static final QueryDefinition QUERY_CHECK_FUNCTIONS = getQuery("query_check_functions.json");
    public static final QueryDefinition QUERY_MULTIFIELD = getQuery("query_multifield.json");
    public static final QueryDefinition QUERY_STANDARDQUERYPARSER = getQuery("query_standardqueryparser.json");
    public static final QueryDefinition QUERY_GEO2D_BOX = getQuery("query_geo2d_box.json");
    public static final QueryDefinition QUERY_GEO3D_BOX = getQuery("query_geo3d_box.json");
    public static final QueryDefinition QUERY_GEO2D_DISTANCE = getQuery("query_geo2d_distance.json");
    public static final QueryDefinition QUERY_GEO3D_DISTANCE = getQuery("query_geo3d_distance.json");
    public static final QueryDefinition DELETE_QUERY = getQuery("query_delete.json");
    public static final PostDefinition.Document UPDATE_DOC = getDoc("update_doc.json");
    public static final PostDefinition.Document UPDATE_DOC_ERROR = getDoc("update_doc_error.json");
    public static final PostDefinition.Documents UPDATE_DOCS = getDocs("update_docs.json");
    public static final PostDefinition.Document UPDATE_DOC_VALUE = getDoc("update_doc_value.json");
    public static final PostDefinition.Documents UPDATE_DOCS_VALUES = getDocs("update_docs_values.json");

    protected abstract IndexServiceInterface getClient() throws URISyntaxException;

    @Test
    public void test000startServer() throws Exception {
        TestServer.startServer();
        Assert.assertNotNull(TestServer.service);
    }

    @Test
    public void test020CheckErrorIndex() throws URISyntaxException {
        IndexServiceInterface client = getClient();
        exceptionRule.expect(WebApplicationException.class);
        exceptionRule.expectMessage("Index not found: " + INDEX_ERROR_NAME);
        Assert.assertNotNull(client.getIndex(INDEX_ERROR_NAME));
    }

    public static void checkErrorStatusCode(RunnableEx<?> runnable, int expectedStatusCode) {
        try {
            runnable.run();
            Assert.fail("WebApplicationException was not thrown");
        } catch (WebApplicationException e) {
            Assert.assertEquals(expectedStatusCode, e.getResponse().getStatus());
        } catch (ServerException e) {
            Assert.assertEquals(expectedStatusCode, e.getStatusCode());
        } catch (IllegalArgumentException e) {
            // Thats ok
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, FieldDefinition> getFieldMap(String res) {
        try (final InputStream is = JsonAbstractTest.class.getResourceAsStream(res)) {
            return FieldDefinition.newFieldMap(IOUtils.toString(is, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static FieldDefinition getField(String res) {
        try (final InputStream is = JsonAbstractTest.class.getResourceAsStream(res)) {
            return FieldDefinition.newField(IOUtils.toString(is, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, AnalyzerDefinition> getAnalyzerMap(String res) {
        try (final InputStream is = JsonAbstractTest.class.getResourceAsStream(res)) {
            return AnalyzerDefinition.newAnalyzerMap(IOUtils.toString(is, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static AnalyzerDefinition getAnalyzer(String res) {
        try (final InputStream is = JsonAbstractTest.class.getResourceAsStream(res)) {
            return AnalyzerDefinition.newAnalyzer(IOUtils.toString(is, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static IndexSettingsDefinition getIndexSettings(String res) {
        try (InputStream is = JsonAbstractTest.class.getResourceAsStream(res)) {
            return ObjectMappers.JSON.readValue(is, IndexSettingsDefinition.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test100CreateIndexWithoutSettings() throws URISyntaxException, IOException {
        IndexServiceInterface client = getClient();
        IndexStatus indexStatus = client.createUpdateIndex(INDEX_MASTER_NAME, null);
        Assert.assertNotNull(indexStatus);
        checkErrorStatusCode(() -> client.getIndex(INDEX_DUMMY_NAME), 404);
        indexStatus = client.getIndex(INDEX_MASTER_NAME);
        Assert.assertNotNull(indexStatus);
        Assert.assertEquals(Long.valueOf(0), indexStatus.numDocs);
        checkAllSizes(client, 0);
        checkErrorStatusCode(() -> client.deleteIndex(INDEX_DUMMY_NAME), 404);
        client.deleteIndex(INDEX_MASTER_NAME);
    }

    @Test
    public void test105CreateIndexWithSettings() throws URISyntaxException, IOException {
        IndexServiceInterface client = getClient();
        IndexStatus indexStatus = client.createUpdateIndex(INDEX_MASTER_NAME, INDEX_MASTER_SETTINGS);
        Assert.assertNotNull(indexStatus);
        Assert.assertNotNull(indexStatus.settings);
        Assert.assertNotNull(indexStatus.settings.similarityClass);
        Assert.assertNull(indexStatus.settings.indexReaderWarmer);
        Assert.assertNull(indexStatus.settings.useSimpleTextCodec);
        checkAllSizes(client, 0);
    }

    @Test
    public void test107CreateIndexWithOtherSettings() throws URISyntaxException, IOException {
        IndexServiceInterface client = getClient();
        IndexStatus indexStatus = client.createUpdateIndex(INDEX_TEST_NAME, INDEX_TEST_SETTINGS);
        Assert.assertNotNull(indexStatus);
        Assert.assertNotNull(indexStatus.settings);
        Assert.assertNull(indexStatus.settings.similarityClass);
        Assert.assertNotNull(indexStatus.settings.indexReaderWarmer);
        Assert.assertFalse(indexStatus.settings.indexReaderWarmer);
        Assert.assertEquals(CustomSimilarity.CUSTOM_SIMILARITY, indexStatus.settings.similarity);
        Assert.assertNotNull(indexStatus.settings.useSimpleTextCodec);
        Assert.assertTrue(indexStatus.settings.useSimpleTextCodec);
        checkAllSizes(client, 0);
    }

    private Response checkResponse(Response response, int... codes) {
        Assert.assertNotNull(response);
        final int returnedCode = response.getStatus();
        for (int code : codes)
            if (code == returnedCode)
                return response;
        Assert.fail("Wrong returned code: " + returnedCode);
        return null;
    }

    @Test
    public void test110putResource() throws IOException, URISyntaxException {
        IndexServiceInterface client = getClient();
        Map<String, IndexInstance.ResourceInfo> resources = client.getResources(INDEX_MASTER_NAME);
        Assert.assertNotNull(resources);
        Assert.assertEquals(0, resources.size());
        try (final InputStream input = JsonAbstractTest.class.getResourceAsStream(SYNONYMS_TXT)) {
            Assert.assertTrue(
                client.postResource(INDEX_MASTER_NAME, SYNONYMS_TXT, SYNONYM_LAST_MODIFIED, input));
        }
        try (final InputStream input = client.getResource(INDEX_MASTER_NAME, SYNONYMS_TXT)) {
            Assert.assertNotNull(input);
            IOUtils.copy(input, NullOutputStream.NULL_OUTPUT_STREAM);
        }
    }

    @Test
    public void test115deleteResource() throws IOException, URISyntaxException {
        IndexServiceInterface client = getClient();
        try (final InputStream input = JsonAbstractTest.class.getResourceAsStream(SYNONYMS_TXT)) {
            Assert.assertTrue(
                client.postResource(INDEX_MASTER_NAME, SYNONYMS2_TXT, SYNONYM_LAST_MODIFIED, input));
        }
        Map<String, IndexInstance.ResourceInfo> resources = client.getResources(INDEX_MASTER_NAME);
        Assert.assertNotNull(resources);
        Assert.assertEquals(2, resources.size());
        Assert.assertTrue(client.deleteResource(INDEX_MASTER_NAME, SYNONYMS2_TXT));
        resources = client.getResources(INDEX_MASTER_NAME);
        Assert.assertNotNull(resources);
        Assert.assertEquals(1, resources.size());
    }

    @Test
    public void test120SetAnalyzers() throws URISyntaxException, IOException {
        IndexServiceInterface client = getClient();
        checkErrorStatusCode(() -> client.setAnalyzers(INDEX_DUMMY_NAME, ANALYZERS_JSON), 404);
        Map<String, AnalyzerDefinition> analyzers =
            client.setAnalyzers(INDEX_MASTER_NAME, ANALYZERS_JSON);
        Assert.assertEquals(analyzers.size(), ANALYZERS_JSON.size());
        IndexStatus indexStatus = client.getIndex(INDEX_MASTER_NAME);
        Assert.assertNotNull(indexStatus.analyzers);
        Assert.assertEquals(indexStatus.analyzers.size(), ANALYZERS_JSON.size());
        checkAllSizes(client, 0);
    }

    @Test
    public void test122DeleteFrenchAnalyzer() throws URISyntaxException {
        final IndexServiceInterface client = getClient();
        checkErrorStatusCode(() -> client.deleteAnalyzer(INDEX_DUMMY_NAME, "FrenchAnalyzer"), 404);
        client.deleteAnalyzer(INDEX_MASTER_NAME, "FrenchAnalyzer");
        final Map<String, AnalyzerDefinition> analyzers = client.getAnalyzers(INDEX_MASTER_NAME);
        Assert.assertNotNull(analyzers);
        Assert.assertNull(analyzers.get("FrenchAnalyzer"));
    }

    @Test
    public void test124SetFrenchAnalyzer() throws URISyntaxException {
        final IndexServiceInterface client = getClient();
        client.setAnalyzer(INDEX_MASTER_NAME, "FrenchAnalyzer", ANALYZER_FRENCH_JSON);
        checkErrorStatusCode(() -> client.getAnalyzers(INDEX_DUMMY_NAME), 404);
        final Map<String, AnalyzerDefinition> analyzers = client.getAnalyzers(INDEX_MASTER_NAME);
        Assert.assertNotNull(analyzers);
        Assert.assertNotNull(analyzers.get("FrenchAnalyzer"));
    }

    @Test
    public void test125GetFrenchAnalyzer() throws URISyntaxException {
        final IndexServiceInterface client = getClient();
        checkErrorStatusCode(() -> client.getAnalyzer(INDEX_MASTER_NAME, DUMMY_ANALYZER_NAME), 404);
        final AnalyzerDefinition analyzer = client.getAnalyzer(INDEX_MASTER_NAME, "FrenchAnalyzer");
        Assert.assertNotNull(analyzer);
        Assert.assertEquals(analyzer.getFilters().size(), ANALYZER_FRENCH_JSON.getFilters().size());
        Assert.assertEquals(analyzer.getTokenizer().size(), ANALYZER_FRENCH_JSON.getTokenizer().size());
    }

    @Test
    public void test126TestFrenchAnalyzer() throws URISyntaxException {
        final IndexServiceInterface client = getClient();
        checkErrorStatusCode(
            () -> client.testAnalyzer(INDEX_DUMMY_NAME, "FrenchAnalyzer", "Bonjour le monde!"), 404);
        final List<TermDefinition> termDefinitions =
            client.testAnalyzer(INDEX_MASTER_NAME, "FrenchAnalyzer", "Bonjour le monde!");
        Assert.assertNotNull(termDefinitions);
        Assert.assertEquals(3, termDefinitions.size());
        Assert.assertEquals("bonjou", termDefinitions.get(0).char_term);
    }

    @Test
    public void test128TestAnalyzerDot() throws URISyntaxException {
        final IndexServiceInterface client = getClient();
        final String dot = client.testAnalyzerDot(INDEX_MASTER_NAME, "EnglishSynonymAnalyzer",
            "The United States is wealthy!");
        Assert.assertNotNull(dot);
        Assert.assertTrue(dot.contains("united"));
        Assert.assertTrue(dot.contains("states"));
        Assert.assertTrue(dot.contains("wealthy"));
    }

    @Test
    public void test130SetFields() throws URISyntaxException, IOException {
        final IndexServiceInterface client = getClient();
        checkErrorStatusCode(() -> client.setFields(INDEX_DUMMY_NAME, null), 404);
        final Map<String, FieldDefinition> fields = client.setFields(INDEX_MASTER_NAME, FIELDS_JSON);
        Assert.assertEquals(fields.size(), FIELDS_JSON.size());
        final IndexStatus indexStatus = client.getIndex(INDEX_MASTER_NAME);
        Assert.assertNotNull(indexStatus.fields);
        Assert.assertEquals(indexStatus.fields.size(), FIELDS_JSON.size());
        checkAllSizes(client, 0);
    }

    @Test
    public void test131GetField() throws URISyntaxException {
        final IndexServiceInterface client = getClient();
        checkErrorStatusCode(() -> client.getField(INDEX_MASTER_NAME, DUMMY_FIELD_NAME), 404);
        FIELDS_JSON.forEach((fieldName, fieldDefinition) -> {
            CustomFieldDefinition fieldDef =
                (CustomFieldDefinition) client.getField(INDEX_MASTER_NAME, fieldName);
            Assert.assertEquals(fieldDef.template, ((CustomFieldDefinition) fieldDefinition).template);
            Assert.assertEquals(fieldDef.getAnalyzer(), fieldDefinition.getAnalyzer());
        });

    }

    @Test
    public void test132DeleteNameField() throws URISyntaxException {
        final IndexServiceInterface client = getClient();
        checkErrorStatusCode(() -> client.deleteField(INDEX_DUMMY_NAME, "name"), 404);
        client.deleteField(INDEX_MASTER_NAME, "name");
        final Map<String, FieldDefinition> fields = client.getFields(INDEX_MASTER_NAME);
        Assert.assertNotNull(fields);
        Assert.assertNull(fields.get("name"));
    }

    @Test
    public void test134SetNameField() throws URISyntaxException {
        final IndexServiceInterface client = getClient();
        checkErrorStatusCode(() -> client.setField(INDEX_DUMMY_NAME, null, null), 404);
        client.setField(INDEX_MASTER_NAME, "name", FIELD_NAME_JSON);
        final Map<String, FieldDefinition> fields = client.getFields(INDEX_MASTER_NAME);
        Assert.assertNotNull(fields);
        Assert.assertNotNull(fields.get("name"));
    }

    public static QueryDefinition getQuery(String res) {
        try (final InputStream is = JsonAbstractTest.class.getResourceAsStream(res)) {
            return QueryDefinition.newQuery(IOUtils.toString(is, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ResultDefinition.WithMap checkQueryIndex(IndexServiceInterface client, String index,
                                                     QueryDefinition queryDef, int expectedCount) {
        checkErrorStatusCode(() -> client.searchQuery(INDEX_DUMMY_NAME, queryDef, null), 404);
        final ResultDefinition.WithMap result = client.searchQuery(index, queryDef, null);
        Assert.assertNotNull(result);
        Assert.assertNotNull(result.totalHits);
        Assert.assertEquals(expectedCount, result.totalHits);
        return result;
    }

    private ResultDefinition.WithMap checkQueryIndex(IndexServiceInterface client, QueryDefinition queryDef,
                                                     int expectedCount) throws IOException {
        return checkQueryIndex(client, INDEX_MASTER_NAME, queryDef, expectedCount);
    }

    private ResultDefinition.WithMap checkQuerySlaveIndex(IndexServiceInterface client, QueryDefinition queryDef,
                                                          int expectedCount) throws IOException {
        return checkQueryIndex(client, INDEX_SLAVE_NAME, queryDef, expectedCount);
    }

    private void checkIndexSize(IndexServiceInterface client, long expectedCount) {
        checkErrorStatusCode(() -> client.getIndex(INDEX_DUMMY_NAME), 404);
        IndexStatus status = client.getIndex(INDEX_MASTER_NAME);
        Assert.assertNotNull(status);
        Assert.assertNotNull(status.numDocs);
        Assert.assertEquals(expectedCount, status.numDocs.longValue());
    }

    private void checkAllSizes(IndexServiceInterface client, int expectedSize) throws IOException {
        checkQueryIndex(client, MATCH_ALL_QUERY, expectedSize);
        checkIndexSize(client, expectedSize);
    }

    public static JsonNode getJsonNode(String res) {
        try (InputStream is = JsonAbstractTest.class.getResourceAsStream(res)) {
            return ObjectMappers.JSON.readTree(is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static PostDefinition.Documents getDocs(String res) {
        try (InputStream is = JsonAbstractTest.class.getResourceAsStream(res)) {
            return ObjectMappers.JSON.readValue(is, PostDefinition.Documents.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static PostDefinition.Document getDoc(String res) {
        try (InputStream is = JsonAbstractTest.class.getResourceAsStream(res)) {
            return ObjectMappers.JSON.readValue(is, PostDefinition.Document.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Number> checkFacetSize(ResultDefinition.WithMap result, String dimName, int size) {
        Assert.assertTrue(result.facets.containsKey(dimName));
        final Map<String, Number> facetCounts = result.facets.get(dimName);
        Assert.assertNotNull(facetCounts);
        Assert.assertEquals(size, result.facets.get(dimName).size());
        return facetCounts;
    }

    private void checkEmptyFacets(ResultDefinition.WithMap result) {
        Assert.assertNotNull(result.facets);
        checkFacetSize(result, "category", 0);
        checkFacetSize(result, "format", 0);
    }

    @Test
    public void test190EmptyQueryFacetFilterDoc() throws URISyntaxException, IOException {
        final IndexServiceInterface client = getClient();
        checkEmptyFacets(checkQueryIndex(client, FACETS_FILTERS_QUERY, 0));
    }

    @Test
    public void test200UpdateDocs() throws URISyntaxException, IOException {
        final IndexServiceInterface client = getClient();
        for (int i = 0; i < 6; i++) { // Yes, six times: we said "testing" !
            checkErrorStatusCode(() -> client.postMappedDocuments(INDEX_DUMMY_NAME, UPDATE_DOCS), 404);
            checkErrorStatusCode(() -> client.postMappedDocuments(INDEX_DUMMY_NAME, null), 404);
            Assert.assertEquals(Integer.valueOf(0), client.postMappedDocuments(INDEX_MASTER_NAME, null));
            final Integer result = client.postMappedDocuments(INDEX_MASTER_NAME, UPDATE_DOCS);
            Assert.assertNotNull(result);
            Assert.assertEquals(Integer.valueOf(UPDATE_DOCS.documents.size()), result);
            checkAllSizes(client, 4);
        }
    }

    private BackupStatus doBackup(IndexServiceInterface client, String backupName) {
        checkErrorStatusCode(() -> client.doBackup(INDEX_DUMMY_NAME, backupName), 404);
        SortedMap<String, BackupStatus> statusMap =
            client.doBackup(INDEX_MASTER_NAME, backupName);
        Assert.assertNotNull(statusMap);
        BackupStatus status = statusMap.get(INDEX_MASTER_NAME);
        Assert.assertNotNull(status);
        Assert.assertNotNull(status.date);
        Assert.assertNotNull(status.bytesSize);
        Assert.assertTrue(status.bytesSize > 0);
        Assert.assertNotNull(status.filesCount);
        Assert.assertTrue(status.filesCount > 0);
        return status;
    }

    private BackupStatus checkBackup(final SortedMap<String, SortedMap<String, BackupStatus>> backups,
                                     final String backupName) {
        Assert.assertNotNull(backups);
        final SortedMap<String, BackupStatus> backupNameResult = backups.get(backupName);
        Assert.assertNotNull(backupNameResult);
        final BackupStatus status = backupNameResult.get(INDEX_MASTER_NAME);
        Assert.assertNotNull(status);
        return status;
    }

    private BackupStatus getAndCheckBackup(final IndexServiceInterface client,
                                           final String backupName,
                                           final boolean extractVersion) {
        return checkBackup(client.getBackups(INDEX_MASTER_NAME, backupName, extractVersion), backupName);
    }

    @Test
    public void test250FirstBackup() throws URISyntaxException {
        final IndexServiceInterface client = getClient();
        Assert.assertTrue(client.getBackups(INDEX_MASTER_NAME, INDEX_BACKUP_NAME1, true).isEmpty());
        final SortedMap<String, SortedMap<String, BackupStatus>> results =
            client.getBackups(INDEX_MASTER_NAME, INDEX_BACKUP_NAME1, true);
        Assert.assertNotNull(results);
        Assert.assertTrue(results.isEmpty());
        BackupStatus status1 = doBackup(client, INDEX_BACKUP_NAME1);
        BackupStatus status2 = getAndCheckBackup(client, INDEX_BACKUP_NAME1, false);
        Assert.assertEquals(status1, status2);
    }

    @Test
    public void test300UpdateDoc() throws URISyntaxException, IOException {
        final IndexServiceInterface client = getClient();
        for (int i = 0; i < 7; i++) { // Seven times: we said "testing" !
            checkErrorStatusCode(() -> client.postMappedDocument(INDEX_DUMMY_NAME, UPDATE_DOC), 404);
            Assert.assertEquals(Integer.valueOf(0), client.postMappedDocument(INDEX_MASTER_NAME, null));
            Assert.assertEquals(Integer.valueOf(1),
                client.postMappedDocument(INDEX_MASTER_NAME, UPDATE_DOC));
            checkAllSizes(client, 5);
        }
    }

    @Test
    public void test350UpdateDocValue() throws URISyntaxException, IOException {
        final IndexServiceInterface client = getClient();

        // Check that the initial stock is 0 for all documents
        ResultDefinition.WithMap result = checkQueryIndex(client, QUERY_CHECK_RETURNED, 5);
        Assert.assertNotNull(result.documents);
        for (ResultDocumentMap document : result.documents) {
            final Integer stock = (Integer) document.fields.get("stock");
            Assert.assertNotNull(stock);
            Assert.assertEquals(0, (int) stock);
        }

        // Update one document value
        checkErrorStatusCode(() -> client.updateMappedDocValues(INDEX_DUMMY_NAME, UPDATE_DOC_VALUE), 404);
        Assert.assertEquals(Integer.valueOf(0), client.updateMappedDocValues(INDEX_MASTER_NAME, null));
        Assert.assertEquals(Integer.valueOf(1),
            client.updateMappedDocValues(INDEX_MASTER_NAME, UPDATE_DOC_VALUE));
        checkAllSizes(client, 5);

        // Update a list of documents values
        checkErrorStatusCode(() -> client.updateMappedDocsValues(INDEX_DUMMY_NAME, UPDATE_DOCS_VALUES),
            404);
        Assert.assertEquals(Integer.valueOf(0), client.updateMappedDocsValues(INDEX_MASTER_NAME, null));
        final Integer count = client.updateMappedDocsValues(INDEX_MASTER_NAME, UPDATE_DOCS_VALUES);
        Assert.assertNotNull(count);
        Assert.assertEquals(Integer.valueOf(UPDATE_DOCS_VALUES.documents.size()), count);
        checkAllSizes(client, 5);

        // Check the result
        result = checkQueryIndex(client, QUERY_CHECK_RETURNED, 5);
        Assert.assertNotNull(result.documents);
        for (ResultDocumentMap document : result.documents) {
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

    private void checkFacetRowsQuery(ResultDefinition.WithMap result) {
        Assert.assertNotNull(result.documents);
        Assert.assertEquals(3, result.documents.size());
        for (ResultDocumentMap doc : result.documents) {
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
        final Map<String, Number> facetCounts = checkFacetSize(result, "FacetQueries", 2);
        Assert.assertEquals(5, facetCounts.get("AllDocs"));
        Assert.assertEquals(2, facetCounts.get("2016,January"));
    }

    @Test
    public void test400QueryDoc() throws URISyntaxException, IOException {
        final IndexServiceInterface client = getClient();
        checkFacetRowsQuery(checkQueryIndex(client, FACETS_ROWS_QUERY, 5));
    }

    @Test
    public void test410GetDocumentById() throws URISyntaxException, IOException {
        final String id = (String) UPDATE_DOC.document.get(FieldDefinition.ID_FIELD);
        IndexServiceInterface client = getClient();
        checkErrorStatusCode(() -> client.getDocument(INDEX_DUMMY_NAME, id), 404);
        checkErrorStatusCode(() -> client.getDocument(INDEX_MASTER_NAME, DUMMY_DOC_ID), 404);
        final Map<String, ?> doc = client.getDocument(INDEX_MASTER_NAME, id);
        Assert.assertNotNull(doc);
    }

    @Test
    public void test412GetDocuments() throws URISyntaxException, IOException {
        final IndexServiceInterface client = getClient();
        checkErrorStatusCode(() -> client.getDocuments(INDEX_DUMMY_NAME, 0, 10), 404);
        final List<Map<String, Object>> docs = client.getDocuments(INDEX_MASTER_NAME, 0, 10);
        Assert.assertNotNull(docs);
        Assert.assertEquals(5L, docs.size());
    }

    private void checkReturnedFields(Map<String, Object> fields, String fieldName, Object... values) {
        Assert.assertNotNull(fields);
        Object object = fields.get(fieldName);
        Assert.assertNotNull(object);
        if (object instanceof ArrayList)
            object = ((ArrayList) object).toArray();
        Assert.assertTrue(Objects.deepEquals(object, values));
    }

    private void checkFacetFiltersResult(ResultDefinition.WithMap result, int simpleFacetExpectCount) {
        Assert.assertNotNull(result.documents);
        Assert.assertEquals(2, result.documents.size());
        Assert.assertNotNull(result.documents.get(0).fields);
        Assert.assertNotNull(result.documents.get(1).fields);
        Assert.assertEquals("Fourth name", result.documents.get(0).fields.get("name"));
        Assert.assertEquals("Fifth name", result.documents.get(1).fields.get("name"));
        checkReturnedFields(result.documents.get(0).fields, "dynamic_multi_facet_cat", "dyn_cat1", "dyn_cat2",
            "dyn_cat3", "dyn_cat4");
        checkReturnedFields(result.documents.get(1).fields, "dynamic_multi_facet_cat", "dyn_cat1", "dyn_cat2",
            "dyn_cat3", "dyn_cat4", "dyn_cat5");
        Assert.assertNotNull(result.facets);
        checkFacetSize(result, "category", 5);
        checkFacetSize(result, "dynamic_multi_facet_cat", 5);
        checkFacetSize(result, "format", 2);
        checkFacetSize(result, "dynamic_simple_facet_type", simpleFacetExpectCount);
    }

    @Test
    public void test410QueryFacetFilterDoc() throws URISyntaxException, IOException {
        final IndexServiceInterface client = getClient();
        checkFacetFiltersResult(checkQueryIndex(client, FACETS_FILTERS_QUERY, 2), 2);
    }

    @Test
    public void test411QueryFacetDrillDown() throws IOException, URISyntaxException {
        final IndexServiceInterface client = getClient();
        checkFacetFiltersResult(checkQueryIndex(client, FACETS_DRILLDOWN_QUERY, 2), 5);
    }

    private <T extends Comparable> void checkDescending(T startValue, String field,
                                                        Collection<ResultDocumentMap> documents) {
        T old = startValue;
        for (ResultDocumentMap document : documents) {
            Assert.assertNotNull(document.fields);
            T val = (T) document.fields.get(field);
            Assert.assertNotNull(val);
            Assert.assertTrue(val.compareTo(old) <= 0);
            old = val;
        }
    }

    private <T extends Comparable> void checkAscending(T startValue, String field,
                                                       Collection<ResultDocumentMap> documents) {
        T old = startValue;
        for (ResultDocumentMap document : documents) {
            Assert.assertNotNull(document.fields);
            T val = (T) document.fields.get(field);
            Assert.assertNotNull(val);
            Assert.assertTrue(val.compareTo(old) >= 0);
            old = val;
        }
    }

    @Test
    public void test420QuerySortFieldPriceDoc() throws URISyntaxException, IOException {
        final IndexServiceInterface client = getClient();
        final ResultDefinition result = checkQueryIndex(client, QUERY_SORTFIELD_PRICE, 5);
        Assert.assertNotNull(result.documents);
        checkDescending(Double.MAX_VALUE, "price", result.documents);
    }

    @Test
    public void test425QuerySortFieldsDoc() throws URISyntaxException, IOException {
        final IndexServiceInterface client = getClient();
        final ResultDefinition result = checkQueryIndex(client, QUERY_SORTFIELDS, 5);
        checkAscending(Double.MIN_VALUE, "price", result.documents);
    }

    @Test
    public void test430collectorFunctions() throws URISyntaxException, IOException {
        final IndexServiceInterface client = getClient();
        final ResultDefinition.WithMap result = checkQueryIndex(client, QUERY_CHECK_FUNCTIONS, 5);
        checkCollector(result, "minStock", 10);
        checkCollector(result, "maxStock", 14);
        checkCollector(result, "minPrice", 1.1D);
        checkCollector(result, "maxPrice", 10.5D);
    }

    private boolean checkSnippets(ResultDocumentMap document, String snippetName, String... patterns) {
        if (document == null)
            return false;
        final Map<String, String> highlights = document.getHighlights();
        if (highlights == null)
            return false;
        final String snippet = highlights.get(snippetName);
        if (snippet == null)
            return false;
        for (String pattern : patterns)
            if (!snippet.contains(pattern))
                return false;
        return true;
    }

    @Test
    public void test440QueryHighlight() throws URISyntaxException, IOException {
        final IndexServiceInterface client = getClient();
        final ResultDefinition<ResultDocumentMap> result = checkQueryIndex(client, QUERY_HIGHLIGHT, 1);
        final ResultDocumentMap document = result.getDocuments().get(0);
        if (!checkSnippets(document, "my_custom_snippet", "<strong>search</strong>", "<strong>engine</strong>"))
            Assert.fail("Snippet not found");
        if (!checkSnippets(document, "my_default_snippet", "<b>search</b>", "<b>engine</b>"))
            Assert.fail("Snippet not found");
    }

    private void checkSynonyms(final IndexServiceInterface client, final String queryString,
                               final String... multiWordsHighlights) throws IOException {
        final QueryBuilder builder = QUERY_HIGHLIGHT.of();
        builder.query(QueryParser.of("description")
            .setDefaultOperator(QueryParserOperator.AND)
            .setQueryString(queryString)
            .build());
        final ResultDefinition<ResultDocumentMap> result = checkQueryIndex(client, builder.build(), 2);
        boolean foundMultiWord1 = false;
        boolean foundMultiWord2 = false;
        boolean foundSimpleWord1 = false;
        boolean foundSimpleWord2 = false;
        for (ResultDocumentMap document : result.getDocuments()) {
            for (String multiWordHighlight : multiWordsHighlights) {
                foundMultiWord1 = foundMultiWord1 ||
                    checkSnippets(document, "my_custom_snippet", "<strong>" + multiWordHighlight + "</strong>");
                foundMultiWord2 = foundMultiWord2 ||
                    checkSnippets(document, "my_default_snippet", "<b>" + multiWordHighlight + "</b>");
            }
            foundSimpleWord1 = foundSimpleWord1 || checkSnippets(document, "my_custom_snippet", "<strong>USA</strong>");
            foundSimpleWord2 = foundSimpleWord2 || checkSnippets(document, "my_default_snippet", "<b>USA</b>");
        }
        Assert.assertTrue(foundMultiWord1 && foundMultiWord2 && foundSimpleWord1 && foundSimpleWord2);
    }

    @Test
    public void test442QuerySynonyms() throws URISyntaxException, IOException {
        final IndexServiceInterface client = getClient();
        checkSynonyms(client, "usa", "United States");
        checkSynonyms(client, "united states", "United", "States");
        checkSynonyms(client, "united states of america", "United", "States");
    }

    @Test
    public void test450MultiFieldQuery() throws URISyntaxException, IOException {
        final IndexServiceInterface client = getClient();
        final ResultDefinition<ResultDocumentMap> result = checkQueryIndex(client, QUERY_MULTIFIELD, 1);
        final ResultDocumentMap document = result.getDocuments().get(0);
        Assert.assertTrue(((List<String>) document.fields.get("description")).get(0).startsWith("A web search engine"));
        Assert.assertEquals(0, document.pos);
    }

    @Test
    public void test452StandardQueryParser() throws URISyntaxException, IOException {
        final IndexServiceInterface client = getClient();
        final ResultDefinition<ResultDocumentMap> result = checkQueryIndex(client, QUERY_STANDARDQUERYPARSER, 1);
        final ResultDocumentMap document = result.getDocuments().get(0);
        Assert.assertTrue(
            ((List<String>) document.getFields().get("description")).get(0).startsWith("A web search engine"));
        Assert.assertEquals(0, document.getPos());
    }

    @Test
    public void test453getQueryCacheStats() throws URISyntaxException {
        final IndexServiceInterface client = getClient();
        IndexStatus.QueryCacheStats cache = client.getIndex(INDEX_MASTER_NAME).queryCache;
        Assert.assertNotNull(cache);
        Assert.assertNotNull(cache.cacheCount);
        Assert.assertNotNull(cache.cacheSize);
        Assert.assertNotNull(cache.hitRate);
        Assert.assertNotNull(cache.missRate);
    }

    @Test
    public void test455getDocument() throws URISyntaxException {
        final IndexServiceInterface client = getClient();
        final Map<String, ?> result = client.getDocument(INDEX_MASTER_NAME, "5");
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsKey("alpha_rank"));
        Assert.assertEquals("e", result.get("alpha_rank"));
        Assert.assertTrue(result.containsKey("price"));
        Assert.assertEquals(10.5D, result.get("price"));
        Assert.assertTrue(result.containsKey("description"));
        final List<Object> list = (List<Object>) result.get("description");
        Assert.assertNotNull(list);
        Assert.assertFalse(list.isEmpty());
        Assert.assertTrue(list.get(0).toString().length() > 0);
    }

    @Test
    public void test460SearchPayload() throws URISyntaxException, IOException {
        final IndexServiceInterface client = getClient();
        final ResultDefinition<ResultDocumentMap> result = checkQueryIndex(client, QUERY_PAYLOAD_FILTER, 1);
        Assert.assertNotNull(result);
        final ResultDocumentMap document = result.getDocuments().get(0);
        Assert.assertEquals(3123, document.score, 0);
    }

    private void checkGeo(final QueryDefinition queryDef, final String name) throws URISyntaxException, IOException {
        final IndexServiceInterface client = getClient();
        final ResultDefinition<ResultDocumentMap> result = checkQueryIndex(client, queryDef, 1);
        Assert.assertNotNull(result);
        final ResultDocumentMap document = result.getDocuments().get(0);
        Assert.assertEquals(name, document.getFields().get("name"));
    }

    @Test
    public void test470QueryGeoBox() throws URISyntaxException, IOException {
        checkGeo(QUERY_GEO2D_BOX, "Second name");
        checkGeo(QUERY_GEO3D_BOX, "Second name");
    }

    @Test
    public void test471QueryGeoDistance() throws URISyntaxException, IOException {
        checkGeo(QUERY_GEO2D_DISTANCE, "First name");
        checkGeo(QUERY_GEO3D_DISTANCE, "First name");
    }

    @Test
    public void test480explainQuery() throws URISyntaxException, IOException {
        final IndexServiceInterface client = getClient();
        final ResultDefinition<ResultDocumentMap> result = checkQueryIndex(client, QUERY_GEO2D_BOX, 1);
        Assert.assertNotNull(result);
        final String docId = result.getDocuments().get(0).getFields().get(FieldDefinition.ID_FIELD).toString();
        final ExplainDefinition explain = client.explainQuery(INDEX_MASTER_NAME, QUERY_GEO2D_BOX, docId);
        Assert.assertNotNull(explain);
        String explainText = client.explainQueryText(INDEX_MASTER_NAME, QUERY_GEO2D_BOX, docId);
        Assert.assertNotNull(explainText);
        String explainDot = client.explainQueryDot(INDEX_MASTER_NAME, QUERY_GEO2D_BOX, docId, 30);
        Assert.assertNotNull(explainDot);
    }

    @Test
    public void test500SecondBackup() throws URISyntaxException {
        final IndexServiceInterface client = getClient();
        BackupStatus status = doBackup(client, INDEX_BACKUP_NAME2);
        Assert.assertEquals(status,
            checkBackup(client.getBackups(INDEX_MASTER_NAME, INDEX_BACKUP_NAME2, false),
                INDEX_BACKUP_NAME2));
        // Same backup again
        status = doBackup(client, INDEX_BACKUP_NAME2);
        Assert.assertEquals(status,
            checkBackup(client.getBackups(INDEX_MASTER_NAME, INDEX_BACKUP_NAME2, false),
                INDEX_BACKUP_NAME2));
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
        final String[] term_results = {"there", "are", "few", "parts", "of", "texts"};
        final IndexServiceInterface client = getClient();
        checkErrorStatusCode(
            () -> client.doAnalyzeIndex(INDEX_DUMMY_NAME, "name", "There are few parts of texts"),
            404);
        checkAnalyzerResult(term_results,
            client.doAnalyzeIndex(INDEX_MASTER_NAME, "name", "There are few parts of texts"));

        checkAnalyzerResult(term_results,
            client.doAnalyzeQuery(INDEX_MASTER_NAME, "name", "There are few parts of texts"));
    }

    @Test
    public void test610TermsEnum() throws URISyntaxException {
        final IndexServiceInterface client = getClient();
        checkErrorStatusCode(() -> client.doExtractTerms(INDEX_DUMMY_NAME, "name", null, null, null), 404);
        Assert.assertNotNull(client.doExtractTerms(INDEX_MASTER_NAME, "name", null, null, null));
        final int firstSize = JavaAbstractTest.checkTermList(
            client.doExtractTerms(INDEX_MASTER_NAME, "name", null, null, 10000)).size();
        final int secondSize = JavaAbstractTest.checkTermList(
            client.doExtractTerms(INDEX_MASTER_NAME, "name", null, 2, 10000)).size();
        Assert.assertEquals(firstSize, secondSize + 2);
        JavaAbstractTest.checkTermList(client.doExtractTerms(INDEX_MASTER_NAME, "name", "a", null, null));
    }

    @Test
    public void test620getFieldStats() throws URISyntaxException {
        final IndexServiceInterface client = getClient();
        client.getFields(INDEX_MASTER_NAME)
            .keySet()
            .forEach(fieldName -> JavaAbstractTest.checkFieldStats(fieldName,
                client.getFieldStats(INDEX_MASTER_NAME, fieldName)));
    }

    @Test
    public void test700paging() throws URISyntaxException {
        final IndexServiceInterface client = getClient();
        QueryBuilder builder = QueryDefinition.of(MatchAllDocs.INSTANCE).returnedField("*");
        builder.start(0).rows(0);
        ResultDefinition.WithMap result = client.searchQuery(INDEX_MASTER_NAME, builder.build(), false);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.totalHits > 0);
        builder.rows(1);
        Set<Object> idSet = new HashSet<>();
        for (int i = 0; i < result.totalHits; i++) {
            builder.start(i);
            ResultDefinition.WithMap result2 =
                client.searchQuery(INDEX_MASTER_NAME, builder.build(), false);
            idSet.add(result2.getDocuments().get(0).getFields().get("$id$"));
        }
        Assert.assertEquals(result.totalHits, idSet.size());
    }

    @Test
    public void test800ThirdBackup() throws URISyntaxException {
        final IndexServiceInterface client = getClient();
        final BackupStatus status = doBackup(client, INDEX_BACKUP_NAME3);
        Assert.assertEquals(status,
            checkBackup(client.getBackups(INDEX_MASTER_NAME, INDEX_BACKUP_NAME3, false),
                INDEX_BACKUP_NAME3));
        client.doBackup("*", INDEX_BACKUP_NAME3);
        Assert.assertEquals(status, getAndCheckBackup(client, INDEX_BACKUP_NAME3, false));
        final SortedMap<String, SortedMap<String, BackupStatus>> backups =
            client.getBackups(INDEX_MASTER_NAME, "*", true);
        Assert.assertNotNull(backups);
        checkBackup(backups, INDEX_BACKUP_NAME1);
        checkBackup(backups, INDEX_BACKUP_NAME2);
        checkBackup(backups, INDEX_BACKUP_NAME3);
    }

    @Test
    public void test810DeleteBackup() throws URISyntaxException {
        final IndexServiceInterface client = getClient();
        Assert.assertEquals(Integer.valueOf(1),
            client.deleteBackups(INDEX_MASTER_NAME, INDEX_BACKUP_NAME1));
        Assert.assertEquals(Integer.valueOf(2), client.deleteBackups("*", "*"));
    }

    private void checkReplication(final IndexServiceInterface client) {

        // Get the status of the master
        final IndexStatus masterStatus = client.getIndex(INDEX_MASTER_NAME);
        Assert.assertNotNull(masterStatus);
        Assert.assertNotNull(masterStatus.version);

        // Get the fields and analyzers of the master
        final Map<String, FieldDefinition> masterFields = client.getFields(INDEX_MASTER_NAME);
        final Map<String, AnalyzerDefinition> masterAnalyzers = client.getAnalyzers(INDEX_MASTER_NAME);
        Assert.assertNotNull(masterFields);
        Assert.assertNotNull(masterAnalyzers);

        // Get the status of the slave
        IndexStatus slaveStatus = client.getIndex(INDEX_SLAVE_NAME);
        Assert.assertNotNull(slaveStatus);
        Assert.assertNotNull(slaveStatus.version);
        Assert.assertEquals(slaveStatus.version, masterStatus.version);
        Assert.assertEquals(slaveStatus.masterUuid, masterStatus.indexUuid);

        // Get the fields and analyzers of the slave
        final Map<String, FieldDefinition> slaveFields = client.getFields(INDEX_SLAVE_NAME);
        final Map<String, AnalyzerDefinition> slaveAnalyzers = client.getAnalyzers(INDEX_SLAVE_NAME);
        Assert.assertNotNull(slaveFields);
        Assert.assertNotNull(slaveAnalyzers);

        // Cbeck equality
        Assert.assertArrayEquals(slaveFields.keySet().toArray(), masterFields.keySet().toArray());
        Assert.assertArrayEquals(slaveAnalyzers.keySet().toArray(), masterAnalyzers.keySet().toArray());
        Assert.assertEquals(masterStatus.version, slaveStatus.version);
        Assert.assertEquals(masterStatus.numDocs, slaveStatus.numDocs);
    }

    private void checkReplicationStatus(final ReplicationStatus status) {
        Assert.assertNotNull(status);
        Assert.assertNotNull(status.size);
        Assert.assertNotNull(status.start);
        Assert.assertNotNull(status.end);
    }

    @Test
    public void test850replicationCheck() throws URISyntaxException {
        final IndexServiceInterface client = getClient();

        final Map<String, FieldDefinition> masterFields = client.getFields(INDEX_MASTER_NAME);
        final Map<String, AnalyzerDefinition> masterAnalyzers = client.getAnalyzers(INDEX_MASTER_NAME);
        Assert.assertNotNull(masterFields);
        Assert.assertNotNull(masterAnalyzers);

        IndexStatus slaveStatus = client.createUpdateIndex(INDEX_SLAVE_NAME, INDEX_SLAVE_SETTINGS);
        Assert.assertNotNull(slaveStatus);

        // First replication
        // First replication has something to do
        checkReplicationStatus(client.replicationCheck(INDEX_SLAVE_NAME));
        checkReplication(client);

        // Second one should do nothing
        checkReplicationStatus(client.replicationCheck(INDEX_SLAVE_NAME));
        checkReplication(client);
    }

    @Test
    public void test851checkTaxonomyReplication() throws IOException, URISyntaxException {
        final IndexServiceInterface client = getClient();
        checkFacetFiltersResult(checkQuerySlaveIndex(client, FACETS_DRILLDOWN_QUERY, 2), 5);
    }

    private static class UpdateDocThread implements Runnable {

        final IndexServiceInterface client;
        final AtomicInteger counter;

        UpdateDocThread(final IndexServiceInterface client, final AtomicInteger counter) {
            this.client = client;
            this.counter = counter;
        }

        @Override
        public void run() {
            while (counter.incrementAndGet() < 50) {
                client.postMappedDocument(INDEX_MASTER_NAME, UPDATE_DOC);
                client.replicationCheck(INDEX_SLAVE_NAME);
            }
        }
    }

    private static class UpdateDocValueThread extends UpdateDocThread {

        UpdateDocValueThread(final IndexServiceInterface client, final AtomicInteger counter) {
            super(client, counter);
        }

        @Override
        public void run() {
            while (counter.incrementAndGet() < 50) {
                client.updateMappedDocValues(INDEX_MASTER_NAME, UPDATE_DOC_VALUE);
                client.replicationCheck(INDEX_SLAVE_NAME);
            }
        }
    }

    @Test
    public void test860stressedReplication() throws URISyntaxException, InterruptedException, ExecutionException {

        final IndexServiceInterface client = getClient();
        checkReplication(client);

        final ExecutorService executor = Executors.newFixedThreadPool(2);
        final AtomicInteger counter = new AtomicInteger();

        final List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            futures.add(executor.submit(new UpdateDocValueThread(client, counter)));
            futures.add(executor.submit(new UpdateDocThread(client, counter)));
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);
        for (Future<?> future : futures)
            future.get();
        checkReplication(client);
    }

    @Test
    public void test880errorRecoveryOnFieldUpdate() throws URISyntaxException {
        final IndexServiceInterface client = getClient();

        // Change the field type
        Assert.assertEquals(Integer.valueOf(1), client.postMappedDocument(INDEX_MASTER_NAME, UPDATE_DOC));
        final FieldDefinition newDef = client.setField(INDEX_MASTER_NAME, "alpha_rank", FIELD_UPDATE_JSON);
        Assert.assertEquals(newDef, FIELD_UPDATE_JSON);
        checkErrorStatusCode(() -> client.postMappedDocument(INDEX_MASTER_NAME, UPDATE_DOC_ERROR), 500);
        Assert.assertNotNull(client.getIndex(INDEX_MASTER_NAME));
        final Map<String, ?> result = client.getDocument(INDEX_MASTER_NAME, "5");
        Assert.assertNotNull(result);
    }

    @Test
    public void test900DeleteDoc() throws URISyntaxException, IOException {
        final IndexServiceInterface client = getClient();
        final ResultDefinition<?> result = client.searchQuery(INDEX_MASTER_NAME, DELETE_QUERY, true);
        Assert.assertNotNull(result);
        Assert.assertEquals(3L, result.totalHits);
        checkAllSizes(client, 3);
    }

    private void checkDelete(String indexName) throws URISyntaxException {
        final IndexServiceInterface client = getClient();
        checkErrorStatusCode(() -> client.deleteIndex(INDEX_DUMMY_NAME), 404);
        Assert.assertTrue(client.deleteIndex(indexName));
    }

    @Test
    public void test970checkUpdatableAnalyzers() throws URISyntaxException {
        final IndexServiceInterface client = getClient();
        IndexStatus indexStatus = client.getIndex(INDEX_MASTER_NAME);
        Assert.assertEquals(1, indexStatus.activeQueryAnalyzers, 0);
        Assert.assertEquals(1, indexStatus.activeIndexAnalyzers, 0);
        indexStatus = client.getIndex(INDEX_SLAVE_NAME);
        Assert.assertEquals(1, indexStatus.activeQueryAnalyzers, 0);
        Assert.assertEquals(1, indexStatus.activeIndexAnalyzers, 0);
    }

    @Test
    public void test980DeleteIndex() throws URISyntaxException {
        checkDelete(INDEX_SLAVE_NAME);
        checkDelete(INDEX_MASTER_NAME);
        final IndexServiceInterface client = getClient();
        Map<String, UUID> indexes = client.getIndexes();
        Assert.assertNotNull(indexes);
        Assert.assertFalse(indexes.containsKey(INDEX_SLAVE_NAME));
        Assert.assertFalse(indexes.containsKey(INDEX_MASTER_NAME));
    }

    @Test
    public void test999stopServer() throws IOException {
        TestServer.stopServer();
        Assert.assertNull(TestServer.service);
    }
}
