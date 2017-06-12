/*
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
import com.qwazr.search.query.AbstractQuery;
import com.qwazr.search.query.QueryParser;
import com.qwazr.search.query.QueryParserOperator;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class QueryParserTest extends AbstractIndexTest {

	@BeforeClass
	public static void setup() throws IOException, InterruptedException, URISyntaxException, java.text.ParseException {
		initIndexManager();
		indexManager.registerConstructorParameter(SynonymMap.class,
				RealTimeSynonymsResourcesTest.getSynonymMap(RealTimeSynonymsResourcesTest.WHITESPACE_ANALYZER,
						RealTimeSynonymsResourcesTest.EN_FR_DE_SYNONYMS));
		initIndexService();
		indexService.postDocument(new IndexRecord("1").textField("Hello world").textSynonymsField("hello world"));
	}

	@Test
	public void testWithDefaultAnalyzer() {
		QueryDefinition queryDef = QueryDefinition.of(QueryParser.of("textField").setDefaultOperator(
				QueryParserOperator.AND).setQueryString("Hello").build()).build();
		checkQuery(queryDef);
	}

	@Test
	public void testWithCustomAnalyzer() {
		QueryDefinition queryDef = QueryDefinition.of(QueryParser.of("textField").setDefaultOperator(
				QueryParserOperator.AND).setQueryString("hello World").setAnalyzer(new StandardAnalyzer()).build()).
				build();
		checkQuery(queryDef);
	}

	@Test
	public void luceneQuery() throws IOException, ReflectiveOperationException, ParseException {
		Query luceneQuery = QueryParser.of("textField").setDefaultOperator(QueryParserOperator.AND).setQueryString(
				"Hello World").build().getQuery(QueryContext.DEFAULT);
		Assert.assertNotNull(luceneQuery);
	}

	@Test
	public void testWithSynonymsOr()
			throws QueryNodeException, ReflectiveOperationException, ParseException, IOException {
		AbstractQuery query = QueryParser.of("textSynonymsField")
				.setDefaultOperator(QueryParserOperator.OR)
				.setSplitOnWhitespace(false)
				.setQueryString("bonjour le monde")
				.build();
		checkQuery(QueryDefinition.of(query).queryDebug(true).build());
	}

	@Test
	public void testWithSynonymsAnd()
			throws QueryNodeException, ReflectiveOperationException, ParseException, IOException {
		AbstractQuery query = QueryParser.of("textSynonymsField")
				.setDefaultOperator(QueryParserOperator.AND)
				.setSplitOnWhitespace(false)
				.setQueryString("bonjour le monde")
				.build();
		checkQuery(QueryDefinition.of(query).queryDebug(true).build());
	}

}
