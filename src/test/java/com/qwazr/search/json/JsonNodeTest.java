/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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
package com.qwazr.search.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.qwazr.search.index.IndexJsonResult;
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.query.ExactDouble;
import com.qwazr.search.query.HasTerm;
import com.qwazr.search.query.SimpleQueryParser;
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.utils.ObjectMappers;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class JsonNodeTest extends AbstractIndexTest {

    private final static String INDEX = "jsonIndex";

    static IndexServiceInterface service;

    static final JsonNode issueJson = getJson("issue.json");

    private static JsonNode getJson(final String resourceName) {
        try (final InputStream is = JsonNodeTest.class.getResourceAsStream(resourceName)) {
            return ObjectMappers.JSON.readTree(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeClass
    public static void setup() {
        service = initIndexManager(true).getService();

        // Create the schema and the index

        service.createUpdateIndex(INDEX,
            IndexSettingsDefinition.of().recordField("record").primaryKey("id").build());

        // Index the json doc
        final IndexJsonResult result = service.postJson(INDEX, true, issueJson);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.count, Integer.valueOf(1));
        Assert.assertNotNull(result.fieldTypes);
        Assert.assertEquals(result.fieldTypes.size(), 213);
        assertThat(result.fieldTypes, allOf(
            hasEntry("assignee.html_url", Set.of(JsonNodeType.STRING)),
            hasEntry("assignee.site_admin", Set.of(JsonNodeType.BOOLEAN)),
            hasEntry("comments", Set.of(JsonNodeType.NUMBER))
        ));
    }

    @Test
    public void getJsonSampleTest() {
        final Map<String, Object> sample = service.getJsonSample(INDEX);
        assertThat(sample, notNullValue());
        assertThat(sample.keySet(), hasSize(1));
        assertThat(sample.keySet(), hasItem("id"));
        assertThat(sample.get("id").toString(), not(emptyOrNullString()));
    }

    @Test
    public void getDocumentTest() throws IOException {
        // Get the document by its id
        final Map<String, Object> doc = service.getDocument(INDEX, "1");
        assertThat(ObjectMappers.JSON.readTree(ObjectMappers.JSON.writeValueAsString(doc)),
            equalTo(issueJson));
    }


    @Test
    public void termQueryTest() throws IOException {
        // Get the document by one deep property
        final ResultDefinition.WithMap result = service.searchQuery(INDEX,
            QueryDefinition.of(new HasTerm("node_id", "MDU6SXNzdWUx"))
                .returnedField("*").queryDebug(true).build(), false);
        assertThat(result.query, equalTo("st€node_id:MDU6SXNzdWUx"));
        assertThat(result.getTotalHits(), equalTo(1L));
        final Map<String, Object> doc = result.getDocuments().get(0).getFields();
        assertThat(ObjectMappers.JSON.readTree(ObjectMappers.JSON.writeValueAsString(doc)),
            equalTo(issueJson));
    }

    @Test
    public void exactDoubleTest() throws IOException {
        // Get the document by one root property
        final ResultDefinition.WithMap result = service.searchQuery(INDEX,
            QueryDefinition.of(new ExactDouble("number", 1347d))
                .returnedField("*").queryDebug(true).build(), false);
        assertThat(result.query, equalTo("pd€number:[1347.0 TO 1347.0]"));
        assertThat(result.getTotalHits(), equalTo(1L));
        final Map<String, Object> doc = result.getDocuments().get(0).getFields();
        assertThat(ObjectMappers.JSON.readTree(ObjectMappers.JSON.writeValueAsString(doc)),
            equalTo(issueJson));
    }

    @Test
    public void deepPropertyTest() throws IOException {
        // Get the document by one deep property
        final ResultDefinition.WithMap result = service.searchQuery(INDEX,
            QueryDefinition.of(new HasTerm("user.login", "octocat"))
                .returnedField("*").queryDebug(true).build(), false);
        assertThat(result.query, equalTo("st€user.login:octocat"));
        assertThat(result.getTotalHits(), equalTo(1L));
        final Map<String, Object> doc = result.getDocuments().get(0).getFields();
        assertThat(ObjectMappers.JSON.readTree(ObjectMappers.JSON.writeValueAsString(doc)),
            equalTo(issueJson));
    }

    @Test
    public void fullTextSearchTest() throws IOException {
        // Get the document by one full text search
        final ResultDefinition.WithMap result = service.searchQuery(INDEX,
            QueryDefinition.of(SimpleQueryParser.of().setAnalyzer("ascii").addField("title").setQueryString("found").build())
                .returnedField("*").queryDebug(true).build(), false);
        assertThat(result.query, equalTo("tt€title:found"));
        assertThat(result.getTotalHits(), equalTo(1L));
        final Map<String, Object> doc = result.getDocuments().get(0).getFields();
        assertThat(ObjectMappers.JSON.readTree(ObjectMappers.JSON.writeValueAsString(doc)),
            equalTo(issueJson));
    }
}
