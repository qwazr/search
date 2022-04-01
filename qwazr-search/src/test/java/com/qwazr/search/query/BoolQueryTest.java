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

import com.qwazr.search.JsonHelpers;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.function.Consumer;

public class BoolQueryTest implements JsonHelpers {

    private Bool check(Integer mmsm, int size, Bool.Builder builder) {
        builder.setMinimumNumberShouldMatch(mmsm);
        Assert.assertEquals(size, builder.getSize());
        final Bool booleanQuery = builder.build();
        Assert.assertNotNull(booleanQuery);
        Assert.assertEquals(mmsm, booleanQuery.minimumNumberShouldMatch);
        if (size == 0)
            Assert.assertTrue(booleanQuery.clauses == null || booleanQuery.clauses.isEmpty());
        else
            Assert.assertEquals(size, booleanQuery.clauses.size());
        return booleanQuery;
    }

    @Test
    public void empty() {
        final Bool.Builder builder = new Bool.Builder();
        check(null, 0, builder);
    }

    @Test
    public void noClause() {
        final Integer mmsm = RandomUtils.nextInt(0, 100);
        final Bool.Builder builder = new Bool.Builder();
        check(mmsm, 0, builder);
    }

    Bool checkClauses(Consumer<Bool.Builder> consumer) {
        Bool.Builder builder = new Bool.Builder();
        final int count = RandomUtils.nextInt(1, 5);
        for (int i = 0; i < count; i++)
            consumer.accept(builder);
        return check(null, count, builder);
    }

    @Test
    public void someClauses() {
        checkClauses(builder -> builder.addClause(Bool.Occur.must, MatchAllDocs.INSTANCE));
    }

    @Test
    public void mustClauses() {
        checkClauses(builder -> builder.must(MatchAllDocs.INSTANCE));
    }

    @Test
    public void shouldClauses() {
        checkClauses(builder -> builder.should(MatchAllDocs.INSTANCE));
    }

    @Test
    public void mustNotClauses() {
        checkClauses(builder -> builder.mustNot(MatchAllDocs.INSTANCE));
    }

    @Test
    public void filterClauses() {
        checkClauses(builder -> builder.filter(MatchAllDocs.INSTANCE));
    }

    @Test
    public void setClauses() {
        final Bool.Clause clause1 =
            new Bool.Clause(Bool.Occur.filter, MatchAllDocs.INSTANCE);
        final Bool.Clause clause2 =
            new Bool.Clause(Bool.Occur.must, MatchNoDocs.INSTANCE);
        Bool booleanQuery = Bool.of().setClauses(clause1, clause2).build();
        Assert.assertNotNull(booleanQuery.clauses);
        Assert.assertEquals(2, booleanQuery.clauses.size());
        Assert.assertEquals(clause1, booleanQuery.clauses.get(0));
        Assert.assertEquals(clause2, booleanQuery.clauses.get(1));
    }

    @Test
    public void setClause() {
        final Bool.Clause clause1 =
            new Bool.Clause(Bool.Occur.filter, MatchAllDocs.INSTANCE);
        Bool booleanQuery = Bool.of().setClause(clause1).build();
        Assert.assertNotNull(booleanQuery.clauses);
        Assert.assertEquals(1, booleanQuery.clauses.size());
        Assert.assertEquals(clause1, booleanQuery.clauses.get(0));
    }

    @Test
    public void jsonEquality() throws IOException {
        final Bool booleanQuery =
            checkClauses(builder -> builder.addClause(Bool.Occur.must, MatchAllDocs.INSTANCE));
        serializeDeserializeAndCheckEquals(booleanQuery, Bool.class);
    }
}
