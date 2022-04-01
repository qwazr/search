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

import com.qwazr.utils.SerializationUtils;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Base64;

final public class SerializableSetterImpl extends FieldSetterAbstract {

	public SerializableSetterImpl(Field field) {
		super(field);
	}

	final public void fromString(final String string, final Object object) {
		fromByte(Base64.getDecoder().decode(string), object);
	}

	@Override
	final public void fromByte(final byte[] bytes, Object object) {
		try {
			set(object, SerializationUtils.fromExternalizorBytes(bytes, (Class<? extends Serializable>) type));
		} catch (IOException | ReflectiveOperationException e) {
			throw error("Serialization failure", bytes);
		}
	}

}
