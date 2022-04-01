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
import com.qwazr.search.field.FieldTypeInterface;
import com.qwazr.search.index.QueryContext;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.NumericUtils;

public class SortedDoubleDocValuesExactQuery extends AbstractExactQuery<Double, SortedDoubleDocValuesExactQuery> {

    @JsonCreator
    public SortedDoubleDocValuesExactQuery(@JsonProperty("generic_field") final String genericField,
                                           @JsonProperty("field") final String field, @JsonProperty("value") final Double value) {
        super(SortedDoubleDocValuesExactQuery.class, genericField, field, value);
    }

    public SortedDoubleDocValuesExactQuery(final String field, final Double value) {
        this(null, field, value);
    }

    @Override
    public Query getQuery(final QueryContext queryContext) {
        return SortedNumericDocValuesField.newSlowExactQuery(
            resolveDocValueField(queryContext.getFieldMap(), 0D, FieldTypeInterface.ValueType.doubleType),
            NumericUtils.doubleToSortableLong(value));
    }
}
