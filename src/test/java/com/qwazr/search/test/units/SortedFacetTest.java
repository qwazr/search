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
import com.qwazr.search.query.BooleanQuery;
import com.qwazr.search.query.FacetPathQuery;
import com.qwazr.search.query.MatchAllDocsQuery;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

public class SortedFacetTest extends AbstractIndexTest {

	public static List<IndexRecord> documents;
	public static LinkedHashMap<String, AtomicInteger> facetTerms;
	public static List<String> facetValues;

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
		facetValues = new ArrayList<>(facetTerms.keySet());
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

	private Map<String, Number> checkFacets(final Map<String, Number> facet) {
		Assert.assertFalse("Facets are empty", facet.isEmpty());
		facet.forEach((value, count) -> Assert.assertEquals(count, facetTerms.get(value).intValue()));
		return facet;
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

	public String getRandomFacetValue() {
		return facetValues.get(RandomUtils.nextInt(0, facetValues.size()));
	}

	@Test
	public void prefixFacet() {
		final String facetPrefix = getRandomFacetValue().substring(0, 1);
		ResultDefinition result = indexService.searchQuery(QueryDefinition.of(new MatchAllDocsQuery())
				.facet("sortedSetDocValuesFacetField", FacetDefinition.of().prefix(facetPrefix).build())
				.build());
		final Map<String, Number> facet = checkResult(result);
		checkFacets(facet);
		facet.forEach((value, count) -> Assert.assertTrue("Wrong prefix: " + value + " - Expected: " + facetPrefix,
				value.startsWith(facetPrefix)));
	}

	@Test
	public void specificValues() {
		final String facetValue = getRandomFacetValue();
		ResultDefinition result = indexService.searchQuery(QueryDefinition.of(new MatchAllDocsQuery())
				.facet("sortedSetDocValuesFacetField", FacetDefinition.of().specificValues(facetValue).build())
				.build());
		Map<String, Number> facets = checkFacets(checkResult(result));
		Assert.assertTrue(facets.containsKey(facetValue));
		Assert.assertEquals(1, facets.size());
	}

	private void checkSort(FacetDefinition.Builder builder, final BiFunction<String, String, Boolean> labelChecker,
			final BiFunction<Number, Number, Boolean> countChecker) {
		ResultDefinition result = indexService.searchQuery(QueryDefinition.of(new MatchAllDocsQuery())
				.facet("sortedSetDocValuesFacetField", builder.build())
				.build());
		final Map<String, Number> facet = checkResult(result);
		Assert.assertEquals(facetTerms.size(), facet.size());
		AtomicReference<String> previousLabel = new AtomicReference<>();
		AtomicReference<Number> previousCount = new AtomicReference<>();
		facet.forEach((label, count) -> {
			Assert.assertEquals(count.intValue(), facetTerms.get(label).intValue());
			if (previousLabel.get() != null)
				Assert.assertTrue(labelChecker.apply(previousLabel.get(), label));
			if (previousCount.get() != null)
				Assert.assertTrue(countChecker.apply(previousCount.get(), count));
			previousLabel.set(label);
			previousCount.set(count);
		});
	}

	@Test
	public void sortValueAscending() {
		checkSort(FacetDefinition.of(100).sort(FacetDefinition.Sort.value_ascending), (s1, s2) -> true,
				(n1, n2) -> n1.longValue() <= n2.longValue());
	}

	@Test
	public void sortValueDescending() {
		checkSort(FacetDefinition.of(100).sort(FacetDefinition.Sort.value_descending), (s1, s2) -> true,
				(n1, n2) -> n1.longValue() >= n2.longValue());
	}

	@Test
	public void defaultSortIsValueDescending() {
		checkSort(FacetDefinition.of(100), (s1, s2) -> true, (n1, n2) -> n1.longValue() >= n2.longValue());
	}

	@Test
	public void sortLabelAscending() {
		checkSort(FacetDefinition.of(100).sort(FacetDefinition.Sort.label_ascending), (s1, s2) -> s1.compareTo(s2) <= 0,
				(n1, n2) -> true);
	}

	@Test
	public void sortLabelDescending() {
		checkSort(FacetDefinition.of(100).sort(FacetDefinition.Sort.label_descending),
				(s1, s2) -> s1.compareTo(s2) >= 0, (n1, n2) -> true);
	}

	@Test
	public void queryCount() {
		String facetName = RandomStringUtils.randomAlphabetic(5);
		String facetTerm1 = getRandomFacetValue();
		String facetTerm2 = getRandomFacetValue();
		int expected = facetTerm1.equals(facetTerm2) ?
				facetTerms.get(facetTerm1).get() :
				facetTerms.get(facetTerm1).get() + facetTerms.get(facetTerm2).get();
		ResultDefinition result = indexService.searchQuery(QueryDefinition.of(new MatchAllDocsQuery())
				.facet("sortedSetDocValuesFacetField", FacetDefinition.of()
						.query(facetName, BooleanQuery.of()
								.addClause(BooleanQuery.Occur.should,
										new FacetPathQuery("sortedSetDocValuesFacetField", facetTerm1))
								.addClause(BooleanQuery.Occur.should,
										new FacetPathQuery("sortedSetDocValuesFacetField", facetTerm2))
								.build())
						.build())
				.build());
		final Map<String, Number> facetResult = checkResult(result);
		Assert.assertEquals(1, facetResult.size());
		Assert.assertEquals(expected, facetResult.get(facetName).intValue());
	}
}
