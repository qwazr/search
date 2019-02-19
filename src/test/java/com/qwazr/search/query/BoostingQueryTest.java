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

import com.qwazr.search.index.QueryContext;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.search.test.units.IndexRecord;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class BoostingQueryTest extends AbstractIndexTest.WithIndexRecord.NoTaxonomy {

    @BeforeClass
    public static void setup() throws IOException, InterruptedException, URISyntaxException {
        initIndexService();
        indexService.postDocument(new IndexRecord.NoTaxonomy("1").textField("Hello World").stringField("de"));
    }

    private AbstractQuery getMatchQuery() {
        return new TermQuery("stringField", "de");
    }

    private AbstractQuery getContextQuery() {
        return new TermQuery("textField", "hello");
    }

    private BoostingQuery getBoostingQuery(float boost) {
        return new BoostingQuery(getMatchQuery(), getContextQuery(), boost);
    }

    @Test
    public void testEquality() {
        Assert.assertEquals(getBoostingQuery(1.0f), getBoostingQuery(1.0f));
        Assert.assertNotEquals(getBoostingQuery(1.0f), getBoostingQuery(2.0f));
    }

    @Test
    public void testBoost() {
        final ResultDefinition.WithObject<? extends IndexRecord> contextResult =
                indexService.searchQuery(QueryDefinition.of(getContextQuery()).build());
        Assert.assertNotNull(contextResult);
        Assert.assertEquals(1L, contextResult.totalHits);

        final ResultDefinition.WithObject<? extends IndexRecord> boosted1Result =
                indexService.searchQuery(QueryDefinition.of(getBoostingQuery(1)).build());
        Assert.assertNotNull(boosted1Result);
        Assert.assertEquals(1L, boosted1Result.totalHits);

        final ResultDefinition.WithObject<? extends IndexRecord> boosted2Result =
                indexService.searchQuery(QueryDefinition.of(getBoostingQuery(2)).build());
        Assert.assertNotNull(boosted2Result);
        Assert.assertEquals(1L, boosted2Result.totalHits);

        Assert.assertEquals(boosted1Result.maxScore * 2, boosted2Result.maxScore, 0);
        Assert.assertEquals(boosted2Result.getDocuments().get(0).score, boosted2Result.maxScore, 0);
    }

    @Test
    public void luceneQuery() throws QueryNodeException, ReflectiveOperationException, ParseException, IOException {
        Query luceneQuery = getBoostingQuery(2).getQuery(QueryContext.DEFAULT);
        Assert.assertEquals("stringField:de/textField:hello", luceneQuery.toString());
    }

}
