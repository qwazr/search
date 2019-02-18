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
package com.qwazr.search.query;

import com.qwazr.search.JsonHelpers;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.function.Consumer;

public class BooleanQueryTest {

    private BooleanQuery check(Integer mmsm, int size, BooleanQuery.Builder builder) {
        builder.setMinimumNumberShouldMatch(mmsm);
        Assert.assertEquals(size, builder.getSize());
        final BooleanQuery booleanQuery = builder.build();
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
        final BooleanQuery.Builder builder = new BooleanQuery.Builder();
        check(null, 0, builder);
    }

    @Test
    public void noClause() {
        final Integer mmsm = RandomUtils.nextInt(0, 100);
        final BooleanQuery.Builder builder = new BooleanQuery.Builder();
        check(mmsm, 0, builder);
    }

    BooleanQuery checkClauses(Consumer<BooleanQuery.Builder> consumer) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        final int count = RandomUtils.nextInt(1, 5);
        for (int i = 0; i < count; i++)
            consumer.accept(builder);
        return check(null, count, builder);
    }

    @Test
    public void someClauses() {
        checkClauses(builder -> builder.addClause(BooleanQuery.Occur.must, new MatchAllDocsQuery()));
    }

    @Test
    public void mustClauses() {
        checkClauses(builder -> builder.must(new MatchAllDocsQuery()));
    }

    @Test
    public void shouldClauses() {
        checkClauses(builder -> builder.should(new MatchAllDocsQuery()));
    }

    @Test
    public void mustNotClauses() {
        checkClauses(builder -> builder.mustNot(new MatchAllDocsQuery()));
    }

    @Test
    public void filterClauses() {
        checkClauses(builder -> builder.filter(new MatchAllDocsQuery()));
    }

    @Test
    public void setClauses() {
        final BooleanQuery.BooleanClause clause1 =
                new BooleanQuery.BooleanClause(BooleanQuery.Occur.filter, new MatchAllDocsQuery());
        final BooleanQuery.BooleanClause clause2 =
                new BooleanQuery.BooleanClause(BooleanQuery.Occur.must, new MatchNoDocsQuery());
        BooleanQuery booleanQuery = BooleanQuery.of().setClauses(clause1, clause2).build();
        Assert.assertNotNull(booleanQuery.clauses);
        Assert.assertEquals(2, booleanQuery.clauses.size());
        Assert.assertEquals(clause1, booleanQuery.clauses.get(0));
        Assert.assertEquals(clause2, booleanQuery.clauses.get(1));
    }

    @Test
    public void setClause() {
        final BooleanQuery.BooleanClause clause1 =
                new BooleanQuery.BooleanClause(BooleanQuery.Occur.filter, new MatchAllDocsQuery());
        BooleanQuery booleanQuery = BooleanQuery.of().setClause(clause1).build();
        Assert.assertNotNull(booleanQuery.clauses);
        Assert.assertEquals(1, booleanQuery.clauses.size());
        Assert.assertEquals(clause1, booleanQuery.clauses.get(0));
    }

    @Test
    public void jsonEquality() throws IOException {
        final BooleanQuery booleanQuery =
                checkClauses(builder -> builder.addClause(BooleanQuery.Occur.must, new MatchAllDocsQuery()));
        JsonHelpers.serializeDeserializeAndCheckEquals(booleanQuery, BooleanQuery.class);
    }
}
