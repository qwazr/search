/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
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
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.NumericUtils;

import java.io.IOException;

public class SortedFloatDocValuesRangeQuery extends AbstractRangeQuery<Float, SortedFloatDocValuesRangeQuery> {

	@JsonCreator
	public SortedFloatDocValuesRangeQuery(@JsonProperty("generic_field") final String genericField,
			@JsonProperty("field") final String field, @JsonProperty("lower_value") final Float lowerValue,
			@JsonProperty("upper_value") final Float upperValue) {
		super(SortedFloatDocValuesRangeQuery.class, genericField, field,
				lowerValue == null ? Float.MIN_VALUE : lowerValue, upperValue == null ? Float.MAX_VALUE : upperValue);
	}

	public SortedFloatDocValuesRangeQuery(final String field, final Float lowerValue, final Float upperValue) {
		this(null, field, lowerValue, upperValue);
	}

	@Override
	public Query getQuery(final QueryContext queryContext) throws IOException {
		return SortedNumericDocValuesField.newRangeQuery(resolveField(queryContext.getFieldMap()),
				NumericUtils.floatToSortableInt(lower_value), NumericUtils.floatToSortableInt(upper_value));
	}
}
