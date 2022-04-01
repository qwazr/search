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
import java.util.List;

public abstract class PrimitiveArraySetterAbstract extends FieldSetterAbstract {

	PrimitiveArraySetterAbstract(Field field) {
		super(field);
	}

	protected abstract void fromNumber(Number number, Object object);

	@Override
	final public void fromDouble(Double value, Object object) {
		fromNumber(value, object);
	}

	@Override
	final public void fromFloat(Float value, Object object) {
		fromNumber(value, object);
	}

	@Override
	final public void fromLong(Long value, Object object) {
		fromNumber(value, object);
	}

	@Override
	final public void fromInteger(Integer value, Object object) {
		fromNumber(value, object);
	}

	@Override
	final public void fromShort(Short value, Object object) {
		fromNumber(value, object);
	}

	@Override
	final public void fromByte(Byte value, Object object) {
		fromNumber(value, object);
	}

	protected abstract void fromNumber(Number[] values, Object object);

	@Override
	final public void fromDouble(Double[] values, Object object) {
		fromNumber(values, object);
	}

	@Override
	final public void fromFloat(Float[] values, Object object) {
		fromNumber(values, object);
	}

	@Override
	final public void fromLong(Long[] values, Object object) {
		fromNumber(values, object);
	}

	@Override
	final public void fromInteger(Integer[] values, Object object) {
		fromNumber(values, object);
	}

	@Override
	final public void fromShort(Short[] values, Object object) {
		fromNumber(values, object);
	}

	@Override
	final public void fromByte(Byte[] values, Object object) {
		fromNumber(values, object);
	}

	@Override
	final public void fromString(List<String> values, Object object) {
		fromString((Collection<String>) values, object);
	}

	protected abstract void fromNumber(Collection<? extends Number> values, Object object);

	@Override
	final public void fromDouble(List<Double> values, Object object) {
		fromNumber(values, object);
	}

	@Override
	final public void fromFloat(List<Float> values, Object object) {
		fromNumber(values, object);
	}

	@Override
	final public void fromLong(List<Long> values, Object object) {
		fromNumber(values, object);
	}

	@Override
	final public void fromInteger(List<Integer> values, Object object) {
		fromNumber(values, object);
	}

	@Override
	final public void fromShort(List<Short> values, Object object) {
		fromNumber(values, object);
	}

	@Override
	final public void fromChar(List<Character> values, Object object) {
		fromChar((Collection<Character>) values, object);
	}

	@Override
	final public void fromByte(List<Byte> values, Object object) {
		fromNumber(values, object);
	}

	@Override
	final public void fromBoolean(List<Boolean> values, Object object) {
		fromBoolean((Collection<Boolean>) values, object);
	}

	@Override
	final public void fromDouble(Collection<Double> values, Object object) {
		fromNumber(values, object);
	}

	@Override
	final public void fromFloat(Collection<Float> values, Object object) {
		fromNumber(values, object);
	}

	@Override
	final public void fromLong(Collection<Long> values, Object object) {
		fromNumber(values, object);
	}

	@Override
	final public void fromInteger(Collection<Integer> values, Object object) {
		fromNumber(values, object);
	}

	@Override
	final public void fromShort(Collection<Short> values, Object object) {
		fromNumber(values, object);
	}

	@Override
	final public void fromByte(Collection<Byte> values, Object object) {
		fromNumber(values, object);
	}

}
