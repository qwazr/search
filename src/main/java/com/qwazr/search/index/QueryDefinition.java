/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.query.AbstractQuery;
import com.qwazr.utils.CollectionsUtils;
import com.qwazr.utils.ObjectMappers;
import com.qwazr.utils.StringUtils;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;

@JsonInclude(Include.NON_EMPTY)
public class QueryDefinition extends BaseQueryDefinition {

	final public LinkedHashMap<String, SortEnum> sorts;
	final public LinkedHashMap<String, CollectorDefinition> collectors;

	public enum SortEnum {

		ascending,

		descending,

		ascending_missing_first,

		ascending_missing_last,

		descending_missing_first,

		descending_missing_last
	}

	final public LinkedHashMap<String, FacetDefinition> facets;

	final public LinkedHashMap<String, HighlighterDefinition> highlighters;

	@JsonProperty("commit_user_data")
	final public Map<String, String> commitUserData;

	@JsonIgnore
	final Query luceneQuery;

	final public AbstractQuery query;

	public static class CollectorDefinition {

		@JsonProperty("class")
		final public String classname;

		final public Object[] arguments;

		@JsonCreator
		public CollectorDefinition(@JsonProperty("class") final String classname,
				@JsonProperty("arguments") final Object... arguments) {
			this.classname = classname;
			this.arguments = arguments == null || arguments.length == 0 ? null : arguments;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(classname);
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof CollectorDefinition))
				return false;
			if (o == this)
				return true;
			final CollectorDefinition c = (CollectorDefinition) o;
			return Objects.equals(classname, c.classname) && Arrays.equals(arguments, c.arguments);
		}
	}

	@JsonCreator
	QueryDefinition(@JsonProperty("start") Integer start, @JsonProperty("rows") Integer rows,
			@JsonProperty("returned_fields") LinkedHashSet<String> returnedFields,
			@JsonProperty("query_debug") Boolean queryDebug,
			@JsonProperty("sorts") LinkedHashMap<String, SortEnum> sorts,
			@JsonProperty("collectors") LinkedHashMap<String, CollectorDefinition> collectors,
			@JsonProperty("facets") LinkedHashMap<String, FacetDefinition> facets,
			@JsonProperty("highlighters") LinkedHashMap<String, HighlighterDefinition> highlighters,
			@JsonProperty("query") AbstractQuery query,
			@JsonProperty("commit_user_data") Map<String, String> commitUserData) {
		super(start, rows, returnedFields, queryDebug);
		this.sorts = sorts;
		this.collectors = collectors;
		this.facets = facets;
		this.highlighters = highlighters;
		this.query = query;
		this.commitUserData = commitUserData;
		luceneQuery = null;
	}

	QueryDefinition(final QueryBuilder builder) {
		super(builder);
		facets = builder.facets;
		sorts = builder.sorts;
		collectors = builder.collectors;
		highlighters = builder.highlighters;
		query = builder.query;
		luceneQuery = builder.luceneQuery;
		commitUserData = builder.commitUserData;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(query);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof QueryDefinition))
			return false;
		if (o == this)
			return true;
		final QueryDefinition q = (QueryDefinition) o;
		return Objects.equals(query, q.query) && CollectionsUtils.equals(sorts, q.sorts) &&
				CollectionsUtils.equals(collectors, q.collectors) && CollectionsUtils.equals(facets, q.facets) &&
				CollectionsUtils.equals(highlighters, q.highlighters) &&
				CollectionsUtils.equals(commitUserData, q.commitUserData);
	}

	public static QueryBuilder of(final QueryDefinition queryDefinition) {
		return new QueryBuilder(queryDefinition);
	}

	public static QueryBuilder of(final Query query) {
		return new QueryBuilder(query);
	}

	public static QueryBuilder of(final AbstractQuery query) {
		return new QueryBuilder(query);
	}

	public static QueryDefinition newQuery(final String jsonString) throws IOException {
		if (StringUtils.isEmpty(jsonString))
			return null;
		return ObjectMappers.JSON.readValue(jsonString, QueryDefinition.class);
	}

}
