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
import com.qwazr.search.query.MultiFieldQuery;
import com.qwazr.search.query.QueryParser;
import com.qwazr.search.query.QueryParserOperator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;

public class MultiFieldQueryTest extends AbstractIndexTest.WithIndexRecord.NoTaxonomy {

	@BeforeClass
	public static void setup() throws IOException, InterruptedException, URISyntaxException, ParseException {
		initIndexManager();
		indexManager.registerConstructorParameter(SynonymMap.class,
				RealTimeSynonymsResourcesTest.getSynonymMap(RealTimeSynonymsResourcesTest.WHITESPACE_ANALYZER,
						RealTimeSynonymsResourcesTest.EN_FR_DE_SYNONYMS));
		initIndexService();
		indexService.postDocument(new IndexRecord.NoTaxonomy("1").textField("Hello World")
				.stringField("Hello World")
				.textSynonymsField1("hello world"));
		indexService.postDocument(
				new IndexRecord.NoTaxonomy("2").textField("aaaaaa bbbbbb").stringField("aaaaaa bbbbbb"));
	}

	@Test
	public void testWithDefaultAnalyzer() {
		QueryDefinition queryDef;

		queryDef = QueryDefinition.of(
				new MultiFieldQuery(QueryParserOperator.OR, "Hello", 0).field("textField", 1F).field("stringField", 1F))
				.queryDebug(true)
				.build();
		checkQuery(queryDef, 1L, "textField:hello stringField:Hello~2");

		queryDef = QueryDefinition.of(new MultiFieldQuery(QueryParserOperator.AND, "Hello", 0).field("textField", 1F)
				.field("stringField", 1F)).queryDebug(true).build();
		checkQuery(queryDef, 1L, "textField:hello stringField:Hello~2");

		queryDef = QueryDefinition.of(
				new MultiFieldQuery(QueryParserOperator.OR, "Hello world", 0).field("textField", 2F)
						.field("stringField", 1F)).queryDebug(true).build();
		checkQuery(queryDef, 1L, "((textField:hello textField:world)~1)^2.0 stringField:Hello world~2");

		queryDef = QueryDefinition.of(
				new MultiFieldQuery(QueryParserOperator.AND, "Hello world", 0).field("textField", 2F)
						.field("stringField", 1F)).queryDebug(true).build();
		checkQuery(queryDef, 1L, "((textField:hello textField:world)~1)^2.0 stringField:Hello world~2");
	}

	@Test
	public void testWithMinShouldMatch() {
		QueryDefinition queryDef = QueryDefinition.of(
				new MultiFieldQuery(QueryParserOperator.OR, "Hello world aaaaaa", 70).field("textField", 3F)
						.field("stringField", 1F)).queryDebug(true).build();
		checkQuery(queryDef, 1L,
				"((textField:hello textField:world textField:aaaaaa)~2)^3.0 stringField:Hello world aaaaaa~2");
	}

	@Test
	public void testWithFieldOperators() {
		QueryDefinition queryDef = QueryDefinition.of(
				new MultiFieldQuery(QueryParserOperator.OR, "Hello world", null).field("textField", 3F, false,
						QueryParserOperator.AND).field("stringField", 1F)).queryDebug(true).build();
		checkQuery(queryDef, 1L, "(+textField:hello +textField:world)^3.0 stringField:Hello world~2");
	}

	@Test
	public void testWithDisjunction() {
		QueryDefinition queryDef = QueryDefinition.of(
				new MultiFieldQuery(QueryParserOperator.AND, "Hello world", null, 0.1f).field("textField", 3F)
						.field("stringField", 1F)).queryDebug(true).build();
		checkQuery(queryDef, 1L, "((+textField:hello +textField:world)^3.0 | stringField:Hello world~2)~0.1");
	}

	@Test
	public void testWithCustomAnalyzer() {
		QueryDefinition queryDef;
		Analyzer analyzer = new StandardAnalyzer();

		queryDef = QueryDefinition.of(
				new MultiFieldQuery(QueryParserOperator.OR, "Hello", 0, null, analyzer).field("textField", 1F)
						.field("stringField", 1F)).queryDebug(true).build();
		checkQuery(queryDef, 1L, "textField:hello stringField:hello");

		queryDef = QueryDefinition.of(
				new MultiFieldQuery(QueryParserOperator.AND, "Hello", 0, null, analyzer).field("textField", 1F)
						.field("stringField", 1F)).queryDebug(true).build();
		checkQuery(queryDef, 1L, "textField:hello stringField:hello");

		queryDef = QueryDefinition.of(
				new MultiFieldQuery(QueryParserOperator.AND, "Hello zzzzz", 0, null, analyzer).field("textField", 1F)
						.field("stringField", 1F)).queryDebug(true).build();
		checkQuery(queryDef, 1L, "((textField:hello textField:zzzzz~2)~1) ((stringField:hello stringField:zzzzz~2)~1)");
	}

	@Test
	public void luceneQuery() throws IOException, ReflectiveOperationException {
		Query luceneQuery = new MultiFieldQuery(QueryParserOperator.AND, "Hello World", 0).field("textField", 2F)
				.field("stringField", 1F)
				.getQuery(QueryContext.DEFAULT);
		Assert.assertNotNull(luceneQuery);
		Assert.assertEquals(
				"((textField:hello~2 textField:world~2)~1)^2.0 ((stringField:hello~2 stringField:world~2)~1)",
				luceneQuery.toString());
	}

	@Test
	public void testWithGraphSynonymsOperatorOrKeywordsIsOneMultiWordSynonym()
			throws QueryNodeException, ReflectiveOperationException,
			org.apache.lucene.queryparser.classic.ParseException, IOException {
		AbstractQuery query =
				new MultiFieldQuery(QueryParserOperator.OR, "bonjour le monde").field("textSynonymsField1", 1.0F)
						.field("textField", 2.0F)
						.field("stringField", 3.0F);
		checkQuery(QueryDefinition.of(query).queryDebug(true).build());
	}

	@Test
	public void testWithGraphSynonymsOperatorOrKeywordsIsContainsMultiWordSynonym()
			throws QueryNodeException, ReflectiveOperationException,
			org.apache.lucene.queryparser.classic.ParseException, IOException {
		AbstractQuery query =
				new MultiFieldQuery(QueryParserOperator.OR, "hello bonjour le monde").field("textSynonymsField1", 1.0F)
						.field("textField", 2.0F)
						.field("stringField", 3.0F);
		checkQuery(QueryDefinition.of(query).queryDebug(true).build());
	}

	@Test
	public void testWithGraphSynonymsOperatorAndKeywordsIsOneMultiWordSynonym()
			throws QueryNodeException, ReflectiveOperationException,
			org.apache.lucene.queryparser.classic.ParseException, IOException {
		AbstractQuery query =
				new MultiFieldQuery(QueryParserOperator.AND, "bonjour le monde").field("textSynonymsField1", 1.0F)
						.field("textField", 2.0F)
						.field("stringField", 3.0F);
		checkQuery(QueryDefinition.of(query).queryDebug(true).build(), 1L,
				"(+((+textSynonymsField1:hello +textSynonymsField1:world) (+textSynonymsField1:bonjour~2 +textSynonymsField1:le~2 +textSynonymsField1:monde~2))) (+textField:bonjour~2 +textField:le~2 +textField:monde~2)^2.0 (stringField:bonjour le monde~2)^3.0");
	}

	@Test
	public void testWithGraphSynonymsOperatorAndComplexAnalyzer() {
		AbstractQuery query =
				new MultiFieldQuery(QueryParserOperator.AND, "Hello Worlds", 50).field("textSynonymsField1", 1.0F)
						.field("textComplexAnalyzer", 2.0F);
		checkQuery(QueryDefinition.of(query).queryDebug(true).build(), 1L,
				"((textSynonymsField1:Hello~2 textSynonymsField1:Worlds~2)~1) ((((+textComplexAnalyzer:hello +textComplexAnalyzer:world)~1) textComplexAnalyzer:helloworld~2))^2.0");
	}

	@Test
	public void testWithGraphSynonymsOperatorAndKeywordsContainsMultiWordSynonymLast()
			throws QueryNodeException, ReflectiveOperationException,
			org.apache.lucene.queryparser.classic.ParseException, IOException {
		AbstractQuery query = QueryParser.of("textSynonymsField1")
				.setDefaultOperator(QueryParserOperator.AND)
				.setSplitOnWhitespace(false)
				.setQueryString("hello bonjour le monde")
				.build();
		checkQuery(QueryDefinition.of(query).queryDebug(true).build(), 1L,
				"+textSynonymsField1:hello +((+textSynonymsField1:hello +textSynonymsField1:world) (+textSynonymsField1:bonjour +textSynonymsField1:le +textSynonymsField1:monde))");
	}

	@Test
	public void testWithGraphSynonymsOperatorAndKeywordsContainsMultiWordSynonymFirst()
			throws QueryNodeException, ReflectiveOperationException,
			org.apache.lucene.queryparser.classic.ParseException, IOException {
		AbstractQuery query = QueryParser.of("textSynonymsField1")
				.setDefaultOperator(QueryParserOperator.AND)
				.setSplitOnWhitespace(false)
				.setQueryString("bonjour le monde hello")
				.build();
		checkQuery(QueryDefinition.of(query).queryDebug(true).build(), 1L,
				"+((+textSynonymsField1:hello +textSynonymsField1:world) (+textSynonymsField1:bonjour +textSynonymsField1:le +textSynonymsField1:monde)) +textSynonymsField1:hello");
	}

}
