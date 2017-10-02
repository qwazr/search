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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.index.QueryContext;
import com.qwazr.utils.ArrayUtils;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

public class FloatMultiRangeQuery extends AbstractMultiRangeQuery<FloatMultiRangeQuery> {

	final public float[] lower_values;
	final public float[] upper_values;

	@JsonCreator
	public FloatMultiRangeQuery(@JsonProperty("generic_field") final String genericField,
			@JsonProperty("field") final String field, @JsonProperty("lower_values") final float[] lowerValues,
			@JsonProperty("upper_values") final float[] upperValues) {
		super(FloatMultiRangeQuery.class, genericField, field);
		this.lower_values = lowerValues;
		this.upper_values = upperValues;
	}

	public FloatMultiRangeQuery(final String field, final float[] lowerValues, final float[] upperValues) {
		this(null, field, lowerValues, upperValues);
	}

	public FloatMultiRangeQuery(final String field, final float lowerValue, final float upperValue) {
		this(field, new float[] { lowerValue }, new float[] { upperValue });
	}

	@Override
	@JsonIgnore
	protected boolean isEqual(FloatMultiRangeQuery q) {
		return super.isEqual(q) && Arrays.equals(lower_values, q.lower_values) &&
				Arrays.equals(upper_values, q.upper_values);
	}

	@Override
	public Query getQuery(final QueryContext queryContext) throws IOException {
		return FloatPoint.newRangeQuery(resolveField(queryContext.getFieldMap()), lower_values, upper_values);
	}

	public static class Builder extends AbstractBuilder<Float> {

		public Builder(String genericField, String field) {
			super(genericField, field);
		}

		@Override
		protected FloatMultiRangeQuery build(final String field, final Collection<Float> lowerValues,
				final Collection<Float> upperValues) {
			return new FloatMultiRangeQuery(genericField, field, ArrayUtils.toPrimitiveFloat(lowerValues),
					ArrayUtils.toPrimitiveFloat(upperValues));
		}
	}

}