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
			for (int j = 0; j < RandomUtils.nextInt(5000, 15000); j++) {
				String value = "" + (char) (26 + RandomUtils.nextInt(0, 10));
				indexRecords.add(new IndexRecord.NoTaxonomy(Integer.toString(k++)).sortedDocValue(value)
						.textField(Boolean.toString(RandomUtils.nextBoolean())));
			}
			indexService.postDocuments(indexRecords);
		}
	}

	@Test
	public void test() {
		QueryDefinition queryDef = QueryDefinition.of(new TermQuery("textField", "true"))
				.collector("collapse", CollapseCollector.class, "sortedDocValue", 5)
				.build();
		FilterCollector.Query filterQuery = indexService.searchQuery(queryDef).getCollector("collapse");
		Assert.assertNotNull(filterQuery);
		queryDef = QueryDefinition.of(filterQuery).queryDebug(true).build();
		ResultDefinition.WithObject<? extends IndexRecord> results = indexService.searchQuery(queryDef);
		Assert.assertNotNull(results);
		Assert.assertEquals(Long.valueOf(500), results.total_hits);
		Assert.assertEquals(Float.valueOf(1.0F), results.max_score);
		Assert.assertEquals("(f)", results.getQuery());
	}

	private void checkGroupLeader(final CollapseCollector.GroupQueue queue, String value, int doc, float score) {
		final CollapseCollector.GroupLeader leader = queue.groupLeaders.get(new BytesRef(value));
		Assert.assertNotNull(leader);
		Assert.assertEquals(doc, leader.doc);
		Assert.assertEquals(score, leader.score, 0);
	}

	@Test
	public void groupQueueDifferentValues() {
		final CollapseCollector.GroupQueue queue = new CollapseCollector.GroupQueue(3);
		for (int i = 1; i < 10; i++) {
			final int doc = i;
			queue.offer(new BytesRef("test" + i), i, score -> new CollapseCollector.GroupLeader(null, doc, score));
		}
		Assert.assertEquals(3, queue.groupLeaders.size());
		checkGroupLeader(queue, "test7", 7, 7f);
		checkGroupLeader(queue, "test8", 8, 8f);
		checkGroupLeader(queue, "test9", 9, 9f);
	}

	@Test
	public void groupQueueSameValues() {
		final CollapseCollector.GroupQueue queue = new CollapseCollector.GroupQueue(3);
		for (int i = 1; i < 10; i++) {
			final int doc = i;
			queue.offer(new BytesRef("test"), i, score -> new CollapseCollector.GroupLeader(null, doc, score));
		}
		Assert.assertEquals(1, queue.groupLeaders.size());
		checkGroupLeader(queue, "test", 9, 9f);
	}

	@Test
	public void groupQueueMixAscendingValues() {
		final CollapseCollector.GroupQueue queue = new CollapseCollector.GroupQueue(3);
		for (int i = 1; i < 10; i++) {
			final int doc = i;
			queue.offer(new BytesRef("test" + i % 2), i, score -> new CollapseCollector.GroupLeader(null, doc, score));
		}
		Assert.assertEquals(2, queue.groupLeaders.size());
		checkGroupLeader(queue, "test0", 8, 8f);
		checkGroupLeader(queue, "test1", 9, 9f);
	}

	@Test
	public void groupQueueMixDescendingValues() {
		final CollapseCollector.GroupQueue queue = new CollapseCollector.GroupQueue(3);
		for (int i = 9; i > 0; i--) {
			final int doc = i;
			queue.offer(new BytesRef("test" + i % 2), i, score -> new CollapseCollector.GroupLeader(null, doc, score));
		}
		Assert.assertEquals(2, queue.groupLeaders.size());
		checkGroupLeader(queue, "test0", 8, 8f);
		checkGroupLeader(queue, "test1", 9, 9f);
	}
}
