/**
 * Copyright 2017 Emmanuel Keller / QWAZR
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
package com.qwazr.search.test.queries;

import com.qwazr.search.index.FacetDefinition;
import com.qwazr.search.index.QueryBuilder;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.query.MatchAllDocsQuery;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

public class PagingTest extends AbstractIndexTest {

	private static LinkedHashMap<String, IndexRecord> documents;

	@BeforeClass
	public static void setup() throws IOException, InterruptedException {
		documents = new LinkedHashMap<>();
		for (int i = 0; i < RandomUtils.nextInt(201, 299); i++) {
			final IndexRecord record =
					new IndexRecord(Integer.toString(i)).sortedDocValues(RandomStringUtils.randomAlphanumeric(5))
							.facetField(Integer.toString(RandomUtils.nextInt(1, 3)));
			documents.put(record.id, record);
			indexService.postDocument(record);
		}
	}

	private void checkPaging(final QueryBuilder builder) {

		builder.start(0).rows(0);
		ResultDefinition.WithObject<IndexRecord> result = indexService.searchQuery(builder.build());
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.total_hits);
		final int totalHits = result.getTotalHits().intValue();

		Assert.assertTrue(totalHits > 0);
		final int rows = RandomUtils.nextInt(8, 20);
		builder.rows(rows);
		final Set<String> idSet = new HashSet<>();
		for (int i = 0; i < totalHits; i += rows) {
			builder.start(i);
			result = indexService.searchQuery(builder.build());
			result.getDocuments().forEach(doc -> idSet.add(doc.record.id));
			if (i + rows > totalHits)
				Assert.assertEquals(totalHits - i, result.getDocuments().size());
			else
				Assert.assertEquals(rows, result.getDocuments().size());
		}
		Assert.assertEquals(totalHits, idSet.size());
	}

	@Test
	public void pagingWithoutSort() throws URISyntaxException {
		checkPaging(QueryDefinition.of(new MatchAllDocsQuery()).returnedField("*"));
	}

	@Test
	public void pagingSort() throws URISyntaxException {
		checkPaging(QueryDefinition.of(new MatchAllDocsQuery())
				.returnedField("*")
				.sort("sortedDocValues", QueryDefinition.SortEnum.ascending));
	}

	@Test
	public void pagingFacetSort() throws URISyntaxException {
		checkPaging(QueryDefinition.of(new MatchAllDocsQuery())
				.returnedField("*")
				.facet("facetField", new FacetDefinition(10, null))
				.sort("sortedDocValues", QueryDefinition.SortEnum.ascending));
	}

	@Test
	public void pagingFacetWithoutSort() throws URISyntaxException {
		checkPaging(QueryDefinition.of(new MatchAllDocsQuery()).returnedField("*"));
	}
}
