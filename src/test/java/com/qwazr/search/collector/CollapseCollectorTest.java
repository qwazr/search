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
import com.qwazr.search.query.Bool;
import com.qwazr.search.query.HasTerm;
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.search.test.units.IndexRecord;
import com.qwazr.utils.RandomUtils;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.lucene.util.BytesRef;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class CollapseCollectorTest extends AbstractIndexTest.WithIndexRecord.NoTaxonomy {

    @BeforeClass
    public static void setup() throws IOException, URISyntaxException {
        initIndexService();
        Collection<IndexRecord.NoTaxonomy> indexRecords = new ArrayList<>();
        int k = 0;
        final int il = RandomUtils.nextInt(8, 12);
        for (int i = 0; i < il; i++) {
            indexRecords.clear();
            final int jl = RandomUtils.nextInt(5000, 15000);
            for (int j = 0; j < jl; j++)
                indexRecords.add(getRandomRecord(i, k++));
            indexService.postDocuments(indexRecords);
        }
    }

    static IndexRecord.NoTaxonomy getRandomRecord(int i, int k) {
        String value = "sdv" + i;
        return new IndexRecord.NoTaxonomy(Integer.toString(k)).sortedDocValue(value)
            .textField("text" + i)
            .stringField(RandomUtils.alphanumeric(25))
            .intPoint(RandomUtils.nextInt(0, 100000))
            .storedField(RandomUtils.alphanumeric(50));
    }

    @Test
    public void test() {
        final QueryDefinition queryDef1 = QueryDefinition.of(Bool.of()
            .addClause(Bool.Occur.should, new HasTerm("textField", "text1"))
            .addClause(Bool.Occur.should, new HasTerm("textField", "text2"))
            .addClause(Bool.Occur.should, new HasTerm("textField", "text3"))
            .addClause(Bool.Occur.should, new HasTerm("textField", "text4"))
            .addClause(Bool.Occur.should, new HasTerm("textField", "text5"))
            .addClause(Bool.Occur.should, new HasTerm("textField", "text6"))
            .build())
            .collector("collapse", CollapseCollector.class, "sortedDocValue", 5)
            .build();

        final ResultDefinition.WithObject<IndexRecord.NoTaxonomy> firstPassResults = indexService.searchQuery(queryDef1);
        Assert.assertNotNull(firstPassResults);
        final CollapseCollector.Query collapseQuery =
            firstPassResults.getCollector("collapse", CollapseCollector.Query.class);
        Assert.assertNotNull(collapseQuery);

        final QueryDefinition queryDef2 = QueryDefinition.of(collapseQuery).queryDebug(true).build();
        Assert.assertTrue(
            collapseQuery.getCollapsed() > 0 && collapseQuery.getCollapsed() < firstPassResults.getTotalHits());
        final ResultDefinition.WithObject<IndexRecord.NoTaxonomy> secondPassResults = indexService.searchQuery(queryDef2);
        Assert.assertNotNull(secondPassResults);

        Assert.assertEquals(collapseQuery.collapsedMap.size(), secondPassResults.totalHits);
        //TODO Restore test
        // for (final ResultDocumentAbstract result : secondPassResults.getDocuments())
        //   Assert.assertNotEquals(-1, collapseQuery.getCollapsed(result.getDoc()));
    }

    private void checkGroupLeader(final CollapseCollector.GroupQueue queue, String value, int doc, float score,
                                  int collapsedCount) {
        final CollapseCollector.GroupLeader leader = queue.getGroupLeaders().get(new BytesRef(value));
        Assert.assertNotNull(leader);
        Assert.assertEquals(doc, leader.doc);
        Assert.assertEquals(score, leader.score, 0);
        Assert.assertEquals(collapsedCount, leader.collapsedCount);
    }

    @Test
    public void groupQueueDifferentValues() {
        final CollapseCollector.GroupQueue queue = new CollapseCollector.GroupQueue(3);
        for (int i = 1; i < 10; i++) {
            final int doc = i;
            queue.offer(new BytesRef("test" + i), i, i * 10,
                (bytesRef, score, collapsed) -> new CollapseCollector.GroupLeader(0, 0, bytesRef, doc, score,
                    collapsed));
        }
        Assert.assertEquals(3, queue.getGroupLeaders().size());
        checkGroupLeader(queue, "test7", 7, 7f, 69);
        checkGroupLeader(queue, "test8", 8, 8f, 79);
        checkGroupLeader(queue, "test9", 9, 9f, 89);
    }

    @Test
    public void groupQueueSameValues() {
        final CollapseCollector.GroupQueue queue = new CollapseCollector.GroupQueue(3);
        for (int i = 1; i < 10; i++) {
            final int doc = i;
            queue.offer(new BytesRef("test"), i, i * 10,
                (bytesRef, score, collapsed) -> new CollapseCollector.GroupLeader(0, 0, bytesRef, doc, score,
                    collapsed));
        }
        Assert.assertEquals(1, queue.getGroupLeaders().size());
        checkGroupLeader(queue, "test", 9, 9f, 449);
    }

    @Test
    public void groupQueueMixAscendingValues() {
        final CollapseCollector.GroupQueue queue = new CollapseCollector.GroupQueue(3);
        for (int i = 1; i < 10; i++) {
            final int doc = i;
            queue.offer(new BytesRef("test" + i % 2), i, i * 10,
                (bytesRef, score, collapsed) -> new CollapseCollector.GroupLeader(0, 0, bytesRef, doc, score,
                    collapsed));
        }
        Assert.assertEquals(2, queue.getGroupLeaders().size());
        checkGroupLeader(queue, "test0", 8, 8f, 199);
        checkGroupLeader(queue, "test1", 9, 9f, 249);
    }

    @Test
    public void groupQueueMixDescendingValues() {
        final CollapseCollector.GroupQueue queue = new CollapseCollector.GroupQueue(3);
        for (int i = 9; i > 0; i--) {
            final int doc = i;
            queue.offer(new BytesRef("test" + i % 2), i, i * 10,
                (bytesRef, score, collapsed) -> new CollapseCollector.GroupLeader(0, 0, bytesRef, doc, score,
                    collapsed));
        }
        Assert.assertEquals(2, queue.getGroupLeaders().size());
        checkGroupLeader(queue, "test0", 8, 8f, 199);
        checkGroupLeader(queue, "test1", 9, 9f, 249);
    }
}
