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

interface SmartFieldProvider {

    enum FieldPrefix {

        storedField('r'), stringField('s'), facetField('f'), docValues('d'), textField('t'), pointField('p');

        final char prefix;

        FieldPrefix(char prefix) {
            this.prefix = prefix;
        }

        private String getLuceneFieldName(final String concreteFieldName, final TypePrefix typePrefix) {
            return String.valueOf(new char[]{prefix, typePrefix.prefix, '€'}).concat(concreteFieldName);
        }

    }

    enum TypePrefix {

        textType('t'), longType('l'), integerType('i'), doubleType('d'), floatType('f');

        final char prefix;

        TypePrefix(char prefix) {
            this.prefix = prefix;
        }

        private String getLuceneFieldName(final String genericFieldName, final FieldPrefix fieldPrefix) {
            return fieldPrefix.getLuceneFieldName(genericFieldName, this);
        }

    }

    private static FieldTypeInterface.FieldNameResolver buildNameProvider(final String genericFieldName,
                                                                          final WildcardMatcher wildcardMatcher,
                                                                          final FieldPrefix fieldPrefix,
                                                                          final TypePrefix typePrefix) {
        if (wildcardMatcher == null) {
            final String fieldName = typePrefix.getLuceneFieldName(genericFieldName, fieldPrefix);
            return concreteFieldName -> fieldName;
        } else {
            return concreteFieldName -> typePrefix.getLuceneFieldName(genericFieldName, fieldPrefix);
        }
    }

    static FieldTypeInterface.FieldNameResolver fieldStoredTextResolver(final String genericFieldName,
                                                                        final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            genericFieldName, wildcardMatcher, FieldPrefix.storedField, TypePrefix.textType);
    }

    static FieldTypeInterface.FieldSupplier fieldStoredText(final String genericFieldName,
                                                            final WildcardMatcher wildcardMatcher) {
        final FieldTypeInterface.FieldNameResolver fieldNameSupplier =
            fieldStoredTextResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new StoredField(fieldNameSupplier.resolve(fieldName), value.toString()));
    }

    static FieldTypeInterface.FieldNameResolver fieldStoredLongResolver(final String genericFieldName,
                                                                        final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            genericFieldName, wildcardMatcher, FieldPrefix.storedField, TypePrefix.longType);
    }

    static FieldTypeInterface.FieldSupplier fieldStoredLong(final String genericFieldName,
                                                            final WildcardMatcher wildcardMatcher) {
        final FieldTypeInterface.FieldNameResolver fieldNameSupplier =
            fieldStoredLongResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new StoredField(fieldNameSupplier.resolve(fieldName), FieldUtils.getLongValue(value)));
    }

    static FieldTypeInterface.FieldNameResolver fieldStoredDoubleResolver(final String genericFieldName,
                                                                          final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            genericFieldName, wildcardMatcher, FieldPrefix.storedField, TypePrefix.doubleType);
    }

    static FieldTypeInterface.FieldSupplier fieldStoredDouble(final String genericFieldName,
                                                              final WildcardMatcher wildcardMatcher) {
        final FieldTypeInterface.FieldNameResolver fieldNameSupplier =
            fieldStoredDoubleResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new StoredField(fieldNameSupplier.resolve(fieldName), FieldUtils.getDoubleValue(value)));
    }

    static FieldTypeInterface.FieldNameResolver fieldStoredIntegerResolver(final String genericFieldName,
                                                                           final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            genericFieldName, wildcardMatcher, FieldPrefix.storedField, TypePrefix.integerType);
    }

    static FieldTypeInterface.FieldSupplier fieldStoredInteger(final String genericFieldName,
                                                               final WildcardMatcher wildcardMatcher) {
        final FieldTypeInterface.FieldNameResolver fieldNameSupplier =
            fieldStoredIntegerResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new StoredField(fieldNameSupplier.resolve(fieldName), FieldUtils.getIntValue(value)));
    }

    static FieldTypeInterface.FieldNameResolver fieldStoredFloatResolver(final String genericFieldName,
                                                                         final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            genericFieldName, wildcardMatcher, FieldPrefix.storedField, TypePrefix.floatType);
    }

    static FieldTypeInterface.FieldSupplier fieldStoredFloat(final String genericFieldName,
                                                             final WildcardMatcher wildcardMatcher) {
        final FieldTypeInterface.FieldNameResolver fieldNameSupplier =
            fieldStoredFloatResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new StoredField(fieldNameSupplier.resolve(fieldName), FieldUtils.getFloatValue(value)));
    }


    static FieldTypeInterface.FieldNameResolver fieldStringTextResolver(final String genericFieldName,
                                                                        final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            genericFieldName, wildcardMatcher, FieldPrefix.stringField, TypePrefix.textType);
    }

    static FieldTypeInterface.FieldSupplier fieldStringText(final String genericFieldName,
                                                            final WildcardMatcher wildcardMatcher,
                                                            final int maxStringLength) {
        final FieldTypeInterface.FieldNameResolver fieldResolver = fieldStringTextResolver(genericFieldName, wildcardMatcher);
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
        final FieldTypeInterface.FieldNameResolver fieldResolver = fieldStringTextResolver(genericFieldName, wildcardMatcher);
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

    static FieldTypeInterface.FieldNameResolver fieldStringLongResolver(final String genericFieldName,
                                                                        final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            genericFieldName, wildcardMatcher, FieldPrefix.stringField, TypePrefix.longType);
    }

    static FieldTypeInterface.FieldSupplier fieldStringLong(final String genericFieldName,
                                                            final WildcardMatcher wildcardMatcher) {
        final FieldTypeInterface.FieldNameResolver fieldNameResolver = fieldStringLongResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new StringField(fieldNameResolver.resolve(fieldName), getLongValue(value), Field.Store.NO));
    }

    static FieldTypeInterface.TermSupplier stringTermLong(final String genericFieldName,
                                                          final WildcardMatcher wildcardMatcher) {
        final FieldTypeInterface.FieldNameResolver fieldNameResolver = fieldStringLongResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value) -> new Term(fieldNameResolver.resolve(fieldName), getLongValue(value));
    }

    private static BytesRef getDoubleValue(Object value) {
        return BytesRefUtils.fromDouble(FieldUtils.getDoubleValue(value));
    }

    static FieldTypeInterface.FieldNameResolver fieldStringDoubleResolver(final String genericFieldName,
                                                                          final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            genericFieldName, wildcardMatcher, FieldPrefix.stringField, TypePrefix.doubleType);
    }

    static FieldTypeInterface.FieldSupplier fieldStringDouble(final String genericFieldName,
                                                              final WildcardMatcher wildcardMatcher) {
        final FieldTypeInterface.FieldNameResolver fieldNameResolver = fieldStringDoubleResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new StringField(fieldNameResolver.resolve(fieldName), getDoubleValue(value), Field.Store.NO));
    }

    static FieldTypeInterface.TermSupplier stringTermDouble(final String genericFieldName,
                                                            final WildcardMatcher wildcardMatcher) {
        final FieldTypeInterface.FieldNameResolver fieldNameResolver = fieldStringDoubleResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value) -> new Term(fieldNameResolver.resolve(fieldName), getDoubleValue(value));
    }

    private static BytesRef getIntegerValue(Object value) {
        return BytesRefUtils.fromInteger(FieldUtils.getIntValue(value));
    }

    static FieldTypeInterface.FieldNameResolver fieldStringIntegerResolver(final String genericFieldName,
                                                                           final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            genericFieldName, wildcardMatcher, FieldPrefix.stringField, TypePrefix.integerType);
    }

    static FieldTypeInterface.FieldSupplier fieldStringInteger(final String genericFieldName,
                                                               final WildcardMatcher wildcardMatcher) {
        final FieldTypeInterface.FieldNameResolver fieldNameResolver = fieldStringIntegerResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new StringField(fieldNameResolver.resolve(fieldName), getIntegerValue(value), Field.Store.NO));
    }

    static FieldTypeInterface.TermSupplier stringTermInteger(final String genericFieldName,
                                                             final WildcardMatcher wildcardMatcher) {
        final FieldTypeInterface.FieldNameResolver fieldNameResolver = fieldStringIntegerResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value) -> new Term(fieldNameResolver.resolve(fieldName), getIntegerValue(value));
    }

    private static BytesRef getFloatValue(Object value) {
        return BytesRefUtils.fromFloat(FieldUtils.getFloatValue(value));
    }

    static FieldTypeInterface.FieldNameResolver fieldStringFloatResolver(final String genericFieldName,
                                                                         final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            genericFieldName, wildcardMatcher, FieldPrefix.stringField, TypePrefix.floatType);
    }

    static FieldTypeInterface.FieldSupplier fieldStringFloat(final String genericFieldName,
                                                             final WildcardMatcher wildcardMatcher) {
        final FieldTypeInterface.FieldNameResolver fieldNameResolver = fieldStringFloatResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new StringField(fieldNameResolver.resolve(fieldName), getFloatValue(value), Field.Store.NO));
    }

    static FieldTypeInterface.TermSupplier stringTermFloat(final String genericFieldName,
                                                           final WildcardMatcher wildcardMatcher) {
        final FieldTypeInterface.FieldNameResolver fieldNameResolver = fieldStringFloatResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value) -> new Term(fieldNameResolver.resolve(fieldName), getFloatValue(value));
    }

    static FieldTypeInterface.FieldSupplier fieldSortedDocValuesText(final String genericFieldName,
                                                                     final WildcardMatcher wildcardMatcher,
                                                                     final int maxStringLength) {
        final FieldTypeInterface.FieldNameResolver fieldNameSupplier = buildNameProvider(
            genericFieldName, wildcardMatcher, FieldPrefix.docValues, TypePrefix.textType);
        return (fieldName, value, builder) -> {
            final String stringValue = value.toString();
            if (stringValue.length() <= maxStringLength)
                builder.accept(genericFieldName, fieldName,
                    new SortedDocValuesField(fieldNameSupplier.resolve(fieldName), new BytesRef(value.toString())));
        };
    }

    static FieldTypeInterface.SortFieldSupplier fieldSortedFieldText(final String genericFieldName,
                                                                     final WildcardMatcher wildcardMatcher) {
        final FieldTypeInterface.FieldNameResolver fieldNameSupplier = buildNameProvider(
            genericFieldName, wildcardMatcher, FieldPrefix.docValues, TypePrefix.textType);
        return (fieldName, sortEnum) -> SortUtils.stringSortField(fieldNameSupplier.resolve(fieldName), sortEnum);
    }

    static FieldTypeInterface.FieldSupplier fieldSortedDocValuesLong(final String genericFieldName,
                                                                     final WildcardMatcher wildcardMatcher) {
        final FieldTypeInterface.FieldNameResolver fieldNameSupplier = buildNameProvider(
            genericFieldName, wildcardMatcher, FieldPrefix.docValues, TypePrefix.longType);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new SortedNumericDocValuesField(fieldNameSupplier.resolve(fieldName), FieldUtils.getLongValue(value)));
    }

    static FieldTypeInterface.SortFieldSupplier fieldSortedFieldLong(final String genericFieldName,
                                                                     final WildcardMatcher wildcardMatcher) {
        final FieldTypeInterface.FieldNameResolver fieldNameSupplier = buildNameProvider(
            genericFieldName, wildcardMatcher, FieldPrefix.docValues, TypePrefix.longType);
        return (fieldName, sortEnum) -> SortUtils.longSortField(fieldNameSupplier.resolve(fieldName), sortEnum);
    }

    static FieldTypeInterface.FieldSupplier fieldSortedDocValuesInteger(final String genericFieldName,
                                                                        final WildcardMatcher wildcardMatcher) {
        final FieldTypeInterface.FieldNameResolver fieldNameSupplier = buildNameProvider(
            genericFieldName, wildcardMatcher, FieldPrefix.docValues, TypePrefix.integerType);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new SortedNumericDocValuesField(fieldNameSupplier.resolve(fieldName), FieldUtils.getIntValue(value)));
    }

    static FieldTypeInterface.SortFieldSupplier fieldSortedFieldInteger(final String genericFieldName,
                                                                        final WildcardMatcher wildcardMatcher) {
        final FieldTypeInterface.FieldNameResolver fieldNameSupplier = buildNameProvider(
            genericFieldName, wildcardMatcher, FieldPrefix.docValues, TypePrefix.integerType);
        return (fieldName, sortEnum) -> SortUtils.integerSortField(fieldNameSupplier.resolve(fieldName), sortEnum);
    }

    static FieldTypeInterface.FieldSupplier fieldSortedDocValuesFloat(final String genericFieldName,
                                                                      final WildcardMatcher wildcardMatcher) {
        final FieldTypeInterface.FieldNameResolver fieldNameSupplier = buildNameProvider(
            genericFieldName, wildcardMatcher, FieldPrefix.docValues, TypePrefix.floatType);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new SortedNumericDocValuesField(fieldNameSupplier.resolve(fieldName),
                NumericUtils.floatToSortableInt(FieldUtils.getFloatValue(value))));
    }

    static FieldTypeInterface.SortFieldSupplier fieldSortedFieldFloat(final String genericFieldName,
                                                                      final WildcardMatcher wildcardMatcher) {
        final FieldTypeInterface.FieldNameResolver fieldNameSupplier = buildNameProvider(
            genericFieldName, wildcardMatcher, FieldPrefix.docValues, TypePrefix.floatType);
        return (fieldName, sortEnum) -> SortUtils.floatSortField(fieldNameSupplier.resolve(fieldName), sortEnum);
    }

    static FieldTypeInterface.FieldSupplier fieldSortedDocValuesDouble(final String genericFieldName,
                                                                       final WildcardMatcher wildcardMatcher) {
        final FieldTypeInterface.FieldNameResolver fieldNameSupplier = buildNameProvider(
            genericFieldName, wildcardMatcher, FieldPrefix.docValues, TypePrefix.doubleType);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new SortedNumericDocValuesField(fieldNameSupplier.resolve(fieldName),
                NumericUtils.doubleToSortableLong(FieldUtils.getDoubleValue(value))));
    }

    static FieldTypeInterface.SortFieldSupplier fieldSortedFieldDouble(final String genericFieldName,
                                                                       final WildcardMatcher wildcardMatcher) {
        final FieldTypeInterface.FieldNameResolver fieldNameSupplier = buildNameProvider(
            genericFieldName, wildcardMatcher, FieldPrefix.docValues, TypePrefix.doubleType);
        return (fieldName, sortEnum) -> SortUtils.doubleSortField(fieldNameSupplier.resolve(fieldName), sortEnum);
    }

    static FieldTypeInterface.FieldNameResolver fieldPointLongResolver(final String genericFieldName,
                                                                       final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            genericFieldName, wildcardMatcher, FieldPrefix.pointField, TypePrefix.longType);
    }

    static FieldTypeInterface.FieldSupplier fieldPointLong(final String genericFieldName,
                                                           final WildcardMatcher wildcardMatcher) {
        final FieldTypeInterface.FieldNameResolver fieldNameSupplier =
            fieldPointLongResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new LongPoint(fieldNameSupplier.resolve(fieldName), FieldUtils.getLongValue(value)));
    }

    static FieldTypeInterface.FieldNameResolver fieldPointDoubleResolver(final String genericFieldName,
                                                                         final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            genericFieldName, wildcardMatcher, FieldPrefix.pointField, TypePrefix.doubleType);
    }

    static FieldTypeInterface.FieldSupplier fieldPointDouble(final String genericFieldName,
                                                             final WildcardMatcher wildcardMatcher) {
        final FieldTypeInterface.FieldNameResolver fieldNameSupplier =
            fieldPointDoubleResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new DoublePoint(fieldNameSupplier.resolve(fieldName), FieldUtils.getDoubleValue(value)));
    }

    static FieldTypeInterface.FieldNameResolver fieldPointIntegerResolver(final String genericFieldName,
                                                                          final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            genericFieldName, wildcardMatcher, FieldPrefix.pointField, TypePrefix.integerType);
    }

    static FieldTypeInterface.FieldSupplier fieldPointInteger(final String genericFieldName,
                                                              final WildcardMatcher wildcardMatcher) {
        final FieldTypeInterface.FieldNameResolver fieldNameSupplier =
            fieldPointIntegerResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new IntPoint(fieldNameSupplier.resolve(fieldName), FieldUtils.getIntValue(value)));
    }

    static FieldTypeInterface.FieldNameResolver fieldPointFloatResolver(final String genericFieldName,
                                                                        final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(
            genericFieldName, wildcardMatcher, FieldPrefix.pointField, TypePrefix.floatType);
    }


    static FieldTypeInterface.FieldSupplier fieldPointFloat(final String genericFieldName,
                                                            final WildcardMatcher wildcardMatcher) {
        final FieldTypeInterface.FieldNameResolver fieldNameSupplier =
            fieldPointFloatResolver(genericFieldName, wildcardMatcher);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new FloatPoint(fieldNameSupplier.resolve(fieldName), FieldUtils.getFloatValue(value)));
    }

    static FieldTypeInterface.FieldNameResolver facetFieldNameSupplier(final String genericFieldName,
                                                                       final WildcardMatcher wildcardMatcher) {
        return buildNameProvider(genericFieldName, wildcardMatcher, FieldPrefix.facetField, TypePrefix.textType);
    }

    static FieldTypeInterface.FieldSupplier facetField(final String genericFieldName,
                                                       final WildcardMatcher wildcardMatcher) {
        final FieldTypeInterface.FieldNameResolver fieldNameSupplier = facetFieldNameSupplier(genericFieldName, wildcardMatcher);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new SortedSetDocValuesFacetField(fieldNameSupplier.resolve(fieldName), value.toString()));
    }

    static FieldTypeInterface.FieldSupplier fullTextField(final String genericFieldName,
                                                          final WildcardMatcher wildcardMatcher) {
        final FieldTypeInterface.FieldNameResolver fieldNameSupplier = buildNameProvider(
            genericFieldName, wildcardMatcher, FieldPrefix.textField, TypePrefix.textType);
        return (fieldName, value, builder) -> builder.accept(genericFieldName, fieldName,
            new TextField(fieldNameSupplier.resolve(fieldName), value.toString(), Field.Store.NO));
    }

}
