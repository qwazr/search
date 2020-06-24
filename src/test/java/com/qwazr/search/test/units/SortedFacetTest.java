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
package com.qwazr.search.test.units;

import com.qwazr.search.index.FacetDefinition;
import com.qwazr.search.index.FacetDefinitionBuilder;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.query.BooleanQuery;
import com.qwazr.search.query.FacetPathQuery;
import com.qwazr.search.query.MatchAllDocs;
import com.qwazr.utils.RandomUtils;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SortedFacetTest extends AbstractIndexTest.WithIndexRecord.NoTaxonomy {

    public static List<IndexRecord.NoTaxonomy> documents;
    public static LinkedHashMap<String, AtomicInteger> facetTerms;
    public static List<String> facetValues;

    public static IndexRecord.NoTaxonomy getNewRandomDocumentsWithFacets(String id,
                                                                         LinkedHashMap<String, AtomicInteger> facetTerms, Collection<IndexRecord.NoTaxonomy> records)
        throws IOException, InterruptedException {
        final IndexRecord.NoTaxonomy record = new IndexRecord.NoTaxonomy(id).sortedSetDocValuesFacetField(
            Integer.toString(RandomUtils.nextInt(18, 22)));
        records.add(record);
        facetTerms.computeIfAbsent(record.sortedSetDocValuesFacetField, key -> new AtomicInteger(0)).incrementAndGet();
        return record;
    }

    @BeforeClass
    public static void setup() throws IOException, InterruptedException, URISyntaxException {
        initIndexService();
        documents = new ArrayList<>();
        facetTerms = new LinkedHashMap<>();
        for (int i = 0; i < RandomUtils.nextInt(50, 100); i++)
            getNewRandomDocumentsWithFacets(Integer.toString(i), facetTerms, documents);
        indexService.postDocuments(documents);
        facetValues = new ArrayList<>(facetTerms.keySet());
    }

    private Map<String, Number> checkResult(ResultDefinition<?> result) {
        Assert.assertNotNull(result);
        Assert.assertEquals(documents.size(), result.totalHits);
        Assert.assertNotNull(result.facets);
        final Map<String, Number> facet = result.getFacet("sortedSetDocValuesFacetField");
        Assert.assertNotNull(facet);
        Assert.assertTrue(result.isAnyFacet());
        return facet;
    }

    @Test
    public void allFacets() {
        ResultDefinition<?> result = indexService.searchQuery(QueryDefinition.of(MatchAllDocs.INSTANCE)
            .facet("sortedSetDocValuesFacetField", FacetDefinition.EMPTY)
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
        ResultDefinition result = indexService.searchQuery(QueryDefinition.of(MatchAllDocs.INSTANCE)
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
        ResultDefinition result = indexService.searchQuery(QueryDefinition.of(MatchAllDocs.INSTANCE)
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
        ResultDefinition<?> result = indexService.searchQuery(QueryDefinition.of(MatchAllDocs.INSTANCE)
            .facet("sortedSetDocValuesFacetField", FacetDefinition.of().specificValues(facetValue).build())
            .build());
        Map<String, Number> facets = checkFacets(checkResult(result));
        Assert.assertTrue(facets.containsKey(facetValue));
        Assert.assertEquals(1, facets.size());
    }

    private void checkSort(FacetDefinitionBuilder builder, final BiFunction<String, String, Boolean> labelChecker,
                           final BiFunction<Number, Number, Boolean> countChecker) {
        ResultDefinition<?> result = indexService.searchQuery(QueryDefinition.of(MatchAllDocs.INSTANCE)
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
        String facetName = RandomUtils.alphanumeric(5);
        String facetTerm1 = getRandomFacetValue();
        String facetTerm2 = getRandomFacetValue();
        int expected = facetTerm1.equals(facetTerm2) ?
            facetTerms.get(facetTerm1).get() :
            facetTerms.get(facetTerm1).get() + facetTerms.get(facetTerm2).get();
        final ResultDefinition<?> result = indexService.searchQuery(QueryDefinition.of(MatchAllDocs.INSTANCE)
            .facet("sortedSetDocValuesFacetField", FacetDefinition.of()
                .query(facetName, BooleanQuery.of()
                    .addClause(BooleanQuery.Occur.should,
                        FacetPathQuery.of("sortedSetDocValuesFacetField").path(facetTerm1).build())
                    .addClause(BooleanQuery.Occur.should,
                        FacetPathQuery.of("sortedSetDocValuesFacetField").path(facetTerm2).build())
                    .build())
                .build())
            .build());
        final Map<String, Number> facetResult = checkResult(result);
        Assert.assertEquals(1, facetResult.size());
        Assert.assertEquals(expected, facetResult.get(facetName).intValue());
    }
}
