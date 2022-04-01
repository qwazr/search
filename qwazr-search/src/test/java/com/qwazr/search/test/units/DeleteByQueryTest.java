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

import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.query.TermsQuery;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class DeleteByQueryTest extends AbstractIndexTest.WithIndexRecord.NoTaxonomy {

    @BeforeClass
    public static void setup() throws URISyntaxException {
        initIndexService();
    }

    @Test
    public void test() throws IOException {
        indexService.postDocument(getNewRecord("1").textField("Hello World").stringField("Hello World"));
        indexService.postDocument(getNewRecord("2").textField("Hello World 2").stringField("Hello World 2"));
        Assert.assertEquals(Long.valueOf(2), indexService.getIndexStatus().numDocs);
        QueryDefinition queryDef =
            QueryDefinition.of(TermsQuery.of(null, FieldDefinition.ID_FIELD).add("1", "2").build()).build();
        indexService.deleteByQuery(queryDef);
        Assert.assertEquals(Long.valueOf(0), indexService.getIndexStatus().numDocs);
    }

}
