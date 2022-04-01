/*
 * Copyright 2016-2017 Emmanuel Keller / QWAZR
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
package com.qwazr.binder.setter;

import com.qwazr.binder.impl.MapSetterImpl;

import java.lang.reflect.Field;
import java.util.Map;

public interface MapSetter extends ErrorSetter {

	default void fromMap(Map<?, ?> values, Object object) {
		throw error("Not supported ", object);
	}

	static FieldSetter from(final Field field, final Class<?> keyType, final Class<?> valueType) {
		return new MapSetterImpl(field, keyType, valueType);
	}
}
