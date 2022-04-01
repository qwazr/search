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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;

final class StringFieldType extends CustomFieldTypeAbstract {

    private StringFieldType(final Builder<CustomFieldDefinition> builder) {
        super(builder);
    }

    static StringFieldType of(final String genericFieldName,
                              final WildcardMatcher wildcardMatcher,
                              final CustomFieldDefinition definition) {
        return new StringFieldType(CustomFieldTypeAbstract
            .of(genericFieldName, wildcardMatcher, definition)
            .bytesRefConverter(BytesRefUtils.Converter.STRING)
            .fieldSupplier(buildFieldSupplier(definition))
            .sortFieldSupplier(SortUtils::stringSortField)
            .primaryTermSupplier(FieldUtils::newStringTerm)
            .valueType(ValueType.textType)
            .fieldTypes(getFieldTypes(definition)));
    }

    private static Collection<FieldType> getFieldTypes(final CustomFieldDefinition definition) {
        if (isStored(definition))
            return Arrays.asList(FieldType.stringField, FieldType.storedField);
        else
            return Collections.singletonList(FieldType.stringField);
    }

    private static FieldTypeInterface.FieldSupplier buildFieldSupplier(final CustomFieldDefinition definition) {
        final Field.Store fieldStore = isStored(definition) ? Field.Store.YES : Field.Store.NO;
        return (fieldName, value, documentBuilder) -> documentBuilder.acceptField(
            new StringField(fieldName, value.toString(), fieldStore));
    }

}
