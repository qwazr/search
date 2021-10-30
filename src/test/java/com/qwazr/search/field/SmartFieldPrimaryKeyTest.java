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
package com.qwazr.search.field;

import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.PostDefinition;
import com.qwazr.search.test.units.AbstractIndexTest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import org.junit.BeforeClass;
import org.junit.Test;

public class SmartFieldPrimaryKeyTest extends AbstractIndexTest {

    private static IndexServiceInterface indexService;

    private static final String indexName = "SmartFieldPrimaryKeyTest";

    @BeforeClass
    public static void setup() throws URISyntaxException {
        indexService = initIndexManager().getService();
        final IndexSettingsDefinition indexSettings = IndexSettingsDefinition
            .of()
            .primaryKey("id")
            .build();
        indexService.createUpdateIndex(indexName, indexSettings);
        indexService.setField(indexName, "id",
            CustomFieldDefinition.of().template(FieldDefinition.Template.StringField).stored(true).build());
        indexService.setField(indexName, "title",
            SmartFieldDefinition.of().type(SmartFieldDefinition.Type.TEXT).stored(true).index(true).analyzer("english").build());
    }

    private void postDocument(String id, String title) {
        assertThat(indexService.postMappedDocument(indexName,
                PostDefinition.Document.of(Map.of("id", id, "title", title), null)),
            equalTo(1));
    }

    @Test
    public void postDocumentTwiceShouldReplace() throws IOException {
        SmartFieldRecord sample = SmartFieldRecord.random();
        postDocument("doc1", "title1");
        assertThat(indexService.getIndex(indexName).numDocs, equalTo(1L));
        postDocument("doc1", "title1b");
        assertThat(indexService.getIndex(indexName).numDocs, equalTo(1L));
        postDocument("doc2", "title2");
        assertThat(indexService.getIndex(indexName).numDocs, equalTo(2L));
        postDocument("doc2", "title2b");
        assertThat(indexService.getIndex(indexName).numDocs, equalTo(2L));
    }
}
