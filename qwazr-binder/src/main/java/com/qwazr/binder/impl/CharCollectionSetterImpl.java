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

final public class CharCollectionSetterImpl extends CollectionSetterAbstract<Character> {

	public CharCollectionSetterImpl(Field field) {
		super(field);
	}

	@Override
	protected Character fromString(String value) {
		return value.isEmpty() ? 0 : value.charAt(0);
	}

	@Override
	protected Character fromNumber(Number value) {
		return (char) value.intValue();
	}

	@Override
	protected Character fromDouble(double value) {
		return (char) value;
	}

	@Override
	protected Character fromFloat(float value) {
		return (char) value;
	}

	@Override
	protected Character fromLong(long value) {
		return (char) value;
	}

	@Override
	protected Character fromShort(short value) {
		return (char) value;
	}

	@Override
	protected Character fromInteger(int value) {
		return (char) value;
	}

	@Override
	protected Character fromChar(Character value) {
		return value;
	}

	@Override
	protected Character fromChar(char value) {
		return (char) value;
	}

	@Override
	protected Character fromByte(byte value) {
		return (char) value;
	}

	@Override
	protected Character fromBoolean(Boolean value) {
		return (char) (value ? 1 : 0);
	}

	@Override
	protected Character fromBoolean(boolean value) {
		return (char) (value ? 1 : 0);
	}

}
