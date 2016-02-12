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
import com.qwazr.utils.IOUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanPositionRangeQuery;
import org.apache.lucene.search.spans.SpanTermQuery;

import java.io.IOException;

public class SpanPositionsQuery extends AbstractQuery {

	final public String field;
	final public Integer distance;

	public SpanPositionsQuery() {
		super(null);
		field = null;
		distance = null;
	}

	SpanPositionsQuery(Float boost, String field, Integer end, Boolean increment_end, String value, Boolean log_boost) {
		super(boost);
		this.field = field;
		this.distance = end;
	}

	@Override
	protected Query getQuery(QueryContext queryContext) throws IOException {

		BooleanQuery.Builder builder = new BooleanQuery.Builder();
		TokenStream tokenStream = queryContext.analyzer.tokenStream(field, queryContext.queryString);
		try {
			CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);
			PositionIncrementAttribute pocincrAttribute = tokenStream.getAttribute(PositionIncrementAttribute.class);
			tokenStream.reset();
			int pos = 0;
			while (tokenStream.incrementToken()) {
				final String charTerm = charTermAttribute.toString();
				int start = pos - distance;
				if (start < 0)
					start = 0;
				final int end = pos + distance + 1;
				for (int i = start; i < end; i++) {
					final float dist = Math.abs(i - pos) + 1;
					final float boost = 1 / dist;
					final SpanTermQuery spanTermQuery = new SpanTermQuery(new Term(field, charTerm));
					SpanPositionRangeQuery spanPositionRangeQuery = new SpanPositionRangeQuery(spanTermQuery, i, i + 1);
					spanPositionRangeQuery.setBoost(boost * this.boost);
					builder.add(new BooleanClause(spanPositionRangeQuery, BooleanClause.Occur.SHOULD));
				}
				pos += pocincrAttribute.getPositionIncrement();
			}
			return builder.build();
		} finally {
			IOUtils.closeQuietly(tokenStream);
		}
	}

}
