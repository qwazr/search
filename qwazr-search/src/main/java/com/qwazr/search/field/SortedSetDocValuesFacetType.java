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
import org.apache.lucene.document.StoredField;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetField;

final class SortedSetDocValuesFacetType extends CustomFieldTypeAbstract {

    private SortedSetDocValuesFacetType(final Builder<CustomFieldDefinition> builder) {
        super(builder);
    }

    static SortedSetDocValuesFacetType of(final String genericFieldName,
                                          final WildcardMatcher wildcardMatcher,
                                          final CustomFieldDefinition definition) {
        final FacetsConfigSupplier facetsConfigSupplier = buildFacetsConfigSupplier(definition);
        final FieldSupplier fieldSupplier = buildFieldSupplier(facetsConfigSupplier, definition);
        return new SortedSetDocValuesFacetType(CustomFieldTypeAbstract.of(genericFieldName, wildcardMatcher, definition)
            .bytesRefConverter(BytesRefUtils.Converter.STRING)
            .fieldSupplier(fieldSupplier)
            .facetsConfigSupplier(facetsConfigSupplier)
            .valueType(ValueType.textType)
            .fieldType(FieldType.facetField));

    }

    private static FieldSupplier buildFieldSupplier(final FacetsConfigSupplier facetsConfigSupplier,
                                                    final CustomFieldDefinition definition) {
        if (isStored(definition)) {
            return (fieldName, value, documentBuilder) -> {
                final String stringValue = FieldUtils.getStringValue(value);
                if (stringValue == null)
                    return;
                documentBuilder.acceptFacetField(new SortedSetDocValuesFacetField(fieldName, stringValue), fieldName, facetsConfigSupplier);
                documentBuilder.acceptField(
                    new StoredField(fieldName, stringValue));
            };
        } else {
            return (fieldName, value, documentBuilder) -> {
                final String stringValue = FieldUtils.getStringValue(value);
                if (stringValue != null)
                    documentBuilder.acceptFacetField(new SortedSetDocValuesFacetField(fieldName, stringValue), fieldName, facetsConfigSupplier);

            };
        }
    }

    private static FacetsConfigSupplier buildFacetsConfigSupplier(final CustomFieldDefinition definition) {
        return CustomFieldTypeAbstract.buildFacetsConfigSuppliers(definition,
            (dimensionName, context, config) -> config.setIndexFieldName(dimensionName, context.sortedSetFacetField));
    }


}
