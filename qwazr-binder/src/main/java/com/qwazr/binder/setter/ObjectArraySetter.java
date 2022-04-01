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
import com.qwazr.binder.impl.BooleanArraySetterImpl;
import com.qwazr.binder.impl.ByteArraySetterImpl;
import com.qwazr.binder.impl.CharArraySetterImpl;
import com.qwazr.binder.impl.DoubleArraySetterImpl;
import com.qwazr.binder.impl.FloatArraySetterImpl;
import com.qwazr.binder.impl.IntegerArraySetterImpl;
import com.qwazr.binder.impl.LongArraySetterImpl;
import com.qwazr.binder.impl.ObjectArraySetterImpl;
import com.qwazr.binder.impl.ShortArraySetterImpl;
import com.qwazr.binder.impl.StringArraySetterImpl;

import java.lang.reflect.Field;

public interface ObjectArraySetter extends ErrorSetter {

	default void fromString(String[] values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromDouble(Double[] values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromFloat(Float[] values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromLong(Long[] values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromInteger(Integer[] values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromShort(Short[] values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromChar(Character[] values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromByte(Byte[] values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromBoolean(Boolean[] values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromObject(Object[] values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromObjectArray(Class<?> type, Object values, Object object) {
		if (type == String.class) {
			fromString((String[]) values, object);
		} else if (type == Double.class) {
			fromDouble((Double[]) values, object);
		} else if (type == Float.class) {
			fromFloat((Float[]) values, object);
		} else if (type == Long.class) {
			fromLong((Long[]) values, object);
		} else if (type == Integer.class) {
			fromInteger((Integer[]) values, object);
		} else if (type == Short.class) {
			fromShort((Short[]) values, object);
		} else if (type == Character.class) {
			fromChar((Character[]) values, object);
		} else if (type == Byte.class) {
			fromByte((Byte[]) values, object);
		} else if (type == Boolean.class) {
			fromBoolean((Boolean[]) values, object);
		} else {
			fromObject((Object[]) values, object);
		}
	}

	static FieldSetter from(final Field field, final Class<?> type) {
		if (type == String.class) {
			return new StringArraySetterImpl(field);
		} else if (type == Double.class) {
			return new DoubleArraySetterImpl(field);
		} else if (type == Float.class) {
			return new FloatArraySetterImpl(field);
		} else if (type == Long.class) {
			return new LongArraySetterImpl(field);
		} else if (type == Integer.class) {
			return new IntegerArraySetterImpl(field);
		} else if (type == Short.class) {
			return new ShortArraySetterImpl(field);
		} else if (type == Character.class) {
			return new CharArraySetterImpl(field);
		} else if (type == Byte.class) {
			return new ByteArraySetterImpl(field);
		} else if (type == Boolean.class) {
			return new BooleanArraySetterImpl(field);
		} else if (type == Object.class) {
			return new ObjectArraySetterImpl(field);
		} else
			throw new BinderException("Unsupported type: " + type, field, null);

	}
}
