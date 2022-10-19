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

final public class LongArraySetterImpl extends NumberArraySetterAbstract<Long> {

	public LongArraySetterImpl(Field field) {
		super(field);
	}

	@Override
	protected Long fromNumber(Number value) {
		return value.longValue();
	}

	@Override
	protected Long fromString(String value) {
		return Long.parseLong(value);
	}

	@Override
	protected Long fromDouble(double value) {
		return (long) value;
	}

	@Override
	protected Long fromFloat(float value) {
		return (long) value;
	}

	@Override
	protected Long fromLong(long value) {
		return value;
	}

	@Override
	protected Long fromShort(short value) {
		return (long) value;
	}

	@Override
	protected Long fromInteger(int value) {
		return (long) value;
	}

	@Override
	protected Long fromChar(Character value) {
		return (long) value;
	}

	@Override
	protected Long fromChar(char value) {
		return (long) value;
	}

	@Override
	protected Long fromByte(byte value) {
		return (long) value;
	}

	@Override
	protected Long fromBoolean(Boolean value) {
		return value ? 1L : 0;
	}

	@Override
	protected Long fromBoolean(boolean value) {
		return value ? 1L : 0;
	}

	@Override
	protected Long[] newArray(int size) {
		return new Long[size];
	}

}