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

package com.qwazr.search.index;

import com.qwazr.search.query.SimpleQueryParser;
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.search.test.units.IndexRecord;
import com.qwazr.utils.StringUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class HighlightingTest extends AbstractIndexTest.WithIndexRecord.NoTaxonomy {

    private final static String text1 =
        "OpenSearchServer is an open-source application server allowing development of index-based applications such as search engines. Available since April 2009 on SourceForge for download, OpenSearchServer was developed under the GPL v3 license and offers a series of full text lexical analyzers. It can be installed on different platforms (Windows, Linux, Macintosh).\n" +
            "While it started as an in-house project by a private media group, OpenSearchServer is now supported by Jaeksoft, a commercial company launched in February 2010. Jaeksoft provides services and roadmap guidance for OpenSearchServer.";
    private final static String text2 =
            "The main features of OpenSearchServer are : An integrated crawler for databases, web pages and rich documents; a user-friendly GUI allowing development of most applications through a web page interface built in Zkoss; snippets; faceting; an HTML renderer for integrating search results in a page; and monitoring and administration features.\n" +
            "OpenSearchServer is written in Java and it can be integrated into almost any kind of application without the need to produce Java code. REST/XML APIs make OpenSearchServer connectable to other programming languages. The \"advanced plugins\" capability allows sophisticated customizations.";

    @BeforeClass
    public static void setup() throws IOException, URISyntaxException {
        initIndexManager();
        initIndexService();
        indexService.postDocument(new IndexRecord.NoTaxonomy("1").textField(text1).storedField(text1));
        indexService.postDocument(new IndexRecord.NoTaxonomy("2").textField(text2).storedField(text2));
    }

    @Test
    public void highlightingTest1Doc() {
        ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result;
        result = indexService.searchQuery(QueryDefinition.of(
            SimpleQueryParser.of().addField("textField").setQueryString("integrated crawler").build())
            .highlighter("textField", HighlighterDefinition.of()
                .withStoredField("storedField")
                .withMaxPassages(5)
                .withMaxNoHighlightPassages(5)
                .withBreak(HighlighterDefinition.BreakIteratorDefinition.Type.sentence, "en")
                .build())
            .build());

        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.totalHits);
        final String textField = result.getDocuments().get(0).getHighlights().get("textField");
        Assert.assertNotNull(textField);
        Assert.assertTrue(textField.contains("<b>integrated</b>"));
        Assert.assertTrue(textField.contains("<b>crawler</b>"));
    }

    @Test
    public void highlightingTest2Docs() {
        ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result;
        result = indexService.searchQuery(QueryDefinition.of(
            SimpleQueryParser.of().addField("textField").setQueryString("opensearchserver").build())
            .highlighter("textField", HighlighterDefinition.of()
                .withStoredField("storedField")
                .withMaxPassages(5)
                .withDefaultAnalyzer("standard")
                .withMaxNoHighlightPassages(5)
                .withBreak(HighlighterDefinition.BreakIteratorDefinition.Type.sentence, "en")
                .build())
            .build());

        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.totalHits);

        {
            final String textField = result.getDocuments().get(0).getHighlights().get("textField");
            Assert.assertNotNull(textField);
            Assert.assertEquals(4, StringUtils.countMatches(textField, "<b>OpenSearchServer</b>"));
        }

        {
            final String textField = result.getDocuments().get(1).getHighlights().get("textField");
            Assert.assertNotNull(textField);
            Assert.assertEquals(3, StringUtils.countMatches(textField, "<b>OpenSearchServer</b>"));
        }
    }

}
