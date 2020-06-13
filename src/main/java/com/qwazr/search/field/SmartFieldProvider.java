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

    static String getLuceneFieldName(final String genericFieldName,
                                     final FieldTypeInterface.FieldType fieldType,
                                     final FieldTypeInterface.ValueType valueType) {
        return String.valueOf(new char[]{fieldType.prefix, valueType.prefix, '€'}).concat(genericFieldName);
    }

    private static SmartFieldNameResolver buildNameProvider(final String genericFieldName,
                                                            final WildcardMatcher wildcardMatcher,
                                                            final FieldType fieldType,
                                                            final ValueType valueType) {
        if (wildcardMatcher == null) {
            final String resolveFieldName = getLuceneFieldName(genericFieldName, fieldType, valueType);
            return fieldName -> resolveFieldName;
        } else {
            return fieldName -> getLuceneFieldName(genericFieldName, fieldType, valueType);
        }
    }

    static SmartFieldNameResolver fieldStoredTextResolver(final String genericFieldName,
                                                          final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            genericFieldName, wildcardMatcher, FieldType.storedField, ValueType.textType);
    }

    static FieldTypeInterface.FieldSupplier fieldStoredText(final String genericFieldName,
                                                            final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier =
            fieldStoredTextResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new StoredField(fieldNameSupplier.resolve(fieldName), value.toString()));
    }

    static SmartFieldNameResolver fieldStoredLongResolver(final String genericFieldName,
                                                          final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            genericFieldName, wildcardMatcher, FieldType.storedField, ValueType.longType);
    }

    static FieldTypeInterface.FieldSupplier fieldStoredLong(final String genericFieldName,
                                                            final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier =
            fieldStoredLongResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new StoredField(fieldNameSupplier.resolve(fieldName), FieldUtils.getLongValue(value)));
    }

    static SmartFieldNameResolver fieldStoredDoubleResolver(final String genericFieldName,
                                                            final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            genericFieldName, wildcardMatcher, FieldType.storedField, ValueType.doubleType);
    }

    static FieldTypeInterface.FieldSupplier fieldStoredDouble(final String genericFieldName,
                                                              final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier =
            fieldStoredDoubleResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new StoredField(fieldNameSupplier.resolve(fieldName), FieldUtils.getDoubleValue(value)));
    }

    static SmartFieldNameResolver fieldStoredIntegerResolver(final String genericFieldName,
                                                             final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            genericFieldName, wildcardMatcher, FieldType.storedField, ValueType.integerType);
    }

    static FieldTypeInterface.FieldSupplier fieldStoredInteger(final String genericFieldName,
                                                               final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier =
            fieldStoredIntegerResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new StoredField(fieldNameSupplier.resolve(fieldName), FieldUtils.getIntValue(value)));
    }

    static SmartFieldNameResolver fieldStoredFloatResolver(final String genericFieldName,
                                                           final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            genericFieldName, wildcardMatcher, FieldType.storedField, ValueType.floatType);
    }

    static FieldTypeInterface.FieldSupplier fieldStoredFloat(final String genericFieldName,
                                                             final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier =
            fieldStoredFloatResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new StoredField(fieldNameSupplier.resolve(fieldName), FieldUtils.getFloatValue(value)));
    }


    static SmartFieldNameResolver fieldStringTextResolver(final String genericFieldName,
                                                          final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            genericFieldName, wildcardMatcher, FieldType.stringField, ValueType.textType);
    }

    static FieldTypeInterface.FieldSupplier fieldStringText(final String genericFieldName,
                                                            final WildcardMatcher wildcardMatcher,
                                                            final int maxStringLength) {
        final SmartFieldNameResolver fieldResolver = fieldStringTextResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value, builder) -> {
            final String stringValue = value.toString();
            if (stringValue.length() <= maxStringLength)
                builder.accept(genericFieldName, fieldName,
                    new StringField(fieldResolver.resolve(fieldName), stringValue, Field.Store.NO));
        };
    }

    static FieldTypeInterface.TermSupplier stringTermText(final String genericFieldName,
                                                          final WildcardMatcher wildcardMatcher,
                                                          final int maxStringLength) {
        final SmartFieldNameResolver fieldResolver = fieldStringTextResolver(genericFieldName, wildcardMatcher);
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

    static SmartFieldNameResolver fieldStringLongResolver(final String genericFieldName,
                                                          final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            genericFieldName, wildcardMatcher, FieldType.stringField, ValueType.longType);
    }

    static FieldTypeInterface.FieldSupplier fieldStringLong(final String genericFieldName,
                                                            final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameResolver = fieldStringLongResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new StringField(fieldNameResolver.resolve(fieldName), getLongValue(value), Field.Store.NO));
    }

    static FieldTypeInterface.TermSupplier stringTermLong(final String genericFieldName,
                                                          final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameResolver = fieldStringLongResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value) -> new Term(fieldNameResolver.resolve(fieldName), getLongValue(value));
    }

    private static BytesRef getDoubleValue(Object value) {
        return BytesRefUtils.fromDouble(FieldUtils.getDoubleValue(value));
    }

    static SmartFieldNameResolver fieldStringDoubleResolver(final String genericFieldName,
                                                            final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            genericFieldName, wildcardMatcher, FieldType.stringField, ValueType.doubleType);
    }

    static FieldTypeInterface.FieldSupplier fieldStringDouble(final String genericFieldName,
                                                              final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameResolver = fieldStringDoubleResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new StringField(fieldNameResolver.resolve(fieldName), getDoubleValue(value), Field.Store.NO));
    }

    static FieldTypeInterface.TermSupplier stringTermDouble(final String genericFieldName,
                                                            final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameResolver = fieldStringDoubleResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value) -> new Term(fieldNameResolver.resolve(fieldName), getDoubleValue(value));
    }

    private static BytesRef getIntegerValue(Object value) {
        return BytesRefUtils.fromInteger(FieldUtils.getIntValue(value));
    }

    static SmartFieldNameResolver fieldStringIntegerResolver(final String genericFieldName,
                                                             final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            genericFieldName, wildcardMatcher, FieldType.stringField, ValueType.integerType);
    }

    static FieldTypeInterface.FieldSupplier fieldStringInteger(final String genericFieldName,
                                                               final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameResolver = fieldStringIntegerResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new StringField(fieldNameResolver.resolve(fieldName), getIntegerValue(value), Field.Store.NO));
    }

    static FieldTypeInterface.TermSupplier stringTermInteger(final String genericFieldName,
                                                             final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameResolver = fieldStringIntegerResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value) -> new Term(fieldNameResolver.resolve(fieldName), getIntegerValue(value));
    }

    private static BytesRef getFloatValue(Object value) {
        return BytesRefUtils.fromFloat(FieldUtils.getFloatValue(value));
    }

    static SmartFieldNameResolver fieldStringFloatResolver(final String genericFieldName,
                                                           final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            genericFieldName, wildcardMatcher, FieldType.stringField, ValueType.floatType);
    }

    static FieldTypeInterface.FieldSupplier fieldStringFloat(final String genericFieldName,
                                                             final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameResolver = fieldStringFloatResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new StringField(fieldNameResolver.resolve(fieldName), getFloatValue(value), Field.Store.NO));
    }

    static FieldTypeInterface.TermSupplier stringTermFloat(final String genericFieldName,
                                                           final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameResolver = fieldStringFloatResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value) -> new Term(fieldNameResolver.resolve(fieldName), getFloatValue(value));
    }

    static FieldTypeInterface.FieldSupplier fieldSortedDocValuesText(final String genericFieldName,
                                                                     final WildcardMatcher wildcardMatcher,
                                                                     final int maxStringLength) {
        final SmartFieldNameResolver fieldNameSupplier = buildNameProvider(
            genericFieldName, wildcardMatcher, FieldType.docValues, ValueType.textType);
        return (fieldName, value, builder) -> {
            final String stringValue = value.toString();
            if (stringValue.length() <= maxStringLength)
                builder.accept(genericFieldName, fieldName,
                    new SortedDocValuesField(fieldNameSupplier.resolve(fieldName), new BytesRef(value.toString())));
        };
    }

    static FieldTypeInterface.SortFieldSupplier fieldSortedFieldText(final String genericFieldName,
                                                                     final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier = buildNameProvider(
            genericFieldName, wildcardMatcher, FieldType.docValues, ValueType.textType);
        return (fieldName, sortEnum) -> SortUtils.stringSortField(fieldNameSupplier.resolve(fieldName), sortEnum);
    }

    static FieldTypeInterface.FieldSupplier fieldSortedDocValuesLong(final String genericFieldName,
                                                                     final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier = buildNameProvider(
            genericFieldName, wildcardMatcher, FieldType.docValues, ValueType.longType);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new SortedNumericDocValuesField(fieldNameSupplier.resolve(fieldName), FieldUtils.getLongValue(value)));
    }

    static FieldTypeInterface.SortFieldSupplier fieldSortedFieldLong(final String genericFieldName,
                                                                     final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier = buildNameProvider(
            genericFieldName, wildcardMatcher, FieldType.docValues, ValueType.longType);
        return (fieldName, sortEnum) -> SortUtils.longSortField(fieldNameSupplier.resolve(fieldName), sortEnum);
    }

    static FieldTypeInterface.FieldSupplier fieldSortedDocValuesInteger(final String genericFieldName,
                                                                        final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier = buildNameProvider(
            genericFieldName, wildcardMatcher, FieldType.docValues, ValueType.integerType);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new SortedNumericDocValuesField(fieldNameSupplier.resolve(fieldName), FieldUtils.getIntValue(value)));
    }

    static FieldTypeInterface.SortFieldSupplier fieldSortedFieldInteger(final String genericFieldName,
                                                                        final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier = buildNameProvider(
            genericFieldName, wildcardMatcher, FieldType.docValues, ValueType.integerType);
        return (fieldName, sortEnum) -> SortUtils.integerSortField(fieldNameSupplier.resolve(fieldName), sortEnum);
    }

    static FieldTypeInterface.FieldSupplier fieldSortedDocValuesFloat(final String genericFieldName,
                                                                      final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier = buildNameProvider(
            genericFieldName, wildcardMatcher, FieldType.docValues, ValueType.floatType);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new SortedNumericDocValuesField(fieldNameSupplier.resolve(fieldName),
                NumericUtils.floatToSortableInt(FieldUtils.getFloatValue(value))));
    }

    static FieldTypeInterface.SortFieldSupplier fieldSortedFieldFloat(final String genericFieldName,
                                                                      final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier = buildNameProvider(
            genericFieldName, wildcardMatcher, FieldType.docValues, ValueType.floatType);
        return (fieldName, sortEnum) -> SortUtils.floatSortField(fieldNameSupplier.resolve(fieldName), sortEnum);
    }

    static FieldTypeInterface.FieldSupplier fieldSortedDocValuesDouble(final String genericFieldName,
                                                                       final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier = buildNameProvider(
            genericFieldName, wildcardMatcher, FieldType.docValues, ValueType.doubleType);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new SortedNumericDocValuesField(fieldNameSupplier.resolve(fieldName),
                NumericUtils.doubleToSortableLong(FieldUtils.getDoubleValue(value))));
    }

    static FieldTypeInterface.SortFieldSupplier fieldSortedFieldDouble(final String genericFieldName,
                                                                       final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier = buildNameProvider(
            genericFieldName, wildcardMatcher, FieldType.docValues, ValueType.doubleType);
        return (fieldName, sortEnum) -> SortUtils.doubleSortField(fieldNameSupplier.resolve(fieldName), sortEnum);
    }

    static SmartFieldNameResolver fieldPointLongResolver(final String genericFieldName,
                                                         final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            genericFieldName, wildcardMatcher, FieldType.pointField, ValueType.longType);
    }

    static FieldTypeInterface.FieldSupplier fieldPointLong(final String genericFieldName,
                                                           final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier =
            fieldPointLongResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new LongPoint(fieldNameSupplier.resolve(fieldName), FieldUtils.getLongValue(value)));
    }

    static SmartFieldNameResolver fieldPointDoubleResolver(final String genericFieldName,
                                                           final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            genericFieldName, wildcardMatcher, FieldType.pointField, ValueType.doubleType);
    }

    static FieldTypeInterface.FieldSupplier fieldPointDouble(final String genericFieldName,
                                                             final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier =
            fieldPointDoubleResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new DoublePoint(fieldNameSupplier.resolve(fieldName), FieldUtils.getDoubleValue(value)));
    }

    static SmartFieldNameResolver fieldPointIntegerResolver(final String genericFieldName,
                                                            final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            genericFieldName, wildcardMatcher, FieldType.pointField, ValueType.integerType);
    }

    static FieldTypeInterface.FieldSupplier fieldPointInteger(final String genericFieldName,
                                                              final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier =
            fieldPointIntegerResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new IntPoint(fieldNameSupplier.resolve(fieldName), FieldUtils.getIntValue(value)));
    }

    static SmartFieldNameResolver fieldPointFloatResolver(final String genericFieldName,
                                                          final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            genericFieldName, wildcardMatcher, FieldType.pointField, ValueType.floatType);
    }


    static FieldTypeInterface.FieldSupplier fieldPointFloat(final String genericFieldName,
                                                            final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier =
            fieldPointFloatResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new FloatPoint(fieldNameSupplier.resolve(fieldName), FieldUtils.getFloatValue(value)));
    }

    static SmartFieldNameResolver facetFieldNameSupplier(final String genericFieldName,
                                                         final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(genericFieldName, wildcardMatcher, FieldType.facetField, ValueType.textType);
    }

    static FieldTypeInterface.FieldSupplier facetField(final String genericFieldName,
                                                       final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier = facetFieldNameSupplier(genericFieldName, wildcardMatcher);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new SortedSetDocValuesFacetField(fieldNameSupplier.resolve(fieldName), value.toString()));
    }

    static FieldTypeInterface.FieldSupplier fullTextField(final String genericFieldName,
                                                          final WildcardMatcher wildcardMatcher) {
        final SmartFieldNameResolver fieldNameSupplier = buildNameProvider(
            genericFieldName, wildcardMatcher, FieldType.textField, ValueType.textType);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new TextField(fieldNameSupplier.resolve(fieldName), value.toString(), Field.Store.NO));
    }

}
