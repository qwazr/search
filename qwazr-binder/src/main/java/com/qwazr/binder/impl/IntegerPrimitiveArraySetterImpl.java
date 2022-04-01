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

final public class IntegerPrimitiveArraySetterImpl extends PrimitiveArraySetterAbstract {

	public IntegerPrimitiveArraySetterImpl(Field field) {
		super(field);
	}

	@Override
	public void fromString(String value, Object object) {
		set(object, new int[] { Integer.parseInt(value) });
	}

	@Override
	final protected void fromNumber(Number number, Object object) {
		set(object, new int[] { number.intValue() });
	}

	@Override
	public void fromChar(Character value, Object object) {
		set(object, new int[] { (int) value });
	}

	@Override
	public void fromBoolean(Boolean value, Object object) {
		set(object, new int[] { value ? 1 : 0 });
	}

	@Override
	public void fromDouble(double value, Object object) {
		set(object, new int[] { (int) value });
	}

	@Override
	public void fromFloat(float value, Object object) {
		set(object, new int[] { (int) value });
	}

	@Override
	public void fromLong(long value, Object object) {
		set(object, new int[] { (int) value });
	}

	@Override
	public void fromInteger(int value, Object object) {
		set(object, new int[] { value });
	}

	@Override
	public void fromShort(short value, Object object) {
		set(object, new int[] { (int) value });
	}

	@Override
	public void fromChar(char value, Object object) {
		set(object, new int[] { (int) value });
	}

	@Override
	public void fromByte(byte value, Object object) {
		set(object, new int[] { (int) value });
	}

	@Override
	public void fromBoolean(boolean value, Object object) {
		set(object, new int[] { value ? 1 : 0 });
	}

	@Override
	protected void fromNumber(Number[] values, Object object) {
		final int[] array = new int[values.length];
		int i = 0;
		for (Number v : values)
			array[i++] = v.intValue();
		set(object, array);
	}

	@Override
	public void fromString(String[] values, Object object) {
		final int[] array = new int[values.length];
		int i = 0;
		for (String v : values)
			array[i++] = Integer.parseInt(v);
		set(object, array);
	}

	@Override
	public void fromChar(Character[] values, Object object) {
		final int[] array = new int[values.length];
		int i = 0;
		for (Character v : values)
			array[i++] = (int) v;
		set(object, array);
	}

	@Override
	public void fromBoolean(Boolean[] values, Object object) {
		final int[] array = new int[values.length];
		int i = 0;
		for (Boolean v : values)
			array[i++] = v ? 1 : 0;
		set(object, array);
	}

	@Override
	protected void fromNumber(Collection<? extends Number> values, Object object) {
		final int[] array = new int[values.size()];
		int i = 0;
		for (Number v : values)
			array[i++] = v.intValue();
		set(object, array);
	}

	@Override
	public void fromString(Collection<String> values, Object object) {
		final int[] array = new int[values.size()];
		int i = 0;
		for (String v : values)
			array[i++] = Byte.parseByte(v);
		set(object, array);
	}

	@Override
	public void fromChar(Collection<Character> values, Object object) {
		final int[] array = new int[values.size()];
		int i = 0;
		for (Character v : values)
			array[i++] = (int) v;
		set(object, array);
	}

	@Override
	public void fromBoolean(Collection<Boolean> values, Object object) {
		final int[] array = new int[values.size()];
		int i = 0;
		for (Boolean v : values)
			array[i++] = v ? 1 : 0;
		set(object, array);
	}

	@Override
	public void fromDouble(double[] values, Object object) {
		final int[] array = new int[values.length];
		int i = 0;
		for (double v : values)
			array[i++] = (int) v;
		set(object, array);
	}

	@Override
	public void fromFloat(float[] values, Object object) {
		final int[] array = new int[values.length];
		int i = 0;
		for (float v : values)
			array[i++] = (int) v;
		set(object, array);
	}

	@Override
	public void fromLong(long[] values, Object object) {
		final int[] array = new int[values.length];
		int i = 0;
		for (long v : values)
			array[i++] = (int) v;
		set(object, array);
	}

	@Override
	public void fromInteger(int[] values, Object object) {
		final int[] array = new int[values.length];
		int i = 0;
		for (int v : values)
			array[i++] = v;
		set(object, array);
	}

	@Override
	public void fromShort(short[] values, Object object) {
		final int[] array = new int[values.length];
		int i = 0;
		for (short v : values)
			array[i++] = (int) v;
		set(object, array);
	}

	@Override
	public void fromChar(char[] values, Object object) {
		final int[] array = new int[values.length];
		int i = 0;
		for (char v : values)
			array[i++] = (int) v;
		set(object, array);
	}

	@Override
	public void fromByte(byte[] values, Object object) {
		final int[] array = new int[values.length];
		int i = 0;
		for (byte v : values)
			array[i++] = (int) v;
		set(object, array);
	}

	@Override
	public void fromBoolean(boolean[] values, Object object) {
		final int[] array = new int[values.length];
		int i = 0;
		for (boolean v : values)
			array[i++] = v ? 1 : 0;
		set(object, array);
	}

	@Override
	public void fromObject(final Collection<Object> values, final Object object) {
		final int[] array = new int[values.size()];
		int i = 0;
		for (Object v : values)
			array[i++] = (int) v;
		set(object, array);
	}
}
