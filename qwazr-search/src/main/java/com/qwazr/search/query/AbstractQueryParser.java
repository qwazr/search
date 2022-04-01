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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.index.QueryContext;
import java.util.Objects;
import org.apache.lucene.util.QueryBuilder;

public abstract class AbstractQueryParser<T extends AbstractQueryParser<T>> extends AbstractQuery<T> {

    @JsonProperty("enable_position_increments")
    final public Boolean enablePositionIncrements;

    @JsonProperty("auto_generate_multi_term_synonyms_phrase_query")
    final public Boolean autoGenerateMultiTermSynonymsPhraseQuery;

    @JsonProperty("enable_graph_queries")
    final public Boolean enableGraphQueries;

    @JsonProperty("query_string")
    final public String queryString;

    @JsonProperty("analyzer")
    final public String analyzer;

    protected AbstractQueryParser(final Class<T> queryClass,
                                  final Boolean enablePositionIncrements,
                                  final Boolean autoGenerateMultiTermSynonymsPhraseQuery,
                                  final Boolean enableGraphQueries,
                                  final String queryString,
                                  final String analyzer) {
        super(queryClass);
        this.analyzer = analyzer;
        this.enablePositionIncrements = enablePositionIncrements;
        this.autoGenerateMultiTermSynonymsPhraseQuery = autoGenerateMultiTermSynonymsPhraseQuery;
        this.enableGraphQueries = enableGraphQueries;
        this.queryString = queryString;

    }

    protected AbstractQueryParser(final Class<T> queryClass, AbstractBuilder<?, ?> builder) {
        super(queryClass);
        this.analyzer = builder.analyzer;
        this.enablePositionIncrements = builder.enablePositionIncrements;
        this.autoGenerateMultiTermSynonymsPhraseQuery = builder.autoGenerateMultiTermSynonymsPhraseQuery;
        this.enableGraphQueries = builder.enableGraphQueries;
        this.queryString = builder.queryString;
    }

    protected void setQueryBuilderParameters(final QueryContext queryContext,
                                             final QueryBuilder queryBuilder) {
        queryBuilder.setAnalyzer(queryContext.resolveQueryAnalyzer(analyzer));
        if (enablePositionIncrements != null)
            queryBuilder.setEnablePositionIncrements(enablePositionIncrements);
        if (autoGenerateMultiTermSynonymsPhraseQuery != null)
            queryBuilder.setAutoGenerateMultiTermSynonymsPhraseQuery(autoGenerateMultiTermSynonymsPhraseQuery);
        if (enableGraphQueries != null)
            queryBuilder.setEnableGraphQueries(enableGraphQueries);
    }

    @Override
    @JsonIgnore
    protected boolean isEqual(T q) {
        return Objects.equals(enablePositionIncrements, q.enablePositionIncrements)
            && Objects.equals(autoGenerateMultiTermSynonymsPhraseQuery, q.autoGenerateMultiTermSynonymsPhraseQuery)
            && Objects.equals(enableGraphQueries, q.enableGraphQueries)
            && Objects.equals(queryString, q.queryString)
            && Objects.equals(analyzer, q.analyzer);
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(enablePositionIncrements, autoGenerateMultiTermSynonymsPhraseQuery, autoGenerateMultiTermSynonymsPhraseQuery, queryString);
    }

    public static abstract class AbstractBuilder<B extends AbstractBuilder<B, T>, T extends AbstractQueryParser<T>> {

        private String analyzer;
        private Boolean enablePositionIncrements;
        private Boolean autoGenerateMultiTermSynonymsPhraseQuery;
        private Boolean enableGraphQueries;
        private String queryString;

        protected abstract B me();

        public abstract T build();

        final public B setAnalyzer(final String analyzer) {
            this.analyzer = analyzer;
            return me();
        }

        final public B setEnablePositionIncrements(Boolean enablePositionIncrements) {
            this.enablePositionIncrements = enablePositionIncrements;
            return me();
        }

        final public B setAutoGenerateMultiTermSynonymsPhraseQuery(Boolean autoGenerateMultiTermSynonymsPhraseQuery) {
            this.autoGenerateMultiTermSynonymsPhraseQuery = autoGenerateMultiTermSynonymsPhraseQuery;
            return me();
        }

        final public B setEnableGraphQueries(Boolean enableGraphQueries) {
            this.enableGraphQueries = enableGraphQueries;
            return me();
        }

        final public B setQueryString(String queryString) {
            this.queryString = queryString;
            return me();
        }
    }
}
