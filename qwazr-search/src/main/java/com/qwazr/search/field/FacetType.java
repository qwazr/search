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
import java.util.Collection;
import java.util.Collections;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.facet.FacetField;

final class FacetType extends CustomFieldTypeAbstract {

    private final boolean store;

    private FacetType(final Builder<CustomFieldDefinition> builder, boolean store) {
        super(builder);
        this.store = store;
    }

    static FacetType of(final String genericFieldName,
                        final WildcardMatcher wildcardMatcher,
                        final CustomFieldDefinition definition) {
        final boolean isStored = isStored(definition);
        final FacetsConfigSupplier facetsConfigSupplier = buildFacetsConfigSupplier(definition);
        return new FacetType(CustomFieldTypeAbstract
            .of(genericFieldName, wildcardMatcher, definition)
            .bytesRefConverter(BytesRefUtils.Converter.STRING)
            .fieldSupplier(buildFieldSupplier(isStored, facetsConfigSupplier))
            .facetsConfigSupplier(facetsConfigSupplier)
            .valueType(ValueType.textType)
            .fieldTypes(getFieldTypes(isStored)), isStored);
    }

    private static Collection<FieldType> getFieldTypes(final boolean isStored) {
        if (isStored)
            return Arrays.asList(FieldType.facetField, FieldType.storedField);
        else
            return Collections.singletonList(FieldType.facetField);
    }

    @Override
    final protected void fillArray(final String fieldName, final String[] values, final DocumentBuilder<?> documentBuilder) {
        documentBuilder.acceptFacetField(new FacetField(fieldName, values), fieldName, facetsConfigSupplier);
        if (store)
            documentBuilder.acceptField(new StoredField(fieldName, Arrays.toString(values)));
    }

    private static FieldSupplier buildFieldSupplier(final boolean isStored,
                                                    final FacetsConfigSupplier facetsConfigSupplier) {
        if (isStored)
            return (fieldName, value, documentBuilder) -> {
                final String stringValue = FieldUtils.getStringValue(value);
                if (stringValue == null)
                    return;
                documentBuilder.acceptFacetField(new FacetField(fieldName, stringValue), fieldName, facetsConfigSupplier);
                documentBuilder.acceptField(new StoredField(fieldName, stringValue));
            };
        else
            return (fieldName, value, documentBuilder) -> {
                final String stringValue = FieldUtils.getStringValue(value);
                if (stringValue != null)
                    documentBuilder.acceptFacetField(new FacetField(fieldName, stringValue), fieldName, facetsConfigSupplier);
            };
    }

    private static FacetsConfigSupplier buildFacetsConfigSupplier(final CustomFieldDefinition definition) {
        return CustomFieldTypeAbstract.buildFacetsConfigSuppliers(definition,
            (dimensionName, context, config) -> config.setIndexFieldName(dimensionName, FieldDefinition.TAXONOMY_FACET_FIELD));
    }
}
