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
import com.qwazr.search.annotations.Index;
import com.qwazr.search.annotations.SmartField;
import com.qwazr.search.test.units.AbstractIndexTest;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

public class SmartFieldSamplesTest extends AbstractIndexTest {

    private static AnnotatedIndexService<Record> indexService;

    @BeforeClass
    public static void setup() throws URISyntaxException {
        indexService = initIndexService(Record.class);
    }

    @Test
    public void getJsonSampleTest() {
        final Map<String, Object> sample = indexService.getJsonSample();
        assertThat(sample, notNullValue());
        assertThat(sample.keySet(), hasSize(4));
    }

    @Test
    public void getJsonSamplesTest() {
        final List<Map<String, Object>> samples = indexService.getJsonSamples(null);
        assertThat(samples, notNullValue());
        assertThat(samples, hasSize(2));
        for (int i = 0; i < 2; i++) {
            final Map<String, Object> sample = samples.get(i);
            assertThat(sample.keySet(), hasSize(4));
            assertThat(sample.keySet(), hasItem("id"));
            assertThat(sample.get("id"), equalTo("id" + i));
        }
    }

    @Index(name = "SmartFieldSorted", primaryKey = "id")
    static public class Record {

        @SmartField(type = SmartFieldDefinition.Type.TEXT, index = true)
        final public String id = null;

        @SmartField(type = SmartFieldDefinition.Type.LONG, sort = true, stored = true)
        final public Long longSort = null;

        @SmartField(type = SmartFieldDefinition.Type.INTEGER, facet = true)
        final public Integer intFacet = null;

        @SmartField(type = SmartFieldDefinition.Type.TEXT, analyzer = SmartAnalyzerSet.english)
        final public String text = null;

        public Record() {
        }
    }
}
