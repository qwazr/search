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
package com.qwazr.utils.reflection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface ConstructorParameters {

    <T> Object registerConstructorParameter(final Class<? extends T> objectClass, final T object);

    default Object registerConstructorParameter(final Object object) {
        return registerConstructorParameter(object.getClass(), object);
    }

    Map<Class<?>, Object> getMap();

    <T> T unregisterConstructorParameter(final Class<? extends T> objectClass);

    default Object unregisterConstructorParameter(final Object object) {
        return unregisterConstructorParameter(object.getClass());
    }

    /**
     * Fill a parameter map from the given parameters collection
     *
     * @param parameters the parameters to check
     */
    default void registerConstructorParameters(final Collection<?> parameters) {
        if (parameters == null)
            return;
        parameters.forEach(this::registerConstructorParameter);
    }

    /**
     * Fill a parameter map from the given parameters
     *
     * @param parameters the parameters to check
     */
    default void registerConstructorParameters(final Object... parameters) {
        if (parameters == null)
            return;
        for (final Object parameter : parameters)
            registerConstructorParameter(parameter);
    }

    /**
     * Find the first constructor who match the largest set of parameters present
     *
     * @param objectClass the class to introspect
     * @param <T>         the type of the class
     * @return the best matching constructor
     * @throws NoSuchMethodException if no matching method is found
     */
    <T> InstanceFactory<T> findBestMatchingConstructor(final Class<T> objectClass) throws NoSuchMethodException;

    static ConstructorParameters withMap(Map<Class<?>, Object> map) {
        return new ConstructorParametersImpl(map);
    }

    static ConstructorParameters withHashMap() {
        return withMap(new HashMap<>());
    }

    static ConstructorParameters withConcurrentMap() {
        return withMap(new ConcurrentHashMap<>());
    }

}
