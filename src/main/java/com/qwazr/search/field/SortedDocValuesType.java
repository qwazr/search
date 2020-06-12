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
import org.apache.lucene.index.Term;
import org.apache.lucene.util.BytesRef;

final class SortedDocValuesType extends CustomFieldTypeAbstract {

    SortedDocValuesType(final String genericFieldName,
                        final WildcardMatcher wildcardMatcher,
                        final CustomFieldDefinition definition) {
        super(genericFieldName, wildcardMatcher,
            BytesRefUtils.Converter.STRING,
            buildFieldSupplier(genericFieldName),
            SortUtils::stringSortField,
            definition);
    }

    private static FieldTypeInterface.FieldSupplier buildFieldSupplier(final String genericFieldName) {
        return (fieldName, value, documentBuilder) ->
            documentBuilder.accept(genericFieldName, fieldName,
                new SortedDocValuesField(fieldName, new BytesRef(value.toString())));
    }

    @Override
    final public ValueConverter<?> getConverter(final String fieldName, final MultiReader reader) {
        return new SingleDVConverter.SortedDVConverter(reader, fieldName);
    }

    @Override
    public String getStoredFieldName(String fieldName) {
        return fieldName;
    }

    @Override
    public Term term(String fieldName, Object value) {
        return new Term(fieldName, value.toString());
    }

}
