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
package com.qwazr.search.test.units;

import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.SynonymMapBuilder;
import com.qwazr.search.query.MultiFieldQueryParser;
import com.qwazr.search.query.Phrase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.net.URISyntaxException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RealTimeSynonymsResourcesTest extends AbstractIndexTest.WithIndexRecord.NoTaxonomy {

    public final static String[] EN_FR_SYNONYMS = new String[]{"hello world", "bonjour le monde"};
    public final static String[] EN_FR_DE_SYNONYMS = new String[]{"hello world", "bonjour le monde", "hallo welt"};

    public final static Analyzer WHITESPACE_ANALYZER = new WhitespaceAnalyzer();

    @BeforeClass
    public static void setup() throws IOException, InterruptedException, URISyntaxException {
        initIndexManager();
        indexManager.registerConstructorParameter(SynonymMap.class, getSynonymMap(WHITESPACE_ANALYZER, EN_FR_SYNONYMS));
        initIndexService();
        indexService.postDocument(new IndexRecord.NoTaxonomy("1").textSynonymsField1("hello world"));
    }

    public static SynonymMap getSynonymMap(Analyzer analyzer, String[]... synonymsList) throws IOException {
        final SynonymMapBuilder builder = new SynonymMapBuilder(analyzer, true, true);
        for (String[] synonyms : synonymsList)
            builder.add(true, synonyms);
        return builder.build();
    }

    @Test
    public void test001_check_en_fr() {
        final MultiFieldQueryParser.Builder builder =
            MultiFieldQueryParser.of().addField("textSynonymsField1").setSplitOnWhitespace(false);

        Assert.assertEquals(1, indexService.searchQuery(
            QueryDefinition.of(builder.setQueryString("hello world").build()).build()).totalHits);
        Assert.assertEquals(1, indexService.searchQuery(
            QueryDefinition.of(builder.setQueryString("bonjour le monde").build()).build()).totalHits);
        Assert.assertEquals(0, indexService.searchQuery(
            QueryDefinition.of(new Phrase("textSynonymsField1", 1, "hallo", "welt")).build()).totalHits);
    }

    @Test
    public void test002_updateSynonymMap() throws IOException {
        indexManager.registerConstructorParameter(SynonymMap.class,
            getSynonymMap(WHITESPACE_ANALYZER, EN_FR_DE_SYNONYMS));
        indexService.refreshAnalyzers();
    }

    @Test
    public void test003_check_en_fr_de() {
        final MultiFieldQueryParser.Builder builder =
            MultiFieldQueryParser.of().addField("textSynonymsField1").setSplitOnWhitespace(false);

        Assert.assertEquals(1, indexService.searchQuery(
            QueryDefinition.of(builder.setQueryString("hello world").build()).build()).totalHits);
        Assert.assertEquals(1, indexService.searchQuery(
            QueryDefinition.of(builder.setQueryString("bonjour le monde").build()).build()).totalHits);
        Assert.assertEquals(1, indexService.searchQuery(
            QueryDefinition.of(builder.setQueryString("hallo welt").build()).build()).totalHits);
    }

}
