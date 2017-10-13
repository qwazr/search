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

import com.qwazr.search.analysis.PayloadBoostBm25Similarity;
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
import org.apache.lucene.queries.payloads.MaxPayloadFunction;
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
		initIndexManager().registerConstructorParameter(new float[] { 8, 5, 3 });
		indexService = initIndexService(WithPayloadBoostRecord.class);
		indexService.postDocument(new WithPayloadBoostRecord("1").textField("Hello World"));
		indexService.postDocument(new WithPayloadBoostRecord("2").textField("How are you ?"));
	}

	@Test
	public void test() {
		ResultDefinition.WithObject<WithPayloadBoostRecord> result = indexService.searchQuery(QueryDefinition.of(
				new PayloadScoreQuery(new SpanTermQuery("full", "hello"), new MaxPayloadFunction(), true))
				.queryDebug(true)
				.build());
		Assert.assertNotNull(result);
		Assert.assertEquals(1L, result.getTotalHits(), 0);
		Assert.assertEquals("PayloadScoreQuery(full:hello, function: MaxPayloadFunction, includeSpanScore: true)",
				result.query);
	}

	@Index(name = "IndexRecord",
			schema = "TestQueries",
			enableTaxonomyIndex = false,
			useCompoundFile = false,
			similarityClass = PayloadBoostBm25Similarity.class)
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

}
