/**
 * Copyright 2015-2016 Emmanuel Keller / QWAZR
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qwazr.classloader.ClassLoaderManager;
import com.qwazr.search.index.QueryContext;
import org.apache.lucene.queries.payloads.AveragePayloadFunction;
import org.apache.lucene.queries.payloads.MaxPayloadFunction;
import org.apache.lucene.queries.payloads.MinPayloadFunction;
import org.apache.lucene.queries.payloads.PayloadFunction;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.Objects;

public class PayloadScoreQuery extends AbstractQuery {

	public final AbstractSpanQuery wrapped_query;
	public final Boolean include_span_score;

	@JsonIgnore
	private PayloadFunction payloadFunction;

	public final String payload_function;

	public enum FunctionType {
		MIN, MAX, AVERAGE;
	}

	public PayloadScoreQuery() {
		wrapped_query = null;
		payloadFunction = null;
		payload_function = null;
		include_span_score = null;
	}

	public PayloadScoreQuery(final AbstractSpanQuery wrappedQuery, final PayloadFunction payloadFunction,
			final Boolean includeSpanScore) {
		this.wrapped_query = wrappedQuery;
		this.payloadFunction = payloadFunction;
		this.payload_function = null;
		this.include_span_score = includeSpanScore == null ? true : includeSpanScore;
	}

	public PayloadScoreQuery(final AbstractSpanQuery wrappedQuery, final String payloadFunction,
			final Boolean includeSpanScore) throws ReflectiveOperationException {
		this.wrapped_query = wrappedQuery;
		this.payload_function = payloadFunction;
		this.payloadFunction = null;
		this.include_span_score = includeSpanScore == null ? true : includeSpanScore;
	}

	public PayloadScoreQuery(final AbstractSpanQuery wrappedQuery, final FunctionType type,
			final Boolean includeSpanScore) {
		this.wrapped_query = wrappedQuery;
		if (type != null) {
			switch (type) {
				case AVERAGE:
					payloadFunction = new AveragePayloadFunction();
					break;
				case MAX:
					payloadFunction = new MaxPayloadFunction();
					break;
				case MIN:
					payloadFunction = new MinPayloadFunction();
					break;
				default:
					payloadFunction = null;
					break;
			}
			payload_function = null;
		} else {
			payloadFunction = null;
			payload_function = null;
		}
		this.include_span_score = includeSpanScore == null ? true : includeSpanScore;
	}

	final static String[] payloadFunctionClassPrefixes = {"", "org.apache.lucene.queries.payloads."};

	private static PayloadFunction getPayloadFunction(final String payloadFunction)
			throws ReflectiveOperationException, IOException {
		return ClassLoaderManager.getInstance().newInstance(payloadFunction, payloadFunctionClassPrefixes);
	}

	@Override
	final public Query getQuery(final QueryContext queryContext)
			throws IOException, ParseException, QueryNodeException, ReflectiveOperationException {
		Objects.requireNonNull(wrapped_query, "The wrapped span query is missing");
		if (payloadFunction == null) {
			Objects.requireNonNull(payload_function, "The payload function is missing");
			payloadFunction = getPayloadFunction(payload_function);
		}
		Objects.requireNonNull(payloadFunction, "The payload function is missing");
		return new org.apache.lucene.queries.payloads.PayloadScoreQuery(wrapped_query.getQuery(queryContext),
				payloadFunction, include_span_score == null ? false : include_span_score);
	}
}
