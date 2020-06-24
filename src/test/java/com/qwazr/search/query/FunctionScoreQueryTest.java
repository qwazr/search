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
package com.qwazr.search.query;

import com.qwazr.search.function.DoubleValuesSource;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.search.test.units.IndexRecord;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class FunctionScoreQueryTest extends AbstractIndexTest.WithIndexRecord.NoTaxonomy {

    @BeforeClass
    public static void setup() throws IOException, InterruptedException, URISyntaxException {
        initIndexService();
        indexService.postDocument(new IndexRecord.NoTaxonomy("1").textField("Hello World")
                .floatDocValue(2.0F)
                .intDocValue(2)
                .doubleDocValue(2.0d)
                .longDocValue(2));
        indexService.postDocument(new IndexRecord.NoTaxonomy("2").textField("How are you ?")
                .floatDocValue(3.0F)
                .intDocValue(3)
                .doubleDocValue(3.0d)
                .longDocValue(3));
    }

    private void checkFieldSourceResult(ResultDefinition.WithObject<? extends IndexRecord<?>> result, float... values) {
        Assert.assertNotNull(result);
        Assert.assertEquals(values.length, result.totalHits);
        int i = 0;
        for (float value : values)
            Assert.assertEquals(value, result.getDocuments().get(i++).getScore(), 0);
    }

    @Test
    public void testFloat() {
        ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result = indexService.searchQuery(QueryDefinition.of(
                new FunctionScoreQuery(MatchAllDocs.INSTANCE,
                        new DoubleValuesSource.FloatField("floatDocValue")))
                .build());
        checkFieldSourceResult(result, 3.0F, 2.0F);
    }

    @Test
    public void testDouble() {
        ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result = indexService.searchQuery(QueryDefinition.of(
                new FunctionScoreQuery(MatchAllDocs.INSTANCE,
                        new DoubleValuesSource.DoubleField("doubleDocValue")))
                .build());
        checkFieldSourceResult(result, 3.0F, 2.0F);
    }

    @Test
    public void testLong() {
        ResultDefinition.WithObject<? extends IndexRecord.NoTaxonomy> result = indexService.searchQuery(QueryDefinition.of(
                new FunctionScoreQuery(MatchAllDocs.INSTANCE,
                        new DoubleValuesSource.LongField("longDocValue")))
                .build());
        checkFieldSourceResult(result, 3.0F, 2.0F);
    }

    @Test
    public void testInt() {
        ResultDefinition.WithObject<? extends IndexRecord.NoTaxonomy> result = indexService.searchQuery(QueryDefinition.of(
                new FunctionScoreQuery(MatchAllDocs.INSTANCE,
                        new DoubleValuesSource.IntField("intDocValue")))
                .build());
        checkFieldSourceResult(result, 3.0F, 2.0F);
    }

}
