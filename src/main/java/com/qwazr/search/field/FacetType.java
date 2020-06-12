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
import java.util.Arrays;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.facet.FacetField;

final class FacetType extends CustomFieldTypeAbstract {

    private final boolean store;

    FacetType(final String genericFieldName,
              final WildcardMatcher wildcardMatcher,
              final CustomFieldDefinition definition) {
        super(genericFieldName, wildcardMatcher,
            BytesRefUtils.Converter.STRING,
            buildFieldSupplier(genericFieldName, definition),
            null,
            definition);
        store = isStored(definition);
    }

    @Override
    final protected void fillArray(final String fieldName, final String[] values, final DocumentBuilder documentBuilder) {
        documentBuilder.accept(genericFieldName, fieldName, new FacetField(fieldName, values));
        if (store)
            documentBuilder.accept(genericFieldName, fieldName, new StoredField(fieldName, Arrays.toString(values)));
    }

    private static FieldSupplier buildFieldSupplier(final String genericFieldName,
                                                    final CustomFieldDefinition definition) {
        if (isStored(definition))
            return (fieldName, value, documentBuilder) -> {
                final String stringValue = FieldUtils.getStringValue(value);
                if (stringValue == null)
                    return;
                documentBuilder.accept(genericFieldName, fieldName, new FacetField(fieldName, stringValue));
                documentBuilder.accept(genericFieldName, fieldName, new StoredField(fieldName, stringValue));
            };
        else
            return (fieldName, value, documentBuilder) -> {
                final String stringValue = FieldUtils.getStringValue(value);
                if (stringValue != null)
                    documentBuilder.accept(genericFieldName, fieldName, new FacetField(fieldName, stringValue));
            };
    }

}
