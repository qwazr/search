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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.qwazr.search.index.UpdatableAnalyzer;
import org.apache.lucene.search.Query;

import java.io.IOException;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "query")
@JsonSubTypes({ @JsonSubTypes.Type(value = SpanFirstQueries.class, name = "spanFirstQueries"),
				@JsonSubTypes.Type(value = TermRangeQuery.class, name = "termRangeQuery") })

public abstract class AbstractQuery {

	public static enum QueryTypeEnum {
		spanFirstQueries, termRangeQuery;
	}

	@JsonIgnore
	public final QueryTypeEnum type;

	public final Float boost;

	protected AbstractQuery(QueryTypeEnum type) {
		this.type = type;
		this.boost = null;
	}

	protected AbstractQuery(QueryTypeEnum type, Float boost) {
		this.type = type;
		this.boost = boost;
	}

	@JsonIgnore
	protected abstract Query getQuery(UpdatableAnalyzer analyzer) throws IOException;

	public final Query getBoostedQuery(UpdatableAnalyzer analyzer) throws IOException {
		Query query = getQuery(analyzer);
		if (boost != null)
			query.setBoost(boost);
		return query;
	}
}
