/*
 * Copyright 2015-2016 Emmanuel Keller / QWAZR
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
package com.qwazr.search.index;

import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.facet.taxonomy.FloatAssociationFacetField;
import org.apache.lucene.facet.taxonomy.IntAssociationFacetField;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;

import java.util.HashMap;
import java.util.Map;

public class BytesRefUtils {

    static private void checkByteSize(final BytesRef bytesRef, final int expectedSize, final String errorMessage) {
        if (bytesRef.bytes.length != expectedSize)
            throw new RuntimeException(errorMessage);
    }

    static private boolean isEmpty(final BytesRef bytesRef) {
        return bytesRef == null || bytesRef.bytes == BytesRef.EMPTY_BYTES || bytesRef.bytes == null;
    }

    public final static BytesRef EMPTY = new BytesRef();

    public interface Converter<T> {

        BytesRef from(T value);

        T to(BytesRef bytesRef);

        BytesRefConverter BYTESREF = new BytesRefConverter();
        StringConverter STRING = new StringConverter();
        IntegerConverter INT = new IntegerConverter();
        LongConverter LONG = new LongConverter();
        FloatConverter FLOAT = new FloatConverter();
        DoubleConverter DOUBLE = new DoubleConverter();
        DoublePointConverter DOUBLE_POINT = new DoublePointConverter();
        FloatPointConverter FLOAT_POINT = new FloatPointConverter();
        IntPointConverter INT_POINT = new IntPointConverter();
        LongPointConverter LONG_POINT = new LongPointConverter();
        IntFacetConverter INT_FACET = new IntFacetConverter();
        FloatFacetConverter FLOAT_FACET = new FloatFacetConverter();
    }

    final static public class BytesRefConverter implements Converter<BytesRef> {

        @Override
        final public BytesRef from(final BytesRef value) {
            return value == null ? new BytesRef() : value;
        }

        @Override
        final public BytesRef to(final BytesRef bytesRef) {
            return isEmpty(bytesRef) ? null : bytesRef;
        }
    }

    final static public class StringConverter implements Converter<String> {

        @Override
        final public BytesRef from(final String value) {
            return value == null ? new BytesRef() : new BytesRef(value);
        }

        @Override
        final public String to(final BytesRef bytesRef) {
            return isEmpty(bytesRef) ? null : bytesRef.utf8ToString();
        }
    }

    public static BytesRef fromInteger(int value) {
        final BytesRef bytesRef = new BytesRef(new byte[4]);
        NumericUtils.intToSortableBytes(value, bytesRef.bytes, 0);
        return bytesRef;
    }

    final static public class IntegerConverter implements Converter<Integer> {

        @Override
        final public BytesRef from(final Integer value) {
            return value == null ? new BytesRef() : fromInteger(value);
        }

        @Override
        final public Integer to(final BytesRef bytesRef) {
            if (isEmpty(bytesRef))
                return null;
            checkByteSize(bytesRef, Integer.BYTES, "Cannot convert value to int");
            return NumericUtils.sortableBytesToInt(bytesRef.bytes, 0);
        }
    }

    public static BytesRef fromLong(long value) {
        final BytesRef bytesRef = new BytesRef(new byte[8]);
        NumericUtils.longToSortableBytes(value, bytesRef.bytes, 0);
        return bytesRef;
    }

    final static public class LongConverter implements Converter<Long> {

        @Override
        final public BytesRef from(final Long value) {
            return value == null ? new BytesRef() : fromLong(value);
        }

        @Override
        final public Long to(final BytesRef bytesRef) {
            if (isEmpty(bytesRef))
                return null;
            checkByteSize(bytesRef, Long.BYTES, "Cannot convert value to long");
            return NumericUtils.sortableBytesToLong(bytesRef.bytes, 0);
        }
    }

    public static BytesRef fromFloat(float value) {
        final BytesRef bytesRef = new BytesRef(new byte[4]);
        NumericUtils.intToSortableBytes(NumericUtils.floatToSortableInt(value), bytesRef.bytes, 0);
        return bytesRef;
    }

    final static public class FloatConverter implements Converter<Float> {

        @Override
        final public BytesRef from(final Float value) {
            return value == null ? new BytesRef() : fromFloat(value);
        }

        @Override
        final public Float to(final BytesRef bytesRef) {
            if (isEmpty(bytesRef))
                return null;
            checkByteSize(bytesRef, Float.BYTES, "Cannot convert value to float");
            return NumericUtils.sortableIntToFloat(NumericUtils.sortableBytesToInt(bytesRef.bytes, 0));
        }
    }

    public static BytesRef fromDouble(double value) {
        final BytesRef bytesRef = new BytesRef(new byte[8]);
        NumericUtils.longToSortableBytes(NumericUtils.doubleToSortableLong(value), bytesRef.bytes, 0);
        return bytesRef;
    }

    final static public class DoubleConverter implements Converter<Double> {

        @Override
        final public BytesRef from(final Double value) {
            return value == null ? new BytesRef() : fromDouble(value);
        }

        @Override
        final public Double to(final BytesRef bytesRef) {
            if (isEmpty(bytesRef))
                return null;
            checkByteSize(bytesRef, Double.BYTES, "Cannot convert value to double");
            return NumericUtils.sortableLongToDouble(NumericUtils.sortableBytesToLong(bytesRef.bytes, 0));
        }
    }

    static private final Map<Class, Converter> CONVERTER_MAP = new HashMap<>();

    static {
        CONVERTER_MAP.put(BytesRef.class, Converter.BYTESREF);
        CONVERTER_MAP.put(String.class, Converter.STRING);
        CONVERTER_MAP.put(Integer.class, Converter.INT);
        CONVERTER_MAP.put(Float.class, Converter.FLOAT);
        CONVERTER_MAP.put(Long.class, Converter.LONG);
        CONVERTER_MAP.put(Double.class, Converter.DOUBLE);
    }

    static public BytesRef fromAny(final Object value) {
        if (value == null)
            return new BytesRef();
        final Converter converter = CONVERTER_MAP.get(value.getClass());
        return converter == null ? new BytesRef(value.toString()) : converter.from(value);
    }

    static public Term toTerm(final String field, final Object value) {
        return new Term(field, fromAny(value));
    }

    final static public class DoublePointConverter implements Converter<Double> {

        @Override
        final public BytesRef from(final Double value) {
            if (value == null)
                return new BytesRef();
            final BytesRef bytesRef = new BytesRef(new byte[8]);
            DoublePoint.encodeDimension(value, bytesRef.bytes, 0);
            return bytesRef;
        }

        @Override
        public final Double to(final BytesRef bytesRef) {
            if (isEmpty(bytesRef))
                return null;
            return DoublePoint.decodeDimension(bytesRef.bytes, 0);
        }
    }

    final static public class FloatPointConverter implements Converter<Float> {

        @Override
        final public BytesRef from(final Float value) {
            if (value == null)
                return new BytesRef();
            final BytesRef bytesRef = new BytesRef(new byte[8]);
            FloatPoint.encodeDimension(value, bytesRef.bytes, 0);
            return bytesRef;
        }

        @Override
        public final Float to(final BytesRef bytesRef) {
            if (isEmpty(bytesRef))
                return null;
            return FloatPoint.decodeDimension(bytesRef.bytes, 0);
        }
    }

    final static public class IntPointConverter implements Converter<Integer> {

        @Override
        final public BytesRef from(final Integer value) {
            if (value == null)
                return new BytesRef();
            final BytesRef bytesRef = new BytesRef(new byte[8]);
            IntPoint.encodeDimension(value, bytesRef.bytes, 0);
            return bytesRef;
        }

        @Override
        public final Integer to(final BytesRef bytesRef) {
            if (isEmpty(bytesRef))
                return null;
            return IntPoint.decodeDimension(bytesRef.bytes, 0);
        }
    }

    final static public class LongPointConverter implements Converter<Long> {

        @Override
        final public BytesRef from(final Long value) {
            if (value == null)
                return new BytesRef();
            final BytesRef bytesRef = new BytesRef(new byte[8]);
            LongPoint.encodeDimension(value, bytesRef.bytes, 0);
            return bytesRef;
        }

        @Override
        public final Long to(final BytesRef bytesRef) {
            if (isEmpty(bytesRef))
                return null;
            return LongPoint.decodeDimension(bytesRef.bytes, 0);
        }
    }

    final static public class IntFacetConverter implements Converter<Integer> {

        @Override
        final public BytesRef from(final Integer value) {
            return value == null ? new BytesRef() : IntAssociationFacetField.intToBytesRef(value);
        }

        @Override
        public final Integer to(final BytesRef bytesRef) {
            return isEmpty(bytesRef) ? null : IntAssociationFacetField.bytesRefToInt(bytesRef);
        }
    }

    final static public class FloatFacetConverter implements Converter<Float> {

        @Override
        final public BytesRef from(final Float value) {
            return value == null ? new BytesRef() : FloatAssociationFacetField.floatToBytesRef(value);
        }

        @Override
        public final Float to(final BytesRef bytesRef) {
            return isEmpty(bytesRef) ? null : FloatAssociationFacetField.bytesRefToFloat(bytesRef);
        }
    }
}
