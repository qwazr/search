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

import com.qwazr.search.index.QueryContext;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.query.AbstractQuery;
import com.qwazr.search.query.QueryParserOperator;
import com.qwazr.search.query.SimpleQueryParser;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class SimpleQueryParserTest extends AbstractIndexTest.WithIndexRecord {

	@BeforeClass
	public static void setup() throws IOException, InterruptedException, URISyntaxException, java.text.ParseException {
		initIndexManager();
		indexManager.registerConstructorParameter(SynonymMap.class,
				RealTimeSynonymsResourcesTest.getSynonymMap(RealTimeSynonymsResourcesTest.WHITESPACE_ANALYZER,
						RealTimeSynonymsResourcesTest.EN_FR_DE_SYNONYMS));
		initIndexService();
		indexService.postDocument(new IndexRecord("1").textField("Hello")
				.stringField("world")
				.textSynonymsField1("hello world"));
	}

	@Test
	public void testWithDefaultAnalyzer() {
		QueryDefinition queryDef = QueryDefinition.of(SimpleQueryParser.of()
				.setDefaultOperator(QueryParserOperator.AND)
				.addBoost("textField", 1F)
				.addBoost("stringField", 1F)
				.setQueryString("Hello")
				.build()).build();
		checkQuery(queryDef);
	}

	@Test
	public void testWithCustomAnalyzer() {
		QueryDefinition queryDef = QueryDefinition.of(SimpleQueryParser.of()
				.setDefaultOperator(QueryParserOperator.AND)
				.addBoost("textField", 1F)
				.addBoost("stringField", 1F)
				.setAnalyzer(new StandardAnalyzer())
				.setQueryString("Hello World")
				.build()).
				build();
		checkQuery(queryDef);
	}

	@Test
	public void luceneQuery() throws IOException, ReflectiveOperationException, ParseException {
		Query luceneQuery = SimpleQueryParser.of().setDefaultOperator(QueryParserOperator.AND).
				addBoost("textField", 1F).addBoost("stringField", 1F).setQueryString("Hello World").build().getQuery(
				QueryContext.DEFAULT);
		Assert.assertNotNull(luceneQuery);
	}

	@Test
	@Ignore
	public void testWithSynonymsOr()
			throws QueryNodeException, ReflectiveOperationException, ParseException, IOException {
		AbstractQuery query = SimpleQueryParser.of()
				.addField("textSynonymsField1", "textField", "stringField")
				.setDefaultOperator(QueryParserOperator.OR)
				.setQueryString("bonjour le monde")
				.build();
		checkQuery(QueryDefinition.of(query).queryDebug(true).build());
	}

	@Test
	@Ignore
	public void testWithSynonymsAnd()
			throws QueryNodeException, ReflectiveOperationException, ParseException, IOException {
		AbstractQuery query = SimpleQueryParser.of()
				.addField("textSynonymsField1", "textSynonymsField2")
				.setDefaultOperator(QueryParserOperator.AND)
				.setQueryString("bonjour le monde")
				.build();
		checkQuery(QueryDefinition.of(query).queryDebug(true).build());
	}

}
