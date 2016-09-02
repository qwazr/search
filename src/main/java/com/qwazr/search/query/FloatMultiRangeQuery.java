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
import com.qwazr.utils.ArrayUtils;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.Collection;

public class FloatMultiRangeQuery extends AbstractMultiRangeQuery<Float> {

	final public float[] lower_values;
	final public float[] upper_values;

	public FloatMultiRangeQuery() {
		lower_values = null;
		upper_values = null;
	}

	public FloatMultiRangeQuery(final String field, final float[] lowerValues, final float[] upperValues) {
		super(field);
		this.lower_values = lowerValues;
		this.upper_values = upperValues;
	}

	@Override
	public Query getQuery(final QueryContext queryContext) throws IOException {
		return FloatPoint.newRangeQuery(field, lower_values, upper_values);
	}

	public static class Builder extends AbstractBuilder<Float> {

		public Builder(String field) {
			super(field);
		}

		@Override
		protected FloatMultiRangeQuery build(final String field, final Collection<Float> lowerValues,
				final Collection<Float> upperValues) {
			return new FloatMultiRangeQuery(field, ArrayUtils.toPrimitiveFloat(lowerValues),
					ArrayUtils.toPrimitiveFloat(upperValues));
		}
	}

}