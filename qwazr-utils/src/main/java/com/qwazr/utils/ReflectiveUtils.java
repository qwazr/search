/**
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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
package com.qwazr.utils;

import java.lang.reflect.Field;
import java.util.Collection;

public class ReflectiveUtils {

	public static <T> Collection<T> getCollection(final Object record, final Field field, final Class<?> fieldClass)
			throws ReflectiveOperationException {
		Collection<T> collection = (Collection<T>) field.get(record);
		if (collection != null)
			return collection;
		collection = (Collection<T>) fieldClass.newInstance();
		field.set(record, collection);
		return collection;
	}

}
