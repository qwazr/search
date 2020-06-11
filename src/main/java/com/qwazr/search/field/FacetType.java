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
import org.apache.lucene.document.StoredField;
import org.apache.lucene.facet.FacetField;

import java.util.Arrays;

final class FacetType extends StorableFieldType {

    FacetType(final String genericFieldName, final WildcardMatcher wildcardMatcher, final FieldDefinition definition) {
        super(of(genericFieldName, wildcardMatcher, (CustomFieldDefinition) definition).bytesRefConverter(
            BytesRefUtils.Converter.STRING));
    }

    @Override
    final protected void fillArray(final String fieldName, final String[] values, final DocumentBuilder documentBuilder) {
        documentBuilder.accept(genericFieldName, fieldName, new FacetField(fieldName, values));
        if (store)
            documentBuilder.accept(genericFieldName, fieldName, new StoredField(fieldName, Arrays.toString(values)));
    }

    private String getStringValue(Object value) {
        if (value == null)
            return null;
        final String stringValue = value.toString();
        return stringValue == null || stringValue.isEmpty() ? null : stringValue;
    }

    @Override
    protected void newFieldWithStore(final String fieldName,
                                     final Object value,
                                     final DocumentBuilder documentBuilder) {
        final String stringValue = getStringValue(value);
        if (stringValue == null)
            return;
        documentBuilder.accept(genericFieldName, fieldName, new FacetField(fieldName, stringValue));
        documentBuilder.accept(genericFieldName, fieldName, new StoredField(fieldName, stringValue));
    }

    @Override
    protected void newFieldNoStore(final String fieldName,
                                   final Object value,
                                   final DocumentBuilder documentBuilder) {
        final String stringValue = getStringValue(value);
        if (stringValue != null)
            documentBuilder.accept(genericFieldName, fieldName, new FacetField(fieldName, stringValue));
    }
}
