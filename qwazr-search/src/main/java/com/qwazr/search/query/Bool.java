/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.annotations.QuerySampleCreator;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.QueryContext;
import com.qwazr.utils.CollectionsUtils;
import com.qwazr.utils.Equalizer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Query;

public class Bool extends AbstractQuery<Bool> {

    @JsonProperty("clauses")
    final public List<Clause> clauses;

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
    public static class Clause extends Equalizer.Immutable<Clause> {

        public final Occur occur;
        public final AbstractQuery<?> query;

        @JsonCreator
        public Clause(@JsonProperty("occur") final Occur occur,
                      @JsonProperty("query") final AbstractQuery<?> query) {
            super(Clause.class);
            this.occur = occur;
            this.query = query;
        }

        @JsonIgnore
        private BooleanClause getNewClause(final QueryContext queryContext)
            throws IOException, ParseException, QueryNodeException, ReflectiveOperationException {
            Objects.requireNonNull(occur, "Occur must not be null");
            return new BooleanClause(query.getQuery(queryContext), occur.occur);
        }

        @Override
        protected int computeHashCode() {
            return Objects.hash(occur, query);
        }

        @Override
        protected boolean isEqual(Clause o) {
            return Objects.equals(occur, o.occur) && Objects.equals(query, o.query);
        }
    }

    @JsonCreator
    public Bool(@JsonProperty("minimum_number_should_match") final Integer minimumNumberShouldMatch,
                @JsonProperty("clauses") final List<Clause> clauses) {
        super(Bool.class);
        this.minimumNumberShouldMatch = minimumNumberShouldMatch;
        this.clauses = clauses;
    }

    @QuerySampleCreator(docUri = CORE_BASE_DOC_URI + "core/org/apache/lucene/search/BooleanQuery.html")
    public Bool(final IndexSettingsDefinition settings,
                final Map<String, AnalyzerDefinition> analyzers,
                final Map<String, FieldDefinition> fields) {
        super(Bool.class);
        minimumNumberShouldMatch = 2;
        final String field = getFullTextField(fields,
            () -> getTextField(fields,
                () -> "text"));
        this.clauses = List.of(
            new Clause(Occur.must, new HasTerm(field, "Hello")),
            new Clause(Occur.should, new HasTerm(field, "World")),
            new Clause(Occur.filter, new HasTerm(field, "code")),
            new Clause(Occur.must_not, new HasTerm(field, "unwanted"))
        );
    }

    public Bool(final List<Clause> clauses) {
        super(Bool.class);
        this.minimumNumberShouldMatch = null;
        this.clauses = clauses;
    }

    public Bool(final Clause... clauses) {
        super(Bool.class);
        this.minimumNumberShouldMatch = null;
        this.clauses = Arrays.asList(clauses);
    }

    public Bool(final Integer minimumNumberShouldMatch, final Clause... clauses) {
        super(Bool.class);
        this.minimumNumberShouldMatch = minimumNumberShouldMatch;
        this.clauses = Arrays.asList(clauses);
    }

    private Bool(final Builder builder) {
        super(Bool.class);
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
            for (Clause clause : clauses)
                builder.add(clause.getNewClause(queryContext));
        return builder.build();
    }

    @Override
    protected boolean isEqual(final Bool query) {
        return CollectionsUtils.equals(clauses, query.clauses) &&
            Objects.equals(minimumNumberShouldMatch, query.minimumNumberShouldMatch);
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(clauses, minimumNumberShouldMatch);
    }

    public static Builder of() {
        return new Builder();
    }

    public static Builder of(final Integer minimumNumberShouldMatch) {
        return of().setMinimumNumberShouldMatch(minimumNumberShouldMatch);
    }

    public final static class Builder {

        private final List<Clause> clauses;
        private Integer minimumNumberShouldMatch;

        public Builder() {
            clauses = new ArrayList<>();
        }

        public final Builder setMinimumNumberShouldMatch(final Integer minimumNumberShouldMatch) {
            this.minimumNumberShouldMatch = minimumNumberShouldMatch;
            return this;
        }

        public final Builder addClause(final Occur occur, final AbstractQuery<?> query) {
            return addClause(new Clause(occur, query));
        }

        public final Builder addClause(final Clause clause) {
            clauses.add(clause);
            return this;
        }

        public final Builder addClauses(final Clause... clauses) {
            Collections.addAll(this.clauses, clauses);
            return this;
        }

        public final Builder setClause(final Clause clause) {
            clauses.clear();
            return addClause(clause);
        }

        public final Builder setClauses(final Clause... clauses) {
            this.clauses.clear();
            return addClauses(clauses);
        }

        public final Builder filter(final AbstractQuery<?> query) {
            return addClause(Occur.filter, query);
        }

        public final Builder must(final AbstractQuery<?> query) {
            return addClause(Occur.must, query);
        }

        public final Builder mustNot(final AbstractQuery<?> query) {
            return addClause(Occur.must_not, query);
        }

        public final Builder should(final AbstractQuery<?> query) {
            return addClause(Occur.should, query);
        }

        public final int getSize() {
            return clauses.size();
        }

        final public Bool build() {
            return new Bool(this);
        }
    }
}
