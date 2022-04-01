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

import com.qwazr.binder.BinderException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

final public class MapSetterImpl extends FieldSetterAbstract {

    final private Class<Map> mapClass;

    public MapSetterImpl(Field field, Class<?> keyType, Class<?> valueType) {
        super(field);
        if (!Map.class.isAssignableFrom(type))
            throw error("The type should be a map", type);
        Class<?> fieldType = type;
        final int modifier = type.getModifiers();
        if (Modifier.isAbstract(modifier) || Modifier.isInterface(modifier))
            fieldType = LinkedHashMap.class;
        this.mapClass = (Class<Map>) fieldType;
    }

    private Map createMap(final Object object) {
        try {
            final Map map = mapClass.newInstance();
            field.set(object, map);
            return map;
        } catch (IllegalAccessException | InstantiationException e) {
            throw new BinderException(field, null, e);
        }
    }

    @Override
    public void fromObject(Object[] values, Object object) {
        final Map map = createMap(object);
        int i = 0;
        while (i < values.length)
            map.put(values[i++], values[i++]);
    }

    @Override
    public void fromString(String[] values, Object object) {
        fromObject(values, object);
    }

    @Override
    public void fromDouble(Double[] values, Object object) {
        fromObject(values, object);
    }

    @Override
    public void fromFloat(Float[] values, Object object) {
        fromObject(values, object);
    }

    @Override
    public void fromLong(Long[] values, Object object) {
        fromObject(values, object);
    }

    @Override
    public void fromShort(Short[] values, Object object) {
        fromObject(values, object);
    }

    @Override
    public void fromInteger(Integer[] values, Object object) {
        fromObject(values, object);
    }

    @Override
    public void fromByte(Byte[] values, Object object) {
        fromObject(values, object);
    }

    @Override
    public void fromChar(Character[] values, Object object) {
        fromObject(values, object);
    }

    @Override
    final public void fromObject(Collection<Object> values, Object object) {
        final Map map = createMap(object);
        final Iterator it = values.iterator();
        while (it.hasNext())
            map.put(it.next(), it.next());
    }

    @Override
    final public void fromCollection(Class<?> type, Collection<?> values, Object object) {
        fromObject((Collection<Object>) values, object);
    }

    @Override
    public void fromMap(Map<?, ?> values, Object object) {
        set(object, values);
    }

}
