/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.index.IndexStatus;
import com.qwazr.search.index.PostDefinition;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.LinkedHashMap;

import static com.qwazr.search.test.JsonAbstractTest.getDocs;
import static com.qwazr.search.test.JsonAbstractTest.getFieldMap;
import static com.qwazr.search.test.JsonAbstractTest.getJsonNode;
import static com.qwazr.search.test.JsonAbstractTest.getQuery;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GettingStartedTest {

    private final static String MY_SCHEMA = "my_schema";
    private final static String MY_INDEX = "my_index";
    public static final LinkedHashMap<String, FieldDefinition> MY_FIELDS_JSON = getFieldMap("my_fields.json");
    public static final JsonNode MY_RECORD_JSON = getJsonNode("my_record.json");
    public static final JsonNode MY_RECORDS_JSON = getJsonNode("my_records.json");
    public static final PostDefinition.Documents MY_DOCS_JSON = getDocs("my_docs.json");
    public static final QueryDefinition MY_SEARCH_JSON = getQuery("my_search.json");

    @Test
    public void test000startServer() throws Exception {
        TestServer.startServer();
        Assert.assertNotNull(TestServer.service);
    }

    @Test
    public void test0100CreateSchema() {
        Assert.assertNotNull(TestServer.remote.createUpdateSchema(MY_SCHEMA));
    }

    @Test
    public void test0110CreateIndex() {
        IndexServiceInterface client = TestServer.remote;
        IndexStatus indexStatus = client.createUpdateIndex(MY_SCHEMA, MY_INDEX, null);
        Assert.assertNotNull(indexStatus);
    }

    @Test
    public void test130SetFields() {
        IndexServiceInterface client = TestServer.remote;
        final LinkedHashMap<String, FieldDefinition> fields = client.setFields(MY_SCHEMA, MY_INDEX, MY_FIELDS_JSON);
        Assert.assertEquals(fields.size(), MY_FIELDS_JSON.size());
    }

    @Test
    public void test200UpdateOneRecord() {
        IndexServiceInterface client = TestServer.remote;
        final Integer result = client.postJson(MY_SCHEMA, MY_INDEX, MY_RECORD_JSON);
        Assert.assertNotNull(result);
        Assert.assertEquals(Integer.valueOf(1), result);
    }

    @Test
    public void test210UpdateRecords() {
        IndexServiceInterface client = TestServer.remote;
        final Integer result = client.postJson(MY_SCHEMA, MY_INDEX, MY_RECORDS_JSON);
        Assert.assertNotNull(result);
        Assert.assertEquals(Integer.valueOf(MY_RECORDS_JSON.size()), result);
    }

    @Test
    public void test220UpdateDocs() {
        IndexServiceInterface client = TestServer.remote;
        final Integer result = client.postMappedDocuments(MY_SCHEMA, MY_INDEX, MY_DOCS_JSON);
        Assert.assertNotNull(result);
        Assert.assertEquals(Integer.valueOf(MY_DOCS_JSON.documents.size()), result);

        // Check commit user data
        final IndexStatus status = client.getIndex(MY_SCHEMA, MY_INDEX);
        Assert.assertNotNull(status.commitUserData);
        Assert.assertEquals("my_value", status.commitUserData.get("my_key"));
    }

    @Test
    public void test300Query() {
        IndexServiceInterface client = TestServer.remote;
        final ResultDefinition.WithMap result = client.searchQuery(MY_SCHEMA, MY_INDEX, MY_SEARCH_JSON, null);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.totalHits);
    }

}
