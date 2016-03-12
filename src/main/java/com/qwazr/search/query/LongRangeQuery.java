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
import org.apache.lucene.search.MultiTermQuery;

import java.io.IOException;

public class LongRangeQuery extends AbstractQuery {

	final public String field;
	final public Long lower_term;
	final public Long upper_term;
	final public Boolean include_lower;
	final public Boolean include_upper;

	public LongRangeQuery() {
		field = null;
		lower_term = null;
		upper_term = null;
		include_lower = null;
		include_upper = null;
	}

	public LongRangeQuery(String field, Long lower_term, Long upper_term, Boolean include_lower,
			Boolean include_upper) {
		this.field = field;
		this.lower_term = lower_term;
		this.upper_term = upper_term;
		this.include_lower = include_lower;
		this.include_upper = include_upper;
	}

	@Override
	final public MultiTermQuery getQuery(QueryContext queryContext) throws IOException {
		return org.apache.lucene.search.NumericRangeQuery
				.newLongRange(field, lower_term, upper_term, include_lower == null ? true : include_lower,
						include_upper == null ? true : include_upper);
	}
}
