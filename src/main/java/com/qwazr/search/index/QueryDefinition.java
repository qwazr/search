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
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.json.JsonMapper;
import org.apache.lucene.queryparser.classic.QueryParser;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@JsonInclude(Include.NON_EMPTY)
public class QueryDefinition {

	final public String default_field = null;
	final public String query_string = null;
	final public Map<String, Float> multi_field = null;

	final public Integer start = null;
	final public Integer rows = null;

	final public Set<String> returned_fields = null;
	final public Map<String, Facet> facets = null;

	final public Boolean allow_leading_wildcard = null;
	final public QueryParser.Operator default_operator = null;

	public static class Facet {
		final public Integer top = null;
	}

	@JsonTypeInfo(use = Id.NAME, include = As.WRAPPER_OBJECT)
	@JsonSubTypes({@JsonSubTypes.Type(value = TermQuery.class, name = "term"),
			@Type(value = GroupQuery.class, name = "group")})
	public static class AbstractQuery {
	}

	final public AbstractQuery query = null;

	@JsonTypeName("term")
	public static class TermQuery extends AbstractQuery {

		final public String field;
		final public String value;

		public TermQuery(String field, String value) {
			this.field = field;
			this.value = value;
		}

		public TermQuery() {
			this(null, null);
		}
	}

	@JsonTypeName("group")
	public static class GroupQuery extends AbstractQuery {

		public static enum OperatorEnum {
			and, or
		}

		final public OperatorEnum operator;

		final public List<AbstractQuery> queries;

		GroupQuery(OperatorEnum operator, List<AbstractQuery> queries) {
			this.operator = operator;
			this.queries = queries;
		}

		public GroupQuery() {
			this(null, null);
		}

		public void addQuery(TermQuery termQuery) {
			queries.add(termQuery);
		}
	}

	public QueryDefinition() {
	}

	public int getEnd() {
		return (start == null ? 0 : start) + (rows == null ? 10 : rows);
	}

	public static QueryDefinition newQuery(String jsonString) throws IOException {
		if (StringUtils.isEmpty(jsonString))
			return null;
		return JsonMapper.MAPPER.readValue(jsonString, QueryDefinition.class);
	}

}
