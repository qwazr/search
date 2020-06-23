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

import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.search.test.units.IndexRecord;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class MoreLikeThisQueryTest extends AbstractIndexTest.WithIndexRecord.NoTaxonomy {

    @BeforeClass
    public static void setup() throws IOException, URISyntaxException {
        initIndexService();
        indexService.postDocument(new IndexRecord.NoTaxonomy("1").mlt("Hello World"));
        indexService.postDocument(new IndexRecord.NoTaxonomy("2").mlt("Hello world again"));
        indexService.postDocument(new IndexRecord.NoTaxonomy("3").mlt("absolutely nothing to match"));
    }

    @Test
    public void mltTest() {
        ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result;
        result = indexService.searchQuery(
            QueryDefinition.of(MoreLikeThisQuery.of("hello again", "mlt").minDocFreq(1).minTermFreq(1).build())
                .build());
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.totalHits);

        //TODO MoreLikeThis with ID
        /*result = indexService.searchQuery(QueryDefinition.of(MoreLikeThisQuery.of(result.getDocuments().get(0).getRecord().id)
            .minDocFreq(1)
            .minTermFreq(1)
            .fieldnames("mlt")
            .isBoost(true)
            .build()).build());
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.totalHits);*/
    }
}
