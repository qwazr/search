/**
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

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

		// Determine the field level occur operator
		final BooleanClause.Occur defaultOccur =
				defaultOperator == null || defaultOperator.queryParseroperator == QueryParser.Operator.AND ?
						BooleanClause.Occur.MUST :
						BooleanClause.Occur.SHOULD;

		// Select the right analyzer
		final Analyzer alzr = analyzer != null ? analyzer : queryContext.getQueryAnalyzer();

		new LinkedHashMap<>();
		final SortedMap<Integer, List<Query>> byPosQueries = new TreeMap<>();
		// Build term queries
		fieldsBoosts.forEach((field, boost) -> {
			try (final TokenStream tokenStream = alzr.tokenStream(field, queryString)) {
				final FieldTermsQuery fieldTermsQuery =
						new FieldTermsQuery(byPosQueries, tokenStream, field, queryContext.getIndexReader());
				fieldTermsQuery.forEachToken();
				tokenStream.end();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});

		// Build pos queries
		final List<Query> posQueries = new ArrayList<>();
		byPosQueries.forEach((position, fieldQueries) -> {
			posQueries.add(getFieldQuery(fieldQueries));
		});

		// Build the final query
		return getRootQuery(minNumberShouldMatch, posQueries, defaultOccur);
	}

	protected Query getRootQuery(final Integer minNumberShouldMatch, final List<Query> queries,
			final BooleanClause.Occur defaultOccur) {
		if (queries.size() == 1)
			return queries.get(0);
		final BooleanQuery.Builder builder = new BooleanQuery.Builder();
		if (minNumberShouldMatch != null)
			builder.setMinimumNumberShouldMatch(minNumberShouldMatch);
		queries.forEach(query -> builder.add(new BooleanClause(query, defaultOccur)));
		return builder.build();
	}

	protected Query getFieldQuery(final List<Query> queries) {
		if (queries.size() == 1)
			return queries.get(0);
		if (tieBreakerMultiplier != null) {
			return new org.apache.lucene.search.DisjunctionMaxQuery(queries, tieBreakerMultiplier);
		} else {
			final BooleanQuery.Builder builder = new org.apache.lucene.search.BooleanQuery.Builder();
			queries.forEach(query -> builder.add(query, BooleanClause.Occur.SHOULD));
			return builder.build();
		}
	}

	protected Query getTermQuery(final int minTermFreq, final Term term) {
		if (minTermFreq == 0)
			return new org.apache.lucene.search.FuzzyQuery(term);
		else
			return new org.apache.lucene.search.TermQuery(term);
	}

	private class FieldTermsQuery extends TermConsumer.WithCharPositionIncrement {

		private final SortedMap<Integer, List<Query>> byPosQueries;
		private final String field;
		private final float boost;
		private final IndexReader indexReader;
		private int minTermFreq;
		private int currentPos;

		private FieldTermsQuery(final SortedMap<Integer, List<Query>> byPosQueries, final TokenStream tokenStream,
				final String field, final IndexReader indexReader) {
			super(tokenStream);
			this.byPosQueries = byPosQueries;
			this.field = field;
			final Float b = fieldsBoosts == null ? null : fieldsBoosts.get(field);
			this.boost = b == null ? 1.0F : b;
			this.indexReader = indexReader;
			this.minTermFreq = Integer.MAX_VALUE;
			this.currentPos = 0;
		}

		@Override
		public boolean token() throws IOException {
			final Term term = new Term(field, charTermAttr.toString());
			final int freq = indexReader == null ? 0 : indexReader.docFreq(term);
			minTermFreq = Math.min(freq, minTermFreq);
			Query termQuery = getTermQuery(minTermFreq, term);
			byPosQueries.computeIfAbsent(currentPos, ArrayList::new)
					.add(boost == 1.0F ? termQuery : new org.apache.lucene.search.BoostQuery(termQuery, boost));
			currentPos += posIncAttr.getPositionIncrement();
			return true;
		}
	}

}
