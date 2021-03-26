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
package com.qwazr.search.query;

import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.search.test.units.IndexRecord;
import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class BoostQueryTest extends AbstractIndexTest.WithIndexRecord.NoTaxonomy {

    @BeforeClass
    public static void setup() throws IOException, URISyntaxException {
        initIndexService();
        indexService.postDocument(new IndexRecord.NoTaxonomy("1").textField("Hello World"));
        indexService.postDocument(new IndexRecord.NoTaxonomy("2").textField("How are you ?"));
    }

    @Test
    public void test() {
        ResultDefinition.WithObject<? extends IndexRecord<?>> result1 = indexService.searchQuery(QueryDefinition.of(
            new HasTerm("textField", "hello")).build());
        Assert.assertNotNull(result1);
        Assert.assertEquals(1, result1.totalHits);

        ResultDefinition.WithObject<? extends IndexRecord<?>> result2 = indexService.searchQuery(QueryDefinition.of(
            new Boost(new HasTerm("textField", "hello"), 3f)).build());
        Assert.assertNotNull(result2);
        Assert.assertEquals(1, result2.totalHits);
        Assert.assertEquals(result1.getDocuments().get(0).getScore() * 3f, result2.getDocuments().get(0).getScore(), 0);
    }
}
