/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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
package com.qwazr.search.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.index.QueryContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.MultiTermQuery;

import java.io.IOException;

public class FuzzyQuery extends AbstractMultiTermQuery {

	final public String text;
	final public Integer max_edits;
	final public Integer max_expansions;
	final public Boolean transpositions;
	final public Integer prefix_length;

	@JsonCreator
	public FuzzyQuery(@JsonProperty("field") final String field, @JsonProperty("text") final String text,
			@JsonProperty("max_edits") final Integer maxEdits,
			@JsonProperty("max_expansions") final Integer maxExpansions,
			@JsonProperty("transpositions") final Boolean transpositions,
			@JsonProperty("prefix_length") final Integer prefixLength) {
		super(field);
		this.text = text;
		this.max_edits = maxEdits;
		this.max_expansions = maxExpansions;
		this.transpositions = transpositions;
		this.prefix_length = prefixLength;
	}

	@Override
	final public MultiTermQuery getQuery(final QueryContext queryContext) throws IOException {
		return new org.apache.lucene.search.FuzzyQuery(new Term(resolveField(queryContext.getFieldMap()), text),
				max_edits == null ? org.apache.lucene.search.FuzzyQuery.defaultMaxEdits : max_edits,
				prefix_length == null ? org.apache.lucene.search.FuzzyQuery.defaultPrefixLength : prefix_length,
				max_expansions == null ? org.apache.lucene.search.FuzzyQuery.defaultMaxExpansions : max_expansions,
				transpositions == null ? org.apache.lucene.search.FuzzyQuery.defaultTranspositions : transpositions);
	}
}
