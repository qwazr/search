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
import com.qwazr.search.field.converters.ValueConverter;
import com.qwazr.search.index.FieldMap;
import com.qwazr.search.index.DocumentBuilder;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.utils.WildcardMatcher;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.SortField;
import org.apache.lucene.util.BytesRef;

public interface FieldTypeInterface {

    void dispatch(final String fieldName, final Object value, final DocumentBuilder luceneDocumentBuilder);

    SortField getSortField(final String fieldName, final QueryDefinition.SortEnum sortEnum);

    ValueConverter<?> getConverter(final String fieldName, final MultiReader reader);

    Object toTerm(final BytesRef bytesRef);

    String getQueryFieldName(String fieldName);

    String getStoredFieldName(String fieldName);

    FieldDefinition getDefinition();

    void copyTo(final String fieldName, final FieldTypeInterface fieldType);

    void setFacetsConfig(final String fieldName, final FieldMap fieldMap, final FacetsConfig facetsConfig);

    Term term(String fieldName, Object value);

    @FunctionalInterface
    interface Facet {
        void config(final String fieldName,
                    final FieldMap fieldMap,
                    final FacetsConfig facetsConfig);
    }

    @FunctionalInterface
    interface FieldProvider {
        void fillValue(final String fieldName,
                       final Object value,
                       final DocumentBuilder documentBuilder);
    }

    @FunctionalInterface
    interface TermProvider {
        Term term(final String fieldName,
                  final Object value);
    }

    @FunctionalInterface
    interface FieldNameProvider {
        String fieldName(final String fieldName);
    }

    @FunctionalInterface
    interface SortFieldProvider {
        SortField sortField(final String fieldName,
                            final QueryDefinition.SortEnum sortEnum);
    }

    @FunctionalInterface
    interface Supplier {
        FieldTypeInterface newFieldType(final String genericFieldName,
                                        final WildcardMatcher wildcardMatcher,
                                        final FieldDefinition definition);
    }

}
