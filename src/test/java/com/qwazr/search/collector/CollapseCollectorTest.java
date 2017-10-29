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
package com.qwazr.search.collector;

import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.index.ResultDocumentAbstract;
import com.qwazr.search.query.BooleanQuery;
import com.qwazr.search.query.TermQuery;
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.search.test.units.IndexRecord;
import com.qwazr.utils.RandomUtils;
import org.apache.lucene.util.BytesRef;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

public class CollapseCollectorTest extends AbstractIndexTest.WithIndexRecord.NoTaxonomy {

	@BeforeClass
	public static void setup() throws IOException, InterruptedException, URISyntaxException {
		initIndexService();
		Collection<IndexRecord.NoTaxonomy> indexRecords = new ArrayList<>();
		int k = 0;
		for (int i = 0; i < RandomUtils.nextInt(8, 12); i++) {
			indexRecords.clear();
			for (int j = 0; j < RandomUtils.nextInt(5000, 15000); j++)
				indexRecords.add(getRandomRecord(i, k++));
			indexService.postDocuments(indexRecords);
		}
	}

	static IndexRecord.NoTaxonomy getRandomRecord(int i, int k) {
		String value = "sdv" + i;// (char) (65 + RandomUtils.nextInt(0, 10));
		return new IndexRecord.NoTaxonomy(Integer.toString(k)).sortedDocValue(value)
				.textField("text" + i)
				.stringField(RandomUtils.alphanumeric(25))
				.intPoint(RandomUtils.nextInt(0, 100000))
				.storedField(RandomUtils.alphanumeric(50));
	}

	@Test
	public void test() {
		QueryDefinition queryDef = QueryDefinition.of(BooleanQuery.of()
				.addClause(BooleanQuery.Occur.should, new TermQuery("textField", "text1"))
				.addClause(BooleanQuery.Occur.should, new TermQuery("textField", "text2"))
				.addClause(BooleanQuery.Occur.should, new TermQuery("textField", "text3"))
				.addClause(BooleanQuery.Occur.should, new TermQuery("textField", "text4"))
				.addClause(BooleanQuery.Occur.should, new TermQuery("textField", "text5"))
				.addClause(BooleanQuery.Occur.should, new TermQuery("textField", "text6"))
				.build()).collector("collapse", CollapseCollector.class, "sortedDocValue", 5).build();
		ResultDefinition.WithObject<IndexRecord.NoTaxonomy> firstPassResults = indexService.searchQuery(queryDef);
		Assert.assertNotNull(firstPassResults);
		CollapseCollector.Query collapseQuery = firstPassResults.getCollector("collapse");
		Assert.assertNotNull(collapseQuery);
		queryDef = QueryDefinition.of(collapseQuery).queryDebug(true).build();
		Assert.assertTrue(
				collapseQuery.getCollapsed() > 0 && collapseQuery.getCollapsed() < firstPassResults.getTotalHits());
		ResultDefinition.WithObject<IndexRecord.NoTaxonomy> secondPassResults = indexService.searchQuery(queryDef);
		Assert.assertNotNull(secondPassResults);
		Assert.assertEquals(Long.valueOf(5), secondPassResults.total_hits);
		for (ResultDocumentAbstract result : secondPassResults.getDocuments())
			Assert.assertNotEquals(-1, collapseQuery.getCollapsed(result.getDoc()));
	}

	private void checkGroupLeader(final CollapseCollector.GroupQueue queue, String value, int doc, float score,
			int collapsedCount) {
		final CollapseCollector.GroupLeader leader = queue.groupLeaders.get(new BytesRef(value));
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
					(bytesRef, score, collapsed) -> new CollapseCollector.GroupLeader(null, bytesRef, doc, score,
							collapsed));
		}
		Assert.assertEquals(3, queue.groupLeaders.size());
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
					(bytesRef, score, collapsed) -> new CollapseCollector.GroupLeader(null, bytesRef, doc, score,
							collapsed));
		}
		Assert.assertEquals(1, queue.groupLeaders.size());
		checkGroupLeader(queue, "test", 9, 9f, 449);
	}

	@Test
	public void groupQueueMixAscendingValues() {
		final CollapseCollector.GroupQueue queue = new CollapseCollector.GroupQueue(3);
		for (int i = 1; i < 10; i++) {
			final int doc = i;
			queue.offer(new BytesRef("test" + i % 2), i, i * 10,
					(bytesRef, score, collapsed) -> new CollapseCollector.GroupLeader(null, bytesRef, doc, score,
							collapsed));
		}
		Assert.assertEquals(2, queue.groupLeaders.size());
		checkGroupLeader(queue, "test0", 8, 8f, 199);
		checkGroupLeader(queue, "test1", 9, 9f, 249);
	}

	@Test
	public void groupQueueMixDescendingValues() {
		final CollapseCollector.GroupQueue queue = new CollapseCollector.GroupQueue(3);
		for (int i = 9; i > 0; i--) {
			final int doc = i;
			queue.offer(new BytesRef("test" + i % 2), i, i * 10,
					(bytesRef, score, collapsed) -> new CollapseCollector.GroupLeader(null, bytesRef, doc, score,
							collapsed));
		}
		Assert.assertEquals(2, queue.groupLeaders.size());
		checkGroupLeader(queue, "test0", 8, 8f, 199);
		checkGroupLeader(queue, "test1", 9, 9f, 249);
	}
}
