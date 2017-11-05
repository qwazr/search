/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.index.QueryContext;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.Objects;

public class BoostingQuery extends AbstractQuery<BoostingQuery> {

	@JsonProperty("match_query")
	public final AbstractQuery matchQuery;

	@JsonProperty("context_query")
	public final AbstractQuery contextQuery;

	public final Float boost;

	@JsonCreator
	public BoostingQuery(@JsonProperty("match_query") final AbstractQuery matchQuery,
			@JsonProperty("context_query") final AbstractQuery contextQuery, @JsonProperty("boost") final Float boost) {
		super(BoostingQuery.class);
		this.matchQuery = Objects.requireNonNull(matchQuery, "The match query is missing");
		this.contextQuery = Objects.requireNonNull(contextQuery, "The context query is missing");
		this.boost = boost;
	}

	@Override
	final public Query getQuery(final QueryContext queryContext)
			throws IOException, ParseException, QueryNodeException, ReflectiveOperationException {
		return new org.apache.lucene.queries.BoostingQuery(matchQuery.getQuery(queryContext),
				contextQuery.getQuery(queryContext), boost == null ? 1.0f : boost);
	}

	@Override
	protected boolean isEqual(final BoostingQuery q) {
		return Objects.equals(matchQuery, q.matchQuery) && Objects.equals(contextQuery, q.contextQuery) &&
				Objects.equals(boost, q.boost);
	}
}
