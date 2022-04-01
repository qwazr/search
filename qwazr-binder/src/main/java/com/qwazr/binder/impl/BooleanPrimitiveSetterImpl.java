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

final public class BooleanPrimitiveSetterImpl extends ObjectSetterAbstract {

	public BooleanPrimitiveSetterImpl(Field field) {
		super(field);
	}

	@Override
	public void fromNull(Object object) {
	}

	@Override
	public void fromString(String value, Object object) {
		setBoolean(object, Boolean.parseBoolean(value));
	}

	@Override
	public void fromDouble(Double value, Object object) {
		setBoolean(object, value != 0);
	}

	@Override
	public void fromFloat(Float value, Object object) {
		setBoolean(object, value != 0);
	}

	@Override
	public void fromLong(Long value, Object object) {
		setBoolean(object, value != 0);
	}

	@Override
	public void fromInteger(Integer value, Object object) {
		setBoolean(object, value != 0);
	}

	@Override
	public void fromShort(Short value, Object object) {
		setBoolean(object, value != 0);
	}

	@Override
	public void fromChar(Character value, Object object) {
		setBoolean(object, value != 0);
	}

	@Override
	public void fromByte(Byte value, Object object) {
		setBoolean(object, value != 0);
	}

	@Override
	public void fromBoolean(Boolean value, Object object) {
		setBoolean(object, value);
	}

	@Override
	public void fromDouble(double value, Object object) {
		setBoolean(object, value != 0);
	}

	@Override
	public void fromFloat(float value, Object object) {
		setBoolean(object, value != 0);
	}

	@Override
	public void fromLong(long value, Object object) {
		setBoolean(object, value != 0);
	}

	@Override
	public void fromInteger(int value, Object object) {
		setBoolean(object, value != 0);
	}

	@Override
	public void fromShort(short value, Object object) {
		setBoolean(object, value != 0);
	}

	@Override
	public void fromChar(char value, Object object) {
		setBoolean(object, value != 0);
	}

	@Override
	public void fromByte(byte value, Object object) {
		setBoolean(object, value != 0);
	}

	@Override
	public void fromBoolean(boolean value, Object object) {
		setBoolean(object, value);
	}
}
