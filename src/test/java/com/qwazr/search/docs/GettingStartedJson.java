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
package com.qwazr.search.docs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.qwazr.search.JsonHelpers;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.IndexJsonResult;
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.index.IndexStatus;
import com.qwazr.search.index.PostDefinition;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.test.TestServer;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GettingStartedJson implements JsonHelpers {

    private final String MY_INDEX = "my_index";
    private final Map<String, FieldDefinition> MY_FIELDS_JSON = getFieldMap("my_fields.json");
    private final JsonNode MY_RECORD_JSON = getJsonNode("my_record.json");
    private final JsonNode MY_RECORDS_JSON = getJsonNode("my_records.json");
    private final PostDefinition.Documents MY_DOCS_JSON = getDocs("my_docs.json");
    private final QueryDefinition MY_SEARCH_JSON = getQuery("my_search.json");
    private final QueryDefinition BUILD_SEARCH_REQUEST_JSON = getQuery("build_search_request.json");

    @Test
    public void test000startServer() throws Exception {
        TestServer.startServer();
        Assert.assertNotNull(TestServer.service);
    }

    @Test
    public void test0110CreateIndex() {
        IndexServiceInterface client = TestServer.remote;
        IndexStatus indexStatus = client.createUpdateIndex(MY_INDEX, null);
        Assert.assertNotNull(indexStatus);
    }

    @Test
    public void test130SetFields() {
        IndexServiceInterface client = TestServer.remote;
        final Map<String, FieldDefinition> fields = client.setFields(MY_INDEX, MY_FIELDS_JSON);
        Assert.assertEquals(fields.size(), MY_FIELDS_JSON.size());
    }

    @Test
    public void test200UpdateOneRecord() {
        IndexServiceInterface client = TestServer.remote;
        final IndexJsonResult result = client.postJson(MY_INDEX, null, MY_RECORD_JSON);
        Assert.assertNotNull(result);
        Assert.assertEquals(Integer.valueOf(1), result.count);
        Assert.assertNull(result.fieldTypes);
    }

    @Test
    public void test210UpdateRecords() {
        IndexServiceInterface client = TestServer.remote;
        final IndexJsonResult result = client.postJson(MY_INDEX, true, MY_RECORDS_JSON);
        Assert.assertNotNull(result);
        Assert.assertEquals(Integer.valueOf(MY_RECORDS_JSON.size()), result.count);
        Assert.assertEquals(result.fieldTypes, Map.of(
            "$id$", Set.of(JsonNodeType.STRING),
            "category", Set.of(JsonNodeType.STRING),
            "description", Set.of(JsonNodeType.STRING),
            "name", Set.of(JsonNodeType.STRING)));
    }

    @Test
    public void test220UpdateDocs() {
        IndexServiceInterface client = TestServer.remote;
        final Integer result = client.postMappedDocuments(MY_INDEX, MY_DOCS_JSON);
        Assert.assertNotNull(result);
        Assert.assertEquals(Integer.valueOf(MY_DOCS_JSON.documents.size()), result);

        // Check commit user data
        final IndexStatus status = client.getIndex(MY_INDEX);
        Assert.assertNotNull(status.commitUserData);
        Assert.assertEquals("my_value", status.commitUserData.get("my_key"));
    }

    @Test
    public void test300Query() {
        IndexServiceInterface client = TestServer.remote;
        final ResultDefinition.WithMap result = client.searchQuery(MY_INDEX, MY_SEARCH_JSON, null);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.totalHits);
    }

    @Test
    public void test350BuildSearchRequestQuery() {
        IndexServiceInterface client = TestServer.remote;
        final ResultDefinition.WithMap result = client.searchQuery(MY_INDEX, BUILD_SEARCH_REQUEST_JSON, false);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.totalHits);
    }

    @Test
    public void test999stopServer() throws IOException {
        TestServer.stopServer();
        Assert.assertNull(TestServer.service);
    }
}
