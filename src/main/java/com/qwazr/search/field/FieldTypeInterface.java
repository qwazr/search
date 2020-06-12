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
import com.qwazr.search.index.DocumentBuilder;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.utils.WildcardMatcher;
import javax.validation.constraints.NotNull;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.SortField;
import org.apache.lucene.util.BytesRef;

public interface FieldTypeInterface {

    enum LuceneFieldType {
        text, point, facet, docValue;
    }

    void dispatch(final String fieldName, final Object value, final DocumentBuilder luceneDocumentBuilder);

    SortField getSortField(final String fieldName, final QueryDefinition.SortEnum sortEnum);

    ValueConverter<?> getConverter(final String fieldName, final MultiReader reader);

    Object toTerm(final BytesRef bytesRef);

    String getQueryFieldName(@NotNull final LuceneFieldType luceneFieldType,
                             @NotNull final String fieldName);

    String getStoredFieldName(String fieldName);

    FieldDefinition getDefinition();

    void copyTo(final String fieldName, final FieldTypeInterface fieldType);

    void applyFacetsConfig(final String fieldName, final FacetsConfig facetsConfig);

    Term term(String fieldName, Object value);

    @FunctionalInterface
    interface Supplier<T extends FieldDefinition> {
        FieldTypeInterface newFieldType(final String genericFieldName,
                                        final WildcardMatcher wildcardMatcher,
                                        final T definition);
    }
    
    @FunctionalInterface
    interface FacetSupplier {
        void setConfig(final String fieldName,
                       final FacetsConfig facetsConfig);
    }

    @FunctionalInterface
    interface FieldSupplier {
        void addFields(final String fieldName,
                       final Object value,
                       final DocumentBuilder documentBuilder);
    }

    @FunctionalInterface
    interface SortFieldSupplier {
        SortField newSortField(final String fieldname,
                               final QueryDefinition.SortEnum sortEnum);
    }

}
