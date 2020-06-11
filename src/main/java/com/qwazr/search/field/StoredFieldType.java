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
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.utils.WildcardMatcher;
import javax.validation.constraints.NotNull;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;

import java.io.Externalizable;
import java.io.Serializable;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.SortField;

final class StoredFieldType extends CustomFieldTypeAbstract.OneField {

    StoredFieldType(final String genericFieldName, final WildcardMatcher wildcardMatcher,
                    final FieldDefinition definition) {
        super(of(genericFieldName, wildcardMatcher, (CustomFieldDefinition) definition).bytesRefConverter(
            BytesRefUtils.Converter.STRING));
    }

    final public String getStoredFieldName(String fieldName) {
        return fieldName;
    }

    @Override
    public Term term(String fieldName, Object value) {
        return null;
    }

    @Override
    public SortField getSortField(final String fieldName, final QueryDefinition.SortEnum sortEnum) {
        return null;
    }

    @Override
    final public String getQueryFieldName(@NotNull final LuceneFieldType luceneFieldType,
                                          @NotNull final String fieldName) {
        return null;
    }

    @Override
    final public void newField(final String fieldName, final Object value, final DocumentBuilder documentBuilder) {
        final Field field;
        final byte[] bytes;
        if (value instanceof String)
            field = new StoredField(fieldName, (String) value);
        else if (value instanceof Integer)
            field = new StoredField(fieldName, (int) value);
        else if (value instanceof Long)
            field = new StoredField(fieldName, (long) value);
        else if (value instanceof Float)
            field = new StoredField(fieldName, (float) value);
        else if ((bytes = TypeUtils.toPrimitiveByteArray(value)) != null)
            field = new StoredField(fieldName, bytes);
        else if (value instanceof Externalizable)
            field = new StoredField(fieldName, TypeUtils.toBytes(fieldName, (Externalizable) value));
        else if (value instanceof Serializable)
            field = new StoredField(fieldName, TypeUtils.toBytes(fieldName, (Serializable) value));
        else // Last change, convert to string
            field = new StoredField(fieldName, value.toString());
        documentBuilder.accept(genericFieldName, fieldName, field);
    }

}
