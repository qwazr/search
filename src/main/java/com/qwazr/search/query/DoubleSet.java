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
import java.util.Arrays;
import java.util.Map;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.search.Query;

public class DoubleSet extends AbstractFieldQuery<DoubleSet> {

    final public double[] values;

    @JsonCreator
    public DoubleSet(@JsonProperty("generic_field") final String genericField,
                     @JsonProperty("field") final String field,
                     @JsonProperty("values") final double... values) {
        super(DoubleSet.class, genericField, field);
        this.values = values;
    }

    @Override
    @JsonIgnore
    protected boolean isEqual(DoubleSet q) {
        return super.isEqual(q) && Arrays.equals(values, q.values);
    }

    public DoubleSet(final String field, final double... values) {
        this(null, field, values);
    }

    @QuerySampleCreator(docUri = CORE_BASE_DOC_URI + "core/org/apache/lucene/document/DoublePoint.html#newSetQuery-java.lang.String-double...-")
    public DoubleSet(final IndexSettingsDefinition settings,
                     final Map<String, AnalyzerDefinition> analyzers,
                     final Map<String, FieldDefinition> fields) {
        this(getDoubleField(fields, () -> "double_field"), 1.57f, 3.14f, 6.28f);
    }

    @Override
    public Query getQuery(final QueryContext queryContext) {
        return DoublePoint.newSetQuery(
            resolvePointField(queryContext.getFieldMap(), 0D, FieldTypeInterface.ValueType.doubleType),
            values
        );
    }
}
