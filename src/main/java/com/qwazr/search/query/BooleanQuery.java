/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.query;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.index.QueryContext;
import com.qwazr.utils.CollectionsUtils;
import com.qwazr.utils.Equalizer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BooleanQuery extends AbstractQuery<BooleanQuery> {

    @JsonProperty("clauses")
    final public List<BooleanClause> clauses;

    @JsonProperty("minimum_number_should_match")
    final public Integer minimumNumberShouldMatch;

    public enum Occur {

        must(org.apache.lucene.search.BooleanClause.Occur.MUST),
        should(org.apache.lucene.search.BooleanClause.Occur.SHOULD),
        must_not(org.apache.lucene.search.BooleanClause.Occur.MUST_NOT),
        filter(org.apache.lucene.search.BooleanClause.Occur.FILTER);

        public final org.apache.lucene.search.BooleanClause.Occur occur;

        Occur(org.apache.lucene.search.BooleanClause.Occur occur) {
            this.occur = occur;
        }
    }

    @JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE,
            setterVisibility = JsonAutoDetect.Visibility.NONE,
            isGetterVisibility = JsonAutoDetect.Visibility.NONE,
            creatorVisibility = JsonAutoDetect.Visibility.NONE,
            fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
    public static class BooleanClause extends Equalizer<BooleanClause> {

        public final Occur occur;
        public final AbstractQuery query;

        @JsonCreator
        public BooleanClause(@JsonProperty("occur") final Occur occur,
                @JsonProperty("query") final AbstractQuery query) {
            super(BooleanClause.class);
            this.occur = occur;
            this.query = query;
        }

        @JsonIgnore
        private org.apache.lucene.search.BooleanClause getNewClause(final QueryContext queryContext)
                throws IOException, ParseException, QueryNodeException, ReflectiveOperationException {
            Objects.requireNonNull(occur, "Occur must not be null");
            return new org.apache.lucene.search.BooleanClause(query.getQuery(queryContext), occur.occur);
        }

        @Override
        public int hashCode() {
            return Objects.hash(occur, query);
        }

        @Override
        public boolean isEqual(BooleanClause o) {
            return Objects.equals(occur, o.occur) && Objects.equals(query, o.query);
        }
    }

    @JsonCreator
    public BooleanQuery(@JsonProperty("minimum_number_should_match") final Integer minimumNumberShouldMatch,
            @JsonProperty("clauses") final List<BooleanClause> clauses) {
        super(BooleanQuery.class);
        this.minimumNumberShouldMatch = minimumNumberShouldMatch;
        this.clauses = clauses;
    }

    public BooleanQuery(final List<BooleanClause> clauses) {
        super(BooleanQuery.class);
        this.minimumNumberShouldMatch = null;
        this.clauses = clauses;
    }

    public BooleanQuery(final BooleanClause... clauses) {
        super(BooleanQuery.class);
        this.minimumNumberShouldMatch = null;
        this.clauses = Arrays.asList(clauses);
    }

    public BooleanQuery(final Integer minimumNumberShouldMatch, final BooleanClause... clauses) {
        super(BooleanQuery.class);
        this.minimumNumberShouldMatch = minimumNumberShouldMatch;
        this.clauses = Arrays.asList(clauses);
    }

    private BooleanQuery(final Builder builder) {
        super(BooleanQuery.class);
        this.minimumNumberShouldMatch = builder.minimumNumberShouldMatch;
        this.clauses = builder.clauses == null ? Collections.emptyList() : new ArrayList<>(builder.clauses);
    }

    @Override
    final public Query getQuery(final QueryContext queryContext)
            throws IOException, ParseException, QueryNodeException, ReflectiveOperationException {
        final org.apache.lucene.search.BooleanQuery.Builder builder =
                new org.apache.lucene.search.BooleanQuery.Builder();
        if (minimumNumberShouldMatch != null)
            builder.setMinimumNumberShouldMatch(minimumNumberShouldMatch);
        if (clauses != null)
            for (BooleanClause clause : clauses)
                builder.add(clause.getNewClause(queryContext));
        return builder.build();
    }

    @Override
    protected boolean isEqual(final BooleanQuery query) {
        return CollectionsUtils.equals(clauses, query.clauses) &&
                Objects.equals(minimumNumberShouldMatch, query.minimumNumberShouldMatch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clauses, minimumNumberShouldMatch);
    }

    public static Builder of() {
        return new Builder();
    }

    public static Builder of(final Integer minimumNumberShouldMatch) {
        return of().setMinimumNumberShouldMatch(minimumNumberShouldMatch);
    }

    public final static class Builder {

        private final List<BooleanClause> clauses;
        private Integer minimumNumberShouldMatch;

        public Builder() {
            clauses = new ArrayList<>();
        }

        public final Builder setMinimumNumberShouldMatch(final Integer minimumNumberShouldMatch) {
            this.minimumNumberShouldMatch = minimumNumberShouldMatch;
            return this;
        }

        public final Builder addClause(final Occur occur, final AbstractQuery query) {
            return addClause(new BooleanClause(occur, query));
        }

        public final Builder addClause(final BooleanClause booleanClause) {
            clauses.add(booleanClause);
            return this;
        }

        public final Builder addClauses(final BooleanClause... booleanClauses) {
            Collections.addAll(clauses, booleanClauses);
            return this;
        }

        public final Builder setClause(final BooleanClause booleanClause) {
            clauses.clear();
            return addClause(booleanClause);
        }

        public final Builder setClauses(final BooleanClause... booleanClauses) {
            clauses.clear();
            return addClauses(booleanClauses);
        }

        public final Builder filter(final AbstractQuery query) {
            return addClause(Occur.filter, query);
        }

        public final Builder must(final AbstractQuery query) {
            return addClause(Occur.must, query);
        }

        public final Builder mustNot(final AbstractQuery query) {
            return addClause(Occur.must_not, query);
        }

        public final Builder should(final AbstractQuery query) {
            return addClause(Occur.should, query);
        }

        public final int getSize() {
            return clauses.size();
        }

        final public BooleanQuery build() {
            return new BooleanQuery(this);
        }
    }
}
