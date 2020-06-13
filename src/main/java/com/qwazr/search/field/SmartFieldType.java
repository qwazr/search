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
        super(new SmartBuilder(genericFieldName, wildcardMatcher, definition));
    }

    private static class SmartBuilder extends Builder<SmartFieldDefinition> {

        private final SmartFieldDefinition.Type type;
        private final int maxKeywordLength;
        private final boolean isIndex;
        private final boolean isFacet;
        private final boolean isStored;
        private final boolean isSort;
        private final boolean isFullText;

        private SmartBuilder(final String genericFieldName,
                             final WildcardMatcher wildcardMatcher,
                             final SmartFieldDefinition definition) {
            super(genericFieldName, wildcardMatcher, definition);
            type = definition.type == null ? SmartFieldDefinition.Type.TEXT : definition.type;
            maxKeywordLength = definition.maxKeywordLength == null
                ? SmartFieldDefinition.DEFAULT_MAX_KEYWORD_LENGTH : definition.maxKeywordLength;
            isIndex = definition.index != null && definition.index;
            isFacet = definition.facet != null && definition.facet;
            isStored = definition.stored != null && definition.stored;
            isSort = definition.sort != null && definition.sort;
            isFullText = isFullTextIndexAnalyzer(definition);
            fieldSupplier(buildFieldSupplier(genericFieldName, wildcardMatcher));
            facetSupplier(buildFacetSupplier(genericFieldName, wildcardMatcher));
            sortFieldSupplier(buildSortFieldSupplier(genericFieldName, wildcardMatcher));
            primaryTermSupplier(buildPrimaryTermSupplier(genericFieldName, wildcardMatcher));
            fieldNameResolver(buildFieldNameResolver(genericFieldName));
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

        private FacetSupplier buildFacetSupplier(final String genericFieldName,
                                                 final WildcardMatcher wildcardMatcher) {
            if (!isFacet)
                return null;
            final SmartFieldProvider.SmartFieldNameResolver fieldNameSupplier = SmartFieldProvider
                .facetFieldNameSupplier(genericFieldName, wildcardMatcher);
            return (fieldName, fieldMap, facetsConfig) -> {
                final String resolvedFieldName = fieldNameSupplier.resolve(fieldName);
                facetsConfig.setMultiValued(resolvedFieldName, true);
                facetsConfig.setIndexFieldName(resolvedFieldName, fieldMap.sortedSetFacetField);
            };
        }

        private FieldSupplier getStoredFieldSupplier(final String genericFieldName,
                                                     final WildcardMatcher wildcardMatcher) {
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

        private FieldSupplier getIndexFieldSupplier(final String genericFieldName,
                                                    final WildcardMatcher wildcardMatcher) {
            switch (type) {
                case TEXT:
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

        private TermSupplier buildPrimaryTermSupplier(final String genericFieldName,
                                                      final WildcardMatcher wildcardMatcher) {
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

        private TermSupplier buildIndexTermSupplier(final String genericFieldName,
                                                    final WildcardMatcher wildcardMatcher) {
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

        private FieldSupplier getSortedDocValuesFieldSupplier(final String genericFieldName,
                                                              final WildcardMatcher wildcardMatcher) {
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

        private FieldTypeInterface.FieldSupplier buildFieldSupplier(final String genericFieldName,
                                                                    final WildcardMatcher wildcardMatcher) {
            final List<FieldSupplier> fieldSupplierList = new ArrayList<>();
            if (isStored)
                addIfNotNull(getStoredFieldSupplier(genericFieldName, wildcardMatcher), fieldSupplierList);
            if (isIndex) {
                addIfNotNull(getIndexFieldSupplier(genericFieldName, wildcardMatcher), fieldSupplierList);
                if (isFullText)
                    fieldSupplierList.add(SmartFieldProvider.fullTextField(genericFieldName, wildcardMatcher));
            }
            if (isFacet)
                addIfNotNull(SmartFieldProvider.facetField(genericFieldName, wildcardMatcher), fieldSupplierList);
            if (isSort)
                addIfNotNull(getSortedDocValuesFieldSupplier(genericFieldName, wildcardMatcher), fieldSupplierList);
            return reduceFieldSuppliers(fieldSupplierList);
        }

        private SortFieldSupplier buildSortFieldSupplier(final String genericFieldName,
                                                         final WildcardMatcher wildcardMatcher) {
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

        private FieldTypeInterface.FieldNameResolver buildFieldNameResolver(final String genericFieldName) {
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
