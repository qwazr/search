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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.annotations.QuerySampleCreator;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.field.FieldTypeInterface;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.QueryContext;
import java.util.Map;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.search.Query;

public class FloatRange extends AbstractRangeQuery<Float, FloatRange> {

    @JsonCreator
    public FloatRange(@JsonProperty("generic_field") final String genericField,
                      @JsonProperty("field") final String field,
                      @JsonProperty("lower_value") final Float lowerValue,
                      @JsonProperty("upper_value") final Float upperValue) {
        super(FloatRange.class, genericField, field,
            lowerValue == null ? FloatDocValuesRangeQuery.MIN : lowerValue,
            upperValue == null ? FloatDocValuesRangeQuery.MAX : upperValue);
    }

    public FloatRange(final String field, final Float lowerValue, final Float upperValue) {
        this(null, field, lowerValue, upperValue);
    }

    @QuerySampleCreator(docUri = CORE_BASE_DOC_URI + "core/org/apache/lucene/document/FloatPoint.html#newRangeQuery-java.lang.String-float-float-")
    public FloatRange(final IndexSettingsDefinition settings,
                      final Map<String, AnalyzerDefinition> analyzers,
                      final Map<String, FieldDefinition> fields) {
        this(getFloatField(fields, () -> "float_field"), 3.14f, 6.28f);
    }

    @Override
    public Query getQuery(final QueryContext queryContext) {
        return FloatPoint.newRangeQuery(
            resolvePointField(queryContext.getFieldMap(), 0F, FieldTypeInterface.ValueType.floatType),
            lowerValue, upperValue
        );
    }
}
