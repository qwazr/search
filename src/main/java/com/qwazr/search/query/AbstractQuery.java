/**
 * Copyright 2015 Emmanuel Keller / QWAZR
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
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.qwazr.search.index.UpdatableAnalyzer;
import org.apache.lucene.search.Query;

import java.io.IOException;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "query")
@JsonSubTypes({ @JsonSubTypes.Type(value = BooleanQuery.class, name = "boolean_query"),
		@JsonSubTypes.Type(value = PhraseQuery.class, name = "phrase_query"),
		@JsonSubTypes.Type(value = SpanFirstQueries.class, name = "span_first_queries"),
		@JsonSubTypes.Type(value = SpanFirstQuery.class, name = "span_first_query"),
		@JsonSubTypes.Type(value = SpanNearQuery.class, name = "span_near_query"),
		@JsonSubTypes.Type(value = SpanNotQuery.class, name = "span_not_query"),
		@JsonSubTypes.Type(value = SpanTermQuery.class, name = "span_term_query"),
		@JsonSubTypes.Type(value = TermQuery.class, name = "term_query"),
		@JsonSubTypes.Type(value = TermRangeQuery.class, name = "term_range_query") })

public abstract class AbstractQuery {

	public final Float boost;

	protected AbstractQuery(Float boost) {
		this.boost = boost;
	}

	@JsonIgnore
	protected abstract Query getQuery(UpdatableAnalyzer analyzer) throws IOException;

	public final Query getBoostedQuery(UpdatableAnalyzer analyzer) throws IOException {
		Query query = getQuery(analyzer);
		if (boost != null)
			query.setBoost(boost);
		return query;
	}
}
