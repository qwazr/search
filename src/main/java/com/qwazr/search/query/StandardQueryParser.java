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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qwazr.search.index.QueryContext;
import com.qwazr.utils.ArrayUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

public class StandardQueryParser extends AbstractQuery {

	@JsonIgnore
	final private Analyzer analyzer;

	final public String[] multi_fields;
	final public String default_field;
	final public LinkedHashMap<String, Float> fields_boost;
	final public Boolean allow_leading_wildcard;
	final public QueryParserOperator default_operator;
	final public Integer phrase_slop;
	final public Boolean enable_position_increments;
	final public Boolean analyzer_range_terms;
	final public Float fuzzy_min_sim;
	final public Integer fuzzy_prefix_length;
	final public Integer max_determinized_states;
	final public Boolean lowercase_expanded_terms;
	final public String query_string;

	public StandardQueryParser() {
		analyzer = null;
		multi_fields = null;
		default_field = null;
		fields_boost = null;
		allow_leading_wildcard = null;
		default_operator = null;
		phrase_slop = null;
		enable_position_increments = null;
		analyzer_range_terms = null;
		fuzzy_min_sim = null;
		fuzzy_prefix_length = null;
		max_determinized_states = null;
		lowercase_expanded_terms = null;
		query_string = null;
	}

	public StandardQueryParser(Analyzer analyzer, String[] multi_fields, String default_field,
			LinkedHashMap<String, Float> fields_boost,
			Boolean allow_leading_wildcard, QueryParserOperator default_operator, Integer phrase_slop,
			Boolean enable_position_increments, Boolean analyzer_range_terms, Float fuzzy_min_sim,
			Integer fuzzy_prefix_length, Integer max_determinized_states, Boolean lowercase_expanded_terms,
			String query_string) {
		this.analyzer = analyzer;
		this.multi_fields = multi_fields;
		this.default_field = default_field;
		this.fields_boost = fields_boost;
		this.allow_leading_wildcard = allow_leading_wildcard;
		this.default_operator = default_operator;
		this.phrase_slop = phrase_slop;
		this.enable_position_increments = enable_position_increments;
		this.analyzer_range_terms = analyzer_range_terms;
		this.fuzzy_min_sim = fuzzy_min_sim;
		this.fuzzy_prefix_length = fuzzy_prefix_length;
		this.max_determinized_states = max_determinized_states;
		this.lowercase_expanded_terms = lowercase_expanded_terms;
		this.query_string = query_string;
	}

	private StandardQueryParser(Builder builder) {
		this.analyzer = builder.analyzer;
		this.multi_fields = builder.multi_fields == null ? null : ArrayUtils.toArray(builder.multi_fields);
		this.default_field = builder.default_field;
		this.fields_boost = builder.fields_boost;
		this.allow_leading_wildcard = builder.allow_leading_wildcard;
		this.default_operator = builder.default_operator;
		this.phrase_slop = builder.phrase_slop;
		this.enable_position_increments = builder.enable_position_increments;
		this.analyzer_range_terms = builder.analyzer_range_terms;
		this.fuzzy_min_sim = builder.fuzzy_min_sim;
		this.fuzzy_prefix_length = builder.fuzzy_prefix_length;
		this.max_determinized_states = builder.max_determinized_states;
		this.lowercase_expanded_terms = builder.lowercase_expanded_terms;
		this.query_string = builder.query_string;
	}

	@Override
	final public Query getQuery(QueryContext queryContext) throws IOException, ParseException, QueryNodeException {

		final org.apache.lucene.queryparser.flexible.standard.StandardQueryParser parser =
				new org.apache.lucene.queryparser.flexible.standard.StandardQueryParser(
						analyzer != null ? analyzer : queryContext.queryAnalyzer);
		if (fields_boost != null)
			parser.setFieldsBoost(fields_boost);
		if (default_operator != null)
			parser.setDefaultOperator(default_operator.queryConfigHandlerOperator);
		if (allow_leading_wildcard != null)
			parser.setAllowLeadingWildcard(allow_leading_wildcard);
		if (phrase_slop != null)
			parser.setPhraseSlop(phrase_slop);
		if (enable_position_increments != null)
			parser.setEnablePositionIncrements(enable_position_increments);
		if (fuzzy_min_sim != null)
			parser.setFuzzyMinSim(fuzzy_min_sim);
		if (fuzzy_prefix_length != null)
			parser.setFuzzyPrefixLength(fuzzy_prefix_length);
		if (lowercase_expanded_terms != null)
			parser.setLowercaseExpandedTerms(lowercase_expanded_terms);
		if (multi_fields != null)
			parser.setMultiFields(multi_fields);
		return parser.parse(query_string == null ? queryContext.queryString : query_string, default_field);
	}

	public static class Builder {

		private Analyzer analyzer = null;
		private Set<String> multi_fields = null;
		private String default_field = null;
		private LinkedHashMap<String, Float> fields_boost = null;
		private Boolean allow_leading_wildcard = null;
		private QueryParserOperator default_operator = null;
		private Integer phrase_slop = null;
		private Boolean enable_position_increments = null;
		private Boolean analyzer_range_terms = null;
		private Float fuzzy_min_sim = null;
		private Integer fuzzy_prefix_length = null;
		private Integer max_determinized_states = null;
		private Boolean lowercase_expanded_terms = null;
		private String query_string = null;

		public Builder setAnalyzerClass(Analyzer analyzer) {
			this.analyzer = analyzer;
			return this;
		}

		public Builder addMultiField(String... fields) {
			if (multi_fields == null)
				multi_fields = new LinkedHashSet<>();
			for (String field : fields)
				multi_fields.add(field);
			return this;
		}

		public Builder setDefaultField(String default_field) {
			this.default_field = default_field;
			return this;
		}

		public Builder addFieldBoost(String field, Float boost) {
			if (fields_boost == null)
				fields_boost = new LinkedHashMap<>();
			fields_boost.put(field, boost);
			return this;
		}

		public Builder setAllowLeadingWildcard(Boolean allow_leading_wildcard) {
			this.allow_leading_wildcard = allow_leading_wildcard;
			return this;
		}

		public Builder setQueryParserOperator(QueryParserOperator default_operator) {
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

		public Builder setQueryString(String query_string) {
			this.query_string = query_string;
			return this;
		}

		public StandardQueryParser build() {
			return new StandardQueryParser(this);
		}
	}
}
