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
import com.qwazr.binder.impl.BooleanSetterImpl;
import com.qwazr.binder.impl.ByteSetterImpl;
import com.qwazr.binder.impl.CharSetterImpl;
import com.qwazr.binder.impl.DoubleSetterImpl;
import com.qwazr.binder.impl.FloatSetterImpl;
import com.qwazr.binder.impl.IntegerSetterImpl;
import com.qwazr.binder.impl.LongSetterImpl;
import com.qwazr.binder.impl.SerializableSetterImpl;
import com.qwazr.binder.impl.ShortSetterImpl;
import com.qwazr.binder.impl.StringSetterImpl;

import java.io.Serializable;
import java.lang.reflect.Field;

public interface ObjectSetter extends ErrorSetter {

	default void fromString(String value, Object object) {
		throw error("Not supported ", object);
	}

	default void fromDouble(Double value, Object object) {
		throw error("Not supported ", object);
	}

	default void fromFloat(Float value, Object object) {
		throw error("Not supported ", object);
	}

	default void fromLong(Long value, Object object) {
		throw error("Not supported ", object);
	}

	default void fromInteger(Integer value, Object object) {
		throw error("Not supported ", object);
	}

	default void fromShort(Short value, Object object) {
		throw error("Not supported ", object);
	}

	default void fromChar(Character value, Object object) {
		throw error("Not supported ", object);
	}

	default void fromByte(Byte value, Object object) {
		throw error("Not supported ", object);
	}

	default void fromBoolean(Boolean value, Object object) {
		throw error("Not supported ", object);
	}
	
	void set(Object object, Object value);

	default void fromObject(final Class<?> type, final Object values, final Object object) {
		if (type == String.class) {
			fromString((String) values, object);
		} else if (type == Double.class) {
			fromDouble((Double) values, object);
		} else if (type == Float.class) {
			fromFloat((Float) values, object);
		} else if (type == Long.class) {
			fromLong((Long) values, object);
		} else if (type == Integer.class) {
			fromInteger((Integer) values, object);
		} else if (type == Short.class) {
			fromShort((Short) values, object);
		} else if (type == Character.class) {
			fromChar((Character) values, object);
		} else if (type == Byte.class) {
			fromByte((Byte) values, object);
		} else if (type == Boolean.class) {
			fromBoolean((Boolean) values, object);
		} else
			throw error("Unsupported type: " + type, values);
	}

	static FieldSetter from(final Field field, final Class<?> type) {
		if (type == String.class) {
			return new StringSetterImpl(field);
		} else if (type == Double.class) {
			return new DoubleSetterImpl(field);
		} else if (type == Float.class) {
			return new FloatSetterImpl(field);
		} else if (type == Long.class) {
			return new LongSetterImpl(field);
		} else if (type == Integer.class) {
			return new IntegerSetterImpl(field);
		} else if (type == Short.class) {
			return new ShortSetterImpl(field);
		} else if (type == Character.class) {
			return new CharSetterImpl(field);
		} else if (type == Byte.class) {
			return new ByteSetterImpl(field);
		} else if (type == Boolean.class) {
			return new BooleanSetterImpl(field);
		} else if (Serializable.class.isAssignableFrom(type))
			return new SerializableSetterImpl(field);
		else
			throw new BinderException("Unsupported type: " + type, field, null);
	}
}
