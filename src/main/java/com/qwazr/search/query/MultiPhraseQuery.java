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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.index.QueryContext;
import com.qwazr.utils.CollectionsUtils;
import org.apache.lucene.index.Term;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MultiPhraseQuery extends AbstractFieldQuery<MultiPhraseQuery> {

	final public List<String[]> terms;
	final public List<Integer> positions;
	final public Integer slop;

	@JsonCreator
	public MultiPhraseQuery(@JsonProperty("generic_field") final String genericField,
			@JsonProperty("field") String field, @JsonProperty("terms") List<String[]> terms,
			@JsonProperty("positions") List<Integer> positions, @JsonProperty("slop") Integer slop) {
		super(MultiPhraseQuery.class, genericField, field);
		this.terms = terms;
		this.positions = positions;
		this.slop = slop;
	}

	public MultiPhraseQuery(String field, List<String[]> terms, List<Integer> positions, Integer slop) {
		this(null, field, terms, positions, slop);
	}

	public MultiPhraseQuery(final String field, final Integer slop) {
		this(field, new ArrayList<>(), new ArrayList<>(), slop);
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
	@JsonIgnore
	protected boolean isEqual(final MultiPhraseQuery q) {
		if (!super.isEqual(q) || !CollectionsUtils.equals(positions, q.positions) && !Objects.equals(slop, q.slop))
			return false;
		if (terms == q.terms)
			return true;
		if (terms == null || q.terms == null)
			return false;
		if (terms.size() != q.terms.size())
			return false;
		int i = 0;
		for (String[] t : terms)
			if (!Arrays.equals(t, q.terms.get(i++)))
				return false;
		return true;
	}

	@Override
	final public org.apache.lucene.search.MultiPhraseQuery getQuery(final QueryContext queryContext)
			throws IOException {
		Objects.requireNonNull(field, "The field property should not be null");
		final String resolvedField = resolveField(queryContext.getFieldMap());
		final org.apache.lucene.search.MultiPhraseQuery.Builder builder =
				new org.apache.lucene.search.MultiPhraseQuery.Builder();
		if (slop != null)
			builder.setSlop(slop);
		if (terms != null) {
			if (positions == null || positions.isEmpty()) {
				for (String[] term : terms)
					builder.add(toTerms(resolvedField, term));
			} else {
				int i = 0;
				for (String[] term : terms) {
					builder.add(toTerms(resolvedField, term), positions.get(i++));
				}
			}
		}
		return builder.build();
	}

	private Term[] toTerms(final String resolvedField, final String[] termArray) {
		final Term[] terms = new Term[termArray.length];
		int i = 0;
		for (String term : termArray)
			terms[i++] = new Term(resolvedField, term);
		return terms;
	}
}
