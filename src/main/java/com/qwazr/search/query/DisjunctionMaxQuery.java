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
import java.util.Arrays;
import java.util.List;

public class DisjunctionMaxQuery extends AbstractQuery {

	final public List<AbstractQuery> queries;
	final public Float tie_breaker_multiplier;

	public DisjunctionMaxQuery() {
		super(null);
		queries = null;
		tie_breaker_multiplier = null;
	}

	public DisjunctionMaxQuery(List<AbstractQuery> queries, Float tie_breaker_multiplier) {
		super(null);
		this.queries = queries;
		this.tie_breaker_multiplier = tie_breaker_multiplier;
	}

	public DisjunctionMaxQuery(Float tie_breaker_multiplier, AbstractQuery... queries) {
		super(null);
		this.queries = Arrays.asList(queries);
		this.tie_breaker_multiplier = tie_breaker_multiplier;
	}

	public DisjunctionMaxQuery(Float boost, List<AbstractQuery> queries, Float tie_breaker_multiplier) {
		super(boost);
		this.queries = queries;
		this.tie_breaker_multiplier = tie_breaker_multiplier;
	}

	public DisjunctionMaxQuery(Float boost, Float tie_breaker_multiplier, AbstractQuery... queries) {
		super(boost);
		this.queries = Arrays.asList(queries);
		this.tie_breaker_multiplier = tie_breaker_multiplier;
	}

	@Override
	final protected Query getQuery(QueryContext queryContext)
					throws IOException, ParseException, QueryNodeException, ReflectiveOperationException {
		org.apache.lucene.search.DisjunctionMaxQuery disjunctionMaxQuery = new org.apache.lucene.search.DisjunctionMaxQuery(
						tie_breaker_multiplier == null ? 0 : tie_breaker_multiplier);
		if (queries != null)
			for (AbstractQuery query : queries)
				disjunctionMaxQuery.add(query.getQuery(queryContext));
		return disjunctionMaxQuery;
	}
}
