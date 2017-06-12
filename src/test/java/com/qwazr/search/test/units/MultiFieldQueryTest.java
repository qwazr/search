/**
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.test.units;

import com.qwazr.search.index.QueryContext;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.query.MultiFieldQuery;
import com.qwazr.search.query.QueryParserOperator;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.Query;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class MultiFieldQueryTest extends AbstractIndexTest {

	@BeforeClass
	public static void setup() throws IOException, InterruptedException, URISyntaxException {
		initIndexService();
		indexService.postDocument(new IndexRecord("1").textField("Hello World").stringField("Hello World"));
	}

	@Test
	public void testWithDefaultAnalyzer() {
		QueryDefinition queryDef = QueryDefinition.of(new MultiFieldQuery(QueryParserOperator.AND, "Hello", 0).boost(
				"textField", 1F).boost("stringField", 1F)).build();
		checkQuery(queryDef);
	}

	@Test
	public void testWithCustomAnalyzer() {
		QueryDefinition queryDef = QueryDefinition.of(new MultiFieldQuery(QueryParserOperator.AND, "Hello", 0, null,
				new StandardAnalyzer()).boost("textField", 1F).boost("stringField", 1F)).build();
		checkQuery(queryDef);
	}

	@Test
	public void luceneQuery() throws IOException, ReflectiveOperationException {
		Query luceneQuery = new MultiFieldQuery(QueryParserOperator.AND, "Hello World", 0).boost("textField", 1F).boost(
				"stringField", 1F).getQuery(QueryContext.DEFAULT);
		Assert.assertNotNull(luceneQuery);
	}

}
