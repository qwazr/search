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
import com.qwazr.search.analysis.TermConsumer;
import com.qwazr.search.index.QueryContext;
import com.qwazr.utils.FunctionUtils;
import com.qwazr.utils.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MultiFieldQuery extends AbstractQuery {

	@JsonProperty("fields_boosts")
	final public Map<String, Float> fieldsBoosts;

	@JsonProperty("default_operator")
	final public QueryParserOperator defaultOperator;

	@JsonProperty("query_string")
	final public String queryString;

	@JsonProperty("min_number_should_match")
	final public Integer minNumberShouldMatch;

	@JsonProperty("tie_breaker_multiplier")
	final public Float tieBreakerMultiplier;

	final private Analyzer analyzer;

	public MultiFieldQuery(final QueryParserOperator defaultOperator, final String queryString) {
		this(new LinkedHashMap<>(), defaultOperator, queryString, null, null, null);
	}

	public MultiFieldQuery(final QueryParserOperator defaultOperator, final String queryString,
			final Integer minNumberShouldMatch) {
		this(new LinkedHashMap<>(), defaultOperator, queryString, minNumberShouldMatch, null, null);
	}

	public MultiFieldQuery(final QueryParserOperator defaultOperator, final String queryString,
			final Integer minNumberShouldMatch, final Float tieBreakerMultiplier) {
		this(new LinkedHashMap<>(), defaultOperator, queryString, minNumberShouldMatch, tieBreakerMultiplier, null);
	}

	public MultiFieldQuery(final Map<String, Float> fieldsBoosts, final QueryParserOperator defaultOperator,
			final String queryString, final Integer minNumberShouldMatch) {
		this(fieldsBoosts, defaultOperator, queryString, minNumberShouldMatch, null, null);
	}

	public MultiFieldQuery(final QueryParserOperator defaultOperator, final String queryString,
			final Integer minNumberShouldMatch, final Float tieBreakerMultiplier, final Analyzer analyzer) {
		this(new LinkedHashMap<>(), defaultOperator, queryString, minNumberShouldMatch, tieBreakerMultiplier, analyzer);
	}

	public MultiFieldQuery(final Map<String, Float> fieldsBoosts, final QueryParserOperator defaultOperator,
			final String queryString, final Integer minNumberShouldMatch, final Float tieBreakerMultiplier,
			final Analyzer analyser) {
		this.fieldsBoosts = fieldsBoosts;
		this.defaultOperator = defaultOperator;
		this.queryString = queryString;
		this.minNumberShouldMatch = minNumberShouldMatch;
		this.tieBreakerMultiplier = tieBreakerMultiplier;
		this.analyzer = analyser;
	}

	@JsonCreator
	public MultiFieldQuery(@JsonProperty("fields_boosts") final Map<String, Float> fieldsBoosts,
			@JsonProperty("default_operator") final QueryParserOperator defaultOperator,
			@JsonProperty("query_string") final String queryString,
			@JsonProperty("min_number_should_match") final Integer minNumberShouldMatch,
			@JsonProperty("tie_breaker_multiplier") final Float tieBreakerMultiplier) {
		this(fieldsBoosts, defaultOperator, queryString, minNumberShouldMatch, tieBreakerMultiplier, null);
	}

	@JsonIgnore
	public MultiFieldQuery boost(final String field, final Float boost) {
		Objects.requireNonNull(field, "The field is missing");
		Objects.requireNonNull(fieldsBoosts);
		if (boost != null)
			fieldsBoosts.put(field, boost);
		else
			fieldsBoosts.put(field, boost);
		return this;
	}

	@Override
	final public Query getQuery(final QueryContext queryContext) throws IOException, ReflectiveOperationException {
		Objects.requireNonNull(fieldsBoosts, "Fields boosts is missing");

		if (StringUtils.isEmpty(queryString))
			return new org.apache.lucene.search.MatchNoDocsQuery();

		// Select the right analyzer
		final Analyzer alzr = analyzer != null ? analyzer : queryContext.getQueryAnalyzer();

		// We look for terms frequency globally
		final Map<String, Integer> termsFreq = new HashMap<>();
		final IndexReader indexReader = queryContext.getIndexReader();
		FunctionUtils.forEachEx(fieldsBoosts, (field, boost) -> {
			try (final TokenStream tokenStream = alzr.tokenStream(field, queryString)) {
				new TermsWithFreq(tokenStream, indexReader, field, termsFreq).forEachToken();
				tokenStream.end();
			}
		});

		// Build the query
		return new FieldQueriesBuilder(alzr, termsFreq).parse(queryString, defaultOperator == null ||
				defaultOperator.queryParseroperator == QueryParser.Operator.AND && minNumberShouldMatch == null ?
				BooleanClause.Occur.MUST :
				BooleanClause.Occur.SHOULD);
	}

	protected Query getRootQuery(final Collection<Query> queries) {
		if (queries.size() == 1)
			return queries.iterator().next();
		if (tieBreakerMultiplier != null) {
			return new org.apache.lucene.search.DisjunctionMaxQuery(queries, tieBreakerMultiplier);
		} else {
			final BooleanQuery.Builder builder = new org.apache.lucene.search.BooleanQuery.Builder();
			queries.forEach(query -> builder.add(query, BooleanClause.Occur.SHOULD));
			return builder.build();
		}
	}

	protected Query getTermQuery(final int freq, final Term term) {
		Query query;
		if (freq > 0)
			query = new org.apache.lucene.search.TermQuery(term);
		else
			query = new org.apache.lucene.search.FuzzyQuery(term);
		return query;
	}

	private class TermsWithFreq extends TermConsumer.WithChar {

		private final IndexReader indexReader;
		private final String field;
		private final Map<String, Integer> termsFreq;

		private TermsWithFreq(final TokenStream tokenStream, final IndexReader indexReader, final String field,
				final Map<String, Integer> termsFreq) {
			super(tokenStream);
			this.indexReader = indexReader;
			this.field = field;
			this.termsFreq = termsFreq;
		}

		@Override
		final public boolean token() throws IOException {
			final String text = charTermAttr.toString();
			final Term term = new Term(field, text);
			final int newFreq = indexReader == null ? 0 : indexReader.docFreq(term);
			if (newFreq > 0) {
				final Integer previousFreq = termsFreq.get(text);
				if (previousFreq == null || newFreq > previousFreq)
					termsFreq.put(text, newFreq);
			}
			return true;
		}
	}

	final class FieldQueriesBuilder extends org.apache.lucene.util.QueryBuilder {

		final Map<String, Integer> termsFreq;

		private FieldQueriesBuilder(final Analyzer analyzer, final Map<String, Integer> termsFreq) {
			super(analyzer);
			this.termsFreq = termsFreq;
		}

		@Override
		final protected Query newTermQuery(Term term) {
			final Integer freq = termsFreq.get(term.text());
			return getTermQuery(freq == null ? 0 : freq, term);
		}

		protected BooleanQuery.Builder newBooleanQuery() {
			return new FieldBooleanBuilder();
		}

		final Query parse(final String queryString, final BooleanClause.Occur defaultOperator) {
			final List<Query> fieldQueries = new ArrayList<>();
			fieldsBoosts.forEach((field, boost) -> {
				Query fieldQuery = createBooleanQuery(field, queryString, defaultOperator);
				if (boost != null && boost != 1.0F)
					fieldQuery = new org.apache.lucene.search.BoostQuery(fieldQuery, boost);
				fieldQueries.add(fieldQuery);
			});
			return getRootQuery(fieldQueries);
		}

	}

	final class FieldBooleanBuilder extends BooleanQuery.Builder {

		private int clauseCount;

		@Override
		public BooleanQuery.Builder add(BooleanClause clause) {
			clauseCount++;
			return super.add(clause);
		}

		@Override
		public BooleanQuery build() {
			if (minNumberShouldMatch != null) {
				final int minShouldMatch = Math.max(1, (clauseCount * minNumberShouldMatch) / 100);
				setMinimumNumberShouldMatch(minShouldMatch);
			}
			return super.build();
		}
	}

}
