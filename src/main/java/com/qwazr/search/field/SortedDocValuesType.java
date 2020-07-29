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

import com.qwazr.search.field.converters.MultiReader;
import com.qwazr.search.field.converters.SingleDVConverter;
import com.qwazr.search.field.converters.ValueConverter;
import com.qwazr.search.index.BytesRefUtils;
import com.qwazr.utils.WildcardMatcher;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.util.BytesRef;

final class SortedDocValuesType extends CustomFieldTypeAbstract {

    private SortedDocValuesType(final Builder<CustomFieldDefinition> builder) {
        super(builder);
    }

    static SortedDocValuesType of(final String genericFieldName,
                                  final WildcardMatcher wildcardMatcher,
                                  final CustomFieldDefinition definition) {
        return new SortedDocValuesType(CustomFieldTypeAbstract
            .of(genericFieldName, wildcardMatcher, definition)
            .bytesRefConverter(BytesRefUtils.Converter.STRING)
            .fieldSupplier(buildFieldSupplier())
            .sortFieldSupplier(SortUtils::stringSortField)
            .valueType(ValueType.textType)
            .fieldType(FieldType.docValues));
    }

    private static FieldSupplier buildFieldSupplier() {
        return (fieldName, value, documentBuilder) ->
            documentBuilder.acceptField(new SortedDocValuesField(fieldName, new BytesRef(value.toString())));
    }

    @Override
    final public ValueConverter<?> getConverter(final String fieldName, final MultiReader reader) {
        return new SingleDVConverter.SortedDVConverter(reader, fieldName);
    }

}
