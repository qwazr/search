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

abstract class SingleSetterAbstract extends FieldSetterAbstract {

	SingleSetterAbstract(final Field field) {
		super(field);
	}

	@Override
	final public void fromDouble(double[] values, Object object) {
		switch (values.length) {
		case 0:
			fromNull(object);
			break;
		case 1:
			fromDouble(values[0], object);
			break;
		default:
			throw error("Cannot add more than 1 one value", values);
		}
	}

	@Override
	final public void fromFloat(float[] values, Object object) {
		switch (values.length) {
		case 0:
			fromNull(object);
			break;
		case 1:
			fromFloat(values[0], object);
			break;
		default:
			throw error("Cannot add more than 1 one value", values);
		}
	}

	@Override
	final public void fromLong(long[] values, Object object) {
		switch (values.length) {
		case 0:
			fromNull(object);
			break;
		case 1:
			fromLong(values[0], object);
			break;
		default:
			throw error("Cannot add more than 1 one value", values);
		}
	}

	@Override
	final public void fromInteger(int[] values, Object object) {
		switch (values.length) {
		case 0:
			fromNull(object);
			break;
		case 1:
			fromInteger(values[0], object);
			break;
		default:
			throw error("Cannot add more than 1 one value", values);
		}
	}

	@Override
	final public void fromShort(short[] values, Object object) {
		switch (values.length) {
		case 0:
			fromNull(object);
			break;
		case 1:
			fromShort(values[0], object);
			break;
		default:
			throw error("Cannot add more than 1 one value", values);
		}
	}

	@Override
	final public void fromChar(char[] values, Object object) {
		switch (values.length) {
		case 0:
			fromNull(object);
			break;
		case 1:
			fromChar(values[0], object);
			break;
		default:
			throw error("Cannot add more than 1 one value", values);
		}
	}

	@Override
	final public void fromByte(byte[] values, Object object) {
		switch (values.length) {
		case 0:
			fromNull(object);
			break;
		case 1:
			fromByte(values[0], object);
			break;
		default:
			throw error("Cannot add more than 1 one value", values);
		}
	}

	@Override
	final public void fromBoolean(boolean[] values, Object object) {
		switch (values.length) {
		case 0:
			fromNull(object);
			break;
		case 1:
			fromBoolean(values[0], object);
			break;
		default:
			throw error("Cannot add more than 1 one value", values);
		}
	}
}
