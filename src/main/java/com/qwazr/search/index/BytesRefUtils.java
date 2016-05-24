/**
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

import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;

public class BytesRefUtils {

	final static public BytesRef from(final String value) {
		if (value == null)
			return new BytesRef(BytesRef.EMPTY_BYTES);
		return new BytesRef(value);
	}

	final static public BytesRef from(final Integer value) {
		if (value == null)
			return new BytesRef(BytesRef.EMPTY_BYTES);
		final BytesRef bytesRef = new BytesRef(new byte[4]);
		NumericUtils.intToSortableBytes(value, bytesRef.bytes, 0);
		return bytesRef;
	}

	final static public BytesRef from(final Long value) {
		if (value == null)
			return new BytesRef(BytesRef.EMPTY_BYTES);
		final BytesRef bytesRef = new BytesRef(new byte[8]);
		NumericUtils.longToSortableBytes(value, bytesRef.bytes, 0);
		return bytesRef;
	}

	final static public BytesRef from(final Float value) {
		if (value == null)
			return new BytesRef(BytesRef.EMPTY_BYTES);
		final BytesRef bytesRef = new BytesRef(new byte[4]);
		NumericUtils.intToSortableBytes(NumericUtils.floatToSortableInt(value), bytesRef.bytes, 0);
		return bytesRef;
	}

	final static public BytesRef from(final Double value) {
		if (value == null)
			return new BytesRef(BytesRef.EMPTY_BYTES);
		final BytesRef bytesRef = new BytesRef(new byte[8]);
		NumericUtils.longToSortableBytes(NumericUtils.doubleToSortableLong(value), bytesRef.bytes, 0);
		return bytesRef;
	}

	final static public BytesRef fromAny(final Object value) {
		if (value == null)
			return new BytesRef(BytesRef.EMPTY_BYTES);
		if (value instanceof String)
			return from((String) value);
		if (value instanceof Integer)
			return from((Integer) value);
		if (value instanceof Float)
			return from((Float) value);
		if (value instanceof Long)
			return from((String) value);
		if (value instanceof Double)
			return from((String) value);
		if (value instanceof BytesRef)
			return (BytesRef) value;
		return new BytesRef(value.toString());
	}

	final static private byte[] checkByteSize(final BytesRef bytesRef, final int expectedSize,
			final String errorMessage) {
		if (bytesRef == null || bytesRef.bytes == null)
			return null;
		if (bytesRef.bytes.length != expectedSize)
			throw new RuntimeException(errorMessage);
		return bytesRef.bytes;
	}

	final static public Integer toInt(final BytesRef bytesRef) {
		final byte[] bytes = checkByteSize(bytesRef, Integer.BYTES, "Cannot convert value to int");
		if (bytes == null)
			return null;
		return NumericUtils.sortableBytesToInt(bytesRef.bytes, 0);
	}

	final static public Float toFloat(final BytesRef bytesRef) {
		final byte[] bytes = checkByteSize(bytesRef, Float.BYTES, "Cannot convert value to float");
		if (bytes == null)
			return null;
		return NumericUtils.sortableIntToFloat(NumericUtils.sortableBytesToInt(bytesRef.bytes, 0));
	}

	final static public Long toLong(final BytesRef bytesRef) {
		final byte[] bytes = checkByteSize(bytesRef, Long.BYTES, "Cannot convert value to long");
		if (bytes == null)
			return null;
		return NumericUtils.sortableBytesToLong(bytesRef.bytes, 0);
	}

	final static public Double toDouble(final BytesRef bytesRef) {
		final byte[] bytes = checkByteSize(bytesRef, Double.BYTES, "Cannot convert value to double");
		if (bytes == null)
			return null;
		return NumericUtils.sortableLongToDouble(NumericUtils.sortableBytesToLong(bytesRef.bytes, 0));
	}

	final static public String toString(final BytesRef bytesRef) {
		if (bytesRef == null || bytesRef.bytes == null)
			return null;
		return bytesRef.utf8ToString();
	}

}
