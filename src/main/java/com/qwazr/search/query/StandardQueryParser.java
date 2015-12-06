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
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.LinkedHashMap;

public class StandardQueryParser extends AbstractQuery {

	final public String[] fields;
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

	public StandardQueryParser() {
		super(null);
		fields = null;
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
	}

	@Override
	final protected Query getQuery(UpdatableAnalyzer analyzer, String queryString)
			throws IOException, ParseException, QueryNodeException {

		final org.apache.lucene.queryparser.flexible.standard.StandardQueryParser parser = new org.apache.lucene.queryparser.flexible.standard.StandardQueryParser(
				analyzer);
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
		return parser.parse(default_field, queryString);
	}
}
