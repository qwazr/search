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
import org.apache.lucene.document.*;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetField;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;
import static com.qwazr.search.field.FieldTypeInterface.FieldType;
import static com.qwazr.search.field.FieldTypeInterface.ValueType;

interface SmartFieldProvider {

    @FunctionalInterface
    interface SmartFieldNameResolver {
        String resolve(String fieldName);
    }

    static String getLuceneFieldName(final String fieldName,
                                     final FieldTypeInterface.FieldType fieldType,
                                     final FieldTypeInterface.ValueType valueType) {
        return String.valueOf(new char[]{fieldType.prefix, valueType.prefix, '€'}).concat(fieldName);
    }

    private static SmartFieldNameResolver buildNameProvider(final String fieldNamePattern,
                                                            final WildcardMatcher wildcardMatcher,
                                                            final FieldType fieldType,
                                                            final ValueType valueType) {
        if (wildcardMatcher == null) {
            final String resolveFieldName = getLuceneFieldName(fieldNamePattern, fieldType, valueType);
            return fieldName -> resolveFieldName;
        } else {
            return fieldName -> getLuceneFieldName(fieldName, fieldType, valueType);
        }
    }

    static SmartFieldNameResolver fieldStoredTextResolver(final String fieldNamePattern,
                                                          final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            fieldNamePattern, wildcardMatcher, FieldType.storedField, ValueType.textType);
    }

    static FieldTypeInterface.FieldSupplier fieldStoredText(final String fieldNamePattern,
                                                            final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier =
            fieldStoredTextResolver(fieldNamePattern, wildcardMatcher);
        return (fieldName, value, builder) -> builder.acceptField(
            new StoredField(fieldNameSupplier.resolve(fieldName), value.toString()));
    }

    static SmartFieldNameResolver fieldStoredLongResolver(final String fieldNamePattern,
                                                          final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            fieldNamePattern, wildcardMatcher, FieldType.storedField, ValueType.longType);
    }

    static FieldTypeInterface.FieldSupplier fieldStoredLong(final String fieldNamePattern,
                                                            final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier =
            fieldStoredLongResolver(fieldNamePattern, wildcardMatcher);
        return (fieldName, value, builder) -> builder.acceptField(
            new StoredField(fieldNameSupplier.resolve(fieldName), FieldUtils.getLongValue(value)));
    }

    static SmartFieldNameResolver fieldStoredDoubleResolver(final String fieldNamePattern,
                                                            final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            fieldNamePattern, wildcardMatcher, FieldType.storedField, ValueType.doubleType);
    }

    static FieldTypeInterface.FieldSupplier fieldStoredDouble(final String fieldNamePattern,
                                                              final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier =
            fieldStoredDoubleResolver(fieldNamePattern, wildcardMatcher);
        return (fieldName, value, builder) -> builder.acceptField(
            new StoredField(fieldNameSupplier.resolve(fieldName), FieldUtils.getDoubleValue(value)));
    }

    static SmartFieldNameResolver fieldStoredIntegerResolver(final String fieldNamePattern,
                                                             final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            fieldNamePattern, wildcardMatcher, FieldType.storedField, ValueType.integerType);
    }

    static FieldTypeInterface.FieldSupplier fieldStoredInteger(final String fieldNamePattern,
                                                               final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier =
            fieldStoredIntegerResolver(fieldNamePattern, wildcardMatcher);
        return (fieldName, value, builder) -> builder.acceptField(
            new StoredField(fieldNameSupplier.resolve(fieldName), FieldUtils.getIntValue(value)));
    }

    static SmartFieldNameResolver fieldStoredFloatResolver(final String fieldNamePattern,
                                                           final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            fieldNamePattern, wildcardMatcher, FieldType.storedField, ValueType.floatType);
    }

    static FieldTypeInterface.FieldSupplier fieldStoredFloat(final String fieldNamePattern,
                                                             final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier =
            fieldStoredFloatResolver(fieldNamePattern, wildcardMatcher);
        return (fieldName, value, builder) -> builder.acceptField(
            new StoredField(fieldNameSupplier.resolve(fieldName), FieldUtils.getFloatValue(value)));
    }


    static SmartFieldNameResolver fieldStringTextResolver(final String fieldNamePattern,
                                                          final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            fieldNamePattern, wildcardMatcher, FieldType.stringField, ValueType.textType);
    }

    static FieldTypeInterface.FieldSupplier fieldStringText(final String fieldNamePattern,
                                                            final WildcardMatcher wildcardMatcher,
                                                            final int maxStringLength) {
        final SmartFieldNameResolver fieldResolver = fieldStringTextResolver(fieldNamePattern, wildcardMatcher);
        return (fieldName, value, builder) -> {
            final String stringValue = value.toString();
            if (stringValue.length() <= maxStringLength)
                builder.acceptField(
                    new StringField(fieldResolver.resolve(fieldName), stringValue, Field.Store.NO));
        };
    }

    static FieldTypeInterface.TermSupplier stringTermText(final String fieldNamePattern,
                                                          final WildcardMatcher wildcardMatcher,
                                                          final int maxStringLength) {
        final SmartFieldNameResolver fieldResolver = fieldStringTextResolver(fieldNamePattern, wildcardMatcher);
        return (fieldName, value) -> {
            final String stringValue = value.toString();
            if (stringValue.length() > maxStringLength)
                return null;
            return new Term(fieldResolver.resolve(fieldName), stringValue);
        };
    }

    private static BytesRef getLongValue(Object value) {
        return BytesRefUtils.fromLong(FieldUtils.getLongValue(value));
    }

    static SmartFieldNameResolver fieldStringLongResolver(final String fieldNamePattern,
                                                          final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            fieldNamePattern, wildcardMatcher, FieldType.stringField, ValueType.longType);
    }

    static FieldTypeInterface.FieldSupplier fieldStringLong(final String fieldNamePattern,
                                                            final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameResolver = fieldStringLongResolver(fieldNamePattern, wildcardMatcher);
        return (fieldName, value, builder) -> builder.acceptField(
            new StringField(fieldNameResolver.resolve(fieldName), getLongValue(value), Field.Store.NO));
    }

    static FieldTypeInterface.TermSupplier stringTermLong(final String fieldNamePattern,
                                                          final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameResolver = fieldStringLongResolver(fieldNamePattern, wildcardMatcher);
        return (fieldName, value) -> new Term(fieldNameResolver.resolve(fieldName), getLongValue(value));
    }

    private static BytesRef getDoubleValue(Object value) {
        return BytesRefUtils.fromDouble(FieldUtils.getDoubleValue(value));
    }

    static SmartFieldNameResolver fieldStringDoubleResolver(final String fieldNamePattern,
                                                            final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            fieldNamePattern, wildcardMatcher, FieldType.stringField, ValueType.doubleType);
    }

    static FieldTypeInterface.FieldSupplier fieldStringDouble(final String fieldNamePattern,
                                                              final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameResolver = fieldStringDoubleResolver(fieldNamePattern, wildcardMatcher);
        return (fieldName, value, builder) -> builder.acceptField(
            new StringField(fieldNameResolver.resolve(fieldName), getDoubleValue(value), Field.Store.NO));
    }

    static FieldTypeInterface.TermSupplier stringTermDouble(final String fieldNamePattern,
                                                            final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameResolver = fieldStringDoubleResolver(fieldNamePattern, wildcardMatcher);
        return (fieldName, value) -> new Term(fieldNameResolver.resolve(fieldName), getDoubleValue(value));
    }

    private static BytesRef getIntegerValue(Object value) {
        return BytesRefUtils.fromInteger(FieldUtils.getIntValue(value));
    }

    static SmartFieldNameResolver fieldStringIntegerResolver(final String fieldNamePattern,
                                                             final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            fieldNamePattern, wildcardMatcher, FieldType.stringField, ValueType.integerType);
    }

    static FieldTypeInterface.FieldSupplier fieldStringInteger(final String fieldNamePattern,
                                                               final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameResolver = fieldStringIntegerResolver(fieldNamePattern, wildcardMatcher);
        return (fieldName, value, builder) -> builder.acceptField(
            new StringField(fieldNameResolver.resolve(fieldName), getIntegerValue(value), Field.Store.NO));
    }

    static FieldTypeInterface.TermSupplier stringTermInteger(final String fieldNamePattern,
                                                             final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameResolver = fieldStringIntegerResolver(fieldNamePattern, wildcardMatcher);
        return (fieldName, value) -> new Term(fieldNameResolver.resolve(fieldName), getIntegerValue(value));
    }

    private static BytesRef getFloatValue(Object value) {
        return BytesRefUtils.fromFloat(FieldUtils.getFloatValue(value));
    }

    static SmartFieldNameResolver fieldStringFloatResolver(final String fieldNamePattern,
                                                           final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            fieldNamePattern, wildcardMatcher, FieldType.stringField, ValueType.floatType);
    }

    static FieldTypeInterface.FieldSupplier fieldStringFloat(final String fieldNamePattern,
                                                             final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameResolver = fieldStringFloatResolver(fieldNamePattern, wildcardMatcher);
        return (fieldName, value, builder) -> builder.acceptField(
            new StringField(fieldNameResolver.resolve(fieldName), getFloatValue(value), Field.Store.NO));
    }

    static FieldTypeInterface.TermSupplier stringTermFloat(final String fieldNamePattern,
                                                           final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameResolver = fieldStringFloatResolver(fieldNamePattern, wildcardMatcher);
        return (fieldName, value) -> new Term(fieldNameResolver.resolve(fieldName), getFloatValue(value));
    }

    static FieldTypeInterface.FieldSupplier fieldSortedDocValuesText(final String fieldNamePattern,
                                                                     final WildcardMatcher wildcardMatcher,
                                                                     final int maxStringLength) {
        final SmartFieldNameResolver fieldNameSupplier = buildNameProvider(
            fieldNamePattern, wildcardMatcher, FieldType.docValues, ValueType.textType);
        return (fieldName, value, builder) -> {
            final String stringValue = value.toString();
            if (stringValue.length() <= maxStringLength)
                builder.acceptField(
                    new SortedDocValuesField(fieldNameSupplier.resolve(fieldName), new BytesRef(value.toString())));
        };
    }

    static FieldTypeInterface.SortFieldSupplier fieldSortedFieldText(final String fieldNamePattern,
                                                                     final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier = buildNameProvider(
            fieldNamePattern, wildcardMatcher, FieldType.docValues, ValueType.textType);
        return (fieldName, sortEnum) -> SortUtils.stringSortField(fieldNameSupplier.resolve(fieldName), sortEnum);
    }

    static FieldTypeInterface.FieldSupplier fieldSortedDocValuesLong(final String fieldNamePattern,
                                                                     final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier = buildNameProvider(
            fieldNamePattern, wildcardMatcher, FieldType.docValues, ValueType.longType);
        return (fieldName, value, builder) -> builder.acceptField(
            new SortedNumericDocValuesField(fieldNameSupplier.resolve(fieldName), FieldUtils.getLongValue(value)));
    }

    static FieldTypeInterface.SortFieldSupplier fieldSortedFieldLong(final String fieldNamePattern,
                                                                     final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier = buildNameProvider(
            fieldNamePattern, wildcardMatcher, FieldType.docValues, ValueType.longType);
        return (fieldName, sortEnum) -> SortUtils.longSortField(fieldNameSupplier.resolve(fieldName), sortEnum);
    }

    static FieldTypeInterface.FieldSupplier fieldSortedDocValuesInteger(final String fieldNamePattern,
                                                                        final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier = buildNameProvider(
            fieldNamePattern, wildcardMatcher, FieldType.docValues, ValueType.integerType);
        return (fieldName, value, builder) -> builder.acceptField(
            new SortedNumericDocValuesField(fieldNameSupplier.resolve(fieldName), FieldUtils.getIntValue(value)));
    }

    static FieldTypeInterface.SortFieldSupplier fieldSortedFieldInteger(final String fieldNamePattern,
                                                                        final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier = buildNameProvider(
            fieldNamePattern, wildcardMatcher, FieldType.docValues, ValueType.integerType);
        return (fieldName, sortEnum) -> SortUtils.integerSortField(fieldNameSupplier.resolve(fieldName), sortEnum);
    }

    static FieldTypeInterface.FieldSupplier fieldSortedDocValuesFloat(final String fieldNamePattern,
                                                                      final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier = buildNameProvider(
            fieldNamePattern, wildcardMatcher, FieldType.docValues, ValueType.floatType);
        return (fieldName, value, builder) -> builder.acceptField(
            new SortedNumericDocValuesField(fieldNameSupplier.resolve(fieldName),
                NumericUtils.floatToSortableInt(FieldUtils.getFloatValue(value))));
    }

    static FieldTypeInterface.SortFieldSupplier fieldSortedFieldFloat(final String fieldNamePattern,
                                                                      final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier = buildNameProvider(
            fieldNamePattern, wildcardMatcher, FieldType.docValues, ValueType.floatType);
        return (fieldName, sortEnum) -> SortUtils.floatSortField(fieldNameSupplier.resolve(fieldName), sortEnum);
    }

    static FieldTypeInterface.FieldSupplier fieldSortedDocValuesDouble(final String fieldNamePattern,
                                                                       final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier = buildNameProvider(
            fieldNamePattern, wildcardMatcher, FieldType.docValues, ValueType.doubleType);
        return (fieldName, value, builder) -> builder.acceptField(
            new SortedNumericDocValuesField(fieldNameSupplier.resolve(fieldName),
                NumericUtils.doubleToSortableLong(FieldUtils.getDoubleValue(value))));
    }

    static FieldTypeInterface.SortFieldSupplier fieldSortedFieldDouble(final String fieldNamePattern,
                                                                       final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier = buildNameProvider(
            fieldNamePattern, wildcardMatcher, FieldType.docValues, ValueType.doubleType);
        return (fieldName, sortEnum) -> SortUtils.doubleSortField(fieldNameSupplier.resolve(fieldName), sortEnum);
    }

    static SmartFieldNameResolver fieldPointLongResolver(final String fieldNamePattern,
                                                         final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            fieldNamePattern, wildcardMatcher, FieldType.pointField, ValueType.longType);
    }

    static FieldTypeInterface.FieldSupplier fieldPointLong(final String fieldNamePattern,
                                                           final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier =
            fieldPointLongResolver(fieldNamePattern, wildcardMatcher);
        return (fieldName, value, builder) -> builder.acceptField(
            new LongPoint(fieldNameSupplier.resolve(fieldName), FieldUtils.getLongValue(value)));
    }

    static SmartFieldNameResolver fieldPointDoubleResolver(final String fieldNamePattern,
                                                           final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            fieldNamePattern, wildcardMatcher, FieldType.pointField, ValueType.doubleType);
    }

    static FieldTypeInterface.FieldSupplier fieldPointDouble(final String fieldNamePattern,
                                                             final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier =
            fieldPointDoubleResolver(fieldNamePattern, wildcardMatcher);
        return (fieldName, value, builder) -> builder.acceptField(
            new DoublePoint(fieldNameSupplier.resolve(fieldName), FieldUtils.getDoubleValue(value)));
    }

    static SmartFieldNameResolver fieldPointIntegerResolver(final String fieldNamePattern,
                                                            final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            fieldNamePattern, wildcardMatcher, FieldType.pointField, ValueType.integerType);
    }

    static FieldTypeInterface.FieldSupplier fieldPointInteger(final String fieldNamePattern,
                                                              final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier =
            fieldPointIntegerResolver(fieldNamePattern, wildcardMatcher);
        return (fieldName, value, builder) -> builder.acceptField(
            new IntPoint(fieldNameSupplier.resolve(fieldName), FieldUtils.getIntValue(value)));
    }

    static SmartFieldNameResolver fieldPointFloatResolver(final String fieldNamePattern,
                                                          final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            fieldNamePattern, wildcardMatcher, FieldType.pointField, ValueType.floatType);
    }


    static FieldTypeInterface.FieldSupplier fieldPointFloat(final String fieldNamePattern,
                                                            final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier =
            fieldPointFloatResolver(fieldNamePattern, wildcardMatcher);
        return (fieldName, value, builder) -> builder.acceptField(
            new FloatPoint(fieldNameSupplier.resolve(fieldName), FieldUtils.getFloatValue(value)));
    }

    static SmartFieldNameResolver facetFieldNameSupplier(final String fieldNamePattern,
                                                         final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(fieldNamePattern, wildcardMatcher, FieldType.facetField, ValueType.textType);
    }

    static FieldTypeInterface.FieldSupplier facetField(final String fieldNamePattern,
                                                       final WildcardMatcher wildcardMatcher,
                                                       final boolean multivalued) {
        final SmartFieldNameResolver fieldNameSupplier = facetFieldNameSupplier(fieldNamePattern, wildcardMatcher);
        return (fieldName, value, builder) -> {
            final String dimensionName = fieldNameSupplier.resolve(fieldName);
            builder.acceptFacetField(
                new SortedSetDocValuesFacetField(dimensionName, value.toString()),
                dimensionName,
                (dim, context, config) -> {
                    config.setIndexFieldName(dim, context.sortedSetFacetField);
                    config.setMultiValued(dim, multivalued);
                });
        };
    }

    static FieldTypeInterface.FieldSupplier fullTextField(final String fieldNamePattern,
                                                          final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier = buildNameProvider(
            fieldNamePattern, wildcardMatcher, FieldType.textField, ValueType.textType);
        return (fieldName, value, builder) -> builder.acceptField(
            new TextField(fieldNameSupplier.resolve(fieldName), value.toString(), Field.Store.NO));
    }

}
