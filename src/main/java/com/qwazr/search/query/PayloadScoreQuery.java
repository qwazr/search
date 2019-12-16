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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.index.QueryContext;
import com.qwazr.utils.ClassLoaderUtils;
import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.queries.payloads.AveragePayloadFunction;
import org.apache.lucene.queries.payloads.MaxPayloadFunction;
import org.apache.lucene.queries.payloads.MinPayloadFunction;
import org.apache.lucene.queries.payloads.PayloadFunction;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.Objects;

public class PayloadScoreQuery extends AbstractQuery<PayloadScoreQuery> {

    @JsonProperty("wrapped_query")
    public final AbstractSpanQuery wrappedQuery;

    @JsonProperty("include_span_score")
    public final Boolean includeSpanScore;

    @JsonProperty("payload_function")
    public final String payloadFunction;

    @JsonIgnore
    private PayloadFunction payloadFunctionInstance;

    public enum FunctionType {
        MIN, MAX, AVERAGE
    }

    public PayloadScoreQuery(final AbstractSpanQuery wrappedQuery, final PayloadFunction payloadFunction,
                             final Boolean includeSpanScore) {
        super(PayloadScoreQuery.class);
        this.wrappedQuery = wrappedQuery;
        this.payloadFunctionInstance = payloadFunction;
        this.payloadFunction = payloadFunction == null ? null : payloadFunction.getClass().getName();
        this.includeSpanScore = includeSpanScore == null ? Boolean.FALSE : includeSpanScore;
    }

    @JsonCreator
    public PayloadScoreQuery(@JsonProperty("wrapped_query") final AbstractSpanQuery wrappedQuery,
                             @JsonProperty("payload_function") final String payloadFunction,
                             @JsonProperty("include_span_score") final Boolean includeSpanScore) {
        super(PayloadScoreQuery.class);
        this.wrappedQuery = wrappedQuery;
        this.payloadFunction = payloadFunction;
        this.payloadFunctionInstance = null;
        this.includeSpanScore = includeSpanScore == null ? Boolean.FALSE : includeSpanScore;
    }

    public PayloadScoreQuery(final AbstractSpanQuery wrappedQuery, final FunctionType type,
                             final Boolean includeSpanScore) {
        super(PayloadScoreQuery.class);
        this.wrappedQuery = wrappedQuery;
        if (type != null) {
            switch (type) {
                case AVERAGE:
                    payloadFunctionInstance = new AveragePayloadFunction();
                    break;
                case MAX:
                    payloadFunctionInstance = new MaxPayloadFunction();
                    break;
                case MIN:
                    payloadFunctionInstance = new MinPayloadFunction();
                    break;
                default:
                    payloadFunctionInstance = null;
                    break;
            }
            payloadFunction = null;
        } else {
            payloadFunction = null;
            payloadFunctionInstance = null;
        }
        this.includeSpanScore = includeSpanScore == null ? Boolean.TRUE : includeSpanScore;
    }

    @Override
    protected boolean isEqual(final PayloadScoreQuery q) {
        return Objects.equals(wrappedQuery, q.wrappedQuery) && Objects.equals(includeSpanScore, q.includeSpanScore) &&
                Objects.equals(payloadFunctionInstance, q.payloadFunctionInstance) &&
                Objects.equals(payloadFunction, q.payloadFunction);
    }

    final static String[] payloadFunctionClassPrefixes = {"", "org.apache.lucene.queries.payloads."};

    private static PayloadFunction getPayloadFunction(final String payloadFunction)
            throws ReflectiveOperationException {
        return (PayloadFunction) ClassLoaderUtils.findClass(payloadFunction, payloadFunctionClassPrefixes)
                .getDeclaredConstructor().newInstance();
    }

    @Override
    final public Query getQuery(final QueryContext queryContext)
            throws IOException, ParseException, QueryNodeException, ReflectiveOperationException {
        Objects.requireNonNull(wrappedQuery, "The wrapped span query is missing");
        if (payloadFunctionInstance == null) {
            Objects.requireNonNull(payloadFunction, "The payload function is missing");
            payloadFunctionInstance = getPayloadFunction(payloadFunction);
        }
        Objects.requireNonNull(payloadFunction, "The payload function is missing");
        return new org.apache.lucene.queries.payloads.PayloadScoreQuery(wrappedQuery.getQuery(queryContext),
                payloadFunctionInstance, b -> PayloadHelper.decodeInt(b.bytes, b.offset), includeSpanScore);
    }
}
