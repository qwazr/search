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
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MultiFieldQuery extends AbstractQuery {

	@JsonProperty("fields_boosts")
	final public Map<String, Float> fieldsBoosts;

	@JsonProperty("fields_disabled_graph")
	final public Set<String> fieldsDisabledGraph;

	@JsonProperty("fields_operator")
	final public Map<String, QueryParserOperator> fieldsOperator;

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
		this(new LinkedHashMap<>(), new LinkedHashSet<>(), defaultOperator, queryString, null, null, null);
	}

	public MultiFieldQuery(final QueryParserOperator defaultOperator, final String queryString,
			final Integer minNumberShouldMatch) {
		this(new LinkedHashMap<>(), new LinkedHashSet<>(), defaultOperator, queryString, minNumberShouldMatch, null,
				null);
	}

	public MultiFieldQuery(final QueryParserOperator defaultOperator, final String queryString,
			final Integer minNumberShouldMatch, final Float tieBreakerMultiplier) {
		this(new LinkedHashMap<>(), new LinkedHashSet<>(), defaultOperator, queryString, minNumberShouldMatch,
				tieBreakerMultiplier, null);
	}

	public MultiFieldQuery(final Map<String, Float> fieldsBoosts, final Set<String> fieldsGraphs,
			final QueryParserOperator defaultOperator, final String queryString, final Integer minNumberShouldMatch) {
		this(fieldsBoosts, fieldsGraphs, defaultOperator, queryString, minNumberShouldMatch, null, null);
	}

	public MultiFieldQuery(final QueryParserOperator defaultOperator, final String queryString,
			final Integer minNumberShouldMatch, final Float tieBreakerMultiplier, final Analyzer analyzer) {
		this(new LinkedHashMap<>(), new LinkedHashSet<>(), defaultOperator, queryString, minNumberShouldMatch,
				tieBreakerMultiplier, analyzer);
	}

	public MultiFieldQuery(final Map<String, Float> fieldsBoosts, final Set<String> fieldsDisabledGraph,
			final QueryParserOperator defaultOperator, final String queryString, final Integer minNumberShouldMatch,
			final Float tieBreakerMultiplier, final Analyzer analyzer) {
		this(fieldsBoosts, fieldsDisabledGraph, new LinkedHashMap<>(), defaultOperator, queryString,
				minNumberShouldMatch, tieBreakerMultiplier, analyzer);
	}

	public MultiFieldQuery(final Map<String, Float> fieldsBoosts, final Set<String> fieldsDisabledGraph,
			final Map<String, QueryParserOperator> fieldsOperator, final QueryParserOperator defaultOperator,
			final String queryString, final Integer minNumberShouldMatch, final Float tieBreakerMultiplier,
			final Analyzer analyzer) {
		this.fieldsBoosts = fieldsBoosts;
		this.fieldsDisabledGraph = fieldsDisabledGraph;
		this.fieldsOperator = fieldsOperator;
		this.defaultOperator = defaultOperator;
		this.queryString = queryString;
		this.minNumberShouldMatch = minNumberShouldMatch;
		this.tieBreakerMultiplier = tieBreakerMultiplier;
		this.analyzer = analyzer;
	}

	@JsonCreator
	public MultiFieldQuery(@JsonProperty("fields_boosts") final Map<String, Float> fieldsBoosts,
			@JsonProperty("fields_disabled_graph") final Set<String> fieldsDisabledGraph,
			@JsonProperty("fields_operator") final Map<String, QueryParserOperator> fieldsOperator,
			@JsonProperty("default_operator") final QueryParserOperator defaultOperator,
			@JsonProperty("query_string") final String queryString,
			@JsonProperty("min_number_should_match") final Integer minNumberShouldMatch,
			@JsonProperty("tie_breaker_multiplier") final Float tieBreakerMultiplier) {
		this(fieldsBoosts, fieldsDisabledGraph, fieldsOperator, defaultOperator, queryString, minNumberShouldMatch,
				tieBreakerMultiplier, null);
	}

	@JsonIgnore
	public MultiFieldQuery field(final String field, final Float boost, final boolean enableGraph,
			final QueryParserOperator operator) {
		Objects.requireNonNull(field, "The field is missing");
		Objects.requireNonNull(fieldsBoosts);
		if (boost != null)
			fieldsBoosts.put(field, boost);
		else
			fieldsBoosts.put(field, 1.0F);
		if (enableGraph)
			fieldsDisabledGraph.remove(field);
		else
			fieldsDisabledGraph.add(field);
		if (operator != null)
			fieldsOperator.put(field, operator);
		else
			fieldsOperator.remove(field);
		return this;
	}

	@JsonIgnore
	public MultiFieldQuery field(final String field, final Float boost, final boolean enableGraph) {
		return field(field, boost, enableGraph, null);
	}

	@JsonIgnore
	public MultiFieldQuery field(final String field, final Float boost) {
		return field(field, boost, true, null);
	}

	@Override
	public Query getQuery(final QueryContext queryContext) throws IOException, ReflectiveOperationException {
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

		// Build the per field queries
		final List<Query> fieldQueries = new ArrayList<>();
		fieldsBoosts.forEach((field, boost) -> fieldQueries.add(
				new FieldQueryBuilder(alzr, field, termsFreq).parse(queryString, getOccur(field), boost)));

		// Build the final query
		return getRootQuery(fieldQueries);
	}

	protected BooleanClause.Occur getOccur(String field) {
		// If minShouldMatch is active, we are using SHOULD
		if (minNumberShouldMatch != null)
			return BooleanClause.Occur.SHOULD;
		// Let's check per field parameter
		final QueryParserOperator operator =
				fieldsOperator == null ? defaultOperator : fieldsOperator.getOrDefault(field, defaultOperator);
		return operator == null || operator == QueryParserOperator.AND ?
				BooleanClause.Occur.MUST :
				BooleanClause.Occur.SHOULD;

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

	final class FieldQueryBuilder extends org.apache.lucene.util.QueryBuilder {

		final Map<String, Integer> termsFreq;
		final String field;

		private FieldQueryBuilder(final Analyzer analyzer, final String field, final Map<String, Integer> termsFreq) {
			super(analyzer);
			this.termsFreq = termsFreq;
			this.field = field;
			setEnableGraphQueries(fieldsDisabledGraph == null || !fieldsDisabledGraph.contains(field));
		}

		@Override
		final protected Query newTermQuery(Term term) {
			final Integer freq = termsFreq.get(term.text());
			return getTermQuery(freq == null ? 0 : freq, term);
		}

		protected BooleanQuery.Builder newBooleanQuery() {
			return new FieldBooleanBuilder();
		}

		final Query parse(final String queryString, final BooleanClause.Occur defaultOperator, final Float boost) {
			final Query fieldQuery = createBooleanQuery(field, queryString, defaultOperator);
			return boost != null && boost != 1.0F ?
					new org.apache.lucene.search.BoostQuery(fieldQuery, boost) :
					fieldQuery;
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
