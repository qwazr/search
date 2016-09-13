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
import org.apache.htrace.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.spans.SpanQuery;

import java.io.IOException;

public class SpanTermQuery extends AbstractSpanQuery {

	final public String field;
	final public Object value;

	@JsonIgnore
	private final Term term;

	public SpanTermQuery() {
		field = null;
		value = null;
		term = null;
	}

	private SpanTermQuery(final String field, final Object value, final Term term) {
		this.field = field;
		this.value = value;
		this.term = term;
	}

	public SpanTermQuery(final Term term) {
		this(null, null, term);
	}

	public SpanTermQuery(final String field, final String value) {
		this(field, value, new Term(field, value));
	}

	public SpanTermQuery(final String field, final Long value) {
		this(field, value, new Term(field, BytesRefUtils.from(value)));
	}

	public SpanTermQuery(final String field, final Double value) {
		this(field, value, new Term(field, BytesRefUtils.from(value)));
	}

	public SpanTermQuery(final String field, final Integer value) {
		this(field, value, new Term(field, BytesRefUtils.from(value)));
	}

	public SpanTermQuery(final String field, final Float value) {
		this(field, value, new Term(field, BytesRefUtils.from(value)));
	}

	@Override
	final public SpanQuery getQuery(final QueryContext queryContext) throws IOException {
		return new org.apache.lucene.search.spans.SpanTermQuery(term);
	}
}
