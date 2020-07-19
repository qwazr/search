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
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StoredField;

final class LongPointType extends CustomFieldTypeAbstract {

    LongPointType(final String genericFieldName,
                  final WildcardMatcher wildcardMatcher,
                  final CustomFieldDefinition definition) {
        super(genericFieldName, wildcardMatcher,
            BytesRefUtils.Converter.LONG_POINT,
            buildFieldSupplier(genericFieldName, definition),
            null,
            null,
            definition,
            ValueType.longType,
            FieldType.pointField);
    }

    private static FieldSupplier buildFieldSupplier(final String genericFieldName,
                                                    final CustomFieldDefinition definition) {
        if (isStored(definition))
            return (fieldName, value, documentBuilder) -> {
                final long longValue = FieldUtils.getLongValue(value);
                documentBuilder.accept(genericFieldName, fieldName, new LongPoint(fieldName, longValue));
                documentBuilder.accept(genericFieldName, fieldName, new StoredField(fieldName, longValue));
            };
        else
            return (fieldName, value, documentBuilder) -> documentBuilder.accept(genericFieldName, fieldName,
                new LongPoint(fieldName, FieldUtils.getLongValue(value)));
    }

}
