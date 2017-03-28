/**
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
package com.qwazr.search.annotations;

import com.qwazr.utils.AnnotationsUtils;
import com.qwazr.utils.FieldMapWrapper;
import com.qwazr.utils.StringUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

class FieldMapWrappers extends FieldMapWrapper.Cache {

	FieldMapWrappers() {
		super(new ConcurrentHashMap<>());
	}

	@Override
	protected <C> FieldMapWrapper<C> newFieldMapWrapper(final Class<C> objectClass) throws NoSuchMethodException {
		final Map<String, Field> fieldMap = new HashMap<>();
		AnnotationsUtils.browseFieldsRecursive(objectClass, field -> {
			if (field.isAnnotationPresent(IndexField.class)) {
				field.setAccessible(true);
				final IndexField indexField = field.getDeclaredAnnotation(IndexField.class);
				final String fieldName = StringUtils.isEmpty(indexField.name()) ? field.getName() : indexField.name();
				fieldMap.put(fieldName, field);
			}
			if (field.isAnnotationPresent(IndexMapping.class)) {
				field.setAccessible(true);
				final IndexMapping indexMapping = field.getDeclaredAnnotation(IndexMapping.class);
				final String fieldName =
						StringUtils.isEmpty(indexMapping.value()) ? field.getName() : indexMapping.value();
				fieldMap.put(fieldName, field);
			}
		});
		return new FieldMapWrapper<>(fieldMap, objectClass);
	}
}
