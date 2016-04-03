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

import com.qwazr.search.index.BytesRefUtils;
import com.qwazr.search.index.QueryContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class TermsQuery extends AbstractQuery {

	final public Collection<Term> terms;

	public TermsQuery() {
		terms = null;
	}

	private TermsQuery(Collection<Term> terms) {
		this.terms = terms;
	}

	public TermsQuery(String field, Collection<Object> terms) {
		Objects.requireNonNull(field, "The field is null");
		Objects.requireNonNull(terms, "The term list is null");
		this.terms = new ArrayList<>(terms.size());
		terms.forEach(term -> terms.add(new Term(field, BytesRefUtils.fromAny(term))));
	}

	public TermsQuery(String field, Object... terms) {
		Objects.requireNonNull(field, "The field is null");
		Objects.requireNonNull(terms, "The term list is null");
		this.terms = new ArrayList<>(terms.length);
		for (Object term : terms)
			this.terms.add(new Term(field, BytesRefUtils.fromAny(term)));
	}

	@Override
	final public Query getQuery(QueryContext queryContext) throws IOException {
		return new org.apache.lucene.queries.TermsQuery(terms);
	}

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {

		private final List<Term> terms;

		public Builder() {
			terms = new ArrayList<>();
		}

		final public void add(final String field, final String term) {
			terms.add(new Term(field, term));
		}

		final public void add(final String field, final BytesRef bytes) {
			terms.add(new Term(field, bytes));
		}

		final public void add(final String field, final Integer value) {
			add(field, BytesRefUtils.from(value));
		}

		final public void add(final String field, final Float value) {
			add(field, BytesRefUtils.from(value));
		}

		final public void add(final String field, final Long value) {
			add(field, BytesRefUtils.from(value));
		}

		final public void add(final String field, final Double value) {
			add(field, BytesRefUtils.from(value));
		}

		final public TermsQuery build() {
			return new TermsQuery(terms);
		}
	}
}
