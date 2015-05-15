/**
 * Copyright 2015 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.index;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.qwazr.search.index.QueryDefinition.AbstractQuery;
import com.qwazr.search.index.QueryDefinition.GroupQuery;
import com.qwazr.search.index.QueryDefinition.GroupQuery.OperatorEnum;
import com.qwazr.search.index.QueryDefinition.TermQuery;
import com.qwazr.utils.json.JsonMapper;

@JsonInclude(Include.NON_EMPTY)
public class QueryBuilder {

	public Set<String> returned_fields = null;
	public Set<String> facet_fields = null;
	public AbstractQuery query = null;

	public QueryBuilder addReturnedField(String field) {
		if (returned_fields == null)
			returned_fields = new LinkedHashSet<String>();
		returned_fields.add(field);
		return this;
	}

	/**
	 * Add a field to the facet list.
	 * 
	 * @param field
	 *            the name of the field
	 * @return this object
	 */
	public QueryBuilder addFacetField(String field) {
		if (facet_fields == null)
			facet_fields = new LinkedHashSet<String>();
		facet_fields.add(field);
		return this;
	}

	/**
	 * Set the main query.
	 * 
	 * @param query
	 *            the query to set
	 * @return this object
	 */
	public QueryBuilder setQuery(AbstractQuery query) {
		this.query = query;
		return this;
	}

	/**
	 * Create a query definition instance
	 * 
	 * @return a QueryDefinition instance
	 */
	public QueryDefinition build() {
		return new QueryDefinition(returned_fields, facet_fields, query);
	}

	/**
	 * Create a GroupQuery instance
	 * 
	 * @param operator
	 *            the operator used by the GroupQuery
	 * @return a new GroupQuery instance
	 */
	public GroupQuery createGroupQuery(OperatorEnum operator) {
		return new GroupQuery(operator, new ArrayList<AbstractQuery>());
	}

	/**
	 * Create a TermQuery instance
	 * 
	 * @param field
	 *            the name of the field
	 * @param value
	 *            the term
	 * @return a new TermQuery instance
	 */
	public TermQuery createTermQuery(String field, String value) {
		return new TermQuery(field, value);
	}

	final public static void main(String[] argv) throws JsonProcessingException {
		QueryBuilder builder = new QueryBuilder();
		builder.addReturnedField("title");
		builder.addFacetField("category");
		GroupQuery groupQuery = builder.createGroupQuery(OperatorEnum.and);
		groupQuery.addQuery(builder.createTermQuery("title", "www"));
		groupQuery.addQuery(builder.createTermQuery("title", "open"));
		builder.setQuery(groupQuery);
		System.out
				.println(JsonMapper.MAPPER.writeValueAsString(builder.build()));
	}
}
