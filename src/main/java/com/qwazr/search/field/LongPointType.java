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
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StoredField;

final class LongPointType extends StorableFieldType {

    LongPointType(final String genericFieldName, final WildcardMatcher wildcardMatcher,
                  final FieldDefinition definition) {
        super(of(genericFieldName, wildcardMatcher, (CustomFieldDefinition) definition).bytesRefConverter(
            BytesRefUtils.Converter.LONG_POINT));
    }

    @Override
    final protected void newFieldWithStore(String fieldName, Object value, DocumentBuilder consumer) {
        final long longValue = FieldUtils.getLongValue(value);
        consumer.accept(genericFieldName, fieldName, new LongPoint(fieldName, longValue));
        consumer.accept(genericFieldName, fieldName, new StoredField(fieldName, longValue));
    }

    @Override
    final protected void newFieldNoStore(String fieldName, Object value, DocumentBuilder consumer) {
        consumer.accept(genericFieldName, fieldName, new LongPoint(fieldName, FieldUtils.getLongValue(value)));
    }

}
