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
import org.apache.lucene.search.spans.SpanQuery;

import java.io.IOException;

public class SpanTermQuery extends AbstractSpanQuery {

	final public String field;
	final public String text;

	public SpanTermQuery() {
		super(null);
		field = null;
		text = null;
	}

	public SpanTermQuery(String field, String text) {
		super(null);
		this.field = field;
		this.text = text;
	}

	public SpanTermQuery(Float boost, String field, String text) {
		super(boost);
		this.field = field;
		this.text = text;
	}

	@Override
	protected SpanQuery getQuery(QueryContext queryContext) throws IOException {
		return new org.apache.lucene.search.spans.SpanTermQuery(new Term(field, text));
	}
}
