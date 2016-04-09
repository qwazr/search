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
import org.apache.lucene.search.Query;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class TermsQuery extends AbstractQuery {

	final public String field;

	final public Collection<Object> terms;

	public TermsQuery() {
		field = null;
		terms = null;
	}

	public TermsQuery(String field, Collection<Object> terms) {
		Objects.requireNonNull(field, "The field is null");
		Objects.requireNonNull(terms, "The term list is null");
		this.field = field;
		this.terms = terms;
	}

	public TermsQuery(String field, Object... terms) {
		Objects.requireNonNull(field, "The field is null");
		Objects.requireNonNull(terms, "The term list is null");
		this.field = field;
		this.terms = new ArrayList<>(terms.length);
	}

	@Override
	final public Query getQuery(QueryContext queryContext) throws IOException {
		final Collection<BytesRef> bytesRefs = new ArrayList<>();
		terms.forEach(term -> bytesRefs.add(BytesRefUtils.fromAny(term)));
		return new org.apache.lucene.queries.TermsQuery(field, bytesRefs);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private String field = null;

		private final List<Object> terms;

		public Builder() {
			terms = new ArrayList<>();
		}

		final public Builder setField(String field) {
			this.field = field;
			return this;
		}

		final public Builder add(final String... term) {
			terms.add(term);
			return this;
		}

		final public Builder add(final BytesRef... bytes) {
			terms.add(bytes);
			return this;
		}

		final public Builder add(final Integer... value) {
			terms.add(value);
			return this;
		}

		final public Builder add(final Float... value) {
			terms.add(value);
			return this;
		}

		final public Builder add(final Long... value) {
			terms.add(value);
			return this;
		}

		final public Builder add(final Double value) {
			terms.add(value);
			return this;
		}

		final public TermsQuery build() {
			return new TermsQuery(field, terms);
		}
	}
}
