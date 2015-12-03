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

import com.qwazr.search.index.UpdatableAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanFirstQuery;
import org.apache.lucene.search.spans.SpanTermQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SpanFirstQueries extends AbstractQuery {

	final public String field;
	final public Integer end;
	final public Boolean increment_end;
	final public Boolean log_boost;

	public SpanFirstQueries() {
		super(null);
		field = null;
		end = null;
		increment_end = null;
		log_boost = null;
	}

	SpanFirstQueries(Float boost, String field, Integer end, Boolean increment_end, String value, Boolean log_boost) {
		super(boost);
		this.field = field;
		this.end = end;
		this.increment_end = increment_end;
		this.log_boost = log_boost;
	}

	@Override
	protected Query getQuery(UpdatableAnalyzer analyzer, String queryString) throws IOException {
		BooleanQuery.Builder builder = new BooleanQuery.Builder();
		TokenStream tokenStream = analyzer.tokenStream(field, queryString);
		CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);
		tokenStream.reset();
		final List<String> terms = new ArrayList<String>();
		int e = end == null ? 0 : end;
		final boolean inc_end = increment_end != null ? increment_end : false;
		while (tokenStream.incrementToken()) {
			SpanFirstQuery query = new SpanFirstQuery(new SpanTermQuery(new Term(field, charTermAttribute.toString())),
					e);
			builder.add(new BooleanClause(query, BooleanClause.Occur.SHOULD));
			if (inc_end)
				e++;
		}
		tokenStream.close();
		return builder.build();
	}

}
