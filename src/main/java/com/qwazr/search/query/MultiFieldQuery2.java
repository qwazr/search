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
import com.qwazr.search.query.lucene.QueryBuilderFix;
import com.qwazr.utils.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiFieldQuery2 extends AbstractQuery {

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

	public MultiFieldQuery2(final QueryParserOperator defaultOperator, final String queryString) {
		this(new LinkedHashMap<>(), defaultOperator, queryString, null, null, null);
	}

	public MultiFieldQuery2(final QueryParserOperator defaultOperator, final String queryString,
			final Integer minNumberShouldMatch) {
		this(new LinkedHashMap<>(), defaultOperator, queryString, minNumberShouldMatch, null, null);
	}

	public MultiFieldQuery2(final QueryParserOperator defaultOperator, final String queryString,
			final Integer minNumberShouldMatch, final Float tieBreakerMultiplier) {
		this(new LinkedHashMap<>(), defaultOperator, queryString, minNumberShouldMatch, tieBreakerMultiplier, null);
	}

	public MultiFieldQuery2(final Map<String, Float> fieldsBoosts, final QueryParserOperator defaultOperator,
			final String queryString, final Integer minNumberShouldMatch) {
		this(fieldsBoosts, defaultOperator, queryString, minNumberShouldMatch, null, null);
	}

	public MultiFieldQuery2(final QueryParserOperator defaultOperator, final String queryString,
			final Integer minNumberShouldMatch, final Float tieBreakerMultiplier, final Analyzer analyzer) {
		this(new LinkedHashMap<>(), defaultOperator, queryString, minNumberShouldMatch, tieBreakerMultiplier, analyzer);
	}

	public MultiFieldQuery2(final Map<String, Float> fieldsBoosts, final QueryParserOperator defaultOperator,
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
	public MultiFieldQuery2(@JsonProperty("fields_boosts") final Map<String, Float> fieldsBoosts,
			@JsonProperty("default_operator") final QueryParserOperator defaultOperator,
			@JsonProperty("query_string") final String queryString,
			@JsonProperty("min_number_should_match") final Integer minNumberShouldMatch,
			@JsonProperty("tie_breaker_multiplier") final Float tieBreakerMultiplier) {
		this(fieldsBoosts, defaultOperator, queryString, minNumberShouldMatch, tieBreakerMultiplier, null);
	}

	@JsonIgnore
	public MultiFieldQuery2 boost(final String field, final Float boost) {
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

		// We look for terms frequency
		final Map<String, AtomicInteger> termsFreq = new HashMap<>();
		final Map<String, Set<Offset>> termsOffsets = new HashMap<>();
		final IndexReader indexReader = queryContext.getIndexReader();
		if (indexReader != null) {
			// Build term queries
			fieldsBoosts.forEach((field, boost) -> {
				try (final TokenStream tokenStream = alzr.tokenStream(field, queryString)) {
					new TermsWithFreq(tokenStream, queryContext.getIndexReader(), field, termsFreq,
							termsOffsets).forEachToken();
					tokenStream.end();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
		}

		// Execute the query
		return new Builder(alzr, termsFreq, termsOffsets).parse(queryString,
				defaultOperator == null || defaultOperator.queryParseroperator == QueryParser.Operator.AND ?
						BooleanClause.Occur.MUST :
						BooleanClause.Occur.SHOULD);
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

	protected Query getTermQuery(final int freq, final Term term) {
		Query query;
		if (freq > 0)
			query = new org.apache.lucene.search.TermQuery(term);
		else
			query = new org.apache.lucene.search.FuzzyQuery(term);
		final Float boost = fieldsBoosts.get(term.field());
		if (boost != null && boost != 1.0F)
			query = new org.apache.lucene.search.BoostQuery(query, boost);
		return query;
	}

	private class TermsWithFreq extends TermConsumer.WithChar {

		private final OffsetAttribute offsetAttr;

		private final IndexReader indexReader;
		private final String field;
		private final Map<String, AtomicInteger> termsFreq;
		private final Map<String, Set<Offset>> termsOffset;

		private TermsWithFreq(final TokenStream tokenStream, final IndexReader indexReader, final String field,
				final Map<String, AtomicInteger> termsFreq, final Map<String, Set<Offset>> termsOffset) {
			super(tokenStream);
			offsetAttr = TermConsumer.getAttribute(tokenStream, OffsetAttribute.class);
			this.indexReader = indexReader;
			this.field = field;
			this.termsFreq = termsFreq;
			this.termsOffset = termsOffset;
		}

		@Override
		final public boolean token() throws IOException {
			final String text = charTermAttr.toString();
			final Term term = new Term(field, text);
			final int freq = indexReader.docFreq(term);
			if (freq > 0)
				termsFreq.computeIfAbsent(text, t -> new AtomicInteger()).addAndGet(freq);
			termsOffset.computeIfAbsent(text, t -> new LinkedHashSet<>()).add(new Offset(offsetAttr));
			return true;
		}
	}

	final class Offset {

		final int start;
		final int end;

		private Offset(OffsetAttribute offsetAttr) {
			this.start = offsetAttr.startOffset();
			this.end = offsetAttr.endOffset();
		}

		@Override
		public int hashCode() {
			return start * 31 + end;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Offset))
				return false;
			if (o == this)
				return true;
			final Offset offset = (Offset) o;
			return start == offset.start && end == offset.end;
		}
	}

	final class Builder extends QueryBuilderFix {

		final Map<String, AtomicInteger> termsFreq;
		final Map<Offset, List<Query>> perTermQueries;
		final Map<String, Set<Offset>> termsOffset;

		private Builder(Analyzer analyzer, Map<String, AtomicInteger> termsFreq,
				Map<String, Set<Offset>> termsOffsets) {
			super(analyzer);
			this.termsFreq = termsFreq;
			setEnableGraphQueries(true);
			setEnablePositionIncrements(true);
			setAutoGenerateMultiTermSynonymsPhraseQuery(true);
			this.perTermQueries = new LinkedHashMap<>();
			this.termsOffset = termsOffsets;
		}

		@Override
		final protected Query newSynonymQuery(final Term[] terms) {
			for (Term term : terms) {
				final Query query = new org.apache.lucene.search.TermQuery(term);
				final String text = term.text();
				final Collection<Offset> offset = termsOffset.get(text);
				if (offset != null)
					offset.forEach(o -> perTermQueries.computeIfAbsent(o, (k) -> new ArrayList<>()).add(query));
			}
			return super.newSynonymQuery(terms);
		}

		@Override
		final protected Query newTermQuery(Term term) {
			final String text = term.text();
			final AtomicInteger freq = termsFreq.get(term.text());
			final Query termQuery = getTermQuery(freq == null ? 0 : freq.get(), term);
			final Collection<Offset> offset = termsOffset.get(text);
			if (offset != null)
				offset.forEach(o -> perTermQueries.computeIfAbsent(o, (k) -> new ArrayList<>()).add(termQuery));
			return termQuery;
		}

		private String concatTerms(Term[] terms) {
			final StringBuilder sb = new StringBuilder();
			boolean start = true;
			for (final Term term : terms) {
				if (!start)
					sb.append(' ');
				else
					start = false;
				sb.append(term.text());
			}
			return sb.toString();
		}

		@Override
		final protected Query newGraphSynonymQuery(Iterator<Query> queries) {
			final BooleanQuery query = (BooleanQuery) super.newGraphSynonymQuery(queries);
			query.clauses().forEach(q -> {
				final org.apache.lucene.search.PhraseQuery pq = (org.apache.lucene.search.PhraseQuery) q.getQuery();
				final Collection<Offset> offset = termsOffset.get(concatTerms(pq.getTerms()));
				if (offset != null)
					offset.forEach(o -> perTermQueries.computeIfAbsent(o, (k) -> new ArrayList<>()).add(query));
			});
			return query;
		}

		final Query parse(final String queryString, final BooleanClause.Occur defaultOperator) {
			final List<Query> byTermQueries = new ArrayList<>();
			fieldsBoosts.forEach((field, boost) -> createBooleanQuery(field, queryString));
			perTermQueries.forEach((t, queries) -> byTermQueries.add(getFieldQuery(queries)));
			return getRootQuery(minNumberShouldMatch, byTermQueries, defaultOperator);
		}

	}

}
