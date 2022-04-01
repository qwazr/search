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
import com.qwazr.binder.impl.BooleanPrimitiveSetterImpl;
import com.qwazr.binder.impl.BytePrimitiveSetterImpl;
import com.qwazr.binder.impl.CharPrimitiveSetterImpl;
import com.qwazr.binder.impl.DoublePrimitiveSetterImpl;
import com.qwazr.binder.impl.FloatPrimitiveSetterImpl;
import com.qwazr.binder.impl.IntegerPrimitiveSetterImpl;
import com.qwazr.binder.impl.LongPrimitiveSetterImpl;
import com.qwazr.binder.impl.ShortPrimitiveSetterImpl;

import java.lang.reflect.Field;

public interface PrimitiveSetter extends ErrorSetter {

	default void fromDouble(double value, Object object) {
		throw error("Not supported ", object);
	}

	default void fromFloat(float value, Object object) {
		throw error("Not supported ", object);
	}

	default void fromLong(long value, Object object) {
		throw error("Not supported ", object);
	}

	default void fromInteger(int value, Object object) {
		throw error("Not supported ", object);
	}

	default void fromShort(short value, Object object) {
		throw error("Not supported ", object);
	}

	default void fromChar(char value, Object object) {
		throw error("Not supported ", object);
	}

	default void fromByte(byte value, Object object) {
		throw error("Not supported ", object);
	}

	default void fromBoolean(boolean value, Object object) {
		throw error("Not supported ", object);
	}

	default void fromPrimitive(final Class<?> type, final Object values, final Object object) {
		if (type == double.class) {
			fromDouble((double) values, object);
		} else if (type == float.class) {
			fromFloat((float) values, object);
		} else if (type == long.class) {
			fromLong((long) values, object);
		} else if (type == int.class) {
			fromInteger((int) values, object);
		} else if (type == short.class) {
			fromShort((short) values, object);
		} else if (type == char.class) {
			fromChar((char) values, object);
		} else if (type == byte.class) {
			fromByte((byte) values, object);
		} else if (type == boolean.class) {
			fromBoolean((boolean) values, object);
		} else
			throw error("Unsupported primitive type: " + type, values);
	}

	static FieldSetter from(final Field field, final Class<?> type) {
		if (type == double.class) {
			return new DoublePrimitiveSetterImpl(field);
		} else if (type == float.class) {
			return new FloatPrimitiveSetterImpl(field);
		} else if (type == long.class) {
			return new LongPrimitiveSetterImpl(field);
		} else if (type == int.class) {
			return new IntegerPrimitiveSetterImpl(field);
		} else if (type == short.class) {
			return new ShortPrimitiveSetterImpl(field);
		} else if (type == char.class) {
			return new CharPrimitiveSetterImpl(field);
		} else if (type == byte.class) {
			return new BytePrimitiveSetterImpl(field);
		} else if (type == boolean.class) {
			return new BooleanPrimitiveSetterImpl(field);
		} else
			throw new BinderException("Unsupported type: " + type, field, null);

	}
}
