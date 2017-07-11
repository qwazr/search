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
package com.qwazr.search.test.units;

import com.qwazr.search.function.ConstValueSource;
import com.qwazr.search.function.DivFloatFunction;
import com.qwazr.search.function.DoubleConstValueSource;
import com.qwazr.search.function.DoubleFieldSource;
import com.qwazr.search.function.FloatFieldSource;
import com.qwazr.search.function.IntFieldSource;
import com.qwazr.search.function.LongFieldSource;
import com.qwazr.search.function.MaxDocValueSource;
import com.qwazr.search.function.MaxFloatFunction;
import com.qwazr.search.function.MinFloatFunction;
import com.qwazr.search.function.NumDocsValueSource;
import com.qwazr.search.function.PowFloatFunction;
import com.qwazr.search.function.ProductFloatFunction;
import com.qwazr.search.function.SumFloatFunction;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.query.CustomScoreQuery;
import com.qwazr.search.query.FunctionQuery;
import com.qwazr.search.query.MatchAllDocsQuery;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class CustomScoreQueryTest extends AbstractIndexTest {

	@BeforeClass
	public static void setup() throws IOException, InterruptedException, URISyntaxException {
		initIndexService();
		indexService.postDocument(new IndexRecord("1").textField("Hello World")
				.floatDocValue(2.0F)
				.intDocValue(2)
				.doubleDocValue(2.0d)
				.longDocValue(2));
		indexService.postDocument(new IndexRecord("2").textField("How are you ?")
				.floatDocValue(3.0F)
				.intDocValue(3)
				.doubleDocValue(3.0d)
				.longDocValue(3));
	}

	private void checkFieldSourceResult(ResultDefinition.WithObject<IndexRecord> result, float... values) {
		Assert.assertNotNull(result);
		Assert.assertEquals(Long.valueOf(values.length), result.total_hits);
		int i = 0;
		for (float value : values)
			Assert.assertEquals(value, result.getDocuments().get(i++).getScore(), 0);
	}

	@Test
	public void testFloat() {
		ResultDefinition.WithObject<IndexRecord> result = indexService.searchQuery(QueryDefinition.of(
				new CustomScoreQuery(new MatchAllDocsQuery(), new FunctionQuery(new FloatFieldSource("floatDocValue"))))
				.build());
		checkFieldSourceResult(result, 3.0F, 2.0F);
	}

	@Test
	public void testDouble() {
		ResultDefinition.WithObject<IndexRecord> result = indexService.searchQuery(QueryDefinition.of(
				new CustomScoreQuery(new MatchAllDocsQuery(),
						new FunctionQuery(new DoubleFieldSource("doubleDocValue")))).build());
		checkFieldSourceResult(result, 3.0F, 2.0F);
	}

	@Test
	public void testLong() {
		ResultDefinition.WithObject<IndexRecord> result = indexService.searchQuery(QueryDefinition.of(
				new CustomScoreQuery(new MatchAllDocsQuery(), new FunctionQuery(new LongFieldSource("longDocValue"))))
				.build());
		checkFieldSourceResult(result, 3.0F, 2.0F);
	}

	@Test
	public void testInt() {
		ResultDefinition.WithObject<IndexRecord> result = indexService.searchQuery(QueryDefinition.of(
				new CustomScoreQuery(new MatchAllDocsQuery(), new FunctionQuery(new IntFieldSource("intDocValue"))))
				.build());
		checkFieldSourceResult(result, 3.0F, 2.0F);
	}

	@Test
	public void testMax() {
		ResultDefinition.WithObject<IndexRecord> result = indexService.searchQuery(QueryDefinition.of(
				new CustomScoreQuery(new MatchAllDocsQuery(), new FunctionQuery(
						new MaxFloatFunction(new IntFieldSource("intDocValue"), new LongFieldSource("longDocValue")))))
				.build());
		checkFieldSourceResult(result, 3.0F, 2.0F);
	}

	@Test
	public void testMin() {
		ResultDefinition.WithObject<IndexRecord> result = indexService.searchQuery(QueryDefinition.of(
				new CustomScoreQuery(new MatchAllDocsQuery(), new FunctionQuery(
						new MinFloatFunction(new IntFieldSource("intDocValue"), new LongFieldSource("longDocValue")))))
				.build());
		checkFieldSourceResult(result, 3.0F, 2.0F);
	}

	@Test
	public void testSum() {
		ResultDefinition.WithObject<IndexRecord> result = indexService.searchQuery(QueryDefinition.of(
				new CustomScoreQuery(new MatchAllDocsQuery(), new FunctionQuery(
						new SumFloatFunction(new FloatFieldSource("floatDocValue"),
								new DoubleFieldSource("doubleDocValue"))))).build());
		checkFieldSourceResult(result, 6.0F, 4.0F);
	}

	@Test
	public void testPow() {
		ResultDefinition.WithObject<IndexRecord> result = indexService.searchQuery(QueryDefinition.of(
				new CustomScoreQuery(new MatchAllDocsQuery(), new FunctionQuery(
						new PowFloatFunction(new LongFieldSource("longDocValue"), new IntFieldSource("intDocValue")))))
				.build());
		checkFieldSourceResult(result, 27.0F, 4.0F);
	}

	@Test
	public void testProduct() {
		ResultDefinition.WithObject<IndexRecord> result = indexService.searchQuery(QueryDefinition.of(
				new CustomScoreQuery(new MatchAllDocsQuery(), new FunctionQuery(
						new ProductFloatFunction(new FloatFieldSource("floatDocValue"),
								new DoubleFieldSource("doubleDocValue"), new LongFieldSource("longDocValue"),
								new IntFieldSource("intDocValue"))))).build());
		checkFieldSourceResult(result, 81.0F, 16.0F);
	}

	@Test
	public void testDiv() {
		ResultDefinition.WithObject<IndexRecord> result = indexService.searchQuery(QueryDefinition.of(
				new CustomScoreQuery(new MatchAllDocsQuery(), new FunctionQuery(
						new DivFloatFunction(new LongFieldSource("longDocValue"), new IntFieldSource("intDocValue")))))
				.build());
		checkFieldSourceResult(result, 1.0F, 1.0F);
	}

	@Test
	public void testConstDouble() {
		ResultDefinition.WithObject<IndexRecord> result = indexService.searchQuery(QueryDefinition.of(
				new CustomScoreQuery(new MatchAllDocsQuery(), new FunctionQuery(new DoubleConstValueSource(5.0d))))
				.build());
		checkFieldSourceResult(result, 5.0F, 5.0F);
	}

	@Test
	public void testConstFloat() {
		ResultDefinition.WithObject<IndexRecord> result = indexService.searchQuery(QueryDefinition.of(
				new CustomScoreQuery(new MatchAllDocsQuery(), new FunctionQuery(new ConstValueSource(6.0f)))).build());
		checkFieldSourceResult(result, 6.0F, 6.0F);
	}

	@Test
	public void testNumDocs() {
		ResultDefinition.WithObject<IndexRecord> result = indexService.searchQuery(QueryDefinition.of(
				new CustomScoreQuery(new MatchAllDocsQuery(), new FunctionQuery(new NumDocsValueSource()))).build());
		checkFieldSourceResult(result, 2.0F, 2.0F);
	}

	@Test
	public void testMaxDocs() {
		ResultDefinition.WithObject<IndexRecord> result = indexService.searchQuery(QueryDefinition.of(
				new CustomScoreQuery(new MatchAllDocsQuery(), new FunctionQuery(new MaxDocValueSource()))).build());
		checkFieldSourceResult(result, 2.0F, 2.0F);
	}
}
