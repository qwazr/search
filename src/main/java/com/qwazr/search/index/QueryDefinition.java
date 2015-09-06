/**
 * Copyright 2015 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
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

import java.util.List;
import java.util.Set;

@JsonInclude(Include.NON_EMPTY)
public class QueryDefinition {

	final public String default_field;
	final public String query_string;

	final public Integer start;
	final public Integer rows;

	final public Set<String> returned_fields;
	final public Set<String> facet_fields;


	@JsonTypeInfo(use = Id.NAME, include = As.WRAPPER_OBJECT)
	@JsonSubTypes({@JsonSubTypes.Type(value = TermQuery.class, name = "term"),
			@Type(value = GroupQuery.class, name = "group")})
	public static class AbstractQuery {
	}

	final public AbstractQuery query;

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
		this.default_field = null;
		this.start = null;
		this.rows = null;
		this.query_string = null;
		this.returned_fields = null;
		this.facet_fields = null;
		this.query = null;
	}

	public QueryDefinition(Set<String> returned_fields, Set<String> facet_fields, AbstractQuery query) {
		this.default_field = null;
		this.start = null;
		this.rows = null;
		this.query_string = null;
		this.returned_fields = returned_fields;
		this.facet_fields = facet_fields;
		this.query = query;
	}


	public int getEnd() {
		return (start == null ? 0 : start) + (rows == null ? 10 : rows);
	}


}
