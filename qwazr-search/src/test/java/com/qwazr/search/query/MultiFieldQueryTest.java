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

import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.search.test.units.IndexRecord;
import com.qwazr.search.test.units.RealTimeSynonymsResourcesTest;
import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.search.Query;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class MultiFieldQueryTest extends AbstractIndexTest.WithIndexRecord.NoTaxonomy {

    @BeforeClass
    public static void setup() throws IOException, URISyntaxException {
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

        queryDef = QueryDefinition.of(MultiFieldQuery.of()
            .defaultOperator(QueryParserOperator.OR)
            .queryString("Hello")
            .minNumberShouldMatch(0)
            .fieldBoost("textField", 1F)
            .fieldBoost("stringField", 1F)
            .enableFuzzyQuery(true)
            .build())
            .returnedField("$id$")
            .queryDebug(true)
            .build();
        checkQuery(queryDef, 1L, "textField:hello stringField:Hello~2", r -> r.id);

        queryDef = QueryDefinition.of(MultiFieldQuery.of()
            .defaultOperator(QueryParserOperator.AND)
            .queryString("Hello")
            .minNumberShouldMatch(0)
            .fieldBoost("textField", 1F)
            .fieldBoost("stringField", 1F)
            .enableFuzzyQuery(true)
            .build())
            .returnedField("$id$")
            .queryDebug(true)
            .build();
        checkQuery(queryDef, 1L, "textField:hello stringField:Hello~2", r -> r.id);

        queryDef = QueryDefinition.of(MultiFieldQuery.of()
            .defaultOperator(QueryParserOperator.OR)
            .queryString("Hello world")
            .minNumberShouldMatch(0)
            .fieldBoost("textField", 2F)
            .fieldBoost("stringField", 1F)
            .enableFuzzyQuery(true)
            .build())
            .returnedField("$id$")
            .queryDebug(true)
            .build();
        checkQuery(queryDef, 1L, "((textField:hello textField:world)~1)^2.0 stringField:Hello world~2", r -> r.id);

        queryDef = QueryDefinition.of(MultiFieldQuery.of()
            .defaultOperator(QueryParserOperator.AND)
            .queryString("Hello world")
            .minNumberShouldMatch(0)
            .fieldBoost("textField", 2F)
            .fieldBoost("stringField", 1F)
            .enableFuzzyQuery(true)
            .build())
            .returnedField("$id$")
            .queryDebug(true)
            .build();
        checkQuery(queryDef, 1L, "((textField:hello textField:world)~1)^2.0 stringField:Hello world~2", r -> r.id);
    }

    @Test
    public void testWithMinShouldMatch() {
        QueryDefinition queryDef = QueryDefinition.of(MultiFieldQuery.of()
            .defaultOperator(QueryParserOperator.OR)
            .queryString("Hello world aaaaaa")
            .minNumberShouldMatch(70)
            .fieldBoost("textField", 3F)
            .fieldBoost("stringField", 1F)
            .enableFuzzyQuery(true)
            .build())
            .queryDebug(true)
            .returnedField("$id$")
            .build();
        checkQuery(queryDef, 1L,
            "((textField:hello textField:world textField:aaaaaa)~2)^3.0 stringField:Hello world aaaaaa~2", r -> r.id);
    }

    @Test
    public void testWithFieldOperators() {
        QueryDefinition queryDef = QueryDefinition.of(MultiFieldQuery.of()
            .defaultOperator(QueryParserOperator.OR)
            .queryString("Hello world")
            .fieldBoost("textField", 3F)
            .fieldAndFilter("textField")
            .fieldDisableGraph("textField")
            .fieldBoost("stringField", 1F)
            .fieldBoost("textSynonymsField1", 2.F)
            .enableFuzzyQuery(true)
            .build())
            .queryDebug(true)
            .returnedField("$id$")
            .build();
        checkQuery(queryDef, 1L,
            "(stringField:Hello world~2 (((+textSynonymsField1:bonjour~2 +textSynonymsField1:le~2 +textSynonymsField1:monde~2) (+textSynonymsField1:hallo~2 +textSynonymsField1:welt~2) (+textSynonymsField1:Hello~2 +textSynonymsField1:world)))^2.0) +(+textField:hello +textField:world)^3.0",
            r -> r.id);
    }

    @Test
    public void testWith2FieldOperators() {
        QueryDefinition queryDef = QueryDefinition.of(MultiFieldQuery.of()
            .defaultOperator(QueryParserOperator.OR)
            .queryString("Hello world")
            .fieldBoost("textField", 3F)
            .fieldAndFilter("textField")
            .fieldBoost("textComplexAnalyzer", 2F)
            .fieldAndFilter("textComplexAnalyzer")
            .fieldDisableGraph("textComplexAnalyzer")
            .fieldBoost("stringField", 1F)
            .fieldBoost("textSynonymsField1", 2.F)
            .enableFuzzyQuery(true)
            .build())
            .returnedField("$id$")
            .queryDebug(true)
            .build();
        checkQuery(queryDef, 1L,
            "(stringField:Hello world~2 (((+textSynonymsField1:bonjour~2 +textSynonymsField1:le~2 +textSynonymsField1:monde~2) (+textSynonymsField1:hallo~2 +textSynonymsField1:welt~2) (+textSynonymsField1:Hello~2 +textSynonymsField1:world)))^2.0) +((+textField:hello +textField:world)^3.0 (+Synonym(textComplexAnalyzer:hello textComplexAnalyzer:helloworld) +textComplexAnalyzer:world)^2.0)",
            r -> r.id);
    }

    @Test
    public void testWithDisjunction() {
        QueryDefinition queryDef = QueryDefinition.of(MultiFieldQuery.of()
            .defaultOperator(QueryParserOperator.AND)
            .queryString("Hello world")
            .tieBreakerMultiplier(0.1f)
            .fieldBoost("textField", 3F)
            .fieldBoost("stringField", 1F)
            .enableFuzzyQuery(true)
            .build())
            .returnedField("$id$")
            .queryDebug(true).build();
        checkQuery(queryDef, 1L, "((+textField:hello +textField:world)^3.0 | stringField:Hello world~2)~0.1", r -> r.id);
    }

    @Test
    public void testWithDisjunctionAndFieldOperators() {
        QueryDefinition queryDef = QueryDefinition.of(MultiFieldQuery.of()
            .defaultOperator(QueryParserOperator.OR)
            .queryString("Hello world")
            .tieBreakerMultiplier(0.1f)
            .fieldBoost("textField", 3F)
            .fieldAndFilter("textField")
            .fieldBoost("stringField", 1F)
            .fieldBoost("textSynonymsField1", 2.F)
            .enableFuzzyQuery(true)
            .build())
            .queryDebug(true)
            .returnedField("$id$")
            .build();
        checkQuery(queryDef, 1L,
            "(stringField:Hello world~2 | (((+textSynonymsField1:bonjour~2 +textSynonymsField1:le~2 +textSynonymsField1:monde~2) (+textSynonymsField1:hallo~2 +textSynonymsField1:welt~2) (+textSynonymsField1:Hello~2 +textSynonymsField1:world)))^2.0)~0.1 +(+textField:hello +textField:world)^3.0",
            r -> r.id);
    }

    @Test
    public void testWithCustomAnalyzer() {
        QueryDefinition queryDef;
        Analyzer analyzer = new StandardAnalyzer();

        queryDef = QueryDefinition.of(MultiFieldQuery.of()
            .defaultOperator(QueryParserOperator.OR)
            .queryString("Hello")
            .minNumberShouldMatch(0)
            .analyzer(analyzer)
            .fieldBoost("textField", 1F)
            .fieldBoost("stringField", 1F)
            .build())
            .queryDebug(true)
            .returnedField("$id$")
            .build();
        checkQuery(queryDef, 1L, "textField:hello stringField:hello", r -> r.id);

        queryDef = QueryDefinition.of(MultiFieldQuery.of()
            .defaultOperator(QueryParserOperator.AND)
            .queryString("Hello")
            .minNumberShouldMatch(0)
            .analyzer(analyzer)
            .fieldBoost("textField", 1F)
            .fieldBoost("stringField", 1F)
            .build())
            .queryDebug(true)
            .returnedField("$id$")
            .build();
        checkQuery(queryDef, 1L, "textField:hello stringField:hello", r -> r.id);

        queryDef = QueryDefinition.of(MultiFieldQuery.of()
            .defaultOperator(QueryParserOperator.AND)
            .queryString("Hello zzzzz")
            .minNumberShouldMatch(0)
            .analyzer(analyzer)
            .fieldBoost("textField", 1F)
            .fieldBoost("stringField", 1F)
            .enableFuzzyQuery(true)
            .build())
            .queryDebug(true)
            .returnedField("$id$")
            .build();
        checkQuery(queryDef, 1L, "((textField:hello textField:zzzzz~2)~1) ((stringField:hello stringField:zzzzz~2)~1)",
            r -> r.id);
    }

    @Test
    public void luceneQuery() throws IOException {
        Query luceneQuery = MultiFieldQuery.of()
            .defaultOperator(QueryParserOperator.AND)
            .queryString("Hello World")
            .minNumberShouldMatch(0)
            .fieldBoost("textField", 2F)
            .fieldBoost("stringField", 1F)
            .enableFuzzyQuery(true)
            .build()
            .getQuery(QueryContextTest.DEFAULT);
        Assert.assertNotNull(luceneQuery);
        Assert.assertEquals(
            "(tt€textField:Hello World~2)^2.0 tt€stringField:Hello World~2",
            luceneQuery.toString());
    }

    @Test
    public void testWithGraphSynonymsOperatorOrKeywordsIsOneMultiWordSynonym() {
        AbstractQuery<?> query = MultiFieldQuery.of()
            .defaultOperator(QueryParserOperator.OR)
            .queryString("bonjour le monde")
            .fieldBoost("textSynonymsField1", 1.0F)
            .fieldBoost("textField", 2.0F)
            .fieldBoost("stringField", 3.0F)
            .build();
        checkQuery(
            QueryDefinition.of(query).returnedField("$id$").queryDebug(true).build(),
            r -> r.id
        );
    }

    @Test
    public void testWithGraphSynonymsOperatorOrKeywordsIsContainsMultiWordSynonym() {
        AbstractQuery<?> query = MultiFieldQuery.of()
            .defaultOperator(QueryParserOperator.OR)
            .queryString("hello bonjour le monde")
            .fieldBoost("textSynonymsField1", 1.0F)
            .fieldBoost("textField", 2.0F)
            .fieldBoost("stringField", 3.0F)
            .build();
        checkQuery(
            QueryDefinition.of(query).returnedField("$id$").queryDebug(true).build(), r -> r.id
        );
    }

    @Test
    public void testWithGraphSynonymsOperatorAndKeywordsIsOneMultiWordSynonym() {
        AbstractQuery<?> query = MultiFieldQuery.of()
            .defaultOperator(QueryParserOperator.AND)
            .queryString("bonjour le monde")
            .fieldBoost("textSynonymsField1", 1.0F)
            .fieldBoost("textField", 2.0F)
            .fieldBoost("stringField", 3.0F)
            .enableFuzzyQuery(true)
            .build();
        checkQuery(
            QueryDefinition.of(query).returnedField("$id$").queryDebug(true).build(), 1L,
            "(+((+textSynonymsField1:hello +textSynonymsField1:world) (+textSynonymsField1:bonjour~2 +textSynonymsField1:le~2 +textSynonymsField1:monde~2))) (+textField:bonjour~2 +textField:le~2 +textField:monde~2)^2.0 (stringField:bonjour le monde~2)^3.0"
            , r -> r.id);
    }

    @Test
    public void testWithGraphSynonymsOperatorAndComplexAnalyzer() {
        AbstractQuery<?> query = MultiFieldQuery.of()
            .defaultOperator(QueryParserOperator.AND)
            .queryString("Hello Worlds")
            .minNumberShouldMatch(50)
            .fieldBoost("textSynonymsField1", 1.0F)
            .fieldBoost("textComplexAnalyzer", 2.0F)
            .enableFuzzyQuery(true)
            .build();
        checkQuery(
            QueryDefinition.of(query).returnedField("$id$").queryDebug(true).build(), 1L,
            "((textSynonymsField1:Hello~2 textSynonymsField1:Worlds~2)~1) ((((+textComplexAnalyzer:hello +textComplexAnalyzer:world)~1) textComplexAnalyzer:helloworld~2))^2.0",
            r -> r.id);
    }

    @Test
    public void testWithGraphSynonymsOperatorAndKeywordsContainsMultiWordSynonymLast() {
        AbstractQuery<?> query = QueryParser.of("textSynonymsField1")
            .setDefaultOperator(QueryParserOperator.AND)
            .setSplitOnWhitespace(false)
            .setQueryString("hello bonjour le monde")
            .build();
        checkQuery(
            QueryDefinition.of(query).returnedField("$id$").queryDebug(true).build(), 1L,
            "+textSynonymsField1:hello +((+textSynonymsField1:hello +textSynonymsField1:world) (+textSynonymsField1:bonjour +textSynonymsField1:le +textSynonymsField1:monde))",
            r -> r.id
        );
    }

    @Test
    public void testWithGraphSynonymsOperatorAndKeywordsContainsMultiWordSynonymFirst() {
        AbstractQuery<?> query = QueryParser.of("textSynonymsField1")
            .setDefaultOperator(QueryParserOperator.AND)
            .setSplitOnWhitespace(false)
            .setQueryString("bonjour le monde hello")
            .build();
        checkQuery(
            QueryDefinition.of(query).returnedField("$id$").queryDebug(true).build(), 1L,
            "+((+textSynonymsField1:hello +textSynonymsField1:world) (+textSynonymsField1:bonjour +textSynonymsField1:le +textSynonymsField1:monde)) +textSynonymsField1:hello",
            r -> r.id
        );
    }

    @Test
    public void testWithQueryWithoutToken() {
        AbstractQuery<?> query = MultiFieldQuery.of()
            .defaultOperator(QueryParserOperator.AND)
            .queryString(" & ")
            .minNumberShouldMatch(50)
            .fieldBoost("textField", 1.0F)
            .fieldBoost("stringField", 2.0F)
            .enableFuzzyQuery(true)
            .build();
        checkQuery(
            QueryDefinition.of(query).returnedField("$id$").queryDebug(true).build(),
            0L, "(stringField: & ~2)^2.0", r -> r.id);
    }

    @Test
    public void testWithQueryWithBlankQueryString() {
        AbstractQuery<?> query = MultiFieldQuery.of()
            .defaultOperator(QueryParserOperator.AND)
            .queryString(" ")
            .minNumberShouldMatch(50)
            .fieldBoost("textField", 1.0F)
            .fieldBoost("textComplexAnalyzer", 2.0F)
            .build();
        checkQuery(
            QueryDefinition.of(query).returnedField("$id$").queryDebug(true).build(), 0L, "",
            r -> r.id
        );
    }

}
