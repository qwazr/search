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

package com.qwazr.search.field;

import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.annotations.Copy;
import com.qwazr.search.annotations.Index;
import com.qwazr.search.annotations.SmartField;
import com.qwazr.search.index.FacetDefinition;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.query.AbstractQuery;
import com.qwazr.search.query.DrillDown;
import com.qwazr.search.query.FacetPath;
import com.qwazr.search.query.MatchAllDocs;
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.utils.LoggerUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

public class SmartFieldFacetTest extends AbstractIndexTest {

    private final static Logger LOGGER = LoggerUtils.getLogger(SmartFieldFacetTest.class);

    private static AnnotatedIndexService<Record> indexService;

    @BeforeClass
    public static void setup() throws IOException, URISyntaxException {
        indexService = initIndexService(Record.class);
        indexService.postDocument(new Record(1, new String[]{"tag1", "tag1and2"}));
        indexService.postDocument(new Record(2, new String[]{"tag2", "tag1and2"}));
    }

    ResultDefinition.WithObject<Record> checkResult(AbstractQuery<?> query, String queryExplain, long... expectedIds) {
        final ResultDefinition.WithObject<Record> result = indexService.searchQuery(QueryDefinition.of(query)
            .returnedField("*")
            .queryDebug(true)
            .facet("tags", FacetDefinition.of().build())
            .build(), Record.class);
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

    @Test
    public void drillDownQueryTest() {
        checkResult(new DrillDown(MatchAllDocs.INSTANCE, false).filter("tags", "tag1"),
            "+*:* #($facets$sdv:ft€tags\u001Ftag1)", 1);
        checkResult(new DrillDown(null, false).filter("tags", "tag2"), "#($facets$sdv:ft€tags\u001Ftag2)", 2);
        checkResult(new DrillDown(MatchAllDocs.INSTANCE, false).filter("tags", "tag1and2"),
            "+*:* #($facets$sdv:ft€tags\u001Ftag1and2)", 1, 2);

        checkResult(new DrillDown(MatchAllDocs.INSTANCE, true).filter("tags", "tag1"),
            "+*:* #($facets$sdv:ft€tags\u001Ftag1)", 1);
        checkResult(new DrillDown(null, true).filter("tags", "tag2"), "#($facets$sdv:ft€tags\u001Ftag2)", 2);
        checkResult(new DrillDown(MatchAllDocs.INSTANCE, true).filter("tags", "tag1and2"),
            "+*:* #($facets$sdv:ft€tags\u001Ftag1and2)", 1, 2);
    }

    @Test
    public void facetPathQueryTest() {
        checkResult(FacetPath.of("tags").path("tag1").build(), "$facets$sdv:ft€tags\u001Ftag1", 1);
        checkResult(FacetPath.of("tags").path("tag1and2").build(), "$facets$sdv:ft€tags\u001Ftag1and2", 1, 2);
    }

    @Test
    public void computeFacetsTest() {
        checkResult(MatchAllDocs.INSTANCE, "*:*", 1, 2);
    }

    @Index(name = "SmartFieldSorted")
    static public class Record {

        @SmartField(name = FieldDefinition.ID_FIELD, type = SmartFieldDefinition.Type.LONG, index = true, stored = true)
        final public long id;

        @SmartField(type = SmartFieldDefinition.Type.TEXT, facet = true, multivalued = true)
        @Copy(to = {@Copy.To(order = 3, field = "full")})
        final public String[] tags;

        Record(long id, String[] tags) {
            this.id = id;
            this.tags = tags;
        }

        public Record() {
            this(0, null);
        }
    }
}
