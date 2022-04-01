/*
 * Copyright 2015-220 Emmanuel Keller / QWAZR
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

import com.qwazr.utils.ArrayUtils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConstructorParametersImpl extends InstancesSupplier.Impl implements ConstructorParameters {

    private static Object[] NO_PARAMS = new Object[0];

    private final Map<Class<?>, Object> map;

    protected ConstructorParametersImpl(Map<Class<?>, Object> map) {
        super(map);
        this.map = map;
    }

    /**
     * Find the first constructor who match the largest set of parameters present
     *
     * @param objectClass the class to introspect
     * @param <T>         the type of the class
     * @return the best matching constructor
     * @throws NoSuchMethodException if no matching method is found
     */
    @Override
    public <T> InstanceFactory<T> findBestMatchingConstructor(final Class<T> objectClass) throws NoSuchMethodException {
        final Constructor<T>[] constructors = (Constructor<T>[]) objectClass.getConstructors();
        if (map.size() == 0) {
            final Constructor<T> constructor = objectClass.getDeclaredConstructor();
            return new InstanceFactory<>(constructor, null);
        }
        int max = -1;
        Constructor<T> bestMatchConstructor = null;
        Object[] bestParameterArray = null;
        for (final Constructor<T> constructor : constructors) {
            final Object[] parameters = findMatchingParameterSet(constructor);
            if (parameters != null && parameters.length > max) {
                bestMatchConstructor = constructor;
                bestParameterArray = parameters;
                if (parameters.length == map.size())
                    break;
                max = parameters.length;
            }
        }
        if (bestParameterArray == null)
            return new InstanceFactory<>(objectClass.getDeclaredConstructor(), null);
        return new InstanceFactory<>(bestMatchConstructor, bestParameterArray);
    }

    /**
     * Build a list of parameters matching the constructor parameter.
     *
     * @param constructor the constructor to check
     * @param <T>         the type of the introspected class
     * @return the ordered parameter list or null
     */
    private <T> Object[] findMatchingParameterSet(final Constructor<T> constructor) {
        final List<Object> parameterList = new ArrayList<>();
        final Class<?>[] parameterClasses = constructor.getParameterTypes();
        if (parameterClasses.length == 0)
            return NO_PARAMS;
        for (final Class<?> parameterClass : parameterClasses) {
            final Object parameter = map.get(parameterClass);
            if (parameter == null)
                return null;
            parameterList.add(parameter);
        }
        return parameterList.toArray(ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    @Override
    public <T> Object registerConstructorParameter(Class<? extends T> objectClass, T object) {
        return registerInstance(objectClass, object);
    }

    @Override
    public Map<Class<?>, Object> getMap() {
        return map;
    }

    @Override
    public <T> T unregisterConstructorParameter(Class<? extends T> objectClass) {
        return unregisterInstance(objectClass);
    }

}
