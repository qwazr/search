/**
 * Copyright 2016 Emmanuel Keller / QWAZR
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
package com.qwazr.search.utils;

import com.qwazr.search.annotations.IndexMapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class IndexDocumentBuilder {

	private static class SourceContext<S> {

		private final S source;
		private final Field[] declaredFields;

		private SourceContext(S source) {
			this.source = source;
			this.declaredFields = source.getClass().getDeclaredFields();
		}

		private Class<?> fieldType;
		private Object fieldValue;
		private IndexMapping[] fieldAnnotations;

		private void set(Field field) throws ReflectiveOperationException {
			this.fieldType = field.getType();
			this.fieldValue = field.get(source);
			this.fieldAnnotations = field.getDeclaredAnnotationsByType(IndexMapping.class);
		}
	}

	private static class TargetContext<T> {

		private final T target;

		private final Class<?> clazz;

		private TargetContext(T target) {
			this.target = target;
			this.clazz = target.getClass();
		}

		private Field field;
		private Class<?> fieldType;

		private void set(String fieldName) throws NoSuchFieldException, SecurityException {
			field = clazz.getDeclaredField(fieldName);
			fieldType = field == null ? null : field.getType();
		}
	}

	public static <S, T> void apply(S source, T target) throws ReflectiveOperationException {

		final SourceContext<S> sourceContext = new SourceContext<>(source);
		final TargetContext<T> targetContext = new TargetContext<>(target);

		// We browse the field from the source object
		for (Field sourceField : sourceContext.declaredFields) {

			sourceContext.set(sourceField);
			if (sourceContext.fieldValue == null || sourceContext.fieldAnnotations == null)
				continue;// No value, No annotations ? let check the next one

			for (IndexMapping sourceAnnotation : sourceContext.fieldAnnotations) {

				// We retrieve the values of the annotations of the source
				// fields
				String[] sourceAnnotationValues = sourceAnnotation.value();
				if (sourceAnnotationValues == null)
					continue; // No value ? Next annotation

				for (String sourceAnnotationValue : sourceAnnotationValues) {

					targetContext.set(sourceAnnotationValue);
					if (targetContext.field == null)
						throw new ReflectiveOperationException("Target field not found: " + targetContext.field);

					if (setIfSameType(sourceContext, targetContext))
						continue;

					if (setIfTargetString(sourceContext, targetContext))
						continue;

					if (setIfCollection(sourceContext, targetContext))
						continue;

					Method method = targetContext.clazz.getMethod(sourceAnnotationValue, sourceContext.fieldType);
					method.invoke(targetContext.target, sourceContext.fieldValue);
				}
			}
		}
	}

	/**
	 * Set the target value if the source and the destination type are equal
	 *
	 * @param sourceContext
	 * @param targetContext
	 * @return
	 * @throws ReflectiveOperationException
	 */
	private static boolean setIfSameType(SourceContext<?> sourceContext, TargetContext<?> targetContext)
					throws ReflectiveOperationException {
		if (targetContext.fieldType != sourceContext.fieldType)
			return false;
		targetContext.field.set(targetContext.target, sourceContext.fieldValue);
		return true;
	}

	/**
	 * Set the target value if the type is String, using toString() method
	 *
	 * @param sourceContext
	 * @param targetContext
	 * @return
	 * @throws ReflectiveOperationException
	 */
	private static boolean setIfTargetString(SourceContext<?> sourceContext, TargetContext<?> targetContext)
					throws ReflectiveOperationException {
		if (targetContext.fieldType != String.class)
			return false;
		targetContext.field.set(targetContext.target, sourceContext.fieldValue.toString());
		return true;
	}

	/**
	 * Add the source value to the collection if the generic type of the
	 * collection and the type of the source value match
	 *
	 * @param sourceContext
	 * @param targetContext
	 * @return
	 * @throws ReflectiveOperationException
	 */
	private static boolean setIfCollection(SourceContext<?> sourceContext, TargetContext<?> targetContext)
					throws ReflectiveOperationException {
		// Is it a collection ?
		if (!Collection.class.isAssignableFrom(targetContext.fieldType))
			return false;
		Type targetGenericType = targetContext.field.getGenericType();
		if (!(targetGenericType instanceof ParameterizedType))
			return false;

		// Check the generic type of the collection
		ParameterizedType parameterizedType = (ParameterizedType) targetGenericType;
		Type[] types = parameterizedType.getActualTypeArguments();
		if (types == null || types.length != 1)
			return false;
		Type targetCollectionType = types[0];

		// Retrieve the collection
		@SuppressWarnings("unchecked") Collection<Object> collection = (Collection<Object>) targetContext.field
						.get(targetContext.target);

		// Add for same type
		if (targetCollectionType == sourceContext.fieldType) {
			collection.add(sourceContext.fieldValue);
			return true;
		}

		// Add with string conversion
		if (targetCollectionType == String.class) {
			collection.add(sourceContext.fieldValue.toString());
			return true;
		}
		return false;
	}

}
