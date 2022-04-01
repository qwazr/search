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

import com.qwazr.binder.BinderException;
import com.qwazr.binder.setter.FieldSetter;

import java.lang.reflect.Field;

abstract class FieldSetterAbstract implements FieldSetter {

	final Field field;
	final Class<?> type;

	FieldSetterAbstract(final Field field) {
		this.field = field;
		this.type = field.getType();
		field.setAccessible(true);
	}

	@Override
	public void fromNull(final Object object) {
		set(object, null);
	}

	@Override
	final public Field getField() {
		return field;
	}

	@Override
	final public Class<?> getType() {
		return type;
	}

	public final void set(final Object object, final Object value) {
		try {
			field.set(object, value);
		} catch (IllegalAccessException e) {
			throw new BinderException(field, value, e);
		}
	}

	@Override
	public final Object get(Object object) {
		try {
			return field.get(object);
		} catch (IllegalAccessException e) {
			throw error("Cannot get value", e);
		}
	}

	final void setBoolean(final Object object, final boolean value) {
		try {
			field.setBoolean(object, value);
		} catch (IllegalAccessException e) {
			throw error("Cannot get value", e);
		}
	}

	final void setByte(final Object object, final byte value) {
		try {
			field.setByte(object, value);
		} catch (IllegalAccessException e) {
			throw new BinderException(field, value, e);
		}
	}

	final void setChar(final Object object, final char value) {
		try {
			field.setChar(object, value);
		} catch (IllegalAccessException e) {
			throw new BinderException(field, value, e);
		}
	}

	final void setDouble(final Object object, final double value) {
		try {
			field.setDouble(object, value);
		} catch (IllegalAccessException e) {
			throw new BinderException(field, value, e);
		}
	}

	final void setFloat(final Object object, final float value) {
		try {
			field.setFloat(object, value);
		} catch (IllegalAccessException e) {
			throw new BinderException(field, value, e);
		}
	}

	final void setLong(final Object object, final long value) {
		try {
			field.setLong(object, value);
		} catch (IllegalAccessException e) {
			throw new BinderException(field, value, e);
		}
	}

	final void setInteger(final Object object, final int value) {
		try {
			field.setInt(object, value);
		} catch (IllegalAccessException e) {
			throw new BinderException(field, value, e);
		}
	}

	final void setShort(final Object object, final short value) {
		try {
			field.setShort(object, value);
		} catch (IllegalAccessException e) {
			throw new BinderException(field, value, e);
		}
	}

	@Override
	public final BinderException error(final String message, final Object value) {
		return new BinderException(message, field, value);
	}

	@Override
	public final BinderException error(final String message, final Object value, final Exception cause) {
		return new BinderException(message, field, value, cause);
	}

}
