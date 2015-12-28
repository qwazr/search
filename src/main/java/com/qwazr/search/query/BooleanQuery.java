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
import com.qwazr.search.index.QueryContext;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class BooleanQuery extends AbstractQuery {

	final public List<BooleanClause> clauses;
	final public Boolean disable_coord;
	final public Integer minimum_number_should_match;

	public enum Occur {

		must(org.apache.lucene.search.BooleanClause.Occur.MUST),
		should(org.apache.lucene.search.BooleanClause.Occur.SHOULD),
		must_not(org.apache.lucene.search.BooleanClause.Occur.MUST_NOT),
		filter(org.apache.lucene.search.BooleanClause.Occur.FILTER);

		private final org.apache.lucene.search.BooleanClause.Occur occur;

		Occur(org.apache.lucene.search.BooleanClause.Occur occur) {
			this.occur = occur;
		}
	}

	public static class BooleanClause {

		public final Occur occur;
		public final AbstractQuery query;

		public BooleanClause() {
			occur = null;
			query = null;
		}

		BooleanClause(Occur occur, AbstractQuery query) {
			this.occur = null;
			this.query = null;
		}

		@JsonIgnore
		final private org.apache.lucene.search.BooleanClause getNewClause(QueryContext queryContext)
			throws IOException, ParseException, QueryNodeException, ReflectiveOperationException {
			Objects.requireNonNull(occur, "Occur must not be null");
			return new org.apache.lucene.search.BooleanClause(query.getQuery(queryContext), occur.occur);
		}
	}

	public BooleanQuery() {
		super(null);
		clauses = null;
		disable_coord = null;
		minimum_number_should_match = null;
	}

	BooleanQuery(Float boost, Boolean disable_coord, Integer minimum_number_should_match, List<BooleanClause> clauses) {
		super(boost);
		this.disable_coord = disable_coord;
		this.minimum_number_should_match = minimum_number_should_match;
		this.clauses = clauses;
	}

	@Override
	final protected Query getQuery(QueryContext queryContext)
		throws IOException, ParseException, QueryNodeException, ReflectiveOperationException {
		final org.apache.lucene.search.BooleanQuery.Builder builder = new org.apache.lucene.search.BooleanQuery.Builder();
		if (disable_coord != null)
			builder.setDisableCoord(disable_coord);
		if (minimum_number_should_match != null)
			builder.setMinimumNumberShouldMatch(minimum_number_should_match);
		if (clauses != null)
			for (BooleanClause clause : clauses)
				builder.add(clause.getNewClause(queryContext));
		return builder.build();
	}
}
