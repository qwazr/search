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
import com.qwazr.search.index.DocumentBuilder;
import com.qwazr.search.index.QueryDefinition;
import org.apache.lucene.document.*;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.SortField;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;

import javax.validation.constraints.NotNull;
import javax.ws.rs.NotAcceptableException;

interface SmartFieldProvider {

    Field getField(final Object value);

    Term getTerm(final Object value);

    SortField getSort(final QueryDefinition.SortEnum sortEnum);

    void apply(final Object value, final DocumentBuilder builder);

    enum FieldPrefix {

        storedField('r'), stringField('s'), facetField('f'), docValues('d'), textField('t'), pointField('p');

        final char prefix;

        FieldPrefix(char prefix) {
            this.prefix = prefix;
        }

        private String getLuceneFieldName(final String genericFieldName, final TypePrefix typePrefix) {
            return String.valueOf(new char[]{prefix, typePrefix.prefix, '€'}).concat(genericFieldName);
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

    class Noop implements SmartFieldProvider {

        public static Noop INSTANCE = new Noop();

        private Noop() {
        }

        @Override
        public Field getField(Object value) {
            return null;
        }

        @Override
        public Term getTerm(final Object value) {
            return null;
        }

        @Override
        public SortField getSort(final QueryDefinition.SortEnum sortEnum) {
            return null;
        }

        @Override
        public void apply(Object value, DocumentBuilder builder) {
        }

    }

    abstract class Base implements SmartFieldProvider {

        private final String genericFieldName;
        protected final String concreteFieldName;

        protected Base(final String genericFieldName,
                       final FieldPrefix fieldPrefix,
                       final TypePrefix typePrefix) {
            this.genericFieldName = genericFieldName;
            this.concreteFieldName = typePrefix.getLuceneFieldName(genericFieldName, fieldPrefix);
        }

        @Override
        public Field getField(final Object value) {
            return null;
        }

        @Override
        public Term getTerm(final Object value) {
            throw new NotAcceptableException("The field '" + genericFieldName + "' does not support term queries.");
        }

        @Override
        public SortField getSort(final QueryDefinition.SortEnum sortEnum) {
            throw new NotAcceptableException("The field '" + genericFieldName + "' does not support sorting.");
        }

        @Override
        final public void apply(final Object value, final DocumentBuilder builder) {
            final Field field = getField(value);
            if (field != null)
                builder.accept(genericFieldName, concreteFieldName, field);
        }

    }

    static SmartFieldProvider stored(final String genericFieldName, final SmartFieldDefinition.Type type) {
        switch (type) {
            case TEXT:
                return new StoredText(genericFieldName);
            case LONG:
                return new StoredLong(genericFieldName);
            case DOUBLE:
                return new StoredDouble(genericFieldName);
            case INTEGER:
                return new StoredInteger(genericFieldName);
            case FLOAT:
                return new StoredFloat(genericFieldName);
            default:
                return Noop.INSTANCE;
        }
    }

    final class StoredText extends Base {

        StoredText(final String genericFieldName) {
            super(genericFieldName, FieldPrefix.storedField, TypePrefix.textType);
        }

        @Override
        public Field getField(final Object value) {
            return new StoredField(concreteFieldName, value.toString());
        }
    }

    final class StoredLong extends Base {

        StoredLong(final String genericFieldName) {
            super(genericFieldName, FieldPrefix.storedField, TypePrefix.longType);
        }

        @Override
        public Field getField(final Object value) {
            return new StoredField(concreteFieldName, FieldUtils.getLongValue(value));
        }
    }

    final class StoredDouble extends Base {

        StoredDouble(final String genericFieldName) {
            super(genericFieldName, FieldPrefix.storedField, TypePrefix.doubleType);
        }

        @Override
        public Field getField(final Object value) {
            return new StoredField(concreteFieldName, FieldUtils.getDoubleValue(value));
        }
    }

    final class StoredInteger extends Base {

        StoredInteger(final String genericFieldName) {
            super(genericFieldName, FieldPrefix.storedField, TypePrefix.integerType);
        }

        @Override
        public Field getField(final Object value) {
            return new StoredField(concreteFieldName, FieldUtils.getIntValue(value));
        }
    }

    final class StoredFloat extends Base {

        StoredFloat(final String genericFieldName) {
            super(genericFieldName, FieldPrefix.storedField, TypePrefix.floatType);
        }

        @Override
        public Field getField(final Object value) {
            return new StoredField(concreteFieldName, FieldUtils.getFloatValue(value));
        }
    }

    static SmartFieldProvider string(final String genericFieldName,
                                     final int nextStringLength,
                                     final SmartFieldDefinition.Type type) {
        switch (type) {
            case TEXT:
                return new StringText(genericFieldName, nextStringLength);
            case LONG:
                return new StringLong(genericFieldName);
            case DOUBLE:
                return new StringDouble(genericFieldName);
            case INTEGER:
                return new StringInteger(genericFieldName);
            case FLOAT:
                return new StringFloat(genericFieldName);
            default:
                return Noop.INSTANCE;
        }
    }

    final class StringText extends Base {

        private final int nextStringLength;

        StringText(final String genericFieldName, final int nextStringLength) {
            super(genericFieldName, FieldPrefix.stringField, TypePrefix.textType);
            this.nextStringLength = nextStringLength;
        }

        @Override
        public Field getField(final Object value) {
            final String stringValue = value.toString();
            if (stringValue.length() > nextStringLength)
                return null;
            return new StringField(concreteFieldName, stringValue, Field.Store.NO);
        }

        @Override
        public Term getTerm(final Object value) {
            return new Term(concreteFieldName, value.toString());
        }
    }

    private static BytesRef getLongValue(Object value) {
        return BytesRefUtils.fromLong(FieldUtils.getLongValue(value));
    }

    final class StringLong extends Base {

        StringLong(final String genericFieldName) {
            super(genericFieldName, FieldPrefix.stringField, TypePrefix.longType);
        }

        @Override
        public Field getField(final Object value) {
            return new StringField(concreteFieldName, getLongValue(value), Field.Store.NO);
        }

        @Override
        public Term getTerm(final Object value) {
            return new Term(concreteFieldName, getLongValue(value));
        }
    }

    private static BytesRef getDoubleValue(Object value) {
        return BytesRefUtils.fromDouble(FieldUtils.getDoubleValue(value));
    }

    final class StringDouble extends Base {

        StringDouble(final String genericFieldName) {
            super(genericFieldName, FieldPrefix.stringField, TypePrefix.doubleType);
        }

        @Override
        public Field getField(final Object value) {
            return new StringField(concreteFieldName, getDoubleValue(value), Field.Store.NO);
        }

        @Override
        public Term getTerm(final Object value) {
            return new Term(concreteFieldName, getDoubleValue(value));
        }
    }

    private static BytesRef getIntegerValue(Object value) {
        return BytesRefUtils.fromInteger(FieldUtils.getIntValue(value));
    }

    final class StringInteger extends Base {

        StringInteger(final String genericFieldName) {
            super(genericFieldName, FieldPrefix.stringField, TypePrefix.integerType);
        }

        @Override
        public Field getField(final Object value) {
            return new StringField(concreteFieldName, getIntegerValue(value), Field.Store.NO);
        }

        @Override
        public Term getTerm(final Object value) {
            return new Term(concreteFieldName, getIntegerValue(value));
        }
    }

    private static BytesRef getFloatValue(Object value) {
        return BytesRefUtils.fromFloat(FieldUtils.getFloatValue(value));
    }

    final class StringFloat extends Base {

        StringFloat(final String genericFieldName) {
            super(genericFieldName, FieldPrefix.stringField, TypePrefix.floatType);
        }

        @Override
        public Field getField(final Object value) {
            return new StringField(concreteFieldName, getFloatValue(value), Field.Store.NO);
        }

        @Override
        public Term getTerm(final Object value) {
            return new Term(concreteFieldName, getFloatValue(value));
        }
    }

    static SmartFieldProvider sortedDocValue(final String genericFieldName, final SmartFieldDefinition.Type type) {
        switch (type) {
            case TEXT:
                return new SortedDocValuesText(genericFieldName);
            case LONG:
                return new SortedDocValuesLong(genericFieldName);
            case DOUBLE:
                return new SortedDocValuesDouble(genericFieldName);
            case INTEGER:
                return new SortedDocValuesInteger(genericFieldName);
            case FLOAT:
                return new SortedDocValuesFloat(genericFieldName);
            default:
                return Noop.INSTANCE;
        }
    }

    final class SortedDocValuesText extends Base {

        SortedDocValuesText(final String genericFieldName) {
            super(genericFieldName, FieldPrefix.docValues, TypePrefix.textType);
        }

        @Override
        public Field getField(final Object value) {
            return new SortedDocValuesField(concreteFieldName, new BytesRef(value.toString()));
        }

        @Override
        public SortField getSort(final QueryDefinition.SortEnum sortEnum) {
            return SortUtils.stringSortField(concreteFieldName, sortEnum);
        }

    }

    final class SortedDocValuesLong extends Base {

        SortedDocValuesLong(final String genericFieldName) {
            super(genericFieldName, FieldPrefix.docValues, TypePrefix.longType);
        }

        @Override
        public Field getField(final Object value) {
            return new SortedNumericDocValuesField(concreteFieldName, FieldUtils.getLongValue(value));
        }

        @Override
        public SortField getSort(final QueryDefinition.SortEnum sortEnum) {
            return SortUtils.longSortField(concreteFieldName, sortEnum);
        }
    }

    final class SortedDocValuesInteger extends Base {

        SortedDocValuesInteger(final String genericFieldName) {
            super(genericFieldName, FieldPrefix.docValues, TypePrefix.integerType);
        }

        @Override
        public Field getField(final Object value) {
            return new SortedNumericDocValuesField(concreteFieldName, FieldUtils.getIntValue(value));
        }

        @Override
        public SortField getSort(final QueryDefinition.SortEnum sortEnum) {
            return SortUtils.integerSortField(concreteFieldName, sortEnum);
        }
    }

    final class SortedDocValuesDouble extends Base {

        SortedDocValuesDouble(final String genericFieldName) {
            super(genericFieldName, FieldPrefix.docValues, TypePrefix.doubleType);
        }

        @Override
        public Field getField(final Object value) {
            return new SortedNumericDocValuesField(concreteFieldName,
                NumericUtils.doubleToSortableLong(FieldUtils.getDoubleValue(value)));
        }

        @Override
        public SortField getSort(final QueryDefinition.SortEnum sortEnum) {
            return SortUtils.doubleSortField(concreteFieldName, sortEnum);
        }
    }

    final class SortedDocValuesFloat extends Base {

        SortedDocValuesFloat(final String genericFieldName) {
            super(genericFieldName, FieldPrefix.docValues, TypePrefix.floatType);
        }

        @Override
        public Field getField(final Object value) {
            return new SortedNumericDocValuesField(concreteFieldName,
                NumericUtils.floatToSortableInt(FieldUtils.getFloatValue(value)));
        }

        @Override
        public SortField getSort(final QueryDefinition.SortEnum sortEnum) {
            return SortUtils.floatSortField(concreteFieldName, sortEnum);
        }
    }

    static SmartFieldProvider point(final String genericFieldName, final SmartFieldDefinition.Type type) {
        switch (type) {
            case LONG:
                return new PointLong(genericFieldName);
            case DOUBLE:
                return new PointDouble(genericFieldName);
            case INTEGER:
                return new PointInteger(genericFieldName);
            case FLOAT:
                return new PointFloat(genericFieldName);
            default:
                return Noop.INSTANCE;
        }
    }

    final class PointLong extends Base {

        PointLong(final String genericFieldName) {
            super(genericFieldName, FieldPrefix.pointField, TypePrefix.longType);
        }

        @Override
        public Field getField(final Object value) {
            return new LongPoint(concreteFieldName,
                value instanceof Number ? ((Number) value).longValue() : Long.parseLong(value.toString())
            );
        }

        @Override
        public Term getTerm(final Object value) {
            return new Term(concreteFieldName, getLongValue(value));
        }
    }

    final class PointDouble extends Base {

        PointDouble(final String genericFieldName) {
            super(genericFieldName, FieldPrefix.pointField, TypePrefix.doubleType);
        }

        @Override
        public Field getField(final Object value) {
            return new DoublePoint(concreteFieldName,
                value instanceof Number ? ((Number) value).doubleValue() : Double.parseDouble(value.toString())
            );
        }

        @Override
        public Term getTerm(final Object value) {
            return new Term(concreteFieldName, getDoubleValue(value));
        }
    }

    final class PointInteger extends Base {

        PointInteger(final String genericFieldName) {
            super(genericFieldName, FieldPrefix.pointField, TypePrefix.integerType);
        }

        @Override
        public Field getField(final Object value) {
            return new IntPoint(concreteFieldName,
                value instanceof Number ? ((Number) value).intValue() : Integer.parseInt(value.toString())
            );
        }

        @Override
        public Term getTerm(final Object value) {
            return new Term(concreteFieldName, getIntegerValue(value));
        }
    }

    final class PointFloat extends Base {

        PointFloat(final String genericFieldName) {
            super(genericFieldName, FieldPrefix.pointField, TypePrefix.floatType);
        }

        @Override
        public Field getField(final Object value) {
            return new FloatPoint(concreteFieldName,
                value instanceof Number ? ((Number) value).floatValue() : Float.parseFloat(value.toString())
            );
        }

        @Override
        public Term getTerm(final Object value) {
            return new Term(concreteFieldName, getFloatValue(value));
        }
    }

    final class Facet extends Base {

        Facet(final String genericFieldName) {
            super(genericFieldName, FieldPrefix.facetField, TypePrefix.textType);
        }

        @Override
        public Field getField(Object value) {
            return new SortedSetDocValuesFacetField(concreteFieldName, value.toString());
        }
    }

    final class FullText extends Base {

        FullText(final String genericFieldName) {
            super(genericFieldName, FieldPrefix.textField, TypePrefix.textType);
        }

        @Override
        public Field getField(@NotNull final Object value) {
            return new TextField(concreteFieldName, value.toString(), Field.Store.NO);
        }
    }

}
