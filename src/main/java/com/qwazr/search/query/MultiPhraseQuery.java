/**
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

import com.qwazr.search.index.QueryContext;
import org.apache.lucene.index.Term;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MultiPhraseQuery extends AbstractQuery {

	final public String field;
	final public List<String[]> terms;
	final public List<Integer> positions;
	final public Integer slop;

	public MultiPhraseQuery() {
		field = null;
		terms = null;
		positions = null;
		slop = null;
	}

	public MultiPhraseQuery(final String field, final Integer slop) {
		this.field = field;
		this.slop = slop;
		this.terms = new ArrayList<>();
		this.positions = new ArrayList<>();
	}

	public MultiPhraseQuery add(final String... terms) {
		this.terms.add(terms);
		return this;
	}

	public MultiPhraseQuery add(final Integer position, final String... terms) {
		this.terms.add(terms);
		this.positions.add(position);
		return this;
	}

	@Override
	final public org.apache.lucene.search.MultiPhraseQuery getQuery(final QueryContext queryContext)
			throws IOException {
		Objects.requireNonNull(field, "The field property should not be null");
		final org.apache.lucene.search.MultiPhraseQuery.Builder builder =
				new org.apache.lucene.search.MultiPhraseQuery.Builder();
		if (slop != null)
			builder.setSlop(slop);
		if (terms != null) {
			if (positions == null || positions.isEmpty()) {
				for (String[] term : terms)
					builder.add(toTerms(term));
			} else {
				int i = 0;
				for (String[] term : terms) {
					builder.add(toTerms(term), positions.get(i++));
				}
			}
		}
		return builder.build();
	}

	private Term[] toTerms(final String[] termArray) {
		final Term[] terms = new Term[termArray.length];
		int i = 0;
		for (String term : termArray)
			terms[i++] = new Term(field, term);
		return terms;
	}
}
