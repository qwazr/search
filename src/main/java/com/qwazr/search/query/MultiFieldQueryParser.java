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

import com.qwazr.search.index.QueryContext;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.LinkedHashMap;

public class MultiFieldQueryParser extends AbstractQuery {

	final public String[] fields;
	final public LinkedHashMap<String, Float> boosts;
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

	public MultiFieldQueryParser() {
		super(null);
		fields = null;
		boosts = null;
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
	}

	@Override
	final protected Query getQuery(QueryContext queryContext) throws IOException, ParseException {
		final org.apache.lucene.queryparser.classic.MultiFieldQueryParser parser = new org.apache.lucene.queryparser.classic.MultiFieldQueryParser(
						fields, queryContext.analyzer, boosts);
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
		return parser.parse(queryContext.queryString);
	}
}
