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

package com.qwazr.search.query;

import com.qwazr.search.analysis.FloatArrayPayloadDecoder;
import com.qwazr.search.analysis.SmartAnalyzerSet;
import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.annotations.Index;
import com.qwazr.search.annotations.IndexField;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.search.test.units.IndexRecord;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.IndexOptions;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class PayloadScoreQueryTest
    extends AbstractIndexTest.WithIndexRecord<PayloadScoreQueryTest.WithPayloadBoostRecord> {

    private static AnnotatedIndexService<WithPayloadBoostRecord> indexService;

    public PayloadScoreQueryTest() {
        super(WithPayloadBoostRecord.class);
    }

    @BeforeClass
    public static void setup() throws IOException, InterruptedException, URISyntaxException {
        initIndexManager().registerConstructorParameter(new float[]{8, 5, 3});
        indexService = initIndexService(WithPayloadBoostRecord.class);
        indexService.postDocument(new WithPayloadBoostRecord("1").textField("Hello World"));
        indexService.postDocument(new WithPayloadBoostRecord("2").textField("How are you ?"));
    }

    @Test
    public void test() {
        ResultDefinition.WithObject<WithPayloadBoostRecord> result = indexService.searchQuery(QueryDefinition.of(
            new PayloadScoreQuery(
                new SpanTermQuery("full", "hello"),
                PayloadScoreQuery.FunctionType.MAX.payloadFunction,
                new FloatArrayPayloadDecoder(10, 100, 1000),
                true))
            .queryDebug(true)
            .build());
        Assert.assertNotNull(result);
        Assert.assertEquals(1L, result.getTotalHits(), 0);
        Assert.assertEquals("PayloadScoreQuery(full:hello, function: MaxPayloadFunction, includeSpanScore: true)",
            result.query);
        Assert.assertEquals(3.4314215183258057, result.getDocuments().get(0).getScore(), 0.00001);

    }

    @Index(name = "IndexRecord", enableTaxonomyIndex = false, useCompoundFile = false)
    public static class WithPayloadBoostRecord extends IndexRecord<WithPayloadBoostRecord> {

        @IndexField(template = FieldDefinition.Template.TextField,
            analyzerClass = FullAsciiIndex.class,
            queryAnalyzerClass = SmartAnalyzerSet.AsciiQuery.class,
            tokenized = true,
            indexOptions = IndexOptions.DOCS_AND_FREQS_AND_POSITIONS)
        final public List<String> full = new ArrayList<>();

        public WithPayloadBoostRecord() {
        }

        WithPayloadBoostRecord(String id) {
            super(id);
        }

        @Override
        protected WithPayloadBoostRecord me() {
            return this;
        }

        @Override
        public WithPayloadBoostRecord textField(String textField) {
            full.add("0 " + textField);
            return super.textField(textField);
        }
    }

    public static class FullAsciiIndex extends SmartAnalyzerSet.PayloadBoost {
        protected TokenStream normalize(String fieldName, TokenStream in) {
            return SmartAnalyzerSet.ascii(in);
        }
    }


    @Test
    public void testWithBoost() {
        final SpanTermQuery spanTermQuery = new SpanTermQuery("full", "hello");
        final PayloadScoreQuery payloadScoreQuery = new PayloadScoreQuery(
            spanTermQuery,
            PayloadScoreQuery.FunctionType.MAX.payloadFunction,
            new FloatArrayPayloadDecoder(10, 100, 1000),
            true);
        final ResultDefinition.WithObject<WithPayloadBoostRecord> result = indexService.searchQuery(
            QueryDefinition.of(payloadScoreQuery).queryDebug(true).build());

        final double expectedScore = 3.4314215183258057;
        Assert.assertEquals(expectedScore, result.getDocuments().get(0).getScore(), 0.00001);

        final double expectedScoreWithBoost = 3 * expectedScore;

        // Test using Boost query
        {
            final Boost boost = new Boost(payloadScoreQuery, 3.0f);
            final ResultDefinition.WithObject<WithPayloadBoostRecord> resultWithBoost = indexService.searchQuery(
                QueryDefinition.of(boost).queryDebug(true).build());

            Assert.assertEquals("(PayloadScoreQuery(full:hello, function: MaxPayloadFunction, includeSpanScore: true))^3.0",
                resultWithBoost.query);

            Assert.assertNotNull(resultWithBoost);
            Assert.assertEquals(1L, resultWithBoost.getTotalHits(), 0);
            Assert.assertEquals(expectedScoreWithBoost, resultWithBoost.getDocuments().get(0).getScore(), 0.00001);
        }

        // Test using SpanBoost query
        {
            final SpanBoost spanBoost = new SpanBoost(spanTermQuery, 3f);
            final PayloadScoreQuery payloadScoreQuery2 = new PayloadScoreQuery(
                spanBoost,
                PayloadScoreQuery.FunctionType.MAX.payloadFunction,
                new FloatArrayPayloadDecoder(10, 100, 1000),
                true);
            final ResultDefinition.WithObject<WithPayloadBoostRecord> resultWithSpanBoost = indexService.searchQuery(
                QueryDefinition.of(payloadScoreQuery2)
                    .queryDebug(true)
                    .build());

            Assert.assertEquals("PayloadScoreQuery((full:hello)^3.0, function: MaxPayloadFunction, includeSpanScore: true)",
                resultWithSpanBoost.query);

            Assert.assertNotNull(resultWithSpanBoost);
            Assert.assertEquals(1L, resultWithSpanBoost.getTotalHits(), 0);
            Assert.assertEquals(expectedScoreWithBoost, resultWithSpanBoost.getDocuments().get(0).getScore(), 0.00001);
        }
    }

}
