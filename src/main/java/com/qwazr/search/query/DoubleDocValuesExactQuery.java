/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
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
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.field.FieldTypeInterface;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.QueryContext;
import java.net.URI;
import java.util.Map;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.NumericUtils;

public class DoubleDocValuesExactQuery extends AbstractExactQuery<Double, DoubleDocValuesExactQuery> {

    @JsonCreator
    public DoubleDocValuesExactQuery(@JsonProperty("generic_field") final String genericField,
                                     @JsonProperty("field") final String field,
                                     @JsonProperty("value") final Double value) {
        super(DoubleDocValuesExactQuery.class, genericField, field, value);
    }

    public DoubleDocValuesExactQuery(final String field, final Double value) {
        this(null, field, value);
    }

    private final static URI DOC = URI.create("core/org/apache/lucene/document/NumericDocValuesField.html#newSlowExactQuery-java.lang.String-long-");

    public DoubleDocValuesExactQuery(final IndexSettingsDefinition settings,
                                     final Map<String, AnalyzerDefinition> analyzers,
                                     final Map<String, FieldDefinition> fields) {
        super(DoubleDocValuesExactQuery.class, DOC, getDoubleField(fields, () -> "doubleField"), 3.14d);
    }

    @Override
    public Query getQuery(final QueryContext queryContext) {
        return NumericDocValuesField.newSlowExactQuery(
            resolveDocValueField(queryContext.getFieldMap(), 0D, FieldTypeInterface.ValueType.doubleType),
            NumericUtils.doubleToSortableLong(value)
        );
    }
}
