/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.field;

import com.qwazr.search.index.BytesRefUtils;
import com.qwazr.utils.WildcardMatcher;
import java.util.Collection;
import java.util.LinkedHashSet;
import javax.validation.constraints.NotNull;
import org.apache.lucene.index.Term;

abstract class CustomFieldTypeAbstract extends FieldTypeAbstract<CustomFieldDefinition> {

    protected CustomFieldTypeAbstract(final String genericFieldName,
                                      final WildcardMatcher wildcardMatcher,
                                      final BytesRefUtils.Converter<?> bytesRefConverter,
                                      final FieldSupplier fieldSupplier,
                                      final SortFieldSupplier sortFieldSupplier,
                                      final CustomFieldDefinition definition) {
        super(genericFieldName, wildcardMatcher, bytesRefConverter, fieldSupplier,
            buildFacetConfig(definition), sortFieldSupplier, definition);
    }

    private static FacetSupplier buildFacetConfig(final CustomFieldDefinition definition) {
        final Collection<FacetSupplier> facetSuppliers = new LinkedHashSet<>();
        if (definition.facetMultivalued != null)
            facetSuppliers.add(
                (fieldName, facetsConfig)
                    -> facetsConfig.setMultiValued(fieldName, definition.facetMultivalued));
        if (definition.facetHierarchical != null)
            facetSuppliers.add(
                (fieldName, facetsConfig)
                    -> facetsConfig.setHierarchical(fieldName, definition.facetHierarchical));
        if (definition.facetRequireDimCount != null)
            facetSuppliers.add((fieldName, facetsConfig)
                -> facetsConfig.setRequireDimCount(fieldName, definition.facetRequireDimCount));
        if (facetSuppliers.isEmpty())
            return null;
        if (facetSuppliers.size() == 1)
            return facetSuppliers.iterator().next();
        final FacetSupplier[] facetSupplierArray = facetSuppliers.toArray(new FacetSupplier[0]);
        return (fieldName, facetsConfig) -> {
            for (final FacetSupplier facetSupplier : facetSupplierArray)
                facetSupplier.setConfig(fieldName, facetsConfig);
        };
    }

    @Override
    public String getQueryFieldName(@NotNull final LuceneFieldType luceneFieldType,
                                    @NotNull final String fieldName) {
        return fieldName;
    }

    @Override
    public String getStoredFieldName(String fieldName) {
        return null;
    }

    @Override
    public Term term(String fieldName, Object value) {
        return null;
    }

}
