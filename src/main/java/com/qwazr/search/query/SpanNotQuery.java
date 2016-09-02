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
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.spans.SpanQuery;

import java.io.IOException;
import java.util.Objects;

public class SpanNotQuery extends AbstractSpanQuery {

	final public AbstractSpanQuery include;
	final public AbstractSpanQuery exclude;
	final public Integer dist;
	final public Integer pre;
	final public Integer post;

	public SpanNotQuery() {
		include = null;
		exclude = null;
		dist = null;
		pre = null;
		post = null;
	}

	public SpanNotQuery(final AbstractSpanQuery include, final AbstractSpanQuery exclude) {
		this.include = include;
		this.exclude = exclude;
		this.dist = null;
		this.pre = null;
		this.post = null;
	}

	public SpanNotQuery(final AbstractSpanQuery include, final AbstractSpanQuery exclude, final Integer dist) {
		this.include = include;
		this.exclude = exclude;
		this.dist = dist;
		this.pre = null;
		this.post = null;
	}

	public SpanNotQuery(final AbstractSpanQuery include, final AbstractSpanQuery exclude, final Integer pre,
			final Integer post) {
		this.include = include;
		this.exclude = exclude;
		this.dist = null;
		this.pre = pre;
		this.post = post;
	}

	@Override
	final public SpanQuery getQuery(final QueryContext queryContext)
			throws IOException, ParseException, QueryNodeException, ReflectiveOperationException, InterruptedException {
		Objects.requireNonNull(include);
		Objects.requireNonNull(exclude);
		final SpanQuery includeQuery = include.getQuery(queryContext);
		final SpanQuery excludeQuery = exclude.getQuery(queryContext);
		if (dist != null)
			return new org.apache.lucene.search.spans.SpanNotQuery(includeQuery, excludeQuery, dist);
		else if (pre != null || post != null)
			return new org.apache.lucene.search.spans.SpanNotQuery(includeQuery, excludeQuery, pre, post);
		return new org.apache.lucene.search.spans.SpanNotQuery(includeQuery, excludeQuery);
	}
}
