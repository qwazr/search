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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.function.AbstractValueSource;
import com.qwazr.search.index.QueryContext;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.Objects;

public class BoostedQuery extends AbstractQuery {

	final public AbstractQuery sub_query;
	final public AbstractValueSource value_source;

	@JsonCreator
	public BoostedQuery(@JsonProperty("sub_query") final AbstractQuery subQuery,
			@JsonProperty("value_source") final AbstractValueSource valueSource) {
		this.sub_query = subQuery;
		this.value_source = valueSource;
	}

	@Override
	final public Query getQuery(final QueryContext queryContext)
			throws IOException, ParseException, QueryNodeException, ReflectiveOperationException {
		Objects.requireNonNull(sub_query, "The sub_query property is missing");
		Objects.requireNonNull(value_source, "The value_source property is missing");
		return new org.apache.lucene.queries.function.BoostedQuery(sub_query.getQuery(queryContext),
				value_source.getValueSource(queryContext));
	}
}