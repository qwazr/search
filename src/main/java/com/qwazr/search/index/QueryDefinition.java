/**
 * Copyright 2015 Emmanuel Keller / QWAZR
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
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
public class QueryDefinition extends BaseQueryDefinition {

    final public String default_field;
    final public String query_string;
    final public Boolean escape_query;
    final public char[] escaped_chars;
    final public Map<String, Float> multi_field;

    final public Set<String> returned_fields;
    final public Map<String, Facet> facets;
    final public Map<String, Set<String>> filters;

    final public Map<String, Integer> postings_highlighter;

    final public Boolean allow_leading_wildcard;
    final public Boolean auto_generate_phrase_queries;
    final public QueryParser.Operator default_operator;

    public static class Facet {
	final public Integer top = null;
    }

    @JsonTypeInfo(use = Id.NAME, include = As.WRAPPER_OBJECT)
    @JsonSubTypes({ @JsonSubTypes.Type(value = TermQuery.class, name = "term"),
	    @Type(value = GroupQuery.class, name = "group") })
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
	default_field = null;
	query_string = null;
	escape_query = null;
	escaped_chars = null;
	multi_field = null;
	returned_fields = null;
	facets = null;
	filters = null;
	postings_highlighter = null;
	allow_leading_wildcard = null;
	auto_generate_phrase_queries = null;
	default_operator = null;
    }

    public static QueryDefinition newQuery(String jsonString) throws IOException {
	if (StringUtils.isEmpty(jsonString))
	    return null;
	return JsonMapper.MAPPER.readValue(jsonString, QueryDefinition.class);
    }

}
