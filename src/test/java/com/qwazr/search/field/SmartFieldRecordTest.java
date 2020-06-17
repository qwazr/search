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

import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.test.units.AbstractIndexTest;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class SmartFieldRecordTest extends AbstractIndexTest {

    private static AnnotatedIndexService<SmartFieldRecord> indexService;

    private final static SmartFieldRecord sample = SmartFieldRecord.random();

    @BeforeClass
    public static void setup() throws URISyntaxException, IOException {
        indexService = initIndexService(SmartFieldRecord.class);
        indexService.postDocument(sample);
    }

    @Test
    public void getRecord() throws IOException, ReflectiveOperationException {
        final SmartFieldRecord doc = indexService.getDocument(sample.id);
        assertThat(doc, notNullValue());
        assertThat(doc, equalTo(sample));
    }
}
