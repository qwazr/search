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
package com.qwazr.search.index;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.query.AbstractQuery;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.json.JsonMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

@JsonInclude(Include.NON_EMPTY)
public class QueryDefinition extends BaseQueryDefinition {

	final public String query_string;
	final public Boolean escape_query;
	final public char[] escaped_chars;

	final public LinkedHashMap<String, SortEnum> sorts;
	final public ArrayList<Function> functions;
	final public LinkedHashMap<String, CollectorDefinition> collectors;

	public enum SortEnum {

		ascending,

		descending,

		ascending_missing_first,

		ascending_missing_last,

		descending_missing_first,

		descending_missing_last
	}

	final public LinkedHashSet<String> returned_fields;

	final public LinkedHashMap<String, FacetDefinition> facets;

	final public LinkedHashMap<String, HighlighterDefinition> highlighters;

	final public AbstractQuery query;

	public static class CollectorDefinition {

		@JsonProperty("class")
		final public String classname;

		final public Object[] arguments;

		public CollectorDefinition() {
			classname = null;
			arguments = null;
		}

		public CollectorDefinition(final String classname, final Object... arguments) {
			this.classname = classname;
			this.arguments = arguments == null || arguments.length == 0 ? null : arguments;
		}
	}

	public static class Function {

		public enum FunctionEnum {
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
		query_string = null;
		escape_query = null;
		escaped_chars = null;
		returned_fields = null;
		facets = null;
		sorts = null;
		functions = null;
		collectors = null;
		highlighters = null;
		query = null;
	}

	QueryDefinition(QueryBuilder builder) {
		super(builder);
		query_string = builder.query_string;
		escape_query = builder.escape_query;
		escaped_chars = builder.escaped_chars;
		returned_fields = builder.returned_fields;
		facets = builder.facets;
		sorts = builder.sorts;
		functions = builder.functions;
		collectors = builder.collectors;
		highlighters = builder.highlighters;
		query = builder.query;
	}

	public static QueryDefinition newQuery(String jsonString) throws IOException {
		if (StringUtils.isEmpty(jsonString))
			return null;
		return JsonMapper.MAPPER.readValue(jsonString, QueryDefinition.class);
	}

}
