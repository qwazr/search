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
import com.qwazr.search.index.QueryBuilder;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.query.MatchAllDocs;
import com.qwazr.utils.RandomUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

public class DynamicMultiFieldFacetTest extends AbstractIndexTest.WithIndexRecord.WithTaxonomy {

    @BeforeClass
    public static void setup() throws URISyntaxException {
        initIndexService();
    }

    @Test
    public void randomTest() throws IOException {
        final IndexRecord.WithTaxonomy record = getNewRecord(RandomUtils.alphanumeric(10));
        for (int i = 0; i < RandomUtils.nextInt(1, 10); i++)
            record.dynamicFacets("dynamic_facets_" + RandomUtils.nextInt(0, 2), RandomUtils.alphanumeric(10));
        indexService.postDocument(record);
        QueryBuilder queryBuilder = QueryDefinition.of(MatchAllDocs.INSTANCE);
        record.dynamicFacets.keySet()
            .forEach(f -> queryBuilder.facet(f,
                FacetDefinition.of(10).genericFieldName("dynamic_facets_*").build()));

        final ResultDefinition.WithObject<? extends IndexRecord<?>> result =
            indexService.searchQuery(queryBuilder.build());

        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.totalHits, 0);
        Assert.assertNotNull(result.facets);
        record.dynamicFacets.forEach((n, l) -> {
            Map<String, Number> facet = result.facets.get(n);
            Assert.assertNotNull(facet);
            l.forEach(v -> Assert.assertTrue(facet.containsKey(v)));
        });
    }
}
