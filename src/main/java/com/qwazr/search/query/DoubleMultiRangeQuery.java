/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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
import java.util.Arrays;
import java.util.Collection;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.search.Query;

public class DoubleMultiRangeQuery extends AbstractMultiRangeQuery<DoubleMultiRangeQuery> {

    @JsonProperty("lower_values")
    final public double[] lowerValues;
    @JsonProperty("upper_values")
    final public double[] upperValues;

    @JsonCreator
    public DoubleMultiRangeQuery(@JsonProperty("generic_field") final String genericField,
                                 @JsonProperty("field") final String field,
                                 @JsonProperty("lower_values") final double[] lowerValues,
                                 @JsonProperty("upper_values") final double[] upperValues) {
        super(DoubleMultiRangeQuery.class, genericField, field);
        this.lowerValues = lowerValues;
        this.upperValues = upperValues;
    }

    public DoubleMultiRangeQuery(final String field, final double[] lowerValues, final double[] upperValues) {
        this(null, field, lowerValues, upperValues);
    }

    public DoubleMultiRangeQuery(final String field, final double lowerValue, final double upperValue) {
        this(field, new double[]{lowerValue}, new double[]{upperValue});
    }

    @Override
    @JsonIgnore
    protected boolean isEqual(DoubleMultiRangeQuery q) {
        return super.isEqual(q) && Arrays.equals(lowerValues, q.lowerValues) &&
            Arrays.equals(upperValues, q.upperValues);
    }

    @Override
    public Query getQuery(final QueryContext queryContext) {
        return DoublePoint.newRangeQuery(
            resolveField(queryContext.getFieldMap(), 0D),
            lowerValues, upperValues);
    }

    public static class Builder extends AbstractBuilder<Double, Builder> {

        public Builder(String genericField, String field) {
            super(genericField, field);
        }

        @Override
        protected Builder me() {
            return this;
        }

        @Override
        protected DoubleMultiRangeQuery build(final String field, final Collection<Double> lowerValues,
                                              final Collection<Double> upperValues) {
            return new DoubleMultiRangeQuery(field, ArrayUtils.toPrimitiveDouble(lowerValues),
                ArrayUtils.toPrimitiveDouble(upperValues));
        }
    }

}
