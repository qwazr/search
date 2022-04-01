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

abstract public class NumberArraySetterAbstract<T> extends ArraySetterAbstract<T> {

	NumberArraySetterAbstract(Field field) {
		super(field);
	}

	protected abstract T fromNumber(Number value);

	@Override
	final protected T fromDouble(Double value) {
		return fromNumber(value);
	}

	@Override
	final protected T fromFloat(Float value) {
		return fromNumber(value);
	}

	@Override
	final protected T fromLong(Long value) {
		return fromNumber(value);
	}

	@Override
	final protected T fromShort(Short value) {
		return fromNumber(value);
	}

	@Override
	final protected T fromInteger(Integer value) {
		return fromNumber(value);
	}

	@Override
	final protected T fromByte(Byte value) {
		return fromNumber(value);
	}

}
