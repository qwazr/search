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

final public class DoubleArraySetterImpl extends NumberArraySetterAbstract<Double> {

	public DoubleArraySetterImpl(Field field) {
		super(field);
	}

	@Override
	protected Double fromNumber(Number value) {
		return value.doubleValue();
	}

	@Override
	protected Double fromString(String value) {
		return Double.parseDouble(value);
	}

	@Override
	protected Double fromDouble(double value) {
		return value;
	}

	@Override
	protected Double fromFloat(float value) {
		return (double) value;
	}

	@Override
	protected Double fromLong(long value) {
		return (double) value;
	}

	@Override
	protected Double fromShort(short value) {
		return (double) value;
	}

	@Override
	protected Double fromInteger(int value) {
		return (double) value;
	}

	@Override
	protected Double fromChar(Character value) {
		return (double) value;
	}

	@Override
	protected Double fromChar(char value) {
		return (double) value;
	}

	@Override
	protected Double fromByte(byte value) {
		return (double) value;
	}

	@Override
	protected Double fromBoolean(Boolean value) {
		return value ? 1d : 0;
	}

	@Override
	protected Double fromBoolean(boolean value) {
		return value ? 1d : 0;
	}

	@Override
	protected Double[] newArray(int size) {
		return new Double[size];
	}

}
