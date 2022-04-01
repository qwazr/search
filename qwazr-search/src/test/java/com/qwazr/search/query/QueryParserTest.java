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
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class QueryParserTest extends AbstractIndexTest.WithIndexRecord.NoTaxonomy {

    @BeforeClass
    public static void setup() throws IOException, URISyntaxException {
        initIndexManager();
        indexManager.registerConstructorParameter(SynonymMap.class,
            RealTimeSynonymsResourcesTest.getSynonymMap(RealTimeSynonymsResourcesTest.WHITESPACE_ANALYZER,
                RealTimeSynonymsResourcesTest.EN_FR_DE_SYNONYMS));
        initIndexService();
        indexService.postDocument(
            new IndexRecord.NoTaxonomy("1").textField("Hello world").textSynonymsField1("hello world"));
    }

    @Test
    public void testWithDefaultAnalyzer() {
        QueryDefinition queryDef = QueryDefinition.of(
            QueryParser.of("textField").setDefaultOperator(QueryParserOperator.AND).setQueryString("Hello").build())
            .returnedField("$id$").build();
        checkQuery(queryDef, r -> r.id);
    }

    @Test
    public void testWithCustomAnalyzer() {
        QueryDefinition queryDef = QueryDefinition.of(QueryParser.of("textField")
            .setDefaultOperator(QueryParserOperator.AND)
            .setQueryString("hello World")
            .setAnalyzer(StandardAnalyzer.class.getName())
            .build())
            .returnedField("$id$")
            .build();
        checkQuery(queryDef, r -> r.id);
    }

    @Test
    public void luceneQuery() throws ParseException {
        Query luceneQuery = QueryParser.of("textField")
            .setDefaultOperator(QueryParserOperator.AND)
            .setQueryString("Hello World")
            .build()
            .getQuery(QueryContextTest.DEFAULT);
        Assert.assertNotNull(luceneQuery);
    }

    @Test
    public void testWithGraphSynonymsOperatorOrKeywordsIsOneMultiWordSynonym() {
        AbstractQuery<?> query = QueryParser.of("textSynonymsField1")
            .setDefaultOperator(QueryParserOperator.OR)
            .setSplitOnWhitespace(false)
            .setQueryString("bonjour le monde")
            .build();
        checkQuery(QueryDefinition.of(query).returnedField("$id$").queryDebug(true).build(), r -> r.id);
    }

    @Test
    public void testWithGraphSynonymsOperatorOrKeywordsIsContainsMultiWordSynonym() {
        AbstractQuery<?> query = QueryParser.of("textSynonymsField1")
            .setDefaultOperator(QueryParserOperator.OR)
            .setSplitOnWhitespace(false)
            .setQueryString("hello bonjour le monde")
            .build();
        checkQuery(QueryDefinition.of(query).returnedField("$id$").queryDebug(true).build(), r -> r.id);
    }

    @Test
    public void testWithGraphSynonymsOperatorAndKeywordsIsOneMultiWordSynonym() {
        AbstractQuery<?> query = QueryParser.of("textSynonymsField1")
            .setDefaultOperator(QueryParserOperator.AND)
            .setSplitOnWhitespace(false)
            .setQueryString("bonjour le monde")
            .build();
        checkQuery(QueryDefinition.of(query).returnedField("$id$").queryDebug(true).build(), r -> r.id);
    }

    @Test
    public void testWithGraphSynonymsOperatorAndKeywordsContainsMultiWordSynonymLast() {
        AbstractQuery<?> query = QueryParser.of("textSynonymsField1")
            .setDefaultOperator(QueryParserOperator.AND)
            .setSplitOnWhitespace(false)
            .setQueryString("hello bonjour le monde")
            .build();
        checkQuery(QueryDefinition.of(query).returnedField("$id$").queryDebug(true).build(), r -> r.id);
    }

    @Test
    public void testWithGraphSynonymsOperatorAndKeywordsContainsMultiWordSynonymFirst() {
        AbstractQuery<?> query = QueryParser.of("textSynonymsField1")
            .setDefaultOperator(QueryParserOperator.AND)
            .setSplitOnWhitespace(false)
            .setQueryString("bonjour le monde hello")
            .build();
        checkQuery(QueryDefinition.of(query).returnedField("$id$").queryDebug(true).build(), r -> r.id);
    }

}
