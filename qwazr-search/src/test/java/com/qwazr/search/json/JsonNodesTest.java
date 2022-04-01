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
import static com.qwazr.search.json.JsonNodeTest.getJson;
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.utils.ObjectMappers;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class JsonNodesTest extends AbstractIndexTest {

    private final static String INDEX = "jsonsIndex";

    static IndexServiceInterface service;

    static final JsonNode issuesJson = getJson("issues.json");

    @BeforeClass
    public static void setup() {
        service = initIndexManager(true).getService();

        // Create the schema and the index

        service.createUpdateIndex(INDEX,
            IndexSettingsDefinition.of().recordField("record").primaryKey("id").build());

        // Index the json doc
        final IndexJsonResult result = service.postJson(INDEX, true, issuesJson);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.count, Integer.valueOf(1));
        Assert.assertNotNull(result.fieldTypes);
        Assert.assertEquals(115, result.fieldTypes.size());
        assertThat(result.fieldTypes, allOf(
            hasEntry("assignee.html_url", Set.of(JsonNodeType.STRING)),
            hasEntry("assignee.site_admin", Set.of(JsonNodeType.BOOLEAN)),
            hasEntry("comments", Set.of(JsonNodeType.NUMBER))
        ));
    }

    @Test
    public void getDocumentTest() throws IOException {
        // Get the document by its id
        final Map<String, Object> doc1 = service.getDocument(INDEX, "1");
        assertThat(ObjectMappers.JSON.readTree(ObjectMappers.JSON.writeValueAsString(doc1)),
            equalTo(issuesJson.get(0)));

    }

}
