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
import com.qwazr.utils.HashUtils;
import com.qwazr.utils.RandomUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SmartFieldSourceTest extends AbstractIndexTest {

    private static AnnotatedIndexService<Record> indexService;

    private final static Record sample = new Record(
        HashUtils.newTimeBasedUUID().toString(),
        System.currentTimeMillis(),
        RandomUtils.nextInt(0, 10), RandomUtils.alphanumeric(10));

    @BeforeClass
    public static void setup() throws URISyntaxException, IOException {
        indexService = initIndexService(Record.class);
        indexService.postDocument(sample);
    }

    @Test
    public void getSource() throws IOException, ReflectiveOperationException {
        final Record doc = indexService.getDocument(sample.id);
        assertThat(doc, notNullValue());
        assertThat(doc, equalTo(sample));
    }

    @Index(name = "SmartFieldSorted", schema = "TestQueries", primaryKey = "id",
        sourceField = FieldDefinition.SOURCE_FIELD)
    static public class Record implements Serializable {

        @SmartField(type = SmartFieldDefinition.Type.TEXT, index = true)
        final public String id;

        @SmartField(type = SmartFieldDefinition.Type.LONG, sort = true)
        final public Long longSort;

        @SmartField(type = SmartFieldDefinition.Type.INTEGER, facet = true)
        final public Integer intFacet;

        @SmartField(type = SmartFieldDefinition.Type.TEXT, index = true, analyzer = SmartAnalyzerSet.english)
        final public String text;

        public Record() {
            this(null, null, null, null);
        }

        private Record(String id, Long longSort, Integer intFacet, String text) {
            this.id = id;
            this.longSort = longSort;
            this.intFacet = intFacet;
            this.text = text;
        }

        @Override
        public boolean equals(final Object other) {
            if (!(other instanceof Record))
                return false;
            final Record o = (Record) other;
            return Objects.equals(id, o.id)
                && Objects.equals(longSort, o.longSort)
                && Objects.equals(intFacet, o.intFacet)
                && Objects.equals(text, o.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, longSort, intFacet, text);
        }
    }
}
