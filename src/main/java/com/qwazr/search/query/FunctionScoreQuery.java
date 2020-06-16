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
import com.qwazr.search.function.DoubleValuesSource;
import com.qwazr.search.index.QueryContext;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.Objects;

public class FunctionScoreQuery extends AbstractQuery<FunctionScoreQuery> {

    public final AbstractQuery<?> inQuery;
    public final AbstractQuery<?> boostMatchQuery;
    public final Float boostValue;
    public final DoubleValuesSource<?> boost;

    @JsonCreator
    private FunctionScoreQuery(@JsonProperty("in") final AbstractQuery<?> inQuery,
                               @JsonProperty("boostMatch") final AbstractQuery<?> boostMatchQuery,
                               @JsonProperty("boostValue") final Float boostValue,
                               @JsonProperty("boost") final DoubleValuesSource<?> boost) {
        super(FunctionScoreQuery.class);
        this.inQuery = inQuery;
        this.boostMatchQuery = boostMatchQuery;
        this.boostValue = boostValue;
        this.boost = boost;
    }

    public FunctionScoreQuery(final AbstractQuery<?> inQuery, final AbstractQuery<?> boostMatchQuery, final Float boostValue) {
        this(inQuery, boostMatchQuery, boostValue, null);
    }

    public FunctionScoreQuery(final AbstractQuery<?> inQuery, final DoubleValuesSource<?> boost) {
        this(inQuery, null, null, boost);
    }

    @Override
    final public Query getQuery(final QueryContext queryContext)
        throws IOException, ParseException, QueryNodeException, ReflectiveOperationException {
        Objects.requireNonNull(inQuery, "Missing inQuery property");
        final Query in = inQuery.getQuery(queryContext);
        if (boost != null) {
            return org.apache.lucene.queries.function.FunctionScoreQuery.boostByValue(in, boost.getValueSource(queryContext));
        } else {
            Objects.requireNonNull(boostMatchQuery, "Missing boostMatch property");
            Objects.requireNonNull(boostValue, "Missing boostValue property");
            return org.apache.lucene.queries.function.FunctionScoreQuery.boostByQuery(in, boostMatchQuery.getQuery(queryContext), boostValue);
        }
    }


    @Override
    protected boolean isEqual(final FunctionScoreQuery q) {
        return Objects.equals(inQuery, q.inQuery) && Objects.equals(boostMatchQuery, q.boostMatchQuery) &&
            Objects.equals(boostValue, q.boostValue) && Objects.equals(boost, q.boost);
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(inQuery, boostMatchQuery, boostValue, boost);
    }
}
