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
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.search.Query;

public class LongMultiRange extends AbstractMultiRangeQuery<LongMultiRange> {

    @JsonProperty("lower_values")
    final public long[] lowerValues;
    @JsonProperty("upper_values")
    final public long[] upperValues;

    @JsonCreator
    public LongMultiRange(@JsonProperty("generic_field") final String genericField,
                          @JsonProperty("field") final String field,
                          @JsonProperty("lower_values") final long[] lowerValues,
                          @JsonProperty("upper_values") final long[] upperValues) {
        super(LongMultiRange.class, genericField, field);
        this.lowerValues = lowerValues;
        this.upperValues = upperValues;
    }

    public LongMultiRange(final String field, final long[] lowerValues, final long[] upperValues) {
        this(null, field, lowerValues, upperValues);
    }

    public LongMultiRange(final String field, final long lowerValue, final long upperValue) {
        this(field, new long[]{lowerValue}, new long[]{upperValue});
    }

    @QuerySampleCreator(docUri = CORE_BASE_DOC_URI + "core/org/apache/lucene/document/LongPoint.html#newRangeQuery-java.lang.String-long:A-long:A-")
    public LongMultiRange(final IndexSettingsDefinition settings,
                          final Map<String, AnalyzerDefinition> analyzers,
                          final Map<String, FieldDefinition> fields) {
        this(getLongField(fields, () -> "long_field"), new long[]{100, 1000}, new long[]{199, 1999});
    }

    @Override
    @JsonIgnore
    protected boolean isEqual(LongMultiRange q) {
        return super.isEqual(q) && Arrays.equals(lowerValues, q.lowerValues) &&
            Arrays.equals(upperValues, q.upperValues);
    }

    @Override
    public Query getQuery(final QueryContext queryContext) {
        final String resolvedField = resolvePointField(queryContext.getFieldMap(), 0L, FieldTypeInterface.ValueType.longType);
        if (lowerValues.length == 1)
            return LongPoint.newRangeQuery(resolvedField, lowerValues[0], upperValues[0]);
        else
            return LongPoint.newRangeQuery(resolvedField, lowerValues, upperValues);
    }

    public static class Builder extends AbstractBuilder<Long, Builder> {

        public Builder(String genericField, String field) {
            super(genericField, field);
        }

        @Override
        protected Builder me() {
            return this;
        }

        @Override
        protected LongMultiRange build(final String field, final Collection<Long> lowerValues,
                                       final Collection<Long> upperValues) {
            return new LongMultiRange(genericField, field, ArrayUtils.toPrimitiveLong(lowerValues),
                ArrayUtils.toPrimitiveLong(upperValues));
        }

    }

}
