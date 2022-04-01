/*
 * Copyright 2017-2018 Emmanuel Keller / QWAZR
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class InstanceFactory<T> {

    final public Constructor<T> constructor;
    final public Object[] parameters;

    public InstanceFactory(final Constructor<T> constructor, final Object[] parameters) {
        this.constructor = constructor;
        if (parameters != null) {
            this.parameters = new Object[parameters.length];
            System.arraycopy(parameters, 0, this.parameters, 0, parameters.length);
        } else
            this.parameters = null;
    }

    public T newInstance() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        return parameters == null || parameters.length == 0 ?
                constructor.newInstance() :
                constructor.newInstance(parameters);
    }

}
