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
import com.qwazr.search.index.QueryContext;
import com.qwazr.utils.ArrayUtils;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.Collection;

public class IntMultiRangeQuery extends AbstractMultiRangeQuery {

	final public int[] lower_values;
	final public int[] upper_values;

	@JsonCreator
	public IntMultiRangeQuery(@JsonProperty("generic_field") final String genericField,
			@JsonProperty("field") final String field, @JsonProperty("lower_values") final int[] lowerValues,
			@JsonProperty("upper_values") final int[] upperValues) {
		super(genericField, field);
		this.lower_values = lowerValues;
		this.upper_values = upperValues;
	}

	public IntMultiRangeQuery(final String field, final int[] lowerValues, final int[] upperValues) {
		this(null, field, lowerValues, upperValues);
	}

	@Override
	public Query getQuery(final QueryContext queryContext) throws IOException {
		return IntPoint.newRangeQuery(resolveField(queryContext.getFieldMap()), lower_values, upper_values);
	}

	public static class Builder extends AbstractBuilder<Integer> {

		public Builder(String genericField, String field) {
			super(genericField, field);
		}

		@Override
		protected IntMultiRangeQuery build(final String field, final Collection<Integer> lowerValues,
				final Collection<Integer> upperValues) {
			return new IntMultiRangeQuery(field, ArrayUtils.toPrimitiveInt(lowerValues),
					ArrayUtils.toPrimitiveInt(upperValues));
		}
	}

}