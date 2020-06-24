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
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.search.Query;

public class IntegerMultiRange extends AbstractMultiRangeQuery<IntegerMultiRange> {

    @JsonProperty("lower_values")
    final public int[] lowerValues;
    @JsonProperty("upper_values")
    final public int[] upperValues;

    @JsonCreator
    public IntegerMultiRange(@JsonProperty("generic_field") final String genericField,
                             @JsonProperty("field") final String field,
                             @JsonProperty("lower_values") final int[] lowerValues,
                             @JsonProperty("upper_values") final int[] upperValues) {
        super(IntegerMultiRange.class, genericField, field);
        this.lowerValues = lowerValues;
        this.upperValues = upperValues;
    }

    public IntegerMultiRange(final String field, final int[] lowerValues, final int[] upperValues) {
        this(null, field, lowerValues, upperValues);
    }

    public IntegerMultiRange(final String field, final int lowerValue, final int upperValue) {
        this(field, new int[]{lowerValue}, new int[]{upperValue});
    }

    @QuerySampleCreator(docUri = CORE_BASE_DOC_URI + "core/org/apache/lucene/document/IntPoint.html#newRangeQuery-java.lang.String-int:A-int:A-")
    public IntegerMultiRange(final IndexSettingsDefinition settings,
                             final Map<String, AnalyzerDefinition> analyzers,
                             final Map<String, FieldDefinition> fields) {
        this(getIntField(fields, () -> "int_field"), new int[]{100, 1000}, new int[]{199, 1999});
    }

    @Override
    @JsonIgnore
    protected boolean isEqual(final IntegerMultiRange q) {
        return super.isEqual(q) && Arrays.equals(lowerValues, q.lowerValues) &&
            Arrays.equals(upperValues, q.upperValues);
    }

    @Override
    public Query getQuery(final QueryContext queryContext) {
        return IntPoint.newRangeQuery(
            resolvePointField(queryContext.getFieldMap(), 0, FieldTypeInterface.ValueType.integerType),
            lowerValues, upperValues
        );
    }

    public static class Builder extends AbstractBuilder<Integer, Builder> {

        public Builder(String genericField, String field) {
            super(genericField, field);
        }

        @Override
        protected Builder me() {
            return this;
        }

        @Override
        protected IntegerMultiRange build(final String field, final Collection<Integer> lowerValues,
                                          final Collection<Integer> upperValues) {
            return new IntegerMultiRange(field, ArrayUtils.toPrimitiveInt(lowerValues),
                ArrayUtils.toPrimitiveInt(upperValues));
        }
    }

}
