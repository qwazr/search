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

import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.annotations.Index;
import com.qwazr.search.annotations.IndexField;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.search.test.units.IndexRecord;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.payloads.DelimitedPayloadTokenFilter;
import org.apache.lucene.analysis.payloads.IntegerEncoder;
import org.apache.lucene.analysis.payloads.PayloadEncoder;
import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.queries.payloads.MinPayloadFunction;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class PayloadScoreQueryScoreTest
        extends AbstractIndexTest.WithIndexRecord<PayloadScoreQueryScoreTest.PayloadRecord> {

    private static AnnotatedIndexService<PayloadRecord> indexService;

    public PayloadScoreQueryScoreTest() {
        super(PayloadRecord.class);
    }

    @BeforeClass
    public static void setup() throws IOException, InterruptedException, URISyntaxException {
        indexService = initIndexService(PayloadRecord.class);
        indexService.postDocument(new PayloadRecord("1").payload("41|1").payload("42|2"));
        indexService.postDocument(new PayloadRecord("2").payload("41|2").payload("42|1"));
    }

    @Test
    public void test() {

        ResultDefinition.WithObject<PayloadRecord> result = indexService.searchQuery(QueryDefinition.of(
                new PayloadScoreQuery(
                        new SpanTermQuery("payloads", "41"),
                        new MinPayloadFunction(),
                        b -> PayloadHelper.decodeFloat(b.bytes, b.offset),
                        true))
                .queryDebug(true)
                .build());
        Assert.assertNotNull(result);
        Assert.assertEquals(2L, result.getTotalHits(), 0);
        Assert.assertEquals("PayloadScoreQuery(payloads:41, function: MinPayloadFunction, includeSpanScore: true)",
                result.query);
    }

    @Index(name = "IndexRecord",
            schema = "TestQueries",
            enableTaxonomyIndex = false,
            useCompoundFile = false)
    public static class PayloadRecord extends IndexRecord<PayloadRecord> {

        @IndexField(template = FieldDefinition.Template.TextField,
                analyzerClass = PayloadAnalyzer.class,
                tokenized = true,
                indexOptions = IndexOptions.DOCS_AND_FREQS_AND_POSITIONS)
        final public List<String> payloads = new ArrayList<>();

        public PayloadRecord() {
        }

        PayloadRecord(String id) {
            super(id);
        }

        PayloadRecord payload(String payload) {
            payloads.add(payload);
            return this;
        }
    }

    public static class PayloadAnalyzer extends Analyzer {

        final private PayloadEncoder encoder;

        public PayloadAnalyzer() {
            encoder = new IntegerEncoder();
        }

        @Override
        protected TokenStreamComponents createComponents(final String fieldName) {

            final Tokenizer tokenizer = new KeywordTokenizer();
            final TokenStream stream = new DelimitedPayloadTokenFilter(tokenizer, '|', encoder);
            return new TokenStreamComponents(tokenizer, stream);
        }
    }
}
