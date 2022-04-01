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

final public class ShortCollectionSetterImpl extends CollectionSetterAbstract<Short> {

	public ShortCollectionSetterImpl(Field field) {
		super(field);
	}

	@Override
	protected Short fromString(String value) {
		return Short.parseShort(value);
	}

	@Override
	protected Short fromNumber(Number value) {
		return value.shortValue();
	}

	@Override
	protected Short fromDouble(double value) {
		return (short) value;
	}

	@Override
	protected Short fromFloat(float value) {
		return (short) value;
	}

	@Override
	protected Short fromLong(long value) {
		return (short) value;
	}

	@Override
	protected Short fromShort(short value) {
		return value;
	}

	@Override
	protected Short fromInteger(int value) {
		return (short) value;
	}

	@Override
	protected Short fromChar(Character value) {
		return (short) value.charValue();
	}

	@Override
	protected Short fromChar(char value) {
		return (short) value;
	}

	@Override
	protected Short fromByte(byte value) {
		return (short) value;
	}

	@Override
	protected Short fromBoolean(Boolean value) {
		return (short) (value ? 1 : 0);
	}

	@Override
	protected Short fromBoolean(boolean value) {
		return (short) (value ? 1 : 0);
	}
	
}
