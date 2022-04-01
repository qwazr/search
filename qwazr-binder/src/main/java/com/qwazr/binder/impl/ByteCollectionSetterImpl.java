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

final public class ByteCollectionSetterImpl extends CollectionSetterAbstract<Byte> {

	public ByteCollectionSetterImpl(Field field) {
		super(field);
	}

	@Override
	protected Byte fromString(String value) {
		return Byte.parseByte(value);
	}

	@Override
	protected Byte fromNumber(Number value) {
		return value.byteValue();
	}

	@Override
	protected Byte fromDouble(double value) {
		return (byte) value;
	}

	@Override
	protected Byte fromFloat(float value) {
		return (byte) value;
	}

	@Override
	protected Byte fromLong(long value) {
		return (byte) value;
	}

	@Override
	protected Byte fromShort(short value) {
		return (byte) value;
	}

	@Override
	protected Byte fromInteger(int value) {
		return (byte) value;
	}

	@Override
	protected Byte fromChar(Character value) {
		return (byte) value.charValue();
	}

	@Override
	protected Byte fromChar(char value) {
		return (byte) value;
	}

	@Override
	protected Byte fromByte(byte value) {
		return value;
	}

	@Override
	protected Byte fromBoolean(Boolean value) {
		return (byte) (value ? 1 : 0);
	}

	@Override
	protected Byte fromBoolean(boolean value) {
		return (byte) (value ? 1 : 0);
	}

}
