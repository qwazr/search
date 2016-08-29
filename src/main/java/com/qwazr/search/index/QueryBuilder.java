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

import com.qwazr.search.query.AbstractQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.similarities.Similarity;

import java.util.*;

public class QueryBuilder {

	Integer start = null;
	Integer rows = null;
	Boolean query_debug = null;
	LinkedHashSet<String> returned_fields = null;

	String query_string = null;
	Boolean escape_query = null;
	char[] escaped_chars = null;

	Similarity similarity = null;

	LinkedHashMap<String, FacetDefinition> facets = null;

	LinkedHashMap<String, QueryDefinition.SortEnum> sorts = null;
	LinkedHashMap<String, QueryDefinition.CollectorDefinition> collectors = null;

	LinkedHashMap<String, HighlighterDefinition> highlighters = null;

	public AbstractQuery query = null;

	public Boolean getQuery_debug() {
		return query_debug;
	}

	public QueryBuilder() {
	}

	public QueryBuilder(final QueryDefinition queryDef) {
		start = queryDef.start;
		rows = queryDef.rows;
		query_debug = queryDef.query_debug;
		returned_fields = queryDef.returned_fields;

		query_string = queryDef.query_string;
		escape_query = queryDef.escape_query;
		escaped_chars = queryDef.escaped_chars;

		facets = queryDef.facets;
		sorts = queryDef.sorts;
		collectors = queryDef.collectors;

		highlighters = queryDef.highlighters;

		query = queryDef.query;
	}

	public QueryBuilder setQuery_debug(final Boolean query_debug) {
		this.query_debug = query_debug;
		return this;
	}

	public Integer getStart() {
		return start;
	}

	public QueryBuilder setStart(final Integer start) {
		this.start = start;
		return this;
	}

	public Integer getRows() {
		return rows;
	}

	public QueryBuilder setRows(final Integer rows) {
		this.rows = rows;
		return this;
	}

	public String getQuery_string() {
		return query_string;
	}

	public QueryBuilder setQuery_string(final String query_string) {
		this.query_string = query_string;
		return this;
	}

	public Boolean getEscape_query() {
		return escape_query;
	}

	public QueryBuilder setEscape_query(final Boolean escape_query) {
		this.escape_query = escape_query;
		return this;
	}

	public char[] getEscaped_chars() {
		return escaped_chars;
	}

	public QueryBuilder setEscaped_chars(final char[] escaped_chars) {
		this.escaped_chars = escaped_chars;
		return this;
	}

	public Set<String> getReturned_fields() {
		return returned_fields;
	}

	public QueryBuilder setReturned_fields(final LinkedHashSet<String> returned_fields) {
		this.returned_fields = returned_fields;
		return this;
	}

	public QueryBuilder addReturned_field(final String... returned_fields) {
		if (this.returned_fields == null)
			this.returned_fields = new LinkedHashSet<>();
		for (String returned_field : returned_fields)
			this.returned_fields.add(returned_field);
		return this;
	}

	public QueryBuilder addReturned_field(final Enum<?>... returned_fields) {
		if (this.returned_fields == null)
			this.returned_fields = new LinkedHashSet<>();
		for (Enum<?> returned_field : returned_fields)
			this.returned_fields.add(returned_field.name());
		return this;
	}

	public QueryBuilder addReturned_field(final Collection<String> returned_fields) {
		if (returned_fields == null)
			return this;
		if (this.returned_fields == null)
			this.returned_fields = new LinkedHashSet<>();
		this.returned_fields.addAll(returned_fields);
		return this;
	}

	public Map<String, FacetDefinition> getFacets() {
		return facets;
	}

	public QueryBuilder setFacets(final LinkedHashMap<String, FacetDefinition> facets) {
		this.facets = facets;
		return this;
	}

	public QueryBuilder addFacet(final String facetName, final FacetDefinition facetDefinition) {
		if (facets == null)
			facets = new LinkedHashMap<>();
		facets.put(facetName, facetDefinition);
		return this;
	}

	public QueryBuilder addFacet(final Enum<?> facetName, final FacetDefinition facetDefinition) {
		if (facetName == null)
			return this;
		return addFacet(facetName.name(), facetDefinition);
	}

	public QueryBuilder setSimilarity(Similarity similarity) {
		this.similarity = similarity;
		return this;
	}

	public LinkedHashMap<String, QueryDefinition.SortEnum> getSorts() {
		return sorts;
	}

	public QueryBuilder setSorts(final LinkedHashMap<String, QueryDefinition.SortEnum> sorts) {
		this.sorts = sorts;
		return this;
	}

	public QueryBuilder addSort(final String fieldName, final QueryDefinition.SortEnum sortEnum) {
		if (sorts == null)
			sorts = new LinkedHashMap<>();
		sorts.put(fieldName, sortEnum);
		return this;
	}

	public QueryBuilder addSort(final Enum<?> fieldName, QueryDefinition.SortEnum sortEnum) {
		if (fieldName == null)
			return this;
		return addSort(fieldName.name(), sortEnum);
	}

	public QueryBuilder addCollector(final String name, final Class<? extends Collector> collectorClass,
			final Object... arguments) {
		if (collectorClass == null)
			return this;
		if (collectors == null)
			collectors = new LinkedHashMap<>();
		collectors.put(name, new QueryDefinition.CollectorDefinition(collectorClass.getName(), arguments));
		return this;
	}

	public LinkedHashMap<String, QueryDefinition.CollectorDefinition> getCollectors() {
		return collectors;
	}

	public Map<String, HighlighterDefinition> getHighlighters() {
		return highlighters;
	}

	public QueryBuilder setHighlighters(final LinkedHashMap<String, HighlighterDefinition> highlighters) {
		this.highlighters = highlighters;
		return this;
	}

	public QueryBuilder addHighlighter(final String name, final HighlighterDefinition highlighter) {
		if (highlighters == null)
			highlighters = new LinkedHashMap<>();
		highlighters.put(name, highlighter);
		return this;
	}

	public QueryBuilder setQuery(final AbstractQuery query) {
		this.query = query;
		return this;
	}

	public QueryDefinition build() {
		return new QueryDefinition(this);
	}
}
