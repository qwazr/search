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
package com.qwazr.binder;

import com.qwazr.binder.impl.SerializableSetterImpl;
import com.qwazr.binder.setter.FieldSetter;
import com.qwazr.utils.SerializationUtils;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldMapWrapper<T> {

    public final Map<String, FieldSetter> fieldMap;
    public final Class<T> objectClass;
    public final Constructor<T> constructor;

    public FieldMapWrapper(final Map<String, FieldSetter> fieldMap, final Class<T> objectClass)
            throws NoSuchMethodException {
        this.fieldMap = fieldMap;
        this.objectClass = objectClass;
        this.constructor = objectClass.getDeclaredConstructor();
    }

    /**
     * Build a new Map by reading the annotations
     *
     * @param row the record
     * @return a new Map
     */
    public Map<String, Object> newMap(final T row) {
        final Map<String, Object> map = new HashMap<>();
        fieldMap.forEach((name, field) -> {
            final Object value = field.get(row);
            if (value == null)
                return;
            try {
                if (field instanceof SerializableSetterImpl)
                    map.put(name, SerializationUtils.toExternalizorBytes((Serializable) value));
                else
                    map.put(name, value);

            } catch (IOException | ReflectiveOperationException e) {
                throw field.error("Cannot convert the field " + name, field, e);
            }
        });
        return map.isEmpty() ? null : map;
    }

    /**
     * Buid a collection of Map by reading the IndexFields of the annotated documents
     *
     * @param rows a collection of records
     * @return a new list of mapped objects
     */
    public List<Map<String, Object>> newMapCollection(final Collection<T> rows) {
        if (rows == null || rows.isEmpty())
            return null;
        final List<Map<String, Object>> list = new ArrayList<>(rows.size());
        rows.forEach(row -> list.add(newMap(row)));
        return list;
    }

    /**
     * Buid a collection of Map by reading the IndexFields of the annotated documents
     *
     * @param rows an array of records
     * @return a new list of mapped objects
     */
    public List<Map<String, Object>> newMapArray(final T... rows) {
        if (rows == null || rows.length == 0)
            return null;
        final List<Map<String, Object>> list = new ArrayList<>(rows.length);
        for (T row : rows)
            list.add(newMap(row));
        return list;
    }

    public T toRecord(final Map<String, Object> fields) throws ReflectiveOperationException, IOException {
        if (fields == null)
            return null;
        final T record = constructor.newInstance();
        for (Map.Entry<String, ?> entry : fields.entrySet()) {
            final String name = entry.getKey();
            final Object value = entry.getValue();
            if (value == null)
                continue;
            final FieldSetter field = fieldMap.get(name);
            if (field != null)
                field.setValue(record, value);
        }
        return record;
    }

    public List<T> toRecords(final Collection<Map<String, Object>> docs)
            throws IOException, ReflectiveOperationException {
        if (docs == null)
            return null;
        final List<T> records = new ArrayList<>();
        for (final Map<String, Object> doc : docs)
            records.add(toRecord(doc));
        return records;
    }

    public List<T> toRecords(final Map<String, Object>... docs) throws IOException, ReflectiveOperationException {
        if (docs == null)
            return null;
        final List<T> records = new ArrayList<>();
        for (Map<String, Object> doc : docs)
            records.add(toRecord(doc));
        return records;
    }

    public abstract static class Cache {

        private final Map<Class<?>, FieldMapWrapper<?>> fieldMapWrappers;

        protected abstract <C> FieldMapWrapper<C> newFieldMapWrapper(final Class<C> objectClass)
                throws NoSuchMethodException;

        public Cache(Map<Class<?>, FieldMapWrapper<?>> map) {
            fieldMapWrappers = map;
        }

        public <C> FieldMapWrapper<C> get(final Class<C> objectClass) {
            return (FieldMapWrapper<C>) fieldMapWrappers.computeIfAbsent(objectClass, cl -> {
                try {
                    return newFieldMapWrapper(cl);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        public void clear() {
            fieldMapWrappers.clear();
        }

    }
}
