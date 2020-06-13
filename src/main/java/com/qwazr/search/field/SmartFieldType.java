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

import com.qwazr.search.analysis.SmartAnalyzerSet;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.WildcardMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.constraints.NotNull;

final class SmartFieldType extends FieldTypeAbstract<SmartFieldDefinition> {

    SmartFieldType(@NotNull final String genericFieldName,
                   final WildcardMatcher wildcardMatcher,
                   @NotNull final SmartFieldDefinition definition) {
        super(genericFieldName, wildcardMatcher,
            null,
            buildFieldSupplier(genericFieldName, wildcardMatcher, definition),
            buildFacetSupplier(genericFieldName, wildcardMatcher, definition),
            buildSortFieldSupplier(genericFieldName, wildcardMatcher, definition),
            buildPrimaryTermSupplier(genericFieldName, wildcardMatcher, definition),
            buildIndexTermSupplier(genericFieldName, wildcardMatcher, definition),
            buildStoreFieldResolver(genericFieldName, wildcardMatcher, definition),
            buildIndexFieldResolver(genericFieldName, wildcardMatcher, definition),
            definition);
    }

    private static SmartFieldDefinition.Type getType(final SmartFieldDefinition definition) {
        return definition.type == null ? SmartFieldDefinition.Type.TEXT : definition.type;
    }

    private static int getMaxKeywordLength(final SmartFieldDefinition definition) {
        return definition.maxKeywordLength == null
            ? SmartFieldDefinition.DEFAULT_MAX_KEYWORD_LENGTH : definition.maxKeywordLength;
    }

    private static boolean isIndex(final SmartFieldDefinition definition) {
        return definition.index != null && definition.index;
    }

    private static boolean isFacet(final SmartFieldDefinition definition) {
        return definition.facet != null && definition.facet;
    }

    private static boolean isStored(final SmartFieldDefinition definition) {
        return definition.stored != null && definition.stored;
    }

    private static boolean isSort(final SmartFieldDefinition definition) {
        return definition.sort != null && definition.sort;
    }

    private static FacetSupplier buildFacetSupplier(final String genericFieldName,
                                                    final WildcardMatcher wildcardMatcher,
                                                    final SmartFieldDefinition definition) {
        if (!isFacet(definition))
            return null;
        final FieldTypeInterface.FieldNameResolver fieldNameSupplier = SmartFieldProvider
            .facetFieldNameSupplier(genericFieldName, wildcardMatcher);
        return (fieldName, fieldMap, facetsConfig) -> {
            final String resolvedFieldName = fieldNameSupplier.resolve(fieldName);
            facetsConfig.setMultiValued(resolvedFieldName, true);
            facetsConfig.setIndexFieldName(resolvedFieldName, fieldMap.sortedSetFacetField);
        };
    }

    private static boolean isFullTextIndexAnalyzer(final SmartFieldDefinition definition) {
        final String keywordName = SmartAnalyzerSet.keyword.name();
        if (StringUtils.isNotEmpty(definition.indexAnalyzer)
            && !Objects.equals(definition.indexAnalyzer, keywordName))
            return true;
        if (StringUtils.isNotEmpty(definition.analyzer)
            && !Objects.equals(definition.analyzer, keywordName))
            return true;
        return false;
    }

    private static FieldSupplier getStoredFieldSupplier(final String genericFieldName,
                                                        final WildcardMatcher wildcardMatcher,
                                                        final SmartFieldDefinition definition) {
        switch (getType(definition)) {
            case TEXT:
                return SmartFieldProvider.fieldStoredText(genericFieldName, wildcardMatcher);
            case LONG:
                return SmartFieldProvider.fieldStoredLong(genericFieldName, wildcardMatcher);
            case DOUBLE:
                return SmartFieldProvider.fieldStoredDouble(genericFieldName, wildcardMatcher);
            case INTEGER:
                return SmartFieldProvider.fieldStoredInteger(genericFieldName, wildcardMatcher);
            case FLOAT:
                return SmartFieldProvider.fieldStoredFloat(genericFieldName, wildcardMatcher);
            default:
                return null;
        }
    }

    private static FieldSupplier getIndexFieldSupplier(final String genericFieldName,
                                                       final WildcardMatcher wildcardMatcher,
                                                       final SmartFieldDefinition definition) {
        switch (getType(definition)) {
            case TEXT:
                final int maxKeywordLength = getMaxKeywordLength(definition);
                return SmartFieldProvider.fieldStringText(genericFieldName, wildcardMatcher, maxKeywordLength);
            case LONG:
                return SmartFieldProvider.fieldPointLong(genericFieldName, wildcardMatcher);
            case DOUBLE:
                return SmartFieldProvider.fieldPointDouble(genericFieldName, wildcardMatcher);
            case INTEGER:
                return SmartFieldProvider.fieldPointInteger(genericFieldName, wildcardMatcher);
            case FLOAT:
                return SmartFieldProvider.fieldPointFloat(genericFieldName, wildcardMatcher);
            default:
                return null;
        }
    }

    private static TermSupplier buildPrimaryTermSupplier(final String genericFieldName,
                                                         final WildcardMatcher wildcardMatcher,
                                                         final SmartFieldDefinition definition) {
        switch (getType(definition)) {
            case TEXT:
                final int maxKeywordLength = getMaxKeywordLength(definition);
                return SmartFieldProvider.stringTermText(genericFieldName, wildcardMatcher, maxKeywordLength);
            case LONG:
                return SmartFieldProvider.stringTermLong(genericFieldName, wildcardMatcher);
            case DOUBLE:
                return SmartFieldProvider.stringTermDouble(genericFieldName, wildcardMatcher);
            case INTEGER:
                return SmartFieldProvider.stringTermInteger(genericFieldName, wildcardMatcher);
            case FLOAT:
                return SmartFieldProvider.stringTermFloat(genericFieldName, wildcardMatcher);
            default:
                return null;
        }
    }

    private static TermSupplier buildIndexTermSupplier(final String genericFieldName,
                                                       final WildcardMatcher wildcardMatcher,
                                                       final SmartFieldDefinition definition) {
        switch (getType(definition)) {
            case TEXT:
                final int maxKeywordLength = getMaxKeywordLength(definition);
                return SmartFieldProvider.stringTermText(genericFieldName, wildcardMatcher, maxKeywordLength);
            case LONG:
                return SmartFieldProvider.stringTermLong(genericFieldName, wildcardMatcher);
            case DOUBLE:
                return SmartFieldProvider.stringTermDouble(genericFieldName, wildcardMatcher);
            case INTEGER:
                return SmartFieldProvider.stringTermInteger(genericFieldName, wildcardMatcher);
            case FLOAT:
                return SmartFieldProvider.stringTermFloat(genericFieldName, wildcardMatcher);
            default:
                return null;
        }
    }

    private static FieldSupplier getSortedDocValuesFieldSupplier(final String genericFieldName,
                                                                 final WildcardMatcher wildcardMatcher,
                                                                 final SmartFieldDefinition definition) {
        switch (getType(definition)) {
            case TEXT:
                final int maxKeywordLength = getMaxKeywordLength(definition);
                return SmartFieldProvider.fieldSortedDocValuesText(genericFieldName, wildcardMatcher, maxKeywordLength);
            case LONG:
                return SmartFieldProvider.fieldSortedDocValuesLong(genericFieldName, wildcardMatcher);
            case DOUBLE:
                return SmartFieldProvider.fieldSortedDocValuesDouble(genericFieldName, wildcardMatcher);
            case INTEGER:
                return SmartFieldProvider.fieldSortedDocValuesInteger(genericFieldName, wildcardMatcher);
            case FLOAT:
                return SmartFieldProvider.fieldSortedDocValuesFloat(genericFieldName, wildcardMatcher);
            default:
                return null;
        }
    }

    private static FieldTypeInterface.FieldSupplier buildFieldSupplier(final String genericFieldName,
                                                                       final WildcardMatcher wildcardMatcher,
                                                                       final SmartFieldDefinition definition) {
        final List<FieldSupplier> fieldSupplierList = new ArrayList<>();
        if (isStored(definition))
            addIfNotNull(getStoredFieldSupplier(genericFieldName, wildcardMatcher, definition), fieldSupplierList);
        if (isIndex(definition)) {
            addIfNotNull(getIndexFieldSupplier(genericFieldName, wildcardMatcher, definition), fieldSupplierList);
            if (isFullTextIndexAnalyzer(definition))
                fieldSupplierList.add(SmartFieldProvider.fullTextField(genericFieldName, wildcardMatcher));
        }
        if (isFacet(definition))
            addIfNotNull(SmartFieldProvider.facetField(genericFieldName, wildcardMatcher), fieldSupplierList);
        if (isSort(definition))
            addIfNotNull(getSortedDocValuesFieldSupplier(genericFieldName, wildcardMatcher, definition), fieldSupplierList);
        return reduceFieldSuppliers(fieldSupplierList);
    }

    private static SortFieldSupplier buildSortFieldSupplier(final String genericFieldName,
                                                            final WildcardMatcher wildcardMatcher,
                                                            final SmartFieldDefinition definition) {
        if (definition.sort == null || !definition.sort)
            return null;
        switch (getType(definition)) {
            case TEXT:
                return SmartFieldProvider.fieldSortedFieldText(genericFieldName, wildcardMatcher);
            case LONG:
                return SmartFieldProvider.fieldSortedFieldLong(genericFieldName, wildcardMatcher);
            case INTEGER:
                return SmartFieldProvider.fieldSortedFieldInteger(genericFieldName, wildcardMatcher);
            case DOUBLE:
                return SmartFieldProvider.fieldSortedFieldDouble(genericFieldName, wildcardMatcher);
            case FLOAT:
                return SmartFieldProvider.fieldSortedFieldFloat(genericFieldName, wildcardMatcher);
            default:
                return null;
        }
    }

    private static FieldTypeInterface.FieldNameResolver buildStoreFieldResolver(final String genericFieldName,
                                                                                final WildcardMatcher wildcardMatcher,
                                                                                final SmartFieldDefinition definition) {
        switch (getType(definition)) {
            case TEXT:
                return SmartFieldProvider.fieldStoredTextResolver(genericFieldName, wildcardMatcher);
            case LONG:
                return SmartFieldProvider.fieldStoredLongResolver(genericFieldName, wildcardMatcher);
            case INTEGER:
                return SmartFieldProvider.fieldStoredIntegerResolver(genericFieldName, wildcardMatcher);
            case DOUBLE:
                return SmartFieldProvider.fieldStoredDoubleResolver(genericFieldName, wildcardMatcher);
            case FLOAT:
                return SmartFieldProvider.fieldStoredFloatResolver(genericFieldName, wildcardMatcher);
            default:
                return null;
        }
    }

    private static FieldTypeInterface.FieldNameResolver buildIndexFieldResolver(final String genericFieldName,
                                                                                final WildcardMatcher wildcardMatcher,
                                                                                final SmartFieldDefinition definition) {
        switch (getType(definition)) {
            case TEXT:
                return SmartFieldProvider.fieldStringTextResolver(genericFieldName, wildcardMatcher);
            case LONG:
                return SmartFieldProvider.fieldPointLongResolver(genericFieldName, wildcardMatcher);
            case INTEGER:
                return SmartFieldProvider.fieldPointIntegerResolver(genericFieldName, wildcardMatcher);
            case DOUBLE:
                return SmartFieldProvider.fieldPointDoubleResolver(genericFieldName, wildcardMatcher);
            case FLOAT:
                return SmartFieldProvider.fieldPointFloatResolver(genericFieldName, wildcardMatcher);
            default:
                return null;
        }
    }
}
