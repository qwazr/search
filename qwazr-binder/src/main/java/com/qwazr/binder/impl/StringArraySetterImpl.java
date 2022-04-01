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

final public class StringArraySetterImpl extends ArraySetterAbstract<String> {

	public StringArraySetterImpl(Field field) {
		super(field);
	}

	@Override
	protected String fromString(String value) {
		return value;
	}

	@Override
	protected String fromDouble(Double value) {
		return value.toString();
	}

	@Override
	protected String fromDouble(double value) {
		return Double.toString(value);
	}

	@Override
	protected String fromFloat(Float value) {
		return value.toString();
	}

	@Override
	protected String fromFloat(float value) {
		return Float.toString(value);
	}

	@Override
	protected String fromLong(Long value) {
		return value.toString();
	}

	@Override
	protected String fromLong(long value) {
		return Long.toString(value);
	}

	@Override
	protected String fromShort(Short value) {
		return value.toString();
	}

	@Override
	protected String fromShort(short value) {
		return Short.toString(value);
	}

	@Override
	protected String fromInteger(Integer value) {
		return value.toString();
	}

	@Override
	protected String fromInteger(int value) {
		return Integer.toString(value);
	}

	@Override
	protected String fromChar(Character value) {
		return value.toString();
	}

	@Override
	protected String fromChar(char value) {
		return Character.toString(value);
	}

	@Override
	protected String fromByte(Byte value) {
		return value.toString();
	}

	@Override
	protected String fromByte(byte value) {
		return Byte.toString(value);
	}

	@Override
	protected String fromBoolean(Boolean value) {
		return value.toString();
	}

	@Override
	protected String fromBoolean(boolean value) {
		return Boolean.toString(value);
	}

	@Override
	protected String[] newArray(int size) {
		return new String[size];
	}

}
