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
package com.qwazr.search.query;

import com.qwazr.search.field.SmartFieldDefinition;
import com.qwazr.search.test.units.AbstractIndexTest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class DrillDownQueryTest extends AbstractIndexTest.WithIndexRecord.WithTaxonomy {

    @BeforeClass
    public static void setup() throws URISyntaxException {
        initIndexService();
    }

    @Test
    public void luceneQuery() throws ReflectiveOperationException, QueryNodeException, ParseException, IOException {
        final Query luceneQuery =
            new DrillDown(MatchAllDocs.INSTANCE, true)
                .filter("dim", "value")
                .getQuery(QueryContextTest.of(Map.of("dim", SmartFieldDefinition.of().facet(true).build())));
        Assert.assertNotNull(luceneQuery);
    }

}
