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
package com.qwazr.search.annotations;

import com.qwazr.binder.FieldMapWrapper;
import com.qwazr.binder.setter.FieldSetter;
import com.qwazr.utils.AnnotationsUtils;
import com.qwazr.utils.StringUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class FieldMapWrappers extends FieldMapWrapper.Cache {

	private final Set<String> indexFields;

	FieldMapWrappers(final Set<String> indexFields) {
		super(new ConcurrentHashMap<>());
		this.indexFields = indexFields;
	}

	static String getFieldName(final String annotationName, final Field field) {
		return StringUtils.isEmpty(annotationName) ? field.getName() : annotationName;
	}

	private String checkFieldName(final String annotationName, final Field field) {
		final String fieldName = getFieldName(annotationName, field);
		if (indexFields != null && !indexFields.contains(fieldName))
			throw new IllegalArgumentException("Unknown field: " + fieldName);
		return fieldName;
	}

	@Override
	protected <C> FieldMapWrapper<C> newFieldMapWrapper(final Class<C> objectClass) throws NoSuchMethodException {
		final Map<String, FieldSetter> fieldMap = new HashMap<>();
		AnnotationsUtils.browseFieldsRecursive(objectClass, field -> {
			if (field.isAnnotationPresent(IndexField.class)) {
				field.setAccessible(true);
				final IndexField indexField = field.getDeclaredAnnotation(IndexField.class);
				fieldMap.put(checkFieldName(indexField.name(), field), FieldSetter.of(field));
			}
			if (field.isAnnotationPresent(SmartField.class)) {
				field.setAccessible(true);
				final SmartField smartField = field.getDeclaredAnnotation(SmartField.class);
				fieldMap.put(checkFieldName(smartField.name(), field), FieldSetter.of(field));
			}
			if (field.isAnnotationPresent(IndexMapping.class)) {
				field.setAccessible(true);
				final IndexMapping indexMapping = field.getDeclaredAnnotation(IndexMapping.class);
				fieldMap.put(checkFieldName(indexMapping.value(), field), FieldSetter.of(field));
			}
		});
		return new FieldMapWrapper<>(fieldMap, objectClass);
	}
}
