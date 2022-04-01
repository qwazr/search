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

final public class ObjectCollectionSetterImpl extends CollectionSetterAbstract<Object> {

	public ObjectCollectionSetterImpl(Field field) {
		super(field);
	}

	@Override
	protected Object fromString(String value) {
		return value;
	}

	@Override
	protected Object fromNumber(Number value) {
		return value;
	}

	@Override
	protected Object fromDouble(double value) {
		return value;
	}

	@Override
	protected Object fromFloat(float value) {
		return value;
	}

	@Override
	protected Object fromLong(long value) {
		return value;
	}

	@Override
	protected Object fromShort(short value) {
		return value;
	}

	@Override
	protected Object fromInteger(int value) {
		return value;
	}

	@Override
	protected Object fromChar(Character value) {
		return value;
	}

	@Override
	protected Object fromChar(char value) {
		return value;
	}

	@Override
	protected Object fromByte(byte value) {
		return value;
	}

	@Override
	protected Object fromBoolean(Boolean value) {
		return value;
	}

	@Override
	protected Object fromBoolean(boolean value) {
		return value;
	}

}
