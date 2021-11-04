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

package com.qwazr.search.field;

import com.qwazr.search.analysis.SmartAnalyzerSet;
import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.annotations.Copy;
import com.qwazr.search.annotations.Index;
import com.qwazr.search.annotations.SmartField;
import com.qwazr.search.index.HighlighterDefinition;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.query.AbstractQuery;
import com.qwazr.search.query.MultiFieldQueryParser;
import com.qwazr.search.query.QueryParser;
import com.qwazr.search.query.QueryParserOperator;
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.utils.LoggerUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Logger;

public class SmartFieldFullTextTest extends AbstractIndexTest {

    private final static Logger LOGGER = LoggerUtils.getLogger(SmartFieldFullTextTest.class);

    private static AnnotatedIndexService<Record> indexService;

    @BeforeClass
    public static void setup() throws IOException, URISyntaxException {
        indexService = initIndexService(Record.class);
        indexService.postDocument(new Record(1, "First news", new String[]{"First sentence", "Second sentence"},
            new String[]{"tag1", "tag1and2"}));
        indexService.postDocument(new Record(2, "Second article", new String[]{"Third sentence", "Fourth sentence"},
            new String[]{"tag2", "tag1and2"}));
        indexService.postDocument(new Record(3, "file.ext", null, null));
    }

    ResultDefinition.WithObject<Record> checkResult(AbstractQuery<?> query, String queryExplain, long... expectedIds) {
        final ResultDefinition.WithObject<Record> result =
            indexService.searchQuery(QueryDefinition.of(query).returnedField("*").queryDebug(true).build(),
                Record.class);
        if (queryExplain != null)
            Assert.assertEquals(queryExplain, result.query);
        else
            LOGGER.info(result.query);
        if (expectedIds == null)
            Assert.assertEquals(0, result.totalHits, 0);
        else
            Assert.assertEquals(expectedIds.length, result.totalHits, 0);
        return result;
    }

    void fullSearch(String queryString, String queryExplain, long... expectedIds) {
        checkResult(
            QueryParser.of("full").setDefaultOperator(QueryParserOperator.AND).setQueryString(queryString).build(),
            queryExplain, expectedIds);
    }

    ResultDefinition.WithObject<Record> multiSearch(String queryString, String queryExplain, long... expectedIds) {
        return checkResult(MultiFieldQueryParser.of()
            .setDefaultOperator(QueryParserOperator.AND)
            .addField("title", "content", "tags")
            .setQueryString(queryString)
            .build(), queryExplain, expectedIds);
    }

    @Test
    public void searchTitleTest() {
        multiSearch("first news",
            "+(tt€title:first tt€content:first tt€tags:first) +(tt€title:new tt€content:news tt€tags:news)", 1);
        fullSearch("first news", "+tt€full:first +tt€full:news", 1);
        multiSearch("second article",
            "+(tt€title:second tt€content:second tt€tags:second) +(tt€title:articl tt€content:article tt€tags:article)",
            2);
        fullSearch("second article", "+tt€full:second +tt€full:article", 2);
    }

    @Test
    public void searchContentTest() {
        multiSearch("first sentence",
            "+(tt€title:first tt€content:first tt€tags:first) +(tt€title:sentenc tt€content:sentence tt€tags:sentence)",
            1);
        fullSearch("first sentence", "+tt€full:first +tt€full:sentence", 1);
        multiSearch("third sentence",
            "+(tt€title:third tt€content:third tt€tags:third) +(tt€title:sentenc tt€content:sentence tt€tags:sentence)",
            2);
        fullSearch("third sentence", "+tt€full:third +tt€full:sentence", 2);
    }

    @Test
    public void searchManyTest() {
        multiSearch("sentence", "tt€title:sentenc tt€content:sentence tt€tags:sentence", 1, 2);
        fullSearch("sentence", "tt€full:sentence", 1, 2);
    }

    @Test
    public void searchCrossFields() {
        multiSearch("news sentence",
            "+(tt€title:new tt€content:news tt€tags:news) +(tt€title:sentenc tt€content:sentence tt€tags:sentence)",
            1);
        fullSearch("news sentence", "+tt€full:news +tt€full:sentence", 1);
        multiSearch("article sentence",
            "+(tt€title:articl tt€content:article tt€tags:article) +(tt€title:sentenc tt€content:sentence tt€tags:sentence)",
            2);
        fullSearch("article sentence", "+tt€full:article +tt€full:sentence", 2);
    }

    @Test
    public void checkWordDelimiter() {
        fullSearch("file.ext", "+tt€full:file +tt€full:ext", 3);
        fullSearch("file", "tt€full:file", 3);
        fullSearch("ext", "tt€full:ext", 3);
    }

    @Test
    public void searchHighlights() {
        final ResultDefinition.WithObject<Record> result = indexService.searchQuery(
            QueryDefinition.of(QueryParser.of("title").setQueryString("article").build())
                .returnedField("*")
                .highlighter("title", HighlighterDefinition.of("title").build())
                .queryDebug(true)
                .build(), Record.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.totalHits, 1L);
        Assert.assertNotNull(result.getDocuments().get(0).highlights);
        Assert.assertTrue(result.getDocuments().get(0).highlights.containsKey("title"));
        Assert.assertEquals(result.getDocuments().get(0).highlights.get("title"), "Second <b>article</b>");

    }

    @Index(name = "SmartFieldSorted")
    static public class Record {

        @SmartField(name = FieldDefinition.ID_FIELD, type = SmartFieldDefinition.Type.LONG, index = true, stored = true)
        final public long id;

        @SmartField(type = SmartFieldDefinition.Type.TEXT,
            index = true,
            analyzer = SmartAnalyzerSet.english,
            stored = true)
        @Copy(to = {@Copy.To(order = 1, field = "full"), @Copy.To(order = 1, field = "autocomplete")})
        final public String title;

        @SmartField(type = SmartFieldDefinition.Type.TEXT,
            index = true,
            analyzerClass = StandardAnalyzer.class,
            stored = true)
        @Copy(to = {@Copy.To(order = 2, field = "full")})
        final public String[] content;

        @SmartField(type = SmartFieldDefinition.Type.TEXT,
            index = true,
            analyzerClass = SmartAnalyzerSet.AsciiIndex.class,
            queryAnalyzerClass = SmartAnalyzerSet.AsciiIndex.class)
        @Copy(to = {@Copy.To(order = 3, field = "full")})
        final public String[] tags;

        @SmartField(type = SmartFieldDefinition.Type.TEXT, index = true)
        final public String nonFullTextTitle;

        @SmartField(type = SmartFieldDefinition.Type.TEXT,
            index = true,
            indexAnalyzerClass = SmartAnalyzerSet.AsciiIndex.class,
            queryAnalyzerClass = SmartAnalyzerSet.AsciiQuery.class)
        final public List<String> full;

        Record(long id, String title, String[] content, String[] tags) {
            this.id = id;
            this.title = this.nonFullTextTitle = title;
            this.content = content;
            this.tags = tags;
            this.full = null;
        }

        public Record() {
            this(0, null, null, null);
        }
    }
}
