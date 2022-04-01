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

public class ExactFloat extends AbstractExactQuery<Float, ExactFloat> {

    final static Float ZERO = 0F;

    @JsonCreator
    public ExactFloat(@JsonProperty("generic_field") final String genericField,
                      @JsonProperty("field") final String field,
                      @JsonProperty("value") final Float value) {
        super(ExactFloat.class, genericField, field, value == null ? ZERO : value);
    }

    public ExactFloat(final String field, final Float value) {
        this(null, field, value);
    }

    @QuerySampleCreator(docUri = CORE_BASE_DOC_URI + "core/org/apache/lucene/document/FloatPoint.html#newExactQuery-java.lang.String-float-s")
    public ExactFloat(final IndexSettingsDefinition settings,
                      final Map<String, AnalyzerDefinition> analyzers,
                      final Map<String, FieldDefinition> fields) {
        this(getFloatField(fields, () -> "floatField"), 3.14f);
    }

    @Override
    public Query getQuery(final QueryContext queryContext) {
        return FloatPoint.newExactQuery(
            resolvePointField(queryContext.getFieldMap(), ZERO, FieldTypeInterface.ValueType.floatType),
            value);
    }
}
