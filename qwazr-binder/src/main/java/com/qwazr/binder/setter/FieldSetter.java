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

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Map;

public interface FieldSetter
		extends CollectionSetter, ListSetter, ObjectArraySetter, ObjectSetter, PrimitiveArraySetter, PrimitiveSetter,
		MapSetter {

	void fromNull(Object object);

	Class<?> getType();

	Field getField();

	Object get(Object object);

	default void setValue(Object object, Object value) {
		if (value == null) {
			fromNull(object);
			return;
		}
		final Class<?> valueClass = value.getClass();
		if (Collection.class.isAssignableFrom(valueClass)) {
			final Collection collection = (Collection) value;
			final Class<?> genericClass = collection.isEmpty() ? Object.class : collection.iterator().next().getClass();
			fromCollection(genericClass, (Collection<?>) value, object);
		} else if (Map.class.isAssignableFrom(valueClass)) {
			fromMap((Map<?, ?>) value, object);
		} else if (valueClass.isArray()) {
			final Class<?> componentType = valueClass.getComponentType();
			if (componentType.isPrimitive())
				fromPrimitiveArray(componentType, value, object);
			else
				fromObjectArray(componentType, value, object);
		} else {
			if (valueClass.isPrimitive())
				fromPrimitive(valueClass, value, object);
			else
				fromObject(valueClass, value, object);
		}
	}

	static FieldSetter of(Field field) {
		final Class<?> fieldType = field.getType();
		if (Collection.class.isAssignableFrom(fieldType)) {
			final Class<?> genericClass =
					(Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
			return CollectionSetter.from(field, genericClass);
		} else if (Map.class.isAssignableFrom(fieldType)) {
			final Class<?> keyClass =
					(Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
			final Object secondArgument = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1];
			final Class<?> valueClass;
			if (secondArgument instanceof ParameterizedType)
				valueClass = (Class<?>) ((ParameterizedType) secondArgument).getActualTypeArguments()[0];
			else
				valueClass = (Class<?>) secondArgument;
			return MapSetter.from(field, keyClass, valueClass);
		} else if (fieldType.isArray()) {
			final Class<?> componentType = fieldType.getComponentType();
			if (componentType.isPrimitive())
				return PrimitiveArraySetter.from(field, componentType);
			else
				return ObjectArraySetter.from(field, componentType);
		} else {
			if (fieldType.isPrimitive())
				return PrimitiveSetter.from(field, fieldType);
			else
				return ObjectSetter.from(field, fieldType);
		}
	}
}
