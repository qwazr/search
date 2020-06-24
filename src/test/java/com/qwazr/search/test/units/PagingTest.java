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

import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.FacetDefinition;
import com.qwazr.search.index.QueryBuilder;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.index.ResultDocumentMap;
import com.qwazr.search.index.ResultDocumentObject;
import com.qwazr.search.query.MatchAllDocs;
import com.qwazr.utils.RandomUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class PagingTest extends AbstractIndexTest.WithIndexRecord.WithTaxonomy {

    private static LinkedHashMap<String, IndexRecord> documents;

    @BeforeClass
    public static void setup() throws IOException, URISyntaxException {
        initIndexService();
        documents = new LinkedHashMap<>();
        for (int i = 0; i < RandomUtils.nextInt(201, 299); i++) {
            final IndexRecord.WithTaxonomy record =
                new IndexRecord.WithTaxonomy(Integer.toString(i)).sortedDocValue(RandomUtils.alphanumeric(5))
                    .facetField(Integer.toString(RandomUtils.nextInt(1, 3)));
            documents.put(record.id, record);
            indexService.postDocument(record);
        }
    }

    private long checkEmpty(ResultDefinition<?> result) {
        Assert.assertNotNull(result);
        return result.getTotalHits();
    }

    private void checkPaging(final QueryBuilder builder) {

        final long totalHits = checkEmpty(indexService.searchQuery(builder.build()));
        Assert.assertEquals(totalHits, checkEmpty(indexService.searchQueryWithMap(builder.build())));

        Assert.assertTrue(totalHits > 0);
        final Set<String> idSetObject = new HashSet<>();
        final Set<String> idSetMap = new HashSet<>();
        int start = 0;
        while (start < totalHits) {

            final int rows = RandomUtils.nextInt(8, 20);
            final long expectedRows = start + rows > totalHits ? totalHits - start : rows;
            final QueryDefinition queryDef = builder.start(start).rows(rows).build();

            // Check Object
            final ResultDefinition.WithObject<IndexRecord.WithTaxonomy> resultObject =
                indexService.searchQuery(queryDef);
            List<ResultDocumentObject<IndexRecord.WithTaxonomy>> objectDocs = resultObject.getDocuments();
            Assert.assertEquals(expectedRows, objectDocs.size());
            int i = 0;
            for (ResultDocumentObject<IndexRecord.WithTaxonomy> objectDoc : objectDocs) {
                ResultDocumentObject<IndexRecord.WithTaxonomy> objectDoc2 = objectDocs.get(i);
                Assert.assertEquals(objectDoc, objectDoc2);
                Assert.assertEquals(i + start, objectDoc.getPos());
                idSetObject.add(objectDoc.record.id);
                i++;
            }

            // Check Map
            final ResultDefinition.WithMap resultMap = indexService.searchQueryWithMap(queryDef);
            List<ResultDocumentMap> mapDocs = resultMap.getDocuments();
            Assert.assertEquals(expectedRows, mapDocs.size());
            i = 0;
            for (ResultDocumentMap mapDoc : mapDocs) {
                ResultDocumentMap mapDoc2 = mapDocs.get(i);
                Assert.assertEquals(mapDoc, mapDoc2);
                Assert.assertEquals(i + start, mapDoc.getPos());
                idSetMap.add((String) mapDoc.getFields().get(FieldDefinition.ID_FIELD));
                i++;
            }

            start += rows;
        }
        Assert.assertEquals(totalHits, idSetObject.size());
        Assert.assertEquals(totalHits, idSetMap.size());
    }

    @Test
    public void pagingWithoutSort() {
        checkPaging(QueryDefinition.of(MatchAllDocs.INSTANCE).returnedField("*"));
    }

    @Test
    public void pagingSort() {
        checkPaging(QueryDefinition.of(MatchAllDocs.INSTANCE)
            .returnedField("*")
            .sort("sortedDocValue", QueryDefinition.SortEnum.ascending));
    }

    @Test
    public void pagingFacetSort() {
        checkPaging(QueryDefinition.of(MatchAllDocs.INSTANCE)
            .returnedField("*")
            .facet("facetField", FacetDefinition.create(10))
            .sort("sortedDocValue", QueryDefinition.SortEnum.ascending));
    }

    @Test
    public void pagingFacetWithoutSort() {
        checkPaging(QueryDefinition.of(MatchAllDocs.INSTANCE).returnedField("*"));
    }
}
