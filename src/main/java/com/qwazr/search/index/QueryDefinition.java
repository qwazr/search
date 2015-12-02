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
package com.qwazr.search.index;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.qwazr.search.query.AbstractQuery;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.json.JsonMapper;

import java.io.IOException;
import java.util.*;

@JsonInclude(Include.NON_EMPTY)
public class QueryDefinition extends BaseQueryDefinition {

	final public String default_field;
	final public String query_string;
	final public Boolean escape_query;
	final public char[] escaped_chars;
	final public LinkedHashMap<String, Float> multi_field;

	public enum QueryBuilderType {
		standard_query_parser, multifield_query_parser;
	}

	final public QueryBuilderType query_builder;

	final public LinkedHashMap<String, SortEnum> sorts;
	final public ArrayList<Function> functions;

	public static enum SortEnum {

		ascending,

		descending,

		ascending_missing_first,

		ascending_missing_last,

		descending_missing_first,

		descending_missing_last
	}

	public static enum DefaultOperatorEnum {
		AND, OR
	}

	final public LinkedHashSet<String> returned_fields;
	final public LinkedHashMap<String, Facet> facets;
	final public List<Map<String, Set<String>>> facet_filters;

	final public LinkedHashMap<String, Integer> postings_highlighter;

	final public Boolean allow_leading_wildcard;
	final public DefaultOperatorEnum default_operator;
	final public Integer phrase_slop;
	final public Boolean enable_position_increments;
	final public Boolean auto_generate_phrase_query;

	final public List<AbstractQuery> boosts;
	final public AbstractQuery filter;

	public static class Facet {

		final public Integer top;

		public Facet(Integer top) {
			this.top = top;
		}

		public Facet() {
			this(null);
		}
	}

	public static class Function {

		public static enum FunctionEnum {
			max, min
		}

		final public FunctionEnum function;
		final public String field;

		public Function() {
			function = null;
			field = null;
		}

		Function(Function function) {
			this.function = function.function;
			this.field = function.field;
		}

		Function(FunctionEnum function, String field) {
			this.function = function;
			this.field = field;
		}
	}

	public QueryDefinition() {
		default_field = null;
		query_string = null;
		escape_query = null;
		escaped_chars = null;
		multi_field = null;
		query_builder = null;
		returned_fields = null;
		facets = null;
		facet_filters = null;
		sorts = null;
		functions = null;
		postings_highlighter = null;
		allow_leading_wildcard = null;
		default_operator = null;
		phrase_slop = null;
		enable_position_increments = null;
		auto_generate_phrase_query = null;
		boosts = null;
		filter = null;
	}

	QueryDefinition(QueryBuilder builder) {
		super(builder);
		default_field = builder.default_field;
		query_string = builder.query_string;
		escape_query = builder.escape_query;
		escaped_chars = builder.escaped_chars;
		multi_field = builder.multi_field;
		query_builder = builder.query_builder;
		returned_fields = builder.returned_fields;
		facets = builder.facets;
		facet_filters = builder.facet_filters;
		sorts = builder.sorts;
		functions = builder.functions;
		postings_highlighter = builder.postings_highlighter;
		allow_leading_wildcard = builder.allow_leading_wildcard;
		default_operator = builder.default_operator;
		phrase_slop = builder.phrase_slop;
		enable_position_increments = builder.enable_position_increments;
		auto_generate_phrase_query = builder.auto_generate_phrase_query;
		boosts = builder.boosts;
		filter = builder.filter;
	}

	public static QueryDefinition newQuery(String jsonString) throws IOException {
		if (StringUtils.isEmpty(jsonString))
			return null;
		return JsonMapper.MAPPER.readValue(jsonString, QueryDefinition.class);
	}

}
