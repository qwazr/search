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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.annotations.QuerySampleCreator;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.FieldMap;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.QueryContext;
import com.qwazr.utils.CollectionsUtils;
import com.qwazr.utils.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Query;

public class SimpleQueryParser extends AbstractQueryParser<SimpleQueryParser> {

    @JsonProperty("weights")
    final public LinkedHashMap<String, Float> weights;
    @JsonProperty("default_operator")
    final public QueryParserOperator defaultOperator;
    @JsonProperty("enabled_operators")
    final public List<Operator> enabledOperators;

    enum Operator {
        and(org.apache.lucene.queryparser.simple.SimpleQueryParser.AND_OPERATOR),
        escape(org.apache.lucene.queryparser.simple.SimpleQueryParser.ESCAPE_OPERATOR),
        fuzzy(org.apache.lucene.queryparser.simple.SimpleQueryParser.FUZZY_OPERATOR),
        near(org.apache.lucene.queryparser.simple.SimpleQueryParser.NEAR_OPERATOR),
        not(org.apache.lucene.queryparser.simple.SimpleQueryParser.NOT_OPERATOR),
        or(org.apache.lucene.queryparser.simple.SimpleQueryParser.OR_OPERATOR),
        phrase(org.apache.lucene.queryparser.simple.SimpleQueryParser.PHRASE_OPERATOR),
        precedence(org.apache.lucene.queryparser.simple.SimpleQueryParser.PRECEDENCE_OPERATORS),
        prefix(org.apache.lucene.queryparser.simple.SimpleQueryParser.PREFIX_OPERATOR),
        whitespace(org.apache.lucene.queryparser.simple.SimpleQueryParser.WHITESPACE_OPERATOR);

        final private int value;

        Operator(final int value) {
            this.value = value;
        }

    }

    @JsonIgnore
    private final int effectiveFlags;

    @JsonCreator
    private SimpleQueryParser(@JsonProperty("enable_position_increments") final Boolean enablePositionIncrements,
                              @JsonProperty("auto_generate_multi_term_synonyms_phrase_query") final Boolean autoGenerateMultiTermSynonymsPhraseQuery,
                              @JsonProperty("enable_graph_queries") final Boolean enableGraphQueries,
                              @JsonProperty("query_string") final String queryString,
                              @JsonProperty("weights") final LinkedHashMap<String, Float> weights,
                              @JsonProperty("default_operator") final QueryParserOperator defaultOperator,
                              @JsonProperty("enabled_operators") final List<Operator> enabledOperators) {
        super(SimpleQueryParser.class, enablePositionIncrements, autoGenerateMultiTermSynonymsPhraseQuery, enableGraphQueries, queryString);
        this.weights = weights;
        this.defaultOperator = defaultOperator;
        this.enabledOperators = enabledOperators;
        this.effectiveFlags = computeFlag(enabledOperators);
    }

    static int computeFlag(List<Operator> operators) {
        if (operators == null)
            return -1;
        int flags = 0;
        for (Operator operator : operators)
            flags = flags | operator.value;
        return flags;
    }

    @QuerySampleCreator(docUri = CORE_BASE_DOC_URI + "queryparser/org/apache/lucene/queryparser/simple/SimpleQueryParser.html")
    public static SimpleQueryParser create(final IndexSettingsDefinition settings,
                                           final Map<String, AnalyzerDefinition> analyzers,
                                           final Map<String, FieldDefinition> fields) {
        final String field = getFullTextField(fields, () -> getTextField(fields, () -> "text"));
        return of()
            .addField(field)
            .addBoost(field, 2.0f)
            .setDefaultOperator(QueryParserOperator.AND)
            .setEnablePositionIncrements(true)
            .setQueryString("Hello World")
            .addOperator(Operator.values())
            .build();
    }

    private SimpleQueryParser(final Builder builder) {
        super(SimpleQueryParser.class, builder);
        this.weights = builder.weights;
        this.defaultOperator = builder.defaultOperator;
        this.enabledOperators = builder.enabledOperators;
        this.effectiveFlags = computeFlag(enabledOperators);
    }

    @Override
    final public Query getQuery(final QueryContext queryContext) {

        final FieldMap fieldMap = queryContext.getFieldMap();

        final Map<String, Float> resolvedBoosts = fieldMap == null || weights == null ? weights
            : FieldMap.resolveFieldNames(weights, new HashMap<>(),
            f -> resolveFullTextField(fieldMap, f, f, StringUtils.EMPTY));

        final org.apache.lucene.queryparser.simple.SimpleQueryParser parser =
            new org.apache.lucene.queryparser.simple.SimpleQueryParser(
                analyzer == null ? queryContext.getQueryAnalyzer() : analyzer, resolvedBoosts, effectiveFlags);

        setQueryBuilderParameters(parser);

        if (defaultOperator != null)
            parser.setDefaultOperator(
                defaultOperator == QueryParserOperator.AND ? BooleanClause.Occur.MUST : BooleanClause.Occur.SHOULD);

        return parser.parse(Objects.requireNonNull(queryString, "The query string is missing"));
    }

    @Override
    @JsonIgnore
    protected boolean isEqual(SimpleQueryParser q) {
        return super.isEqual(q)
            && CollectionsUtils.equals(weights, q.weights)
            && Objects.equals(defaultOperator, q.defaultOperator)
            && effectiveFlags == q.effectiveFlags;
    }

    public static Builder of() {
        return new Builder();
    }

    public static class Builder extends AbstractBuilder<Builder, SimpleQueryParser> {

        private LinkedHashMap<String, Float> weights;
        private QueryParserOperator defaultOperator;
        private List<Operator> enabledOperators;

        protected Builder() {
            super(Builder.class);
        }

        public Builder addField(String... fieldSet) {
            for (String field : fieldSet)
                addBoost(field, 1.0f);
            return this;
        }

        public Builder addBoost(String field, Float boost) {
            if (weights == null)
                weights = new LinkedHashMap<>();
            weights.put(field, boost);
            return this;
        }

        public Builder setDefaultOperator(QueryParserOperator defaultOperator) {
            this.defaultOperator = defaultOperator;
            return this;
        }

        public Builder addOperator(Operator... operators) {
            if (enabledOperators == null)
                enabledOperators = new ArrayList<>(operators.length);
            Collections.addAll(enabledOperators, operators);
            return this;
        }

        @Override
        public SimpleQueryParser build() {
            return new SimpleQueryParser(this);
        }
    }
}
