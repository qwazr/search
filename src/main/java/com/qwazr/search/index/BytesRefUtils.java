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
import org.apache.lucene.util.BytesRefBuilder;
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
		final BytesRefBuilder builder = new BytesRefBuilder();
		NumericUtils.intToPrefixCoded(value, 0, builder);
		return builder.toBytesRef();
	}

	final static public BytesRef from(final Long value) {
		if (value == null)
			return new BytesRef(BytesRef.EMPTY_BYTES);
		final BytesRefBuilder builder = new BytesRefBuilder();
		NumericUtils.longToPrefixCoded(value, 0, builder);
		return builder.toBytesRef();
	}

	final static public BytesRef from(final Float value) {
		if (value == null)
			return new BytesRef(BytesRef.EMPTY_BYTES);
		final BytesRefBuilder builder = new BytesRefBuilder();
		NumericUtils.intToPrefixCoded(NumericUtils.floatToSortableInt(value), 0, builder);
		return builder.toBytesRef();
	}

	final static public BytesRef from(final Double value) {
		if (value == null)
			return new BytesRef(BytesRef.EMPTY_BYTES);
		final BytesRefBuilder builder = new BytesRefBuilder();
		NumericUtils.longToPrefixCoded(NumericUtils.doubleToSortableLong(value), 0, builder);
		return builder.toBytesRef();
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

}
