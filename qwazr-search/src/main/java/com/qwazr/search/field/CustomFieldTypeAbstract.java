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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class CustomFieldTypeAbstract extends FieldTypeAbstract<CustomFieldDefinition> {

    protected CustomFieldTypeAbstract(final Builder<CustomFieldDefinition> builder) {
        super(builder);
    }

    protected static boolean isStored(final CustomFieldDefinition definition) {
        return definition.stored != null && definition.stored;
    }

    protected static FacetsConfigSupplier buildFacetsConfigSuppliers(final CustomFieldDefinition definition,
                                                                     final FacetsConfigSupplier... facetSupplierArray) {
        final List<FacetsConfigSupplier> facetSuppliers = new ArrayList<>();
        if (definition.facetMultivalued != null)
            facetSuppliers.add(
                (fieldName, fieldsContext, facetsConfig)
                    -> facetsConfig.setMultiValued(fieldName, definition.facetMultivalued));
        if (definition.facetHierarchical != null)
            facetSuppliers.add(
                (fieldName, fieldsContext, facetsConfig)
                    -> facetsConfig.setHierarchical(fieldName, definition.facetHierarchical));
        if (definition.facetRequireDimCount != null)
            facetSuppliers.add((fieldName, fieldsContext, facetsConfig)
                -> facetsConfig.setRequireDimCount(fieldName, definition.facetRequireDimCount));
        Collections.addAll(facetSuppliers, facetSupplierArray);
        return reduceFacetsConfigSuppliers(facetSuppliers);
    }

    protected static FacetsConfigSupplier reduceFacetsConfigSuppliers(final List<FacetsConfigSupplier> facetSuppliers) {
        if (facetSuppliers == null || facetSuppliers.isEmpty())
            return null;
        if (facetSuppliers.size() == 1)
            return facetSuppliers.get(0);
        final FacetsConfigSupplier[] facetsConfigSupplierArray = facetSuppliers.toArray(new FacetsConfigSupplier[0]);
        return (fieldName, fieldsContext, facetsConfig) -> {
            for (final FacetsConfigSupplier facetsConfigSupplier : facetsConfigSupplierArray)
                facetsConfigSupplier.setConfig(fieldName, fieldsContext, facetsConfig);
        };
    }

}
