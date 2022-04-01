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
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.query.Phrase;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class FieldCopyToTest extends AbstractIndexTest.WithIndexRecord.NoTaxonomy {

    @BeforeClass
    public static void setup() throws IOException, URISyntaxException {
        initIndexService();
        indexService.postDocument(
            new IndexRecord.NoTaxonomy("1").copyText2("Boosted Text 1").copyText1("Copied Text 2"));
    }

    @Test
    public void testBoostedText() {
        ResultDefinition<?> result = indexService.searchQuery(
            QueryDefinition.of(new Phrase("textField", 1, "boosted", "text", "1")).build());
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.totalHits);
    }

    @Test
    public void testCopiedText() {
        ResultDefinition<?> result = indexService.searchQuery(
            QueryDefinition.of(new Phrase("textField", 1, "copied", "text", "2")).build());
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.totalHits);
    }
}
