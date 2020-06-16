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
package com.qwazr.search.collector;

import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.search.test.units.IndexRecord;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.TimeLimitingCollector;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;

public class TimeLimitingCollectorTest extends AbstractIndexTest.WithIndexRecord.NoTaxonomy {

    @BeforeClass
    public static void setup() throws IOException, URISyntaxException {
        initIndexService(true);
        Collection<IndexRecord.NoTaxonomy> indexRecords = new ArrayList<>();
        for (int i = 0; i < 100; i++)
            indexRecords.add(new IndexRecord.NoTaxonomy(Integer.toString(i)).intPoint((i)));
        indexService.postDocuments(indexRecords);
    }

    @AfterClass
    public static void cleanup() {
        TimeLimitingCollector.getGlobalTimerThread().stopTimer();
    }

    @Test
    public void mixingClassicAndLuceneCollectorTest() {
        QueryDefinition queryDef = QueryDefinition.of(new MatchAllDocsQuery())
            .collector("timeLimiter", TimeLimiterCollector.Lucene.class, 1000L)
            .collector("slowDown", SlowDownCollector.Classic.class, 100)
            .build();
        ResultDefinition.WithObject<? extends IndexRecord> results = indexService.searchQuery(queryDef);
        Assert.assertNotNull(results);
        assertThat(results.totalHits, lessThan(20L));
        assertThat(results.getCollector("timeLimiter"), nullValue());
        assertThat(results.getCollector("slowDown"), equalTo(100));
    }

    @Test
    public void concurrentCollectorTest() {
        QueryDefinition queryDef = QueryDefinition.of(new MatchAllDocsQuery())
            .collector("timeLimiter", TimeLimiterCollector.class, 1000L)
            .collector("slowDown", SlowDownCollector.Concurrent.class, 100)
            .build();
        ResultDefinition.WithObject<? extends IndexRecord> results = indexService.searchQuery(queryDef);
        Assert.assertNotNull(results);
        assertThat(results.totalHits, lessThan(20L));
        assertThat(results.getCollector("timeLimiter"), equalTo(Boolean.FALSE));
        assertThat(results.getCollector("slowDown"), equalTo(100));
    }

    @Test
    public void mixingParallelAndLuceneCollectorTest() {
        QueryDefinition queryDef = QueryDefinition.of(new MatchAllDocsQuery())
            .collector("timeLimiter", TimeLimiterCollector.class, 1000L)
            .collector("slowDown", SlowDownCollector.Lucene.class, 100)
            .build();
        ResultDefinition.WithObject<? extends IndexRecord> results = indexService.searchQuery(queryDef);
        Assert.assertNotNull(results);
        assertThat(results.totalHits, lessThan(20L));
        assertThat(results.getCollector("timeLimiter"), equalTo(Boolean.FALSE));
        assertThat(results.getCollector("slowDown"), nullValue());
    }

    @Test
    public void mixingClassicAndParallelCollectorTest() {
        QueryDefinition queryDef = QueryDefinition.of(new MatchAllDocsQuery())
            .collector("timeLimiter", TimeLimiterCollector.class, 1000L)
            .collector("slowDown", SlowDownCollector.Classic.class, 100)
            .build();
        ResultDefinition.WithObject<? extends IndexRecord> results = indexService.searchQuery(queryDef);
        Assert.assertNotNull(results);
        assertThat(results.totalHits, lessThan(20L));
        assertThat(results.getCollector("timeLimiter"), equalTo(Boolean.FALSE));
        assertThat(results.getCollector("slowDown"), equalTo(100));
    }
}
