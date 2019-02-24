/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.index.QueryContext;
import com.qwazr.utils.ClassLoaderUtils;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Objects;

@Deprecated
public class CustomScoreQuery extends AbstractQuery<CustomScoreQuery> {

    public final AbstractQuery subQuery;
    public final FunctionQuery scoringQuery;
    public final FunctionQuery[] scoringQueries;
    @JsonProperty("customScoreProvider")
    public final String customScoreProviderClassName;
    @JsonIgnore
    public final Class<? extends CustomScoreProvider> customScoreProviderClass;

    @JsonCreator
    private CustomScoreQuery(@JsonProperty("subQuery") final AbstractQuery subQuery,
                             @JsonProperty("customScoreProvider") final String customScoreProviderClassName,
                             @JsonProperty("scoringQuery") final FunctionQuery scoringQuery,
                             @JsonProperty("scoringQueries") final FunctionQuery... scoringQueries) {
        super(CustomScoreQuery.class);
        this.subQuery = subQuery;
        this.scoringQuery = scoringQuery;
        this.scoringQueries = scoringQueries;
        this.customScoreProviderClassName = customScoreProviderClassName;
        this.customScoreProviderClass = null;
    }

    public CustomScoreQuery(final AbstractQuery subQuery, final FunctionQuery... scoringQueries) {
        this(subQuery, null, (String) null, scoringQueries);
    }

    public CustomScoreQuery(final AbstractQuery subQuery,
                            final Class<? extends CustomScoreProvider> customScoreProviderClass,
                            final String customScoreProviderClassName, final FunctionQuery... scoringQueries) {
        super(CustomScoreQuery.class);
        this.subQuery = subQuery;
        if (scoringQueries == null || scoringQueries.length == 0) {
            this.scoringQuery = null;
            this.scoringQueries = null;
        } else {
            if (scoringQueries.length == 1) {
                this.scoringQuery = scoringQueries[0];
                this.scoringQueries = null;
            } else {
                this.scoringQuery = null;
                this.scoringQueries = scoringQueries;
            }
        }
        this.customScoreProviderClassName = customScoreProviderClassName;
        this.customScoreProviderClass = customScoreProviderClass;
    }

    public CustomScoreQuery(@JsonProperty("subQuery") final AbstractQuery subQuery,
                            @JsonProperty("customScoreProvider") final String customScoreProviderClass,
                            @JsonProperty("scoringQueries") final FunctionQuery... scoringQueries) {
        this(subQuery, null, customScoreProviderClass, scoringQueries);
    }

    public CustomScoreQuery(final AbstractQuery subQuery,
                            final Class<? extends CustomScoreProvider> customScoreProviderClass,
                            final FunctionQuery... scoringQueries) {
        this(subQuery, customScoreProviderClass, null, scoringQueries);
    }

    @Override
    final public Query getQuery(final QueryContext queryContext)
            throws IOException, ParseException, QueryNodeException, ReflectiveOperationException {
        Objects.requireNonNull(subQuery, "Missing subQuery property");
        final Query query = subQuery.getQuery(queryContext);
        final org.apache.lucene.queries.CustomScoreQuery customScoreQuery;

        if (customScoreProviderClass != null)
            customScoreQuery = buildCustomScoreQueryProvider(query, queryContext, customScoreProviderClass);
        else if (customScoreProviderClassName != null)
            customScoreQuery = buildCustomScoreQueryProvider(query, queryContext, getProviderClass());
        else
            customScoreQuery = buildCustomScoreQuery(query, queryContext);
        return customScoreQuery;
    }

    private org.apache.lucene.queries.CustomScoreQuery buildCustomScoreQuery(final Query query,
                                                                             final QueryContext queryContext)
            throws ReflectiveOperationException, IOException, ParseException, QueryNodeException {
        if (scoringQueries != null)
            return new org.apache.lucene.queries.CustomScoreQuery(query,
                    FunctionQuery.getQueries(scoringQueries, queryContext));
        else if (scoringQuery != null)
            return new org.apache.lucene.queries.CustomScoreQuery(query, scoringQuery.getQuery(queryContext));
        else
            return new org.apache.lucene.queries.CustomScoreQuery(query);
    }

    private Class<? extends CustomScoreProvider> getProviderClass() throws ReflectiveOperationException {
        Class<? extends CustomScoreProvider> customScoreProviderClass =
                ClassLoaderUtils.findClass(customScoreProviderClassName);
        Objects.requireNonNull(customScoreProviderClass, "Cannot find the class for " + customScoreProviderClassName);
        return customScoreProviderClass;
    }

    private org.apache.lucene.queries.CustomScoreQuery buildCustomScoreQueryProvider(final Query query,
                                                                                     final QueryContext queryContext, final Class<? extends CustomScoreProvider> customScoreProviderClass)
            throws ReflectiveOperationException, ParseException, IOException, QueryNodeException {

        final Constructor<? extends CustomScoreProvider> customScoreProviderConstructor =
                customScoreProviderClass.getConstructor(LeafReaderContext.class);

        if (scoringQueries != null)
            return new CustomScoreQueryWithProvider(customScoreProviderConstructor, query,
                    FunctionQuery.getQueries(scoringQueries, queryContext));
        else if (scoringQuery != null)
            return new CustomScoreQueryWithProvider(customScoreProviderConstructor, query,
                    scoringQuery.getQuery(queryContext));
        else
            return new CustomScoreQueryWithProvider(customScoreProviderConstructor, query);
    }

    @Override
    protected boolean isEqual(final CustomScoreQuery q) {
        return Objects.equals(subQuery, q.subQuery) && Objects.equals(scoringQuery, q.scoringQuery) &&
                Arrays.equals(scoringQueries, q.scoringQueries) &&
                Objects.equals(customScoreProviderClassName, q.customScoreProviderClassName) &&
                Objects.equals(customScoreProviderClass, q.customScoreProviderClass);
    }

    private static class CustomScoreQueryWithProvider extends org.apache.lucene.queries.CustomScoreQuery {

        private final Constructor<? extends CustomScoreProvider> customScoreProviderConstructor;

        private CustomScoreQueryWithProvider(
                final Constructor<? extends CustomScoreProvider> customScoreProviderConstructor, final Query subQuery) {
            super(subQuery);
            this.customScoreProviderConstructor = customScoreProviderConstructor;
        }

        private CustomScoreQueryWithProvider(
                final Constructor<? extends CustomScoreProvider> customScoreProviderConstructor, final Query subQuery,
                final org.apache.lucene.queries.function.FunctionQuery scoringQuery) {
            super(subQuery, scoringQuery);
            this.customScoreProviderConstructor = customScoreProviderConstructor;
        }

        private CustomScoreQueryWithProvider(
                final Constructor<? extends CustomScoreProvider> customScoreProviderConstructor, final Query subQuery,
                final org.apache.lucene.queries.function.FunctionQuery[] scoringQueries) {
            super(subQuery, scoringQueries);
            this.customScoreProviderConstructor = customScoreProviderConstructor;
        }

        protected CustomScoreProvider getCustomScoreProvider(final LeafReaderContext context) {
            try {
                return customScoreProviderConstructor.newInstance(context);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
