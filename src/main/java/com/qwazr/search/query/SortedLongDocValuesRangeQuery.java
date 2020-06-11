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

public class SortedLongDocValuesRangeQuery extends AbstractRangeQuery<Long, SortedLongDocValuesRangeQuery> {

    @JsonCreator
    public SortedLongDocValuesRangeQuery(@JsonProperty("generic_field") final String genericField,
                                         @JsonProperty("field") final String field, @JsonProperty("lower_value") final Long lowerValue,
                                         @JsonProperty("upper_value") final Long upperValue) {
        super(SortedLongDocValuesRangeQuery.class, genericField, field,
            lowerValue == null ? LongDocValuesRangeQuery.MIN : lowerValue,
            upperValue == null ? LongDocValuesRangeQuery.MAX : upperValue);
    }

    public SortedLongDocValuesRangeQuery(final String field, final Long lowerValue, final Long upperValue) {
        this(null, field, lowerValue, upperValue);
    }

    @Override
    public Query getQuery(final QueryContext queryContext) {
        return SortedNumericDocValuesField.newSlowRangeQuery(
            resolveField(queryContext.getFieldMap(), FieldTypeInterface.LuceneFieldType.docValue),
            lowerValue, upperValue);
    }
}
