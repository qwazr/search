/**
 * Copyright 2015 Emmanuel Keller / QWAZR
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
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TermsQuery extends AbstractQuery {

	final public String field;
	final public List<String> terms;

	public TermsQuery() {
		super(null);
		field = null;
		terms = null;
	}

	TermsQuery(Float boost, String field, List<String> terms) {
		super(boost);
		this.field = field;
		this.terms = terms;
	}

	@Override
	protected Query getQuery(QueryContext queryContext) throws IOException {
		final List<Term> termList = new ArrayList<Term>(terms == null ? 0 : terms.size());
		if (terms != null)
			terms.forEach(term -> termList.add(new Term(field, term)));
		return new org.apache.lucene.queries.TermsQuery(termList);
	}
}
