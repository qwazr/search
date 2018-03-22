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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PhraseQuery extends AbstractFieldQuery<PhraseQuery> {

	final public List<String> terms;
	final public Integer slop;

	@JsonCreator
	public PhraseQuery(@JsonProperty("generic_field") final String genericField,
			@JsonProperty("field") final String field, @JsonProperty("slop") final Integer slop,
			@JsonProperty("terms") final List<String> terms) {
		super(PhraseQuery.class, genericField, field);
		this.slop = slop;
		this.terms = terms;
	}

	public PhraseQuery(final String field, final Integer slop, final List<String> terms) {
		this(null, field, slop, terms);
	}

	public PhraseQuery(final String field, final Integer slop, final String... terms) {
		this(field, slop, Arrays.asList(terms));
	}

	@Override
	@JsonIgnore
	protected boolean isEqual(PhraseQuery q) {
		return super.equals(q) && CollectionsUtils.equals(terms, q.terms) && Objects.equals(slop, q.slop);
	}

	@Override
	final public org.apache.lucene.search.PhraseQuery getQuery(final QueryContext queryContext) {
		Objects.requireNonNull(field, "The field property should not be null");
		org.apache.lucene.search.PhraseQuery.Builder builder = new org.apache.lucene.search.PhraseQuery.Builder();
		if (slop != null)
			builder.setSlop(slop);
		if (terms != null) {
			final String resolvedField = resolveField(queryContext.getFieldMap());
			for (String term : terms)
				builder.add(new Term(resolvedField, term));
		}
		return builder.build();
	}
}
