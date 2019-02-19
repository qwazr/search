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
package com.qwazr.search.query;

import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.ExplainDefinition;
import com.qwazr.search.index.QueryContext;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.search.test.units.IndexRecord;
import org.apache.lucene.search.Query;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class TermsQueryTest extends AbstractIndexTest.WithIndexRecord.NoTaxonomy {

    @BeforeClass
    public static void setup() throws IOException, InterruptedException, URISyntaxException {
        initIndexService();
        indexService.postDocument(new IndexRecord.NoTaxonomy("1").textField("Hello World").stringField("Hello World"));
        indexService.postDocument(
                new IndexRecord.NoTaxonomy("2").textField("Hello World 2").stringField("Hello World 2"));
    }

    private void checkQuery(QueryDefinition queryDef, long totalHits) {
        ResultDefinition.WithObject<? extends IndexRecord> result = indexService.searchQuery(queryDef);
        Assert.assertNotNull(result);
        Assert.assertEquals(totalHits, result.totalHits);
        Assert.assertNotNull(result.documents);
        ExplainDefinition explain = indexService.explainQuery(queryDef, result.documents.get(0).getDoc());
        Assert.assertNotNull(explain);
    }

    @Test
    public void testArray() {
        QueryDefinition queryDef =
                QueryDefinition.of(TermsQuery.of(FieldDefinition.ID_FIELD).add("1", "2").build()).build();
        checkQuery(queryDef, 2L);
    }

    @Test
    public void luceneQuery() throws IOException {
        Query luceneQuery =
                TermsQuery.of(FieldDefinition.ID_FIELD).add("1", "2").build().getQuery(QueryContext.DEFAULT);
        Assert.assertNotNull(luceneQuery);
    }

}
