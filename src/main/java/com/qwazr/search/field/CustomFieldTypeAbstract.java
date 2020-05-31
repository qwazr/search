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

import com.qwazr.search.index.DocumentBuilder;

abstract class CustomFieldTypeAbstract extends FieldTypeAbstract<CustomFieldDefinition> {

    protected CustomFieldTypeAbstract(final Builder<CustomFieldDefinition> builder) {
        super(builder);
    }

    @Override
    final Builder<CustomFieldDefinition> setup(Builder<CustomFieldDefinition> builder) {

        // Setup facets
        if (builder.definition.facetMultivalued != null)
            builder.facetConfig(((fieldName, fieldMap, facetsConfig) -> facetsConfig.setMultiValued(fieldName,
                builder.definition.facetMultivalued)));
        if (builder.definition.facetHierarchical != null)
            builder.facetConfig(((fieldName, fieldMap, facetsConfig) -> facetsConfig.setHierarchical(fieldName,
                builder.definition.facetHierarchical)));
        if (builder.definition.facetRequireDimCount != null)
            builder.facetConfig(((fieldName, fieldMap, facetsConfig) -> facetsConfig.setRequireDimCount(fieldName,
                builder.definition.facetRequireDimCount)));

        setupFields(builder);
        if (builder.definition.stored != null && builder.definition.stored)
            builder.storedFieldNameProvider(f -> f);

        builder.queryFieldNameProvider(f -> f);

        return builder;
    }

    abstract void setupFields(Builder<CustomFieldDefinition> builder);

    static abstract class OneField extends CustomFieldTypeAbstract {

        protected OneField(Builder<CustomFieldDefinition> builder) {
            super(builder);
        }

        @Override
        final void setupFields(Builder<CustomFieldDefinition> builder) {
            builder.fieldProvider(this::newField);
        }

        abstract void newField(final String fieldName,
                               final Object value,
                               final DocumentBuilder documentBuilder);
    }

    static abstract class NoField extends CustomFieldTypeAbstract {

        protected NoField(Builder<CustomFieldDefinition> builder) {
            super(builder);
        }

        @Override
        final void setupFields(Builder<CustomFieldDefinition> builder) {
        }

    }

}
