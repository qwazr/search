/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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
import com.qwazr.utils.CollectionsUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BooleanQuery extends AbstractQuery<BooleanQuery> {

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

		@JsonCreator
		public BooleanClause(@JsonProperty("occur") final Occur occur,
				@JsonProperty("query") final AbstractQuery query) {
			this.occur = occur;
			this.query = query;
		}

		@JsonIgnore
		private org.apache.lucene.search.BooleanClause getNewClause(final QueryContext queryContext)
				throws IOException, ParseException, QueryNodeException, ReflectiveOperationException {
			Objects.requireNonNull(occur, "Occur must not be null");
			return new org.apache.lucene.search.BooleanClause(query.getQuery(queryContext), occur.occur);
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof BooleanClause))
				return false;
			if (o == this)
				return true;
			final BooleanClause bc = (BooleanClause) o;
			return Objects.equals(occur, bc.occur) && Objects.equals(query, bc.query);
		}
	}

	@JsonCreator
	public BooleanQuery(@JsonProperty("disable_coord") final Boolean disable_coord,
			@JsonProperty("minimum_number_should_match") final Integer minimum_number_should_match,
			@JsonProperty("clauses") final List<BooleanClause> clauses) {
		super(BooleanQuery.class);
		this.disable_coord = disable_coord;
		this.minimum_number_should_match = minimum_number_should_match;
		this.clauses = clauses;
	}

	public BooleanQuery(final List<BooleanClause> clauses) {
		super(BooleanQuery.class);
		this.disable_coord = null;
		this.minimum_number_should_match = null;
		this.clauses = clauses;
	}

	public BooleanQuery(final BooleanClause... clauses) {
		super(BooleanQuery.class);
		this.disable_coord = null;
		this.minimum_number_should_match = null;
		this.clauses = Arrays.asList(clauses);
	}

	public BooleanQuery(final Boolean disable_coord, final List<BooleanClause> clauses) {
		super(BooleanQuery.class);
		this.disable_coord = disable_coord;
		this.minimum_number_should_match = null;
		this.clauses = clauses;
	}

	public BooleanQuery(final Boolean disable_coord, final BooleanClause... clauses) {
		super(BooleanQuery.class);
		this.disable_coord = disable_coord;
		this.minimum_number_should_match = null;
		this.clauses = Arrays.asList(clauses);
	}

	public BooleanQuery(final Boolean disable_coord, final Integer minimum_number_should_match,
			final BooleanClause... clauses) {
		super(BooleanQuery.class);
		this.disable_coord = disable_coord;
		this.minimum_number_should_match = minimum_number_should_match;
		this.clauses = Arrays.asList(clauses);
	}

	private BooleanQuery(final Builder builder) {
		super(BooleanQuery.class);
		this.disable_coord = builder.disableCoord;
		this.minimum_number_should_match = builder.minimumNumberShouldMatch;
		this.clauses = builder.clauses == null ? Collections.emptyList() : new ArrayList<>(builder.clauses);
	}

	@Override
	final public Query getQuery(final QueryContext queryContext)
			throws IOException, ParseException, QueryNodeException, ReflectiveOperationException {
		final org.apache.lucene.search.BooleanQuery.Builder builder =
				new org.apache.lucene.search.BooleanQuery.Builder();
		if (disable_coord != null)
			builder.setDisableCoord(disable_coord);
		if (minimum_number_should_match != null)
			builder.setMinimumNumberShouldMatch(minimum_number_should_match);
		if (clauses != null)
			for (BooleanClause clause : clauses)
				builder.add(clause.getNewClause(queryContext));
		return builder.build();
	}

	@Override
	protected boolean isEqual(BooleanQuery query) {
		return CollectionsUtils.equals(clauses, query.clauses) && Objects.equals(disable_coord, query.disable_coord) &&
				Objects.equals(minimum_number_should_match, query.minimum_number_should_match);
	}

	public static Builder of() {
		return new Builder();
	}

	public static Builder of(final Boolean disableCoord, final Integer minimumNumberShouldMatch) {
		return of().setDisableCoord(disableCoord).setMinimumNumberShouldMatch(minimumNumberShouldMatch);
	}

	public final static class Builder {

		private final List<BooleanClause> clauses;
		private Boolean disableCoord;
		private Integer minimumNumberShouldMatch;

		public Builder() {
			clauses = new ArrayList<>();
		}

		public final Builder setDisableCoord(final Boolean disableCoord) {
			this.disableCoord = disableCoord;
			return this;
		}

		public final Builder setMinimumNumberShouldMatch(final Integer minimumNumberShouldMatch) {
			this.minimumNumberShouldMatch = minimumNumberShouldMatch;
			return this;
		}

		public final Builder addClause(final Occur occur, final AbstractQuery query) {
			return addClause(new BooleanClause(occur, query));
		}

		public final Builder addClause(final BooleanClause booleanClause) {
			clauses.add(booleanClause);
			return this;
		}

		public final Builder addClauses(final BooleanClause... booleanClauses) {
			Collections.addAll(clauses, booleanClauses);
			return this;
		}

		public final Builder setClause(final BooleanClause booleanClause) {
			clauses.clear();
			return addClause(booleanClause);
		}

		public final Builder setClauses(final BooleanClause... booleanClauses) {
			clauses.clear();
			return addClauses(booleanClauses);
		}

		public final Builder filter(final AbstractQuery query) {
			return addClause(Occur.filter, query);
		}

		public final Builder must(final AbstractQuery query) {
			return addClause(Occur.must, query);
		}

		public final Builder mustNot(final AbstractQuery query) {
			return addClause(Occur.must_not, query);
		}

		public final Builder should(final AbstractQuery query) {
			return addClause(Occur.should, query);
		}

		public final int getSize() {
			return clauses.size();
		}

		final public BooleanQuery build() {
			return new BooleanQuery(this);
		}
	}
}
