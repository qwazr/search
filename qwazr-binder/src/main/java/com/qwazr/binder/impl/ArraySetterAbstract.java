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
import java.util.function.Function;

public abstract class ArraySetterAbstract<T> extends FieldSetterAbstract {

	ArraySetterAbstract(Field field) {
		super(field);
	}

	protected abstract T fromString(String value);

	protected abstract T fromDouble(Double value);

	protected abstract T fromDouble(double value);

	protected abstract T fromFloat(Float value);

	protected abstract T fromFloat(float value);

	protected abstract T fromLong(Long value);

	protected abstract T fromLong(long value);

	protected abstract T fromShort(Short value);

	protected abstract T fromShort(short value);

	protected abstract T fromInteger(Integer value);

	protected abstract T fromInteger(int value);

	protected abstract T fromChar(Character value);

	protected abstract T fromChar(char value);

	protected abstract T fromByte(Byte value);

	protected abstract T fromByte(byte value);

	protected abstract T fromBoolean(Boolean value);

	protected abstract T fromBoolean(boolean value);

	protected abstract T[] newArray(int size);

	private void createArray(Object object, T... values) {
		set(object, values);
	}

	@Override
	final public void fromString(String value, Object object) {
		createArray(object, fromString(value));
	}

	@Override
	final public void fromDouble(Double value, Object object) {
		createArray(object, fromDouble(value));
	}

	@Override
	final public void fromFloat(Float value, Object object) {
		createArray(object, fromFloat(value));
	}

	@Override
	final public void fromLong(Long value, Object object) {
		createArray(object, fromLong(value));
	}

	@Override
	final public void fromInteger(Integer value, Object object) {
		createArray(object, fromInteger(value));
	}

	@Override
	final public void fromShort(Short value, Object object) {
		createArray(object, fromShort(value));
	}

	@Override
	final public void fromChar(Character value, Object object) {
		createArray(object, fromChar(value));
	}

	@Override
	final public void fromByte(Byte value, Object object) {
		createArray(object, fromByte(value));
	}

	@Override
	final public void fromBoolean(Boolean value, Object object) {
		createArray(object, fromBoolean(value));
	}

	@Override
	final public void fromDouble(double value, Object object) {
		createArray(object, fromDouble(value));
	}

	@Override
	final public void fromFloat(float value, Object object) {
		createArray(object, fromFloat(value));
	}

	@Override
	final public void fromLong(long value, Object object) {
		createArray(object, fromLong(value));
	}

	@Override
	final public void fromInteger(int value, Object object) {
		createArray(object, fromInteger(value));
	}

	@Override
	final public void fromShort(short value, Object object) {
		createArray(object, fromShort(value));
	}

	@Override
	final public void fromChar(char value, Object object) {
		createArray(object, fromChar(value));
	}

	@Override
	final public void fromByte(byte value, Object object) {
		createArray(object, fromByte(value));
	}

	@Override
	final public void fromBoolean(boolean value, Object object) {
		createArray(object, fromBoolean(value));
	}

	private <V> void fromArray(final V[] values, final Object object, final Function<V, T> converter) {
		final T[] array = newArray((values.length));
		int i = 0;
		for (V value : values)
			array[i++] = converter.apply(value);
		set(object, array);
	}

	@Override
	final public void fromString(String[] values, Object object) {
		fromArray(values, object, this::fromString);
	}

	@Override
	final public void fromDouble(Double[] values, Object object) {
		fromArray(values, object, this::fromDouble);
	}

	@Override
	final public void fromFloat(Float[] values, Object object) {
		fromArray(values, object, this::fromFloat);
	}

	@Override
	final public void fromLong(Long[] values, Object object) {
		fromArray(values, object, this::fromLong);
	}

	@Override
	final public void fromInteger(Integer[] values, Object object) {
		fromArray(values, object, this::fromInteger);
	}

	@Override
	final public void fromShort(Short[] values, Object object) {
		fromArray(values, object, this::fromShort);
	}

	@Override
	final public void fromChar(Character[] values, Object object) {
		fromArray(values, object, this::fromChar);
	}

	@Override
	final public void fromByte(Byte[] values, Object object) {
		fromArray(values, object, this::fromByte);
	}

	@Override
	final public void fromBoolean(Boolean[] values, Object object) {
		fromArray(values, object, this::fromBoolean);
	}

	private <V> void fromCollection(final Collection<V> values, final Object object, final Function<V, T> converter) {
		final T[] array = newArray((values.size()));
		int i = 0;
		for (V value : values)
			array[i++] = converter.apply(value);
		set(object, array);
	}

	@Override
	final public void fromObject(final Collection<Object> values, final Object object) {
		final T[] array = newArray((values.size()));
		int i = 0;
		for (Object value : values)
			array[i++] = (T) value;
		set(object, array);
	}

	@Override
	final public void fromString(List<String> values, Object object) {
		fromCollection(values, object, this::fromString);
	}

	@Override
	final public void fromDouble(List<Double> values, Object object) {
		fromCollection(values, object, this::fromDouble);
	}

	@Override
	final public void fromFloat(List<Float> values, Object object) {
		fromCollection(values, object, this::fromFloat);
	}

	@Override
	final public void fromLong(List<Long> values, Object object) {
		fromCollection(values, object, this::fromLong);
	}

	@Override
	final public void fromInteger(List<Integer> values, Object object) {
		fromCollection(values, object, this::fromInteger);
	}

	@Override
	final public void fromShort(List<Short> values, Object object) {
		fromCollection(values, object, this::fromShort);
	}

	@Override
	final public void fromChar(List<Character> values, Object object) {
		fromCollection(values, object, this::fromChar);
	}

	@Override
	final public void fromByte(List<Byte> values, Object object) {
		fromCollection(values, object, this::fromByte);
	}

	@Override
	final public void fromBoolean(List<Boolean> values, Object object) {
		fromCollection(values, object, this::fromBoolean);
	}

	@Override
	final public void fromString(Collection<String> values, Object object) {
		fromCollection(values, object, this::fromString);
	}

	@Override
	final public void fromDouble(Collection<Double> values, Object object) {
		fromCollection(values, object, this::fromDouble);
	}

	@Override
	final public void fromFloat(Collection<Float> values, Object object) {
		fromCollection(values, object, this::fromFloat);
	}

	@Override
	final public void fromLong(Collection<Long> values, Object object) {
		fromCollection(values, object, this::fromLong);
	}

	@Override
	final public void fromInteger(Collection<Integer> values, Object object) {
		fromCollection(values, object, this::fromInteger);
	}

	@Override
	final public void fromShort(Collection<Short> values, Object object) {
		fromCollection(values, object, this::fromShort);
	}

	@Override
	final public void fromChar(Collection<Character> values, Object object) {
		fromCollection(values, object, this::fromChar);
	}

	@Override
	final public void fromByte(Collection<Byte> values, Object object) {
		fromCollection(values, object, this::fromByte);
	}

	@Override
	final public void fromBoolean(Collection<Boolean> values, Object object) {
		fromCollection(values, object, this::fromBoolean);
	}

	@Override
	final public void fromDouble(double[] values, Object object) {
		final T[] array = newArray(values.length);
		int i = 0;
		for (double value : values)
			array[i++] = fromDouble(value);
		set(object, array);
	}

	@Override
	final public void fromFloat(float[] values, Object object) {
		final T[] array = newArray(values.length);
		int i = 0;
		for (float value : values)
			array[i++] = fromFloat(value);
		set(object, array);
	}

	@Override
	final public void fromLong(long[] values, Object object) {
		final T[] array = newArray(values.length);
		int i = 0;
		for (long value : values)
			array[i++] = fromLong(value);
		set(object, array);
	}

	@Override
	final public void fromInteger(int[] values, Object object) {
		final T[] array = newArray(values.length);
		int i = 0;
		for (int value : values)
			array[i++] = fromInteger(value);
		set(object, array);
	}

	@Override
	final public void fromShort(short[] values, Object object) {
		final T[] array = newArray(values.length);
		int i = 0;
		for (short value : values)
			array[i++] = fromShort(value);
		set(object, array);
	}

	@Override
	final public void fromChar(char[] values, Object object) {
		final T[] array = newArray(values.length);
		int i = 0;
		for (char value : values)
			array[i++] = fromChar(value);
		set(object, array);
	}

	@Override
	final public void fromByte(byte[] values, Object object) {
		final T[] array = newArray(values.length);
		int i = 0;
		for (byte value : values)
			array[i++] = fromByte(value);
		set(object, array);
	}

	@Override
	final public void fromBoolean(boolean[] values, Object object) {
		final T[] array = newArray(values.length);
		int i = 0;
		for (boolean value : values)
			array[i++] = fromBoolean(value);
		set(object, array);
	}

}
