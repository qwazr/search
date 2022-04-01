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

final public class ShortSetterImpl extends ObjectSetterAbstract {

	public ShortSetterImpl(Field field) {
		super(field);
	}

	@Override
	public void fromString(String value, Object object) {
		set(object, Short.parseShort(value));
	}

	@Override
	public void fromDouble(Double value, Object object) {
		set(object, value.shortValue());
	}

	@Override
	public void fromFloat(Float value, Object object) {
		set(object, value.shortValue());
	}

	@Override
	public void fromLong(Long value, Object object) {
		set(object, value.shortValue());
	}

	@Override
	public void fromInteger(Integer value, Object object) {
		set(object, value.shortValue());
	}

	@Override
	public void fromShort(Short value, Object object) {
		set(object, value);
	}

	@Override
	public void fromChar(Character value, Object object) {
		set(object, (short) (int) value);
	}

	@Override
	public void fromByte(Byte value, Object object) {
		set(object, value.shortValue());
	}

	@Override
	public void fromBoolean(Boolean value, Object object) {
		set(object, (short) (value ? 1 : 0));
	}

	@Override
	public void fromDouble(double value, Object object) {
		set(object, (short) value);
	}

	@Override
	public void fromFloat(float value, Object object) {
		set(object, (short) value);
	}

	@Override
	public void fromLong(long value, Object object) {
		set(object, (short) value);
	}

	@Override
	public void fromInteger(int value, Object object) {
		set(object, (short) value);
	}

	@Override
	public void fromShort(short value, Object object) {
		set(object, value);
	}

	@Override
	public void fromChar(char value, Object object) {
		set(object, (short) value);
	}

	@Override
	public void fromByte(byte value, Object object) {
		set(object, (short) value);
	}

	@Override
	public void fromBoolean(boolean value, Object object) {
		set(object, (short) (value ? 1 : 0));
	}
}
