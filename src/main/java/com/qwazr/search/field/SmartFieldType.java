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
                   final String primaryKey,
                   @NotNull final SmartFieldDefinition definition) {
        super(new SmartBuilder(genericFieldName, wildcardMatcher, primaryKey, definition));
    }

    private static class SmartBuilder extends Builder<SmartFieldDefinition> {

        private final SmartFieldDefinition.Type type;
        private final int maxKeywordLength;
        private final boolean isPrimaryKey;
        private final boolean isIndex;
        private final boolean isFacet;
        private final boolean isStored;
        private final boolean isSort;
        private final boolean isMultivalued;
        private final boolean isFullText;

        private SmartBuilder(final String genericFieldName,
                             final WildcardMatcher wildcardMatcher,
                             final String primaryKey,
                             final SmartFieldDefinition definition) {
            super(genericFieldName, wildcardMatcher, definition);
            type = definition.type == null ? SmartFieldDefinition.Type.TEXT : definition.type;
            maxKeywordLength = definition.maxKeywordLength == null
                ? SmartFieldDefinition.DEFAULT_MAX_KEYWORD_LENGTH : definition.maxKeywordLength;
            isPrimaryKey = primaryKey != null && primaryKey.equals(genericFieldName);
            isIndex = (definition.index != null && definition.index);
            isFacet = definition.facet != null && definition.facet;
            isStored = definition.stored != null && definition.stored;
            isSort = definition.sort != null && definition.sort;
            isMultivalued = definition.multivalued != null && definition.multivalued;
            isFullText = isFullTextIndexAnalyzer(definition);
            valueType(getValueType());
            fieldSupplier(buildFieldSupplier());
            facetsConfigSupplier(buildFacetsConfigSupplier());
            sortFieldSupplier(buildSortFieldSupplier());
            primaryTermSupplier(buildPrimaryTermSupplier());
            fieldNameResolver(buildFieldNameResolver());
        }

        private static boolean isFullTextIndexAnalyzer(final SmartFieldDefinition definition) {
            final String keywordName = SmartAnalyzerSet.keyword.name();
            if (StringUtils.isNotEmpty(definition.indexAnalyzer)
                && !Objects.equals(definition.indexAnalyzer, keywordName))
                return true;
            return StringUtils.isNotEmpty(definition.analyzer)
                && !Objects.equals(definition.analyzer, keywordName);
        }

        private ValueType getValueType() {
            switch (type) {
                case TEXT:
                    return ValueType.textType;
                case LONG:
                    return ValueType.longType;
                case DOUBLE:
                    return ValueType.doubleType;
                case INTEGER:
                    return ValueType.integerType;
                case FLOAT:
                    return ValueType.floatType;
                default:
                    return null;
            }
        }

        private FacetsConfigSupplier buildFacetsConfigSupplier() {
            if (!isFacet)
                return null;
            final SmartFieldProvider.SmartFieldNameResolver fieldNameSupplier = SmartFieldProvider
                .facetFieldNameSupplier(genericFieldName, wildcardMatcher);
            return (fieldName, fieldsContext, facetsConfig) -> {
                final String resolvedFieldName = fieldNameSupplier.resolve(fieldName);
                facetsConfig.setMultiValued(resolvedFieldName, isMultivalued);
                facetsConfig.setIndexFieldName(resolvedFieldName, fieldsContext.sortedSetFacetField);
            };
        }

        private FieldSupplier getStoredFieldSupplier() {
            switch (type) {
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

        private FieldSupplier getIndexFieldSupplier() {
            switch (type) {
                case TEXT:
                    fieldType(FieldType.stringField);
                    return SmartFieldProvider.fieldStringText(genericFieldName, wildcardMatcher, maxKeywordLength);
                case LONG:
                    fieldType(FieldType.pointField);
                    return SmartFieldProvider.fieldPointLong(genericFieldName, wildcardMatcher);
                case DOUBLE:
                    fieldType(FieldType.pointField);
                    return SmartFieldProvider.fieldPointDouble(genericFieldName, wildcardMatcher);
                case INTEGER:
                    fieldType(FieldType.pointField);
                    return SmartFieldProvider.fieldPointInteger(genericFieldName, wildcardMatcher);
                case FLOAT:
                    fieldType(FieldType.pointField);
                    return SmartFieldProvider.fieldPointFloat(genericFieldName, wildcardMatcher);
                default:
                    return null;
            }
        }

        private FieldSupplier getPrimaryFieldBuilder() {
            if (!isPrimaryKey)
                return null;
            switch (type) {
                case TEXT:
                    return SmartFieldProvider.fieldStringText(genericFieldName, wildcardMatcher, maxKeywordLength);
                case LONG:
                    return SmartFieldProvider.fieldStringLong(genericFieldName, wildcardMatcher);
                case DOUBLE:
                    return SmartFieldProvider.fieldStringDouble(genericFieldName, wildcardMatcher);
                case INTEGER:
                    return SmartFieldProvider.fieldStringInteger(genericFieldName, wildcardMatcher);
                case FLOAT:
                    return SmartFieldProvider.fieldStringFloat(genericFieldName, wildcardMatcher);
                default:
                    return null;
            }
        }

        private TermSupplier buildPrimaryTermSupplier() {
            if (!isPrimaryKey)
                return null;
            switch (type) {
                case TEXT:
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

        private FieldSupplier getSortedDocValuesFieldSupplier() {
            switch (type) {
                case TEXT:
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

        private FieldTypeInterface.FieldSupplier buildFieldSupplier() {
            final List<FieldSupplier> fieldSupplierList = new ArrayList<>();
            if (isPrimaryKey)
                if (addIfNotNull(getPrimaryFieldBuilder(), fieldSupplierList))
                    fieldType(FieldType.stringField);
            if (isStored)
                if (addIfNotNull(getStoredFieldSupplier(), fieldSupplierList))
                    fieldType(FieldType.storedField);
            if (isIndex) {
                addIfNotNull(getIndexFieldSupplier(), fieldSupplierList);
                if (isFullText) {
                    fieldSupplierList.add(SmartFieldProvider.fullTextField(genericFieldName, wildcardMatcher));
                    fieldType(FieldType.textField);
                }
            }
            if (isFacet)
                if (addIfNotNull(SmartFieldProvider.facetField(genericFieldName, wildcardMatcher, isMultivalued), fieldSupplierList))
                    fieldType(FieldType.facetField);
            if (isSort)
                if (addIfNotNull(getSortedDocValuesFieldSupplier(), fieldSupplierList))
                    fieldType(FieldType.docValues);
            return reduceFieldSuppliers(fieldSupplierList);
        }

        private SortFieldSupplier buildSortFieldSupplier() {
            if (!isSort)
                return null;
            switch (type) {
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

        private FieldTypeInterface.FieldNameResolver buildFieldNameResolver() {
            final FieldType defaultFieldType;
            if (isIndex) {
                if (isFullText) {
                    defaultFieldType = FieldType.textField;
                } else {
                    switch (type) {
                        case TEXT:
                            defaultFieldType = FieldType.stringField;
                            break;
                        case LONG:
                        case INTEGER:
                        case FLOAT:
                        case DOUBLE:
                            defaultFieldType = FieldType.pointField;
                            break;
                        default:
                            defaultFieldType = null;
                            break;
                    }
                }
            } else if (isFacet) {
                defaultFieldType = FieldType.facetField;
            } else if (isStored) {
                defaultFieldType = FieldType.storedField;
            } else if (isSort) {
                defaultFieldType = FieldType.docValues;
            } else
                defaultFieldType = null;
            final ValueType defaultValueType;
            switch (type) {
                case TEXT:
                    defaultValueType = ValueType.textType;
                    break;
                case LONG:
                    defaultValueType = ValueType.longType;
                    break;
                case INTEGER:
                    defaultValueType = ValueType.integerType;
                    break;
                case DOUBLE:
                    defaultValueType = ValueType.doubleType;
                    break;
                case FLOAT:
                    defaultValueType = ValueType.floatType;
                    break;
                default:
                    defaultValueType = null;
                    break;
            }
            return (fieldName, fieldType, valueType) -> SmartFieldProvider.getLuceneFieldName(
                fieldName == null ? genericFieldName : fieldName,
                fieldType == null ? defaultFieldType : fieldType,
                valueType == null ? defaultValueType : valueType);
        }

    }
}
