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

import com.qwazr.search.index.FacetDefinition;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.query.MatchAllDocsQuery;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SortedFacetTest extends AbstractIndexTest {

	public static List<IndexRecord> documents;
	public static Map<String, AtomicInteger> facetTerms;

	@BeforeClass
	public static void setup() throws IOException, InterruptedException, URISyntaxException {
		initIndexService();
		documents = new ArrayList<>();
		facetTerms = new LinkedHashMap<>();
		for (int i = 0; i < RandomUtils.nextInt(50, 100); i++) {
			final IndexRecord record = new IndexRecord(Integer.toString(i)).sortedSetDocValuesFacetField(
					Integer.toString(RandomUtils.nextInt(18, 22)));
			documents.add(record);
			indexService.postDocument(record);
			facetTerms.computeIfAbsent(record.sortedSetDocValuesFacetField, key -> new AtomicInteger(0))
					.incrementAndGet();
		}
	}

	private Map<String, Number> checkResult(ResultDefinition result) {
		Assert.assertNotNull(result);
		Assert.assertEquals(documents.size(), result.total_hits.intValue());
		Assert.assertNotNull(result.facets);
		final Map<String, Number> facet = result.getFacet("sortedSetDocValuesFacetField");
		Assert.assertNotNull(facet);
		return facet;
	}

	@Test
	public void allFacets() {
		ResultDefinition result = indexService.searchQuery(QueryDefinition.of(new MatchAllDocsQuery())
				.facet("sortedSetDocValuesFacetField", new FacetDefinition())
				.build());
		final Map<String, Number> facet = checkResult(result);
		Assert.assertEquals(facetTerms.size(), facet.size());
		facetTerms.forEach((value, count) -> Assert.assertEquals(count.get(), facet.get(value)));
	}

	private void checkFacets(final Map<String, Number> facet) {
		Assert.assertFalse("Facets are empty", facet.isEmpty());
		facet.forEach((value, count) -> Assert.assertEquals(count, facetTerms.get(value).intValue()));
	}

	@Test
	public void limitFacet() {
		ResultDefinition result = indexService.searchQuery(QueryDefinition.of(new MatchAllDocsQuery())
				.facet("sortedSetDocValuesFacetField", FacetDefinition.of(2).build())
				.build());
		checkResult(result);
		final Map<String, Number> facet = checkResult(result);
		Assert.assertEquals(2, facet.size());
		checkFacets(facet);
	}

	@Test
	public void prefixFacet() {
		final String facetPrefix = facetTerms.keySet().iterator().next().substring(0, 1);
		ResultDefinition result = indexService.searchQuery(QueryDefinition.of(new MatchAllDocsQuery())
				.facet("sortedSetDocValuesFacetField", FacetDefinition.of().prefix(facetPrefix).build())
				.build());
		checkResult(result);
		final Map<String, Number> facet = checkResult(result);
		checkFacets(facet);
		facet.forEach((value, count) -> Assert.assertTrue("Wrong prefix: " + value + " - Expected: " + facetPrefix,
				value.startsWith(facetPrefix)));
	}
}
