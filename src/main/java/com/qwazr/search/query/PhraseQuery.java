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

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class PhraseQuery extends AbstractQuery {

	final public String field;
	final public List<String> terms;
	final public Integer slop;

	public PhraseQuery() {
		field = null;
		terms = null;
		slop = null;
	}

	public PhraseQuery(final String field, final Integer slop, final List<String> terms) {
		this.field = field;
		this.slop = slop;
		this.terms = terms;
	}

	@Override
	final public org.apache.lucene.search.PhraseQuery getQuery(final QueryContext queryContext) throws IOException {
		Objects.requireNonNull(field, "The field property should not be null");
		org.apache.lucene.search.PhraseQuery.Builder builder = new org.apache.lucene.search.PhraseQuery.Builder();
		if (slop != null)
			builder.setSlop(slop);
		if (terms != null)
			for (String term : terms)
				builder.add(new Term(field, term));
		return builder.build();
	}
}
