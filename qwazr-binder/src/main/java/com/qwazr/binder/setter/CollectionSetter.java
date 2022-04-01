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
import com.qwazr.binder.impl.BooleanCollectionSetterImpl;
import com.qwazr.binder.impl.ByteCollectionSetterImpl;
import com.qwazr.binder.impl.CharCollectionSetterImpl;
import com.qwazr.binder.impl.DoubleCollectionSetterImpl;
import com.qwazr.binder.impl.FloatCollectionSetterImpl;
import com.qwazr.binder.impl.IntegerCollectionSetterImpl;
import com.qwazr.binder.impl.LongCollectionSetterImpl;
import com.qwazr.binder.impl.ObjectCollectionSetterImpl;
import com.qwazr.binder.impl.ShortCollectionSetterImpl;
import com.qwazr.binder.impl.StringCollectionSetterImpl;

import java.lang.reflect.Field;
import java.util.Collection;

public interface CollectionSetter extends ErrorSetter {

	default void fromString(Collection<String> values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromDouble(Collection<Double> values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromFloat(Collection<Float> values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromLong(Collection<Long> values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromInteger(Collection<Integer> values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromShort(Collection<Short> values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromChar(Collection<Character> values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromByte(Collection<Byte> values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromBoolean(Collection<Boolean> values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromObject(Collection<Object> values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromCollection(Class<?> type, Collection<?> values, Object object) {
		if (type == String.class)
			fromString((Collection<String>) values, object);
		else if (type == Double.class)
			fromDouble((Collection<Double>) values, object);
		else if (type == Float.class)
			fromFloat((Collection<Float>) values, object);
		else if (type == Long.class)
			fromLong((Collection<Long>) values, object);
		else if (type == Integer.class)
			fromInteger((Collection<Integer>) values, object);
		else if (type == Short.class)
			fromShort((Collection<Short>) values, object);
		else if (type == Character.class)
			fromChar((Collection<Character>) values, object);
		else if (type == Byte.class)
			fromByte((Collection<Byte>) values, object);
		else if (type == Boolean.class)
			fromBoolean((Collection<Boolean>) values, object);
		else
			fromObject((Collection<Object>) values, object);
	}

	static FieldSetter from(final Field field, final Class<?> type) {
		if (type == String.class) {
			return new StringCollectionSetterImpl(field);
		} else if (type == Double.class) {
			return new DoubleCollectionSetterImpl(field);
		} else if (type == Float.class) {
			return new FloatCollectionSetterImpl(field);
		} else if (type == Long.class) {
			return new LongCollectionSetterImpl(field);
		} else if (type == Integer.class) {
			return new IntegerCollectionSetterImpl(field);
		} else if (type == Short.class) {
			return new ShortCollectionSetterImpl(field);
		} else if (type == Character.class) {
			return new CharCollectionSetterImpl(field);
		} else if (type == Byte.class) {
			return new ByteCollectionSetterImpl(field);
		} else if (type == Boolean.class) {
			return new BooleanCollectionSetterImpl(field);
		} else if (type == Object.class) {
			return new ObjectCollectionSetterImpl(field);
		} else
			throw new BinderException("Unsupported type: " + type, field, null);

	}
}
