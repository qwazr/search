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

import com.qwazr.search.field.CustomFieldDefinition;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.QueryBuilder;
import com.qwazr.search.index.QueryContext;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.query.AbstractQuery;
import com.qwazr.search.query.Bool;
import com.qwazr.search.query.ExactInteger;
import com.qwazr.search.query.MatchAllDocs;
import com.qwazr.search.query.QueryContextTest;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.junit.Assert;
import org.junit.Test;

public class IntQueryFilterTest {

    @Test
    public void shouldMakeFilterOnIntPoint() throws ReflectiveOperationException, IOException, ParseException, QueryNodeException {

        final QueryContext queryContext = QueryContextTest.of(
            Map.of(
                "intField",
                CustomFieldDefinition.of().template(FieldDefinition.Template.IntPoint).build())
        );

        final AbstractQuery<?> query = new Bool(
            List.of(
                new Bool.Clause(Bool.Occur.filter, new ExactInteger("intField", 1)),
                new Bool.Clause(Bool.Occur.must, MatchAllDocs.INSTANCE)
            ));

        final QueryDefinition qd = new QueryBuilder().query(query).build();

        Assert.assertEquals(qd.getQuery().getQuery(queryContext).toString(),
            "#" + "intField" + ":[1 TO 1] +*:*");
    }

}
