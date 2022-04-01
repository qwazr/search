/*
 * Copyright 2016-2017 Emmanuel Keller / QWAZR
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
package com.qwazr.binder.setter;

import com.qwazr.binder.BinderException;
import com.qwazr.binder.impl.BooleanPrimitiveArraySetterImpl;
import com.qwazr.binder.impl.BytePrimitiveArraySetterImpl;
import com.qwazr.binder.impl.CharPrimitiveArraySetterImpl;
import com.qwazr.binder.impl.DoublePrimitiveArraySetterImpl;
import com.qwazr.binder.impl.FloatPrimitiveArraySetterImpl;
import com.qwazr.binder.impl.IntegerPrimitiveArraySetterImpl;
import com.qwazr.binder.impl.LongPrimitiveArraySetterImpl;
import com.qwazr.binder.impl.ShortPrimitiveArraySetterImpl;

import java.lang.reflect.Field;

public interface PrimitiveArraySetter extends ErrorSetter {

	default void fromDouble(double[] values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromFloat(float[] values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromLong(long[] values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromInteger(int[] values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromShort(short[] values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromChar(char[] values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromByte(byte[] values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromBoolean(boolean[] values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromPrimitiveArray(Class<?> type, Object values, Object object) {
		if (type == double.class) {
			fromDouble((double[]) values, object);
		} else if (type == float.class) {
			fromFloat((float[]) values, object);
		} else if (type == long.class) {
			fromLong((long[]) values, object);
		} else if (type == int.class) {
			fromInteger((int[]) values, object);
		} else if (type == short.class) {
			fromShort((short[]) values, object);
		} else if (type == char.class) {
			fromChar((char[]) values, object);
		} else if (type == byte.class) {
			fromByte((byte[]) values, object);
		} else if (type == boolean.class) {
			fromBoolean((boolean[]) values, object);
		} else
			throw error("Unsupported primitive type: " + type, values);
	}

	static FieldSetter from(final Field field, final Class<?> type) {
		if (type == double.class) {
			return new DoublePrimitiveArraySetterImpl(field);
		} else if (type == float.class) {
			return new FloatPrimitiveArraySetterImpl(field);
		} else if (type == long.class) {
			return new LongPrimitiveArraySetterImpl(field);
		} else if (type == int.class) {
			return new IntegerPrimitiveArraySetterImpl(field);
		} else if (type == short.class) {
			return new ShortPrimitiveArraySetterImpl(field);
		} else if (type == char.class) {
			return new CharPrimitiveArraySetterImpl(field);
		} else if (type == byte.class) {
			return new BytePrimitiveArraySetterImpl(field);
		} else if (type == boolean.class) {
			return new BooleanPrimitiveArraySetterImpl(field);
		} else
			throw new BinderException("Unsupported type: " + type, field, null);

	}
}
