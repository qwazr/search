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

final public class BooleanCollectionSetterImpl extends CollectionSetterAbstract<Boolean> {

	public BooleanCollectionSetterImpl(Field field) {
		super(field);
	}

	@Override
	protected Boolean fromString(String value) {
		return Boolean.parseBoolean(value);
	}

	@Override
	protected Boolean fromNumber(Number value) {
		return value.doubleValue() != 0;
	}

	@Override
	protected Boolean fromDouble(double value) {
		return value != 0;
	}

	@Override
	protected Boolean fromFloat(float value) {
		return value != 0;
	}

	@Override
	protected Boolean fromLong(long value) {
		return value != 0;
	}

	@Override
	protected Boolean fromShort(short value) {
		return value != 0;
	}

	@Override
	protected Boolean fromInteger(int value) {
		return value != 0;
	}

	@Override
	protected Boolean fromChar(Character value) {
		return value != 0;
	}

	@Override
	protected Boolean fromChar(char value) {
		return value != 0;
	}

	@Override
	protected Boolean fromByte(byte value) {
		return value != 0;
	}

	@Override
	protected Boolean fromBoolean(Boolean value) {
		return value;
	}

	@Override
	protected Boolean fromBoolean(boolean value) {
		return value;
	}

}
