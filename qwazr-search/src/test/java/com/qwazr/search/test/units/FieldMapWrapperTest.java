/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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
package com.qwazr.search.test.units;

import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.query.TermQuery;
import com.qwazr.utils.RandomUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class FieldMapWrapperTest extends AbstractIndexTest.WithIndexRecord.WithTaxonomy {

    private static List<IndexRecord.WithTaxonomy> documents;

    @BeforeClass
    public static void setup() throws IOException, InterruptedException, URISyntaxException {
        initIndexService();
        documents = new ArrayList<>();
        for (int i = 0; i < RandomUtils.nextInt(1, 10); i++)
            documents.add(new IndexRecord.WithTaxonomy(RandomUtils.alphanumeric(RandomUtils.nextInt(2, 5))).intDocValue(
                    RandomUtils.nextInt(2, 5)));
        indexService.postDocuments(documents);
    }

    @Test
    public void registerWrong() throws ReflectiveOperationException {
        try {
            indexService.registerClass(IndexPartialDummyRecord.class);
            Assert.fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("nonExistingField"));
        }
    }

    @Test
    public void getDocument() throws ReflectiveOperationException, IOException {
        IndexRecord indexRecord = documents.get(RandomUtils.nextInt(0, documents.size()));
        IndexPartialRecord record = indexService.getDocument(indexRecord.id, IndexPartialRecord.class);
        Assert.assertNotNull(record);
        Assert.assertEquals(indexRecord.id, record.id);
        Assert.assertEquals(indexRecord.intDocValue, record.intDocValue);
    }

    @Test
    public void getDocuments() throws ReflectiveOperationException, IOException {
        List<IndexPartialRecord> records = indexService.getDocuments(0, documents.size(), IndexPartialRecord.class);
        Assert.assertNotNull(records);
        Assert.assertEquals(records.size(), documents.size());
        int i = 0;
        for (IndexRecord document : documents) {
            Assert.assertEquals(document.id, records.get(i).id);
            Assert.assertEquals(document.intDocValue, records.get(i).intDocValue);
            i++;
        }
    }

    @Test
    public void searchQuery() {
        IndexRecord indexRecord = documents.get(RandomUtils.nextInt(0, documents.size()));
        ResultDefinition.WithObject<IndexPartialRecord> results = indexService.searchQuery(
                QueryDefinition.of(new TermQuery(FieldDefinition.ID_FIELD, indexRecord.id)).returnedField("*").build(),
                IndexPartialRecord.class);
        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.totalHits);
        IndexPartialRecord record = results.getDocuments().get(0).record;
        Assert.assertEquals(indexRecord.id, record.id);
        Assert.assertEquals(indexRecord.intDocValue, record.intDocValue);
    }

}
