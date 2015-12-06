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

import com.qwazr.search.analysis.UpdatableAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.spans.SpanQuery;

import java.io.IOException;
import java.util.List;

public class SpanNearQuery extends AbstractSpanQuery {

	final public List<AbstractSpanQuery> clauses;
	final public String field;
	final public Boolean in_order;
	final public Integer slop;

	public SpanNearQuery() {
		super(null);
		clauses = null;
		field = null;
		in_order = null;
		slop = null;
	}

	SpanNearQuery(Float boost, List<AbstractSpanQuery> clauses, String field, Boolean in_order, Integer slop) {
		super(boost);
		this.clauses = clauses;
		this.field = field;
		this.in_order = in_order;
		this.slop = slop;
	}

	@Override
	final protected SpanQuery getQuery(UpdatableAnalyzer analyzer, String queryString)
			throws IOException, ParseException, QueryNodeException {
		final org.apache.lucene.search.spans.SpanNearQuery.Builder builder = new org.apache.lucene.search.spans.SpanNearQuery.Builder(
				field, in_order == null ? false : in_order);
		if (slop != null)
			builder.setSlop(slop);
		if (clauses != null)
			for (AbstractSpanQuery clause : clauses)
				builder.addClause(clause.getQuery(analyzer, queryString));
		return builder.build();
	}
}
