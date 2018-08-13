/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class MaxQueryStartTest extends AbstractIndexTest.WithIndexRecord.NoTaxonomy {

    @BeforeClass
    public static void setup() throws IOException, URISyntaxException, InterruptedException {
        initIndexManager();
        initIndexService();

        indexService.postDocument(new IndexRecord.NoTaxonomy("1").textField(RandomStringUtils.randomAlphanumeric(1000)));
    }

    @Test
    public void maxQueryStartValueTest() {
        final ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result;
        result = indexService.searchQuery(QueryDefinition.of(
                SimpleQueryParser.of().addField("textField").setQueryString("test").build())
                .start(500_000_000).build());
        Assert.assertNotNull(result);
    }
}
