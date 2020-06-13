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
import com.qwazr.search.index.FieldMap;
import com.qwazr.search.index.QueryContext;
import com.qwazr.utils.StringUtils;
import java.util.Objects;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.SortedSetDocValuesField;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.BytesRef;

public class SortedSetDocValuesRangeQuery extends AbstractRangeQuery<String, SortedSetDocValuesRangeQuery> {

    @JsonProperty("lower_inclusive")
    public final Boolean lowerInclusive;

    @JsonProperty("upper_inclusive")
    public final Boolean upperInclusive;

    @JsonCreator
    public SortedSetDocValuesRangeQuery(@JsonProperty("generic_field") final String genericField,
                                        @JsonProperty("field") final String field,
                                        @JsonProperty("lower_value") final String lowerValue,
                                        @JsonProperty("upper_value") final String upperValue,
                                        @JsonProperty("lower_inclusive") final Boolean lowerInclusive,
                                        @JsonProperty("upper_inclusive") final Boolean upperInclusive) {
        super(SortedSetDocValuesRangeQuery.class, genericField, field,
            Objects.requireNonNull(lowerValue, "The lower value is null"),
            Objects.requireNonNull(upperValue, "The upper value is null"));
        this.lowerInclusive = lowerInclusive == null ? Boolean.TRUE : lowerInclusive;
        this.upperInclusive = upperInclusive == null ? Boolean.FALSE : upperInclusive;
    }

    public SortedSetDocValuesRangeQuery(final String field, final String lowerValue, final String upperValue,
                                        final Boolean lowerInclusive, final Boolean upperInclusive) {
        this(null, field, lowerValue, upperValue, lowerInclusive, upperInclusive);
    }

    @Override
    public Query getQuery(final QueryContext queryContext) {
        return SortedSetDocValuesField.newSlowRangeQuery(
            resolveDocValueField(queryContext.getFieldMap(), StringUtils.EMPTY, FieldTypeInterface.ValueType.textType),
            new BytesRef(lowerValue), new BytesRef(upperValue), lowerInclusive, upperInclusive);
    }

}
