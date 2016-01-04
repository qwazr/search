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
package com.qwazr.search.query;

import com.qwazr.search.index.QueryContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;

import java.io.IOException;

public class FuzzyQuery extends AbstractQuery {

	final public String field;
	final public String text;
	final public Integer max_edits;
	final public Integer max_expansions;
	final public Boolean transpositions;
	final public Integer prefix_length;

	public FuzzyQuery() {
		super(null);
		field = null;
		text = null;
		max_edits = null;
		max_expansions = null;
		transpositions = null;
		prefix_length = null;
	}

	FuzzyQuery(Float boost, String field, String text, Integer max_edits, Integer max_expansions,
					Boolean transpositions, Integer prefix_length) {
		super(boost);
		this.field = field;
		this.text = text;
		this.max_edits = max_edits;
		this.max_expansions = max_expansions;
		this.transpositions = transpositions;
		this.prefix_length = prefix_length;
	}

	@Override
	protected Query getQuery(QueryContext queryContext) throws IOException {
		return new org.apache.lucene.search.FuzzyQuery(new Term(field, text),
						max_edits == null ? org.apache.lucene.search.FuzzyQuery.defaultMaxEdits : max_edits,
						prefix_length == null ? org.apache.lucene.search.FuzzyQuery.defaultPrefixLength : prefix_length,
						max_expansions == null ?
										org.apache.lucene.search.FuzzyQuery.defaultMaxExpansions :
										max_expansions, transpositions == null ?
						org.apache.lucene.search.FuzzyQuery.defaultTranspositions :
						transpositions);
	}
}
