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
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.annotations.QuerySampleCreator;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.field.FieldTypeInterface;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.QueryContext;
import com.qwazr.utils.ArrayUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.search.Query;

public class FloatMultiRange extends AbstractMultiRangeQuery<FloatMultiRange> {

    @JsonProperty("lower_values")
    final public float[] lowerValues;
    @JsonProperty("upper_values")
    final public float[] upperValues;

    @JsonCreator
    public FloatMultiRange(@JsonProperty("generic_field") final String genericField,
                           @JsonProperty("field") final String field,
                           @JsonProperty("lower_values") final float[] lowerValues,
                           @JsonProperty("upper_values") final float[] upperValues) {
        super(FloatMultiRange.class, genericField, field);
        this.lowerValues = lowerValues;
        this.upperValues = upperValues;
    }

    public FloatMultiRange(final String field, final float[] lowerValues, final float[] upperValues) {
        this(null, field, lowerValues, upperValues);
    }

    public FloatMultiRange(final String field, final float lowerValue, final float upperValue) {
        this(field, new float[]{lowerValue}, new float[]{upperValue});
    }

    @QuerySampleCreator(docUri = CORE_BASE_DOC_URI + "core/org/apache/lucene/document/FloatPoint.html#newRangeQuery-java.lang.String-float:A-float:A-")
    public FloatMultiRange(final IndexSettingsDefinition settings,
                           final Map<String, AnalyzerDefinition> analyzers,
                           final Map<String, FieldDefinition> fields) {
        this(getFloatField(fields, () -> "float_field"), new float[]{1.57f, 6.28f}, new float[]{3.14f, 7.85f});
    }

    @Override
    @JsonIgnore
    protected boolean isEqual(FloatMultiRange q) {
        return super.isEqual(q) && Arrays.equals(lowerValues, q.lowerValues) &&
            Arrays.equals(upperValues, q.upperValues);
    }

    @Override
    public Query getQuery(final QueryContext queryContext) {
        return FloatPoint.newRangeQuery(
            resolvePointField(queryContext.getFieldMap(), 0F, FieldTypeInterface.ValueType.floatType),
            lowerValues, upperValues);
    }

    public static class Builder extends AbstractBuilder<Float, Builder> {

        public Builder(String genericField, String field) {
            super(genericField, field);
        }

        @Override
        protected Builder me() {
            return this;
        }

        @Override
        protected FloatMultiRange build(final String field, final Collection<Float> lowerValues,
                                        final Collection<Float> upperValues) {
            return new FloatMultiRange(genericField, field, ArrayUtils.toPrimitiveFloat(lowerValues),
                ArrayUtils.toPrimitiveFloat(upperValues));
        }
    }

}
