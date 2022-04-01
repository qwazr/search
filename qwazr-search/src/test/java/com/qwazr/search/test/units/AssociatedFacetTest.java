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
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.query.MatchAllDocs;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class AssociatedFacetTest extends AbstractIndexTest.WithIndexRecord.WithTaxonomy {

    @BeforeClass
    public static void setup() throws IOException, URISyntaxException {
        initIndexService();
        indexService.postDocument(new IndexRecord.WithTaxonomy("1").intAssociatedFacet(111, "int1"));
        indexService.postDocument(new IndexRecord.WithTaxonomy("2").intAssociatedFacet(222, "int2"));
        indexService.postDocument(new IndexRecord.WithTaxonomy("3").intAssociatedFacet(333, "int3"));
        indexService.postDocument(new IndexRecord.WithTaxonomy("4").floatAssociatedFacet(444f, "float4"));
        indexService.postDocument(new IndexRecord.WithTaxonomy("5").floatAssociatedFacet(555f, "float5"));
        indexService.postDocument(new IndexRecord.WithTaxonomy("6").floatAssociatedFacet(666f, "float6"));
    }

    private void checkIntFacets(ResultDefinition<?> result, String... expectedKeys) {
        Assert.assertNotNull(result.facets);
        Map<String, Number> facet = result.facets.get("intAssociatedFacet");
        Assert.assertNotNull(facet);
        for (String key : expectedKeys)
            Assert.assertTrue("Key not found: " + key, facet.containsKey(key));
    }

    private void checkIntFacets(ResultDefinition<?> result) {
        checkIntFacets(result, "int1", "int2", "int3");
    }

    @Test
    public void intFacets() {
        ResultDefinition<?> result = indexService.searchQuery(
            QueryDefinition.of(MatchAllDocs.INSTANCE).facet("intAssociatedFacet", FacetDefinition.EMPTY).build());
        Assert.assertNotNull(result);
        Assert.assertEquals(6, result.totalHits);
        checkIntFacets(result);
    }

    private void checkFloatFacets(ResultDefinition<?> result, String... expectedKeys) {
        Assert.assertNotNull(result.facets);
        Assert.assertTrue(result.isAnyFacet());
        Map<String, Number> facet = result.getFacets().get("floatAssociatedFacet");
        Assert.assertNotNull(facet);
        for (String key : expectedKeys)
            Assert.assertTrue(facet.containsKey(key));
    }

    private void checkFloatFacets(ResultDefinition<?> result) {
        checkFloatFacets(result, "float4", "float5", "float6");
    }

    @Test
    public void floatFacets() {
        ResultDefinition<?> result = indexService.searchQuery(QueryDefinition.of(MatchAllDocs.INSTANCE)
            .facet("floatAssociatedFacet", FacetDefinition.EMPTY)
            .build());
        Assert.assertNotNull(result);
        Assert.assertEquals(6, result.totalHits);
        checkFloatFacets(result);
    }

    @Test
    public void allFacets() {
        ResultDefinition<?> result = indexService.searchQuery(QueryDefinition.of(MatchAllDocs.INSTANCE)
            .facet("floatAssociatedFacet", FacetDefinition.EMPTY)
            .facet("intAssociatedFacet", FacetDefinition.of().build())
            .build());
        Assert.assertNotNull(result);
        Assert.assertEquals(6, result.totalHits);
        checkIntFacets(result);
        checkFloatFacets(result);
    }

    @Test
    public void limitFacet() {
        ResultDefinition<?> result = indexService.searchQuery(QueryDefinition.of(MatchAllDocs.INSTANCE)
            .facet("floatAssociatedFacet", FacetDefinition.of(2).build())
            .facet("intAssociatedFacet", FacetDefinition.create(2))
            .build());
        Assert.assertNotNull(result);
        Assert.assertEquals(6, result.totalHits);
        Assert.assertNotNull(result.facets);
        checkIntFacets(result, "int2", "int3");
        checkFloatFacets(result, "float5", "float6");
    }
}
