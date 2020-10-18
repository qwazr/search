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
package com.qwazr.search.index;

import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.query.HasTerm;
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.search.test.units.IndexRecord;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static com.qwazr.search.test.units.IndexRecord.SORTED_DOC_VALUE_FIELD_NAME;
import static com.qwazr.search.test.units.IndexRecord.TEXT_FIELD_NAME;

public class QueryResultScoreTest extends AbstractIndexTest.WithIndexRecord.NoTaxonomy {

    private final static String textDoc1 = "OpenSearchServer is an open-source application, a very useful application";
    private final static String textDoc2 = "Java is a programming language used in application development";
    private final static HasTerm hasTermQuery = new HasTerm(TEXT_FIELD_NAME, "application");

    @BeforeClass
    public static void setup() throws IOException, URISyntaxException {
        initIndexManager();
        initIndexService();
        indexService.postDocument(new IndexRecord.NoTaxonomy("1").textField(textDoc1).sortedDocValue(textDoc1));
        indexService.postDocument(new IndexRecord.NoTaxonomy("2").textField(textDoc2).sortedDocValue(textDoc2));
    }

    @Test
    public void shouldHaveScoreForSimpleQuery() {
        final QueryDefinition queryDefinition = QueryDefinition.of(hasTermQuery)
            .returnedField("*")
            .build();

        final ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result = indexService.searchQuery(queryDefinition);

        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.totalHits);
        checkRecord(result.getDocuments().get(0), "1", 0.112288802);
        checkRecord(result.getDocuments().get(1), "2", 0.084697060);
    }

    @Test
    public void shouldHaveScoreForQueryWithMoreThanOneSortIncludingSCORE_FIELD() {
        final QueryDefinition queryDefinition = QueryDefinition.of(hasTermQuery)
            .returnedField("*")
            .sort(SORTED_DOC_VALUE_FIELD_NAME, QueryDefinition.SortEnum.ascending_missing_last)
            .sort(FieldDefinition.SCORE_FIELD, QueryDefinition.SortEnum.descending)
            .build();

        final ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result = indexService.searchQuery(queryDefinition);

        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.totalHits);
        checkRecord(result.getDocuments().get(0), "2", 0.084697060);
        checkRecord(result.getDocuments().get(1), "1", 0.112288802);
    }

    @Test
    public void shouldHaveScoreForQueryWithMoreThanOneSortExcludingSCORE_FIELD() {
        final QueryDefinition queryDefinition = QueryDefinition.of(hasTermQuery)
            .returnedField("*")
            .sort(SORTED_DOC_VALUE_FIELD_NAME, QueryDefinition.SortEnum.ascending_missing_last)
            /* Specifically exclude SCORE_FIELD from result sorting strategy, result scores are then undefined (NaN)
            .sort(FieldDefinition.SCORE_FIELD, QueryDefinition.SortEnum.descending) */
            .build();

        final ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result = indexService.searchQuery(queryDefinition);

        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.totalHits);
        checkRecord(result.getDocuments().get(0), "2", Float.NaN);
        checkRecord(result.getDocuments().get(1), "1", Float.NaN);
    }

}
