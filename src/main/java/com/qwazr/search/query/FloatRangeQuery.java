/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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
import com.qwazr.search.index.QueryContext;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.search.Query;

import java.io.IOException;

public class FloatRangeQuery extends AbstractRangeQuery<Float, FloatRangeQuery> {

	@JsonCreator
	public FloatRangeQuery(@JsonProperty("generic_field") final String genericField,
			@JsonProperty("field") final String field, @JsonProperty("lower_value") final Float lowerValue,
			@JsonProperty("upper_value") final Float upperValue) {
		super(FloatRangeQuery.class, genericField, field,
				lowerValue == null ? FloatDocValuesRangeQuery.MIN : lowerValue,
				upperValue == null ? FloatDocValuesRangeQuery.MAX : upperValue);
	}

	public FloatRangeQuery(final String field, final Float lowerValue, final Float upperValue) {
		this(null, field, lowerValue, upperValue);
	}

	@Override
	public Query getQuery(final QueryContext queryContext) throws IOException {
		return FloatPoint.newRangeQuery(resolveField(queryContext.getFieldMap()), lower_value, upper_value);
	}
}
