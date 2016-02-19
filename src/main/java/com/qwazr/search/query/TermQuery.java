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
import com.qwazr.search.index.ValueUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

public class TermQuery extends AbstractQuery {

	final public String field;
	final public BytesRef ref;

	public TermQuery() {
		super(null);
		field = null;
		ref = null;
	}

	public TermQuery(String field, String text) {
		super(null);
		this.field = field;
		this.ref = ValueUtils.getNewBytesRef(text);
	}

	public TermQuery(Float boost, String field, String text) {
		super(boost);
		this.field = field;
		this.ref = new BytesRef(text);
	}

	public TermQuery(String field, long value) {
		super(null);
		this.field = field;
		this.ref = ValueUtils.getNewBytesRef(value);
	}

	public TermQuery(String field, int value) {
		super(null);
		this.field = field;
		this.ref = ValueUtils.getNewBytesRef(value);
	}

	public TermQuery(String field, double value) {
		super(null);
		this.field = field;
		this.ref = ValueUtils.getNewBytesRef(value);
	}

	public TermQuery(String field, float value) {
		super(null);
		this.field = field;
		this.ref = ValueUtils.getNewBytesRef(value);
	}

	@Override
	protected Query getQuery(QueryContext queryContext) throws IOException {
		return new org.apache.lucene.search.TermQuery(new Term(field, ref));
	}
}
