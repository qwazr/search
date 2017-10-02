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
import org.apache.lucene.search.MultiTermQuery;

import java.io.IOException;
import java.util.Objects;

public class WildcardQuery extends AbstractMultiTermQuery<WildcardQuery> {

	final public String term;

	@JsonCreator
	public WildcardQuery(@JsonProperty("generic_field") final String genericField,
			@JsonProperty("field") final String field, @JsonProperty("term") final String term) {
		super(WildcardQuery.class, genericField, field);
		this.term = term;
	}

	public WildcardQuery(final String field, final String term) {
		this(null, field, term);
	}

	@Override
	@JsonIgnore
	protected boolean isEqual(WildcardQuery q) {
		return super.isEqual(q) && Objects.equals(term, q.term);
	}

	@Override
	final public MultiTermQuery getQuery(final QueryContext queryContext) throws IOException {
		return new org.apache.lucene.search.WildcardQuery(getResolvedTerm(queryContext.getFieldMap(), term));
	}

}
