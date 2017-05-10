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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.index.BytesRefUtils;
import com.qwazr.search.index.QueryContext;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermInSetQuery;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class TermsQuery extends AbstractMultiTermQuery {

	final public Collection<Object> terms;

	@JsonIgnore
	final private Collection<BytesRef> bytesRefCollection;

	@JsonCreator
	public TermsQuery(@JsonProperty("field") final String field,
			@JsonProperty("terms") final Collection<Object> terms) {
		super(field);
		this.terms = Objects.requireNonNull(terms, "The term list is null");
		;
		this.bytesRefCollection = null;
	}

	public TermsQuery(final String field, final Object... terms) {
		super(field);
		Objects.requireNonNull(field, "The field is null");
		Objects.requireNonNull(terms, "The term list is null");
		this.bytesRefCollection = null;
		this.terms = new ArrayList<>(terms.length);
		Collections.addAll(this.terms, terms);
	}

	private TermsQuery(final Builder builder) {
		super(builder);
		this.terms = null;
		this.bytesRefCollection = builder.terms;
	}

	@Override
	final public Query getQuery(final QueryContext queryContext) throws IOException {
		final Collection<BytesRef> bytesRefs;
		if (bytesRefCollection == null) {
			bytesRefs = new ArrayList<>();
			terms.forEach(term -> bytesRefs.add(BytesRefUtils.fromAny(term)));
		} else
			bytesRefs = bytesRefCollection;
		return new TermInSetQuery(field, bytesRefs);
	}

	public static Builder of(final String field) {
		return new Builder(field);
	}

	public static class Builder extends MultiTermBuilder<TermsQuery> {

		private Builder(final String field) {
			super(field);
		}

		final public TermsQuery build() {
			return new TermsQuery(this);
		}
	}
}
