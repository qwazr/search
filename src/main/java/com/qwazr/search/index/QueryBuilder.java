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
	Boolean queryDebug = null;
	LinkedHashSet<String> returnedFields = null;

	String queryString = null;
	Boolean escapeQuery = null;
	char[] escapedChars = null;

	Similarity similarity = null;

	LinkedHashMap<String, FacetDefinition> facets = null;

	LinkedHashMap<String, QueryDefinition.SortEnum> sorts = null;
	LinkedHashMap<String, QueryDefinition.CollectorDefinition> collectors = null;

	LinkedHashMap<String, HighlighterDefinition> highlighters = null;

	AbstractQuery query = null;

	public QueryBuilder() {
	}

	public QueryBuilder(final AbstractQuery query) {
		this.query = query;
	}

	public QueryBuilder(final QueryDefinition queryDef) {
		start = queryDef.start;
		rows = queryDef.rows;
		queryDebug = queryDef.query_debug;
		returnedFields = queryDef.returned_fields;

		queryString = queryDef.query_string;
		escapeQuery = queryDef.escape_query;
		escapedChars = queryDef.escaped_chars;

		facets = queryDef.facets;
		sorts = queryDef.sorts;
		collectors = queryDef.collectors;

		highlighters = queryDef.highlighters;

		query = queryDef.query;
	}

	public QueryBuilder queryDebug(final Boolean queryDebug) {
		this.queryDebug = queryDebug;
		return this;
	}

	public QueryBuilder start(final Integer start) {
		this.start = start;
		return this;
	}

	public QueryBuilder rows(final Integer rows) {
		this.rows = rows;
		return this;
	}

	public QueryBuilder queryString(final String queryString) {
		this.queryString = queryString;
		return this;
	}

	public QueryBuilder escapeQuery(final Boolean escapeQuery) {
		this.escapeQuery = escapeQuery;
		return this;
	}

	public QueryBuilder escapedChars(final char[] escapedChars) {
		this.escapedChars = escapedChars;
		return this;
	}

	public QueryBuilder returnedFields(final Collection<String> returnedFields) {
		if (returnedFields == null || returnedFields.isEmpty())
			return this;
		if (this.returnedFields == null)
			this.returnedFields = new LinkedHashSet<>();
		this.returnedFields.addAll(returnedFields);
		return this;
	}

	public QueryBuilder returnedField(final Collection<String> returnedFields) {
		return returnedFields(returnedFields);
	}

	public QueryBuilder returnedField(final String... returnedFields) {
		if (returnedFields == null || returnedFields.length == 0)
			return this;
		if (this.returnedFields == null)
			this.returnedFields = new LinkedHashSet<>();
		Collections.addAll(this.returnedFields, returnedFields);
		return this;
	}

	public QueryBuilder returnedFields(final String... returnedFields) {
		return returnedField(returnedFields);
	}

	public QueryBuilder returnedField(final Enum<?>... returnedFields) {
		if (returnedFields == null || returnedFields.length == 0)
			return this;
		if (this.returnedFields == null)
			this.returnedFields = new LinkedHashSet<>();
		for (Enum<?> returned_field : returnedFields)
			this.returnedFields.add(returned_field.name());
		return this;
	}

	public QueryBuilder facets(final Map<String, FacetDefinition> facets) {
		if (facets == null || facets.isEmpty())
			return this;
		if (this.facets == null)
			this.facets = new LinkedHashMap<>();
		this.facets.putAll(facets);
		return this;
	}

	public QueryBuilder facet(final String facetName, final FacetDefinition facetDefinition) {
		if (facetName == null || facetDefinition == null)
			return this;
		if (this.facets == null)
			this.facets = new LinkedHashMap<>();
		this.facets.put(facetName, facetDefinition);
		return this;
	}

	public QueryBuilder facet(final Enum<?> facetName, final FacetDefinition facetDefinition) {
		if (facetName == null || facetDefinition == null)
			return this;
		return facet(facetName.name(), facetDefinition);
	}

	public QueryBuilder similarity(final Similarity similarity) {
		this.similarity = similarity;
		return this;
	}

	public QueryBuilder sorts(final Map<String, QueryDefinition.SortEnum> sorts) {
		if (sorts == null || sorts.isEmpty())
			return this;
		if (this.sorts == null)
			this.sorts = new LinkedHashMap<>();
		this.sorts.putAll(sorts);
		return this;
	}

	public QueryBuilder sort(final String fieldName, final QueryDefinition.SortEnum sortEnum) {
		if (fieldName == null || sortEnum == null)
			return this;
		if (this.sorts == null)
			this.sorts = new LinkedHashMap<>();
		this.sorts.put(fieldName, sortEnum);
		return this;
	}

	public QueryBuilder sort(final Enum<?> fieldName, final QueryDefinition.SortEnum sortEnum) {
		if (fieldName == null || sortEnum == null)
			return this;
		return sort(fieldName.name(), sortEnum);
	}

	public QueryBuilder collector(final String name, final Class<? extends Collector> collectorClass,
			final Object... arguments) {
		if (name == null || collectorClass == null)
			return this;
		if (this.collectors == null)
			this.collectors = new LinkedHashMap<>();
		this.collectors.put(name, new QueryDefinition.CollectorDefinition(collectorClass.getName(), arguments));
		return this;
	}

	public QueryBuilder highlighters(final Map<String, HighlighterDefinition> highlighters) {
		if (highlighters == null || highlighters.isEmpty())
			return this;
		if (this.highlighters == null)
			this.highlighters = new LinkedHashMap<>();
		this.highlighters.putAll(highlighters);
		return this;
	}

	public QueryBuilder highlighter(final String name, final HighlighterDefinition highlighter) {
		if (name == null || highlighter == null)
			return this;
		if (this.highlighters == null)
			this.highlighters = new LinkedHashMap<>();
		this.highlighters.put(name, highlighter);
		return this;
	}

	public QueryBuilder query(final AbstractQuery query) {
		this.query = query;
		return this;
	}

	public QueryDefinition build() {
		return new QueryDefinition(this);
	}
}
