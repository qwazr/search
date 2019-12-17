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
import org.apache.lucene.queries.payloads.PayloadDecoder;
import org.apache.lucene.queries.payloads.PayloadFunction;
import org.apache.lucene.queries.payloads.SumPayloadFunction;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.Objects;

public class PayloadScoreQuery extends AbstractQuery<PayloadScoreQuery> {

    @JsonProperty("wrapped_query")
    public final AbstractSpanQuery wrappedQuery;

    @JsonProperty("include_span_score")
    public final Boolean includeSpanScore;

    @JsonProperty("payload_function")
    public final String payloadFunction;

    @JsonProperty("payload_decoder")
    public final String payloadDecoder;

    @JsonIgnore
    private final PayloadDecoder payloadDecoderInstance;

    @JsonIgnore
    private final PayloadFunction payloadFunctionInstance;

    public enum FunctionType {

        MIN(new MinPayloadFunction()),
        MAX(new MaxPayloadFunction()),
        AVERAGE(new AveragePayloadFunction()),
        SUM(new SumPayloadFunction());

        public final PayloadFunction payloadFunction;

        FunctionType(final PayloadFunction payloadFunction) {
            this.payloadFunction = payloadFunction;
        }
    }

    public enum DecoderType implements PayloadDecoder {
        INTEGER() {
            @Override
            public float computePayloadFactor(BytesRef payload) {
                return PayloadHelper.decodeFloat(payload.bytes, payload.offset);
            }
        }, FLOAT {
            @Override
            public float computePayloadFactor(BytesRef payload) {
                return PayloadHelper.decodeInt(payload.bytes, payload.offset);
            }
        }
    }

    public PayloadScoreQuery(final AbstractSpanQuery<?> wrappedQuery,
                             final PayloadFunction payloadFunctionInstance,
                             final PayloadDecoder payloadDecoderInstance,
                             final Boolean includeSpanScore) {
        super(PayloadScoreQuery.class);
        this.wrappedQuery = Objects.requireNonNull(wrappedQuery, "The wrapped span query is missing");
        this.payloadFunctionInstance = Objects.requireNonNull(payloadFunctionInstance, "The payload function is missing");
        this.payloadFunction = payloadFunctionInstance.getClass().getName();
        this.payloadDecoderInstance = Objects.requireNonNull(payloadDecoderInstance, "The payload decoder is missing");
        this.payloadDecoder = payloadDecoderInstance.getClass().getName();
        this.includeSpanScore = includeSpanScore == null ? Boolean.FALSE : includeSpanScore;
    }

    @JsonCreator
    public PayloadScoreQuery(@JsonProperty("wrapped_query") final AbstractSpanQuery<?> wrappedQuery,
                             @JsonProperty("payload_function") final String payloadFunction,
                             @JsonProperty("payload_decoder") final String payloadDecoder,
                             @JsonProperty("include_span_score") final Boolean includeSpanScore) throws ReflectiveOperationException {
        super(PayloadScoreQuery.class);
        this.wrappedQuery = Objects.requireNonNull(wrappedQuery, "The wrapped span query is missing");
        this.payloadFunction = Objects.requireNonNull(payloadFunction, "The payload function is missing");
        this.payloadFunctionInstance = getPayloadFunction(payloadFunction);
        this.payloadDecoder = Objects.requireNonNull(payloadDecoder, "The payload decoder is missing");
        this.payloadDecoderInstance = getPayloadDecoder(payloadDecoder);
        this.includeSpanScore = includeSpanScore == null ? Boolean.FALSE : includeSpanScore;
    }

    public PayloadScoreQuery(final AbstractSpanQuery<?> wrappedQuery,
                             final FunctionType functionType,
                             final DecoderType decoderType,
                             final Boolean includeSpanScore) {
        this(wrappedQuery,
                Objects.requireNonNull(functionType, "The function is missing").payloadFunction,
                Objects.requireNonNull(decoderType, "The decoder is missing"),
                includeSpanScore);
    }

    @Override
    protected boolean isEqual(final PayloadScoreQuery q) {
        return Objects.equals(wrappedQuery, q.wrappedQuery) &&
                Objects.equals(includeSpanScore, q.includeSpanScore) &&
                Objects.equals(payloadFunctionInstance, q.payloadFunctionInstance) &&
                Objects.equals(payloadFunction, q.payloadFunction) &&
                Objects.equals(payloadDecoderInstance, q.payloadDecoderInstance) &&
                Objects.equals(payloadDecoder, q.payloadDecoder);
    }

    final static String[] payloadFunctionClassPrefixes = {"", "org.apache.lucene.queries.payloads."};

    private static PayloadFunction getPayloadFunction(final String payloadFunction)
            throws ReflectiveOperationException {
        return (PayloadFunction) ClassLoaderUtils.findClass(payloadFunction, payloadFunctionClassPrefixes)
                .getDeclaredConstructor().newInstance();
    }

    private static PayloadDecoder getPayloadDecoder(final String payloadDecoder)
            throws ReflectiveOperationException {
        return (PayloadDecoder) ClassLoaderUtils.findClass(payloadDecoder)
                .getDeclaredConstructor().newInstance();
    }

    @Override
    final public Query getQuery(final QueryContext queryContext)
            throws IOException, ParseException, QueryNodeException, ReflectiveOperationException {
        return new org.apache.lucene.queries.payloads.PayloadScoreQuery(wrappedQuery.getQuery(queryContext),
                payloadFunctionInstance, payloadDecoderInstance, includeSpanScore);
    }
}
