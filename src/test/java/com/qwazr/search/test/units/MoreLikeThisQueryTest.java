/**
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.test.units;

import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.query.MoreLikeThisQuery;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class MoreLikeThisQueryTest extends AbstractIndexTest {

	@BeforeClass
	public static void setup() throws IOException, InterruptedException, URISyntaxException {
		initIndexService();
		indexService.postDocument(new IndexRecord("1").mlt("Hello World"));
		indexService.postDocument(new IndexRecord("2").mlt("Hello world again"));
		indexService.postDocument(new IndexRecord("3").mlt("absolutely nothing to match"));
	}

	@Test
	public void mltTest() {
		ResultDefinition.WithObject<IndexRecord> result;
		result = indexService.searchQuery(
				QueryDefinition.of(MoreLikeThisQuery.of("hello again", "mlt").minDocFreq(1).minTermFreq(1).build())
						.build());
		Assert.assertNotNull(result);
		Assert.assertEquals(Long.valueOf(2), result.total_hits);

		result = indexService.searchQuery(QueryDefinition.of(MoreLikeThisQuery.of(result.getDocuments().get(0).getDoc())
				.minDocFreq(1)
				.minTermFreq(1)
				.fieldnames("mlt")
				.isBoost(true)
				.build()).build());
		Assert.assertNotNull(result);
		Assert.assertEquals(Long.valueOf(2), result.total_hits);
	}
}
