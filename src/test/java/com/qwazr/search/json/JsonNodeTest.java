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
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.utils.ObjectMappers;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class JsonNodeTest extends AbstractIndexTest {

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
    public void indexAndGetIssue() throws IOException {
        final JsonNode issueJson = getJson("issue.json");

        service.createUpdateSchema("schema");
        service.createUpdateIndex("schema", "index",
            IndexSettingsDefinition.of().recordField("record").primaryKey("id").build());

        service.postJson("schema", "index", issueJson);

        final Map<String, Object> document = service.getDocument("schema", "index", "1");

        final JsonNode docJson = ObjectMappers.JSON.readTree(ObjectMappers.JSON.writeValueAsString(document));
        assertThat(docJson, equalTo(issueJson));
    }
}
