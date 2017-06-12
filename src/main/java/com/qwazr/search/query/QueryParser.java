/*
 * Copyright 2017 Emmanuel Keller / QWAZR
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
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.Objects;

public class QueryParser extends AbstractQuery {

	@JsonIgnore
	final private Analyzer analyzer;

	final public String field;
	final public Boolean allow_leading_wildcard;
	final public QueryParserOperator default_operator;
	final public Integer phrase_slop;
	final public Boolean enable_position_increments;
	final public Boolean auto_generate_phrase_query;
	final public Boolean analyzer_range_terms;
	final public Float fuzzy_min_sim;
	final public Integer fuzzy_prefix_length;
	final public Integer max_determinized_states;
	final public Boolean lowercase_expanded_terms;
	@JsonProperty("auto_generate_multi_term_synonyms_phrase_query")
	final public Boolean autoGenerateMultiTermSynonymsPhraseQuery;
	@JsonProperty("enable_graph_queries")
	final public Boolean enableGraphQueries;
	@JsonProperty("split_on_whitespace")
	final public Boolean splitOnWhitespace;
	final public String query_string;

	@JsonCreator
	private QueryParser() {
		analyzer = null;
		field = null;
		allow_leading_wildcard = null;
		default_operator = null;
		phrase_slop = null;
		enable_position_increments = null;
		auto_generate_phrase_query = null;
		analyzer_range_terms = null;
		fuzzy_min_sim = null;
		fuzzy_prefix_length = null;
		max_determinized_states = null;
		lowercase_expanded_terms = null;
		autoGenerateMultiTermSynonymsPhraseQuery = null;
		enableGraphQueries = null;
		splitOnWhitespace = null;
		query_string = null;
	}

	public QueryParser(Builder builder) {
		this.analyzer = builder.analyzer;
		this.field = Objects.requireNonNull(builder.field, "The field is missing");
		this.allow_leading_wildcard = builder.allow_leading_wildcard;
		this.default_operator = builder.default_operator;
		this.phrase_slop = builder.phrase_slop;
		this.enable_position_increments = builder.enable_position_increments;
		this.auto_generate_phrase_query = builder.auto_generate_phrase_query;
		this.analyzer_range_terms = builder.analyzer_range_terms;
		this.fuzzy_min_sim = builder.fuzzy_min_sim;
		this.fuzzy_prefix_length = builder.fuzzy_prefix_length;
		this.max_determinized_states = builder.max_determinized_states;
		this.lowercase_expanded_terms = builder.lowercase_expanded_terms;
		this.autoGenerateMultiTermSynonymsPhraseQuery = builder.autoGenerateMultiTermSynonymsPhraseQuery;
		this.splitOnWhitespace = builder.splitOnWhitespace;
		this.enableGraphQueries = builder.enableGraphQueries;
		this.query_string = builder.query_string;
	}

	@Override
	final public Query getQuery(final QueryContext queryContext) throws IOException, ParseException {
		final org.apache.lucene.queryparser.classic.QueryParser parser =
				new org.apache.lucene.queryparser.classic.QueryParser(field,
						analyzer == null ? queryContext.getQueryAnalyzer() : analyzer);
		if (default_operator != null)
			parser.setDefaultOperator(default_operator.queryParseroperator);
		if (allow_leading_wildcard != null)
			parser.setAllowLeadingWildcard(allow_leading_wildcard);
		if (phrase_slop != null)
			parser.setPhraseSlop(phrase_slop);
		if (enable_position_increments != null)
			parser.setEnablePositionIncrements(enable_position_increments);
		if (auto_generate_phrase_query != null)
			parser.setAutoGeneratePhraseQueries(auto_generate_phrase_query);
		if (analyzer_range_terms != null)
			parser.setAnalyzeRangeTerms(analyzer_range_terms);
		if (fuzzy_min_sim != null)
			parser.setFuzzyMinSim(fuzzy_min_sim);
		if (fuzzy_prefix_length != null)
			parser.setFuzzyPrefixLength(fuzzy_prefix_length);
		if (lowercase_expanded_terms != null)
			parser.setLowercaseExpandedTerms(lowercase_expanded_terms);
		if (max_determinized_states != null)
			parser.setMaxDeterminizedStates(max_determinized_states);
		if (autoGenerateMultiTermSynonymsPhraseQuery != null)
			parser.setAutoGenerateMultiTermSynonymsPhraseQuery(autoGenerateMultiTermSynonymsPhraseQuery);
		if (enableGraphQueries != null)
			parser.setEnableGraphQueries(enableGraphQueries);
		if (splitOnWhitespace != null)
			parser.setSplitOnWhitespace(splitOnWhitespace);
		return parser.parse(query_string);
	}

	public static Builder of(String field) {
		return new Builder().setField(field);
	}

	public static class Builder {

		private Analyzer analyzer;
		private String field;
		private Boolean allow_leading_wildcard;
		private QueryParserOperator default_operator;
		private Integer phrase_slop;
		private Boolean enable_position_increments;
		private Boolean auto_generate_phrase_query;
		private Boolean analyzer_range_terms;
		private Float fuzzy_min_sim;
		private Integer fuzzy_prefix_length;
		private Integer max_determinized_states;
		private Boolean lowercase_expanded_terms;
		private Boolean autoGenerateMultiTermSynonymsPhraseQuery;
		private Boolean enableGraphQueries;
		private Boolean splitOnWhitespace;
		private String query_string;

		public QueryParser build() {
			return new QueryParser(this);
		}

		public Builder setAnalyzer(Analyzer analyzer) {
			this.analyzer = analyzer;
			return this;
		}

		public Builder setField(String field) {
			this.field = field;
			return this;
		}

		public Builder setAllowLeadingWildcard(Boolean allow_leading_wildcard) {
			this.allow_leading_wildcard = allow_leading_wildcard;
			return this;
		}

		public Builder setDefaultOperator(QueryParserOperator default_operator) {
			this.default_operator = default_operator;
			return this;
		}

		public Builder setPhraseSlop(Integer phrase_slop) {
			this.phrase_slop = phrase_slop;
			return this;
		}

		public Builder setEnablePositionIncrements(Boolean enable_position_increments) {
			this.enable_position_increments = enable_position_increments;
			return this;
		}

		public Builder setAutoGeneratePhraseQuery(Boolean auto_generate_phrase_query) {
			this.auto_generate_phrase_query = auto_generate_phrase_query;
			return this;
		}

		public Builder setAnalyzerRangeTerms(Boolean analyzer_range_terms) {
			this.analyzer_range_terms = analyzer_range_terms;
			return this;
		}

		public Builder setFuzzyMinSim(Float fuzzy_min_sim) {
			this.fuzzy_min_sim = fuzzy_min_sim;
			return this;
		}

		public Builder setFuzzyPrefixLength(Integer fuzzy_prefix_length) {
			this.fuzzy_prefix_length = fuzzy_prefix_length;
			return this;
		}

		public Builder setMaxDeterminizedStates(Integer max_determinized_states) {
			this.max_determinized_states = max_determinized_states;
			return this;
		}

		public Builder setLowercaseExpandedTerms(Boolean lowercase_expanded_terms) {
			this.lowercase_expanded_terms = lowercase_expanded_terms;
			return this;
		}

		public Builder setAutoGenerateMultiTermSynonymsPhraseQuery(Boolean autoGenerateMultiTermSynonymsPhraseQuery) {
			this.autoGenerateMultiTermSynonymsPhraseQuery = autoGenerateMultiTermSynonymsPhraseQuery;
			return this;
		}

		public Builder setEnableGraphQueries(Boolean enableGraphQueries) {
			this.enableGraphQueries = enableGraphQueries;
			return this;
		}

		public Builder setSplitOnWhitespace(Boolean splitOnWhitespace) {
			this.splitOnWhitespace = splitOnWhitespace;
			return this;
		}

		public Builder setQueryString(String queryString) {
			this.query_string = queryString;
			return this;
		}
	}
}
