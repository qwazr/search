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

import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class QueryBuilder {

	String default_field = null;
	String query_string = null;
	Boolean escape_query = null;
	char[] escaped_chars = null;
	Map<String, Float> multi_field = null;

	Set<String> returned_fields = null;
	Map<String, QueryDefinition.Facet> facets = null;
	Map<String, Set<String>> facet_drilldown = null;

	Map<String, Integer> postings_highlighter = null;

	Boolean allow_leading_wildcard = null;
	StandardQueryConfigHandler.Operator default_operator = null;
	Integer phrase_slop = null;
	Boolean enable_position_increments = null;
	Boolean auto_generate_phrase_query = null;

	public Boolean getAuto_generate_phrase_query() {
		return auto_generate_phrase_query;
	}

	public QueryBuilder setAuto_generate_phrase_query(Boolean auto_generate_phrase_query) {
		this.auto_generate_phrase_query = auto_generate_phrase_query;
		return this;
	}

	public String getDefault_field() {
		return default_field;
	}

	public QueryBuilder setDefault_field(String default_field) {
		this.default_field = default_field;
		return this;
	}

	public String getQuery_string() {
		return query_string;
	}

	public QueryBuilder setQuery_string(String query_string) {
		this.query_string = query_string;
		return this;
	}

	public Boolean getEscape_query() {
		return escape_query;
	}

	public QueryBuilder setEscape_query(Boolean escape_query) {
		this.escape_query = escape_query;
		return this;
	}

	public char[] getEscaped_chars() {
		return escaped_chars;
	}

	public QueryBuilder setEscaped_chars(char[] escaped_chars) {
		this.escaped_chars = escaped_chars;
		return this;
	}

	public Map<String, Float> getMulti_field() {
		return multi_field;
	}

	public QueryBuilder setMulti_field(Map<String, Float> multi_field) {
		this.multi_field = multi_field;
		return this;
	}

	public QueryBuilder addMulti_field(String field, Float boost) {
		if (multi_field == null)
			multi_field = new LinkedHashMap<String, Float>();
		multi_field.put(field, boost);
		return this;
	}

	public Set<String> getReturned_fields() {
		return returned_fields;
	}

	public QueryBuilder setReturned_fields(Set<String> returned_fields) {
		this.returned_fields = returned_fields;
		return this;
	}

	public QueryBuilder addReturned_field(String returned_field) {
		if (returned_fields == null)
			returned_fields = new LinkedHashSet<String>();
		returned_fields.add(returned_field);
		return this;
	}

	public Map<String, QueryDefinition.Facet> getFacets() {
		return facets;
	}

	public QueryBuilder setFacets(Map<String, QueryDefinition.Facet> facets) {
		this.facets = facets;
		return this;
	}

	public QueryBuilder addFacet(String facetName, QueryDefinition.Facet facetDefinition) {
		if (facets == null)
			facets = new LinkedHashMap<String, QueryDefinition.Facet>();
		facets.put(facetName, facetDefinition);
		return this;
	}

	public Map<String, Set<String>> getFacet_drilldown() {
		return facet_drilldown;
	}

	public QueryBuilder setFacet_drilldown(Map<String, Set<String>> facet_drilldown) {
		this.facet_drilldown = facet_drilldown;
		return this;
	}

	public QueryBuilder addFacet_drilldown(String field, Set<String> values) {
		if (facet_drilldown == null)
			facet_drilldown = new LinkedHashMap<String, Set<String>>();
		facet_drilldown.put(field, values);
		return this;
	}

	public Map<String, Integer> getPostings_highlighter() {
		return postings_highlighter;
	}

	public QueryBuilder setPostings_highlighter(Map<String, Integer> postings_highlighter) {
		this.postings_highlighter = postings_highlighter;
		return this;
	}

	public QueryBuilder addPosting_highlighter(String field, Integer max_size) {
		if (postings_highlighter == null)
			postings_highlighter = new LinkedHashMap<String, Integer>();
		postings_highlighter.put(field, max_size);
		return this;
	}

	public Boolean getAllow_leading_wildcard() {
		return allow_leading_wildcard;
	}

	public QueryBuilder setAllow_leading_wildcard(Boolean allow_leading_wildcard) {
		this.allow_leading_wildcard = allow_leading_wildcard;
		return this;
	}

	public StandardQueryConfigHandler.Operator getDefault_operator() {
		return default_operator;
	}

	public QueryBuilder setDefault_operator(StandardQueryConfigHandler.Operator default_operator) {
		this.default_operator = default_operator;
		return this;
	}

	public Integer getPhrase_slop() {
		return phrase_slop;
	}

	public QueryBuilder setPhrase_slop(Integer phrase_slop) {
		this.phrase_slop = phrase_slop;
		return this;
	}

	public Boolean getEnable_position_increments() {
		return enable_position_increments;
	}

	public QueryBuilder setEnable_position_increments(Boolean enable_position_increments) {
		this.enable_position_increments = enable_position_increments;
		return this;
	}

	public QueryDefinition build() {
		return new QueryDefinition(this);
	}
}
