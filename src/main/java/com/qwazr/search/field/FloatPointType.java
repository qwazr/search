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
package com.qwazr.search.field;

import com.qwazr.search.index.BytesRefUtils;
import com.qwazr.utils.WildcardMatcher;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.document.StoredField;

final class FloatPointType extends CustomFieldTypeAbstract {

    private FloatPointType(final Builder<CustomFieldDefinition> builder) {
        super(builder);
    }

    static FloatPointType of(final String genericFieldName,
                             final WildcardMatcher wildcardMatcher,
                             final CustomFieldDefinition definition) {
        return new FloatPointType(CustomFieldTypeAbstract
            .of(genericFieldName, wildcardMatcher, definition)
            .bytesRefConverter(BytesRefUtils.Converter.FLOAT_POINT)
            .fieldSupplier(buildFieldSupplier(definition))
            .valueType(ValueType.floatType)
            .fieldType(FieldType.pointField));
    }

    private static FieldTypeInterface.FieldSupplier buildFieldSupplier(final CustomFieldDefinition definition) {
        if (isStored(definition))
            return (fieldName, value, documentBuilder) -> {
                final float floatValue = FieldUtils.getFloatValue(value);
                documentBuilder.acceptField(new FloatPoint(fieldName, floatValue));
                documentBuilder.acceptField(new StoredField(fieldName, floatValue));
            };
        else
            return (fieldName, value, documentBuilder) -> documentBuilder.acceptField(
                new FloatPoint(fieldName, FieldUtils.getFloatValue(value)));
    }

}
