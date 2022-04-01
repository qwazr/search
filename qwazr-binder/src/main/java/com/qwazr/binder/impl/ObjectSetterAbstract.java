/*
 * Copyright 2017 Emmanuel Keller / QWAZR
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
package com.qwazr.binder.impl;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

abstract class ObjectSetterAbstract extends SingleSetterAbstract {

	ObjectSetterAbstract(Field field) {
		super(field);
	}

	final <T> void fromObjectArray(final T[] values, final Object object, final BiConsumer<T, Object> consumer) {
		if (values.length == 0)
			fromNull(object);
		else if (values.length == 1)
			consumer.accept(values[0], object);
		else
			throw error("Cannot set more than 1 value", values);
	}

	final <T> void fromObjectCollection(final Collection<T> values, final Object object,
			final BiConsumer<T, Object> consumer) {
		switch (values.size()) {
		case 0:
			fromNull(object);
			break;
		case 1:
			consumer.accept(values.iterator().next(), object);
			break;
		default:
			throw error("Cannot set more than 1 value", values);
		}
	}

	final <T> void fromObjectList(final List<T> values, final Object object, final BiConsumer<T, Object> consumer) {
		switch (values.size()) {
		case 0:
			fromNull(object);
			break;
		case 1:
			consumer.accept(values.get(0), object);
			break;
		default:
			throw error("Cannot set more than 1 value", values);
		}
	}

	@Override
	final public void fromString(String[] values, Object object) {
		fromObjectArray(values, object, this::fromString);
	}

	@Override
	final public void fromDouble(Double[] values, Object object) {
		fromObjectArray(values, object, this::fromDouble);
	}

	@Override
	final public void fromFloat(Float[] values, Object object) {
		fromObjectArray(values, object, this::fromFloat);
	}

	@Override
	final public void fromLong(Long[] values, Object object) {
		fromObjectArray(values, object, this::fromLong);
	}

	@Override
	final public void fromInteger(Integer[] values, Object object) {
		fromObjectArray(values, object, this::fromInteger);
	}

	@Override
	final public void fromShort(Short[] values, Object object) {
		fromObjectArray(values, object, this::fromShort);
	}

	@Override
	final public void fromChar(Character[] values, Object object) {
		fromObjectArray(values, object, this::fromChar);
	}

	@Override
	final public void fromByte(Byte[] values, Object object) {
		fromObjectArray(values, object, this::fromByte);
	}

	@Override
	final public void fromBoolean(Boolean[] values, Object object) {
		fromObjectArray(values, object, this::fromBoolean);
	}

	@Override
	final public void fromString(Collection<String> values, Object object) {
		fromObjectCollection(values, object, this::fromString);
	}

	@Override
	final public void fromDouble(Collection<Double> values, Object object) {
		fromObjectCollection(values, object, this::fromDouble);
	}

	@Override
	final public void fromFloat(Collection<Float> values, Object object) {
		fromObjectCollection(values, object, this::fromFloat);
	}

	@Override
	final public void fromLong(Collection<Long> values, Object object) {
		fromObjectCollection(values, object, this::fromLong);
	}

	@Override
	final public void fromInteger(Collection<Integer> values, Object object) {
		fromObjectCollection(values, object, this::fromInteger);
	}

	@Override
	final public void fromShort(Collection<Short> values, Object object) {
		fromObjectCollection(values, object, this::fromShort);
	}

	@Override
	final public void fromChar(Collection<Character> values, Object object) {
		fromObjectCollection(values, object, this::fromChar);
	}

	@Override
	final public void fromByte(Collection<Byte> values, Object object) {
		fromObjectCollection(values, object, this::fromByte);
	}

	@Override
	final public void fromBoolean(Collection<Boolean> values, Object object) {
		fromObjectCollection(values, object, this::fromBoolean);
	}

	@Override
	final public void fromObject(Collection<Object> values, Object object) {
		fromObjectCollection(values, object, (v, o) -> set(o, v));
	}

	@Override
	final public void fromString(List<String> values, Object object) {
		fromObjectList(values, object, this::fromString);
	}

	@Override
	final public void fromDouble(List<Double> values, Object object) {
		fromObjectList(values, object, this::fromDouble);
	}

	@Override
	final public void fromFloat(List<Float> values, Object object) {
		fromObjectList(values, object, this::fromFloat);
	}

	@Override
	final public void fromLong(List<Long> values, Object object) {
		fromObjectList(values, object, this::fromLong);
	}

	@Override
	final public void fromInteger(List<Integer> values, Object object) {
		fromObjectList(values, object, this::fromInteger);
	}

	@Override
	final public void fromShort(List<Short> values, Object object) {
		fromObjectList(values, object, this::fromShort);
	}

	@Override
	final public void fromChar(List<Character> values, Object object) {
		fromObjectList(values, object, this::fromChar);
	}

	@Override
	final public void fromByte(List<Byte> values, Object object) {
		fromObjectList(values, object, this::fromByte);
	}

	@Override
	final public void fromBoolean(List<Boolean> values, Object object) {
		fromObjectList(values, object, this::fromBoolean);
	}

}
