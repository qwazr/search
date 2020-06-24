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
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.query.ExactDouble;
import com.qwazr.search.query.HasTerm;
import com.qwazr.search.query.TermQuery;
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.utils.ObjectMappers;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.BeforeClass;
import org.junit.Test;

public class JsonNodeTest extends AbstractIndexTest {

    private final static String INDEX = "jsonIndex";

    static IndexServiceInterface service;

    private static JsonNode getJson(final String resourceName) throws IOException {
        try (final InputStream is = JsonNodeTest.class.getResourceAsStream(resourceName)) {
            return ObjectMappers.JSON.readTree(is);
        }
    }

    @BeforeClass
    public static void setup() {
        service = initIndexManager(true).getService();
    }

    @Test
    public void getJsonSampleTest() {
        final Map<String, Object> sample = service.getJsonSample(INDEX);
        assertThat(sample, notNullValue());
        assertThat(sample.keySet(), hasSize(1));
        assertThat(sample.keySet(), hasItem("id"));
        assertThat(sample.get("id").toString(), not(isEmptyOrNullString()));
    }

    @Test
    public void indexAndGetIssue() throws IOException {
        final JsonNode issueJson = getJson("issue.json");

        // Create the schema and the index
        service.createUpdateIndex(INDEX,
            IndexSettingsDefinition.of().recordField("record").primaryKey("id").build());

        // Index the json doc
        service.postJson(INDEX, issueJson);

        // Get the document by its id
        {
            final Map<String, Object> doc = service.getDocument(INDEX, "1");
            assertThat(ObjectMappers.JSON.readTree(ObjectMappers.JSON.writeValueAsString(doc)),
                equalTo(issueJson));
        }

        // Get the document by one deep property
        {
            final ResultDefinition.WithMap result = service.searchQuery(INDEX,
                QueryDefinition.of(new TermQuery("node_id", "MDU6SXNzdWUx"))
                    .returnedField("*").queryDebug(true).build(), false);
            final Map<String, Object> doc = result.getDocuments().get(0).getFields();
            assertThat(result.query, equalTo("st€node_id:MDU6SXNzdWUx"));
            assertThat(ObjectMappers.JSON.readTree(ObjectMappers.JSON.writeValueAsString(doc)),
                equalTo(issueJson));
        }

        // Get the document by one root property
        {
            final ResultDefinition.WithMap result = service.searchQuery(INDEX,
                QueryDefinition.of(new ExactDouble("number", 1347d))
                    .returnedField("*").queryDebug(true).build(), false);
            assertThat(result.query, equalTo("pd€number:[1347.0 TO 1347.0]"));
            final Map<String, Object> doc = result.getDocuments().get(0).getFields();
            assertThat(ObjectMappers.JSON.readTree(ObjectMappers.JSON.writeValueAsString(doc)),
                equalTo(issueJson));
        }

        // Get the document by one deep property
        {
            final ResultDefinition.WithMap result = service.searchQuery(INDEX,
                QueryDefinition.of(new HasTerm("user.login", "octocat"))
                    .returnedField("*").queryDebug(true).build(), false);
            final Map<String, Object> doc = result.getDocuments().get(0).getFields();
            assertThat(result.query, equalTo("st€user.login:octocat"));
            assertThat(ObjectMappers.JSON.readTree(ObjectMappers.JSON.writeValueAsString(doc)),
                equalTo(issueJson));
        }
    }
}
