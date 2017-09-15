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
import com.qwazr.search.index.BytesRefUtils;
import com.qwazr.search.index.QueryContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class SynonymQuery extends AbstractMultiTermQuery {

	final public Collection<Object> terms;

	@JsonIgnore
	final private Term[] termArray;

	@JsonCreator
	public SynonymQuery(@JsonProperty("generic_field") final String genericField,
			@JsonProperty("field") final String field, @JsonProperty("terms") final Collection<Object> terms) {
		super(genericField, field);
		this.terms = Objects.requireNonNull(terms, "The term list is null");
		this.termArray = null;
	}

	public SynonymQuery(final String field, final Collection<Object> terms) {
		this(null, field, terms);
	}

	public SynonymQuery(final String field, final Object... terms) {
		super(null, field);
		Objects.requireNonNull(terms, "The term list is null");
		this.terms = new ArrayList<>(terms.length);
		Collections.addAll(this.terms, terms);
		this.termArray = null;
	}

	private SynonymQuery(final Builder builder) {
		super(builder);
		this.terms = builder.objects;
		termArray = new Term[builder.bytesRefs.size()];
		int i = 0;
		for (BytesRef br : builder.bytesRefs) {
			termArray[i++] = new Term(builder.field, br);
		}
	}

	@Override
	final public Query getQuery(final QueryContext queryContext) throws IOException {
		final Term[] ta;
		final String resolvedField = resolveField(queryContext.getFieldMap());
		if (termArray == null) {
			ta = new Term[terms.size()];
			int i = 0;
			for (Object t : terms)
				ta[i++] = new Term(resolvedField, BytesRefUtils.fromAny(t));
		} else
			ta = termArray;
		return new org.apache.lucene.search.SynonymQuery(ta);
	}

	public static Builder of(final String genericField, final String field) {
		return new Builder(genericField, field);
	}

	public static class Builder extends MultiTermBuilder<SynonymQuery> {

		private Builder(final String genericField, final String field) {
			super(genericField, field);
		}

		final public SynonymQuery build() {
			return new SynonymQuery(this);
		}
	}
}
