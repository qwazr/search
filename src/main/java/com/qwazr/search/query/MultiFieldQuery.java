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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.analysis.TermConsumer;
import com.qwazr.search.index.FieldMap;
import com.qwazr.search.index.QueryContext;
import com.qwazr.utils.CollectionsUtils;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.concurrent.ConcurrentUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

public class MultiFieldQuery extends AbstractQuery<MultiFieldQuery> {

    @JsonProperty("fields_boosts")
    final public Map<String, Float> fieldsBoosts;

    @JsonProperty("fields_disabled_graph")
    final public Set<String> fieldsDisabledGraph;

    @JsonProperty("fields_and_filter")
    final public Set<String> fieldsAndFilter;

    @JsonProperty("default_operator")
    final public QueryParserOperator defaultOperator;

    @JsonProperty("query_string")
    final public String queryString;

    @JsonProperty("min_number_should_match")
    final public Integer minNumberShouldMatch;

    @JsonProperty("tie_breaker_multiplier")
    final public Float tieBreakerMultiplier;

    @JsonProperty("enable_fuzzy_query")
    final public Boolean enableFuzzyQuery;

    final private Analyzer analyzer;

    MultiFieldQuery(Builder builder) {
        this(builder.fieldsBoosts, builder.fieldsDisabledGraph, builder.fieldsAndFilter, builder.defaultOperator,
            builder.queryString, builder.minNumberShouldMatch, builder.tieBreakerMultiplier,
            builder.enableFuzzyQuery, builder.analyzer);
    }

    MultiFieldQuery(final Map<String, Float> fieldsBoosts, final Set<String> fieldsDisabledGraph,
                    final Set<String> fieldsAndFilter, final QueryParserOperator defaultOperator, final String queryString,
                    final Integer minNumberShouldMatch, final Float tieBreakerMultiplier, final Boolean enableFuzzyQuery,
                    final Analyzer analyzer) {
        super(MultiFieldQuery.class);
        this.fieldsBoosts = fieldsBoosts;
        this.fieldsDisabledGraph = fieldsDisabledGraph;
        this.fieldsAndFilter = fieldsAndFilter;
        this.defaultOperator = defaultOperator;
        this.queryString = queryString;
        this.minNumberShouldMatch = minNumberShouldMatch;
        this.tieBreakerMultiplier = tieBreakerMultiplier;
        this.enableFuzzyQuery = enableFuzzyQuery;
        this.analyzer = analyzer;
    }

    @JsonCreator
    public MultiFieldQuery(@JsonProperty("fields_boosts") final Map<String, Float> fieldsBoosts,
                           @JsonProperty("fields_disabled_graph") final Set<String> fieldsDisabledGraph,
                           @JsonProperty("fields_and_filter") final Set<String> fieldsAndFilter,
                           @JsonProperty("default_operator") final QueryParserOperator defaultOperator,
                           @JsonProperty("query_string") final String queryString,
                           @JsonProperty("min_number_should_match") final Integer minNumberShouldMatch,
                           @JsonProperty("tie_breaker_multiplier") final Float tieBreakerMultiplier,
                           @JsonProperty("enable_fuzzy_query") final Boolean enableFuzzyQuery) {
        this(fieldsBoosts, fieldsDisabledGraph, fieldsAndFilter, defaultOperator, queryString, minNumberShouldMatch,
            tieBreakerMultiplier, enableFuzzyQuery, null);
    }

    @Override
    protected boolean isEqual(MultiFieldQuery q) {
        return CollectionsUtils.equals(fieldsBoosts, q.fieldsBoosts) &&
            CollectionsUtils.equals(fieldsDisabledGraph, q.fieldsDisabledGraph) &&
            CollectionsUtils.equals(fieldsAndFilter, q.fieldsAndFilter) &&
            Objects.equals(defaultOperator, q.defaultOperator) && Objects.equals(queryString, q.queryString) &&
            Objects.equals(minNumberShouldMatch, q.minNumberShouldMatch) &&
            Objects.equals(tieBreakerMultiplier, q.tieBreakerMultiplier) &&
            Objects.equals(enableFuzzyQuery, q.enableFuzzyQuery) && Objects.equals(analyzer, q.analyzer);
    }

    @Override
    final public Query getQuery(final QueryContext queryContext) throws IOException {
        Objects.requireNonNull(fieldsBoosts, "Fields boosts is missing");

        if (StringUtils.isEmpty(queryString))
            return new org.apache.lucene.search.MatchNoDocsQuery();

        // Select the right analyzer
        final Analyzer alzr = analyzer != null ? analyzer : queryContext.getQueryAnalyzer();

        // We look for terms frequency globally
        final Map<String, Integer> termsFreq = new HashMap<>();
        final IndexReader indexReader = queryContext.getIndexReader();
        ConcurrentUtils.forEachEx(fieldsBoosts, (field, boost) -> {
            try (final TokenStream tokenStream = alzr.tokenStream(field, queryString)) {
                new TermsWithFreq(tokenStream, indexReader, field, termsFreq).forEachToken();
                tokenStream.end();
            }
        });

        final FieldMap fieldMap = queryContext.getFieldMap();

        // Build the per field queries
        final List<Query> fieldQueries = new ArrayList<>();
        final List<Query> andFieldQueries = fieldsAndFilter != null ? new ArrayList<>() : null;
        final BooleanClause.Occur defaultOccur = defaultOperator == null || defaultOperator == QueryParserOperator.AND ?
            BooleanClause.Occur.MUST :
            BooleanClause.Occur.SHOULD;
        fieldsBoosts.forEach((field, boost) -> {
            final List<Query> queries;
            final BooleanClause.Occur occur;
            if (fieldsAndFilter != null && fieldsAndFilter.contains(field)) {
                queries = andFieldQueries;
                occur = minNumberShouldMatch != null ? BooleanClause.Occur.SHOULD : BooleanClause.Occur.MUST;
            } else {
                queries = fieldQueries;
                occur = minNumberShouldMatch != null ? BooleanClause.Occur.SHOULD : defaultOccur;
            }
            final Query query =
                new FieldQueryBuilder(alzr, fieldMap == null
                    ? field : fieldMap.getFieldType(field, field, StringUtils.EMPTY)
                    .resolveFieldName(field, null, null),
                    termsFreq).parse(queryString, occur, boost);
            if (query != null)
                queries.add(query);
        });

        // Build the final query
        final Query fieldsQuery = getRootQuery(fieldQueries);
        if (andFieldQueries == null || andFieldQueries.isEmpty())
            return fieldsQuery;
        final Query andFieldsQuery = getRootQuery(andFieldQueries);
        final BooleanQuery.Builder builder = new org.apache.lucene.search.BooleanQuery.Builder();
        builder.add(fieldsQuery, BooleanClause.Occur.SHOULD);
        builder.add(andFieldsQuery, BooleanClause.Occur.MUST);
        return builder.build();
    }

    protected int getMinShouldMatch(final int clauseCount) {
        return Math.round(Math.max(1, (float) (clauseCount * minNumberShouldMatch) / 100));
    }

    protected Query getRootQuery(final Collection<Query> queries) {
        if (queries.size() == 1)
            return queries.iterator().next();
        if (tieBreakerMultiplier != null) {
            return new org.apache.lucene.search.DisjunctionMaxQuery(queries, tieBreakerMultiplier);
        } else {
            final BooleanQuery.Builder builder = new org.apache.lucene.search.BooleanQuery.Builder();
            queries.forEach(query -> {
                if (query != null)
                    builder.add(query, BooleanClause.Occur.SHOULD);
            });
            return builder.build();
        }
    }

    protected Query getTermQuery(final int freq, final Term term) {
        Query query;
        if (enableFuzzyQuery == null || !enableFuzzyQuery || freq > 0)
            query = new org.apache.lucene.search.TermQuery(term);
        else
            query = new org.apache.lucene.search.FuzzyQuery(term);
        return query;
    }

    protected Query getBoostQuery(final Query fieldQuery, final Float boost) {
        return boost != null && boost != 1.0F && fieldQuery != null ?
            new org.apache.lucene.search.BoostQuery(fieldQuery, boost) :
            fieldQuery;
    }

    private static class TermsWithFreq extends TermConsumer.WithChar {

        private final IndexReader indexReader;
        private final String field;
        private final Map<String, Integer> termsFreq;

        private TermsWithFreq(final TokenStream tokenStream, final IndexReader indexReader, final String field,
                              final Map<String, Integer> termsFreq) {
            super(tokenStream);
            this.indexReader = indexReader;
            this.field = field;
            this.termsFreq = termsFreq;
        }

        @Override
        final public boolean token() throws IOException {
            if (charTermAttr == null)
                return false;
            final String text = charTermAttr.toString();
            final Term term = new Term(field, text);
            final int newFreq = indexReader == null ? 0 : indexReader.docFreq(term);
            if (newFreq > 0) {
                final Integer previousFreq = termsFreq.get(text);
                if (previousFreq == null || newFreq > previousFreq)
                    termsFreq.put(text, newFreq);
            }
            return true;
        }
    }

    final class FieldQueryBuilder extends org.apache.lucene.util.QueryBuilder {

        final Map<String, Integer> termsFreq;
        final String field;

        private FieldQueryBuilder(final Analyzer analyzer, final String field, final Map<String, Integer> termsFreq) {
            super(analyzer);
            this.termsFreq = termsFreq;
            this.field = field;
            setEnableGraphQueries(fieldsDisabledGraph == null || !fieldsDisabledGraph.contains(field));
        }

        @Override
        final protected Query newTermQuery(Term term, float boost) {
            final Integer freq = termsFreq.get(term.text());
            return getTermQuery(freq == null ? 0 : freq, term);
        }

        protected BooleanQuery.Builder newBooleanQuery() {
            return new FieldBooleanBuilder();
        }

        final Query parse(final String queryString, final BooleanClause.Occur defaultOperator, final Float boost) {
            final Query fieldQuery = createBooleanQuery(field, queryString, defaultOperator);
            return getBoostQuery(fieldQuery, boost);
        }

    }

    final class FieldBooleanBuilder extends BooleanQuery.Builder {

        private int clauseCount;

        @Override
        public BooleanQuery.Builder add(BooleanClause clause) {
            clauseCount++;
            return super.add(clause);
        }

        @Override
        public BooleanQuery build() {
            if (minNumberShouldMatch != null) {
                setMinimumNumberShouldMatch(getMinShouldMatch(clauseCount));
            }
            return super.build();
        }
    }

    public static Builder of() {
        return new Builder();
    }

    public static class Builder {

        Map<String, Float> fieldsBoosts;
        Set<String> fieldsDisabledGraph;
        Set<String> fieldsAndFilter;
        QueryParserOperator defaultOperator;
        String queryString;
        Integer minNumberShouldMatch;
        Float tieBreakerMultiplier;
        Boolean enableFuzzyQuery;
        Analyzer analyzer;

        public Builder fieldBoost(String field, Float boost) {
            if (fieldsBoosts == null)
                fieldsBoosts = new LinkedHashMap<>();
            fieldsBoosts.put(field, boost);
            return this;
        }

        public Builder fieldDisableGraph(String... fields) {
            if (fieldsDisabledGraph == null)
                fieldsDisabledGraph = new LinkedHashSet<>();
            Collections.addAll(fieldsDisabledGraph, fields);
            return this;
        }

        public Builder fieldAndFilter(String... fields) {
            if (fieldsAndFilter == null)
                fieldsAndFilter = new LinkedHashSet<>();
            Collections.addAll(fieldsAndFilter, fields);
            return this;
        }

        public Builder defaultOperator(QueryParserOperator defaultOperator) {
            this.defaultOperator = defaultOperator;
            return this;
        }

        public Builder queryString(String queryString) {
            this.queryString = queryString;
            return this;
        }

        public Builder minNumberShouldMatch(Integer minNumberShouldMatch) {
            this.minNumberShouldMatch = minNumberShouldMatch;
            return this;
        }

        public Builder tieBreakerMultiplier(Float tieBreakerMultiplier) {
            this.tieBreakerMultiplier = tieBreakerMultiplier;
            return this;
        }

        public Builder enableFuzzyQuery(Boolean enableFuzzyQuery) {
            this.enableFuzzyQuery = enableFuzzyQuery;
            return this;
        }

        public Builder analyzer(Analyzer analyzer) {
            this.analyzer = analyzer;
            return this;
        }

        public MultiFieldQuery build() {
            return new MultiFieldQuery(this);
        }
    }

}
