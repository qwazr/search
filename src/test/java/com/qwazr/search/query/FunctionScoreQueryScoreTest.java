/*
 *  Copyright 2015-2020 Emmanuel Keller / QWAZR
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.qwazr.search.query;

import com.qwazr.search.function.DoubleValuesSource;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.index.ResultDocumentObject;
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.search.test.units.IndexRecord;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class FunctionScoreQueryScoreTest extends AbstractIndexTest.WithIndexRecord.NoTaxonomy {

    @BeforeClass
    public static void setup() throws IOException, InterruptedException, URISyntaxException {
        initIndexService();
        indexService.postDocument(
                new IndexRecord.NoTaxonomy("1")
                        .textField("Sampson: No, sir, I do not bite my thumb at you sir; but I bite my thumb, sir.")
                        .floatDocValue(1000.0F));
        indexService.postDocument(
                new IndexRecord.NoTaxonomy("2")
                        .textField("Gregory: Do you quarrel, sir?")
                        .floatDocValue(1.0F));
        indexService.postDocument(
                new IndexRecord.NoTaxonomy("3")
                        .textField("Abraham: Quarrel, sir? No, sir.")
                        .floatDocValue(100.0F));
    }

    private void checkRecord(final ResultDocumentObject<IndexRecord.NoTaxonomy> record, final String id, final double score) {
        Assert.assertEquals(record.getRecord().id, id);
        Assert.assertEquals(record.getScore(), score, 0.0001);
    }

    @Test
    public void testWithout() {
        ResultDefinition.WithObject<IndexRecord.NoTaxonomy>
                result =
                indexService.searchQuery(QueryDefinition.of(
                        new TermQuery("textField", "sir"))
                        .returnedField("*")
                        .build());
        Assert.assertNotNull(result);
        Assert.assertEquals(3, result.totalHits);

        checkRecord(result.getDocuments().get(0), "3", 0.09599176);
        checkRecord(result.getDocuments().get(1), "1", 0.07955062);
        checkRecord(result.getDocuments().get(2), "2", 0.074927434);
    }

    @Test
    public void testWith() {
        ResultDefinition.WithObject<IndexRecord.NoTaxonomy>
                result =
                indexService.searchQuery(QueryDefinition.of(
                        new FunctionScoreQuery(
                                new TermQuery("textField", "sir"),
                                new DoubleValuesSource.FloatField("floatDocValue")))
                        .returnedField("*")
                        .build());
        Assert.assertNotNull(result);
        Assert.assertEquals(3, result.totalHits);

        checkRecord(result.getDocuments().get(0), "1", 79.55061);
        checkRecord(result.getDocuments().get(1), "3", 9.599176);
        checkRecord(result.getDocuments().get(2), "2", 0.074927434);
    }
}
