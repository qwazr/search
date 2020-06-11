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
import com.qwazr.search.index.DocumentBuilder;
import com.qwazr.utils.WildcardMatcher;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StoredField;

final class IntPointType extends StorableFieldType {

    IntPointType(final String genericFieldName, final WildcardMatcher wildcardMatcher,
                 final FieldDefinition definition) {
        super(of(genericFieldName, wildcardMatcher, (CustomFieldDefinition) definition).bytesRefConverter(
            BytesRefUtils.Converter.INT_POINT));
    }

    @Override
    protected void newFieldWithStore(String fieldName, Object value, DocumentBuilder consumer) {
        final int intValue = FieldUtils.getIntValue(value);
        consumer.accept(genericFieldName, fieldName, new IntPoint(fieldName, intValue));
        consumer.accept(genericFieldName, fieldName, new StoredField(fieldName, intValue));
    }

    @Override
    protected void newFieldNoStore(String fieldName, Object value, DocumentBuilder consumer) {
        consumer.accept(genericFieldName, fieldName, new IntPoint(fieldName, FieldUtils.getIntValue(value)));
    }
}
