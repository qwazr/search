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

import com.qwazr.search.index.QueryContext;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DisjunctionMaxQuery extends AbstractQuery {

	final public List<AbstractQuery> queries;
	final public Float tie_breaker_multiplier;

	public DisjunctionMaxQuery() {
		queries = null;
		tie_breaker_multiplier = null;
	}

	public DisjunctionMaxQuery(final List<AbstractQuery> queries, final Float tie_breaker_multiplier) {
		this.queries = queries;
		this.tie_breaker_multiplier = tie_breaker_multiplier;
	}

	public DisjunctionMaxQuery(final Float tie_breaker_multiplier, final AbstractQuery... queries) {
		this.queries = Arrays.asList(queries);
		this.tie_breaker_multiplier = tie_breaker_multiplier;
	}

	@Override
	final public Query getQuery(final QueryContext queryContext)
			throws IOException, ParseException, QueryNodeException, ReflectiveOperationException, InterruptedException {
		Objects.requireNonNull(queries, "The queries are missing");
		final List<Query> queryList = new ArrayList<>(queries.size());
		for (AbstractQuery query : queries)
			queryList.add(query.getQuery(queryContext));
		return new org.apache.lucene.search.DisjunctionMaxQuery(queryList,
				tie_breaker_multiplier == null ? 0 : tie_breaker_multiplier);
	}
}
