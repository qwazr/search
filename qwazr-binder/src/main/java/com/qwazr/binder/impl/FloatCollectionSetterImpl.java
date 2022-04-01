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

final public class FloatCollectionSetterImpl extends CollectionSetterAbstract<Float> {

	public FloatCollectionSetterImpl(Field field) {
		super(field);
	}

	@Override
	protected Float fromString(String value) {
		return Float.parseFloat(value);
	}

	@Override
	protected Float fromNumber(Number value) {
		return value.floatValue();
	}

	@Override
	protected Float fromDouble(double value) {
		return (float) value;
	}

	@Override
	protected Float fromFloat(float value) {
		return value;
	}

	@Override
	protected Float fromLong(long value) {
		return (float) value;
	}

	@Override
	protected Float fromShort(short value) {
		return (float) value;
	}

	@Override
	protected Float fromInteger(int value) {
		return (float) value;
	}

	@Override
	protected Float fromChar(Character value) {
		return (float) value;
	}

	@Override
	protected Float fromChar(char value) {
		return (float) value;
	}

	@Override
	protected Float fromByte(byte value) {
		return (float) value;
	}

	@Override
	protected Float fromBoolean(Boolean value) {
		return (float) (value ? 1 : 0);
	}

	@Override
	protected Float fromBoolean(boolean value) {
		return (float) (value ? 1 : 0);
	}

}
