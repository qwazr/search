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

final public class IntegerArraySetterImpl extends NumberArraySetterAbstract<Integer> {

	public IntegerArraySetterImpl(Field field) {
		super(field);
	}

	@Override
	protected Integer fromNumber(Number value) {
		return value.intValue();
	}

	@Override
	protected Integer fromString(String value) {
		return Integer.parseInt(value);
	}

	@Override
	protected Integer fromDouble(double value) {
		return (int) value;
	}

	@Override
	protected Integer fromFloat(float value) {
		return (int) value;
	}

	@Override
	protected Integer fromLong(long value) {
		return (int) value;
	}

	@Override
	protected Integer fromShort(short value) {
		return (int) value;
	}

	@Override
	protected Integer fromInteger(int value) {
		return value;
	}

	@Override
	protected Integer fromChar(Character value) {
		return (int) value;
	}

	@Override
	protected Integer fromChar(char value) {
		return (int) value;
	}

	@Override
	protected Integer fromByte(byte value) {
		return (int) value;
	}

	@Override
	protected Integer fromBoolean(Boolean value) {
		return value ? 1 : 0;
	}

	@Override
	protected Integer fromBoolean(boolean value) {
		return value ? 1 : 0;
	}

	@Override
	protected Integer[] newArray(int size) {
		return new Integer[size];
	}

}
