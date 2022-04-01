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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public abstract class CollectionSetterAbstract<T> extends FieldSetterAbstract {

    final private Class<Collection<T>> collectionClass;

    CollectionSetterAbstract(Field field) {
        super(field);
        Class<?> fieldType = type;
        final int modifier = type.getModifiers();
        if (Modifier.isAbstract(modifier) || Modifier.isInterface(modifier)) {
            if (Set.class.isAssignableFrom(type))
                fieldType = LinkedHashSet.class;
            else
                fieldType = ArrayList.class;
        }
        if (!Collection.class.isAssignableFrom(type))
            throw error("The type should be a collection", fieldType);
        this.collectionClass = (Class<Collection<T>>) fieldType;
    }

    private Collection<T> createCollection(final Object object) {
        try {
            final Collection<T> collection = collectionClass.newInstance();
            field.set(object, collection);
            return collection;
        } catch (IllegalAccessException | InstantiationException e) {
            throw new BinderException(field, null, e);
        }
    }

    protected abstract T fromNumber(Number value);

    protected abstract T fromString(String value);

    protected abstract T fromDouble(double value);

    protected abstract T fromFloat(float value);

    protected abstract T fromLong(long value);

    protected abstract T fromShort(short value);

    protected abstract T fromInteger(int value);

    protected abstract T fromChar(Character value);

    protected abstract T fromChar(char value);

    protected abstract T fromByte(byte value);

    protected abstract T fromBoolean(Boolean value);

    protected abstract T fromBoolean(boolean value);

    @Override
    final public void fromString(String value, Object object) {
        createCollection(object).add(fromString(value));
    }

    @Override
    final public void fromDouble(Double value, Object object) {
        createCollection(object).add(fromNumber(value));
    }

    @Override
    final public void fromFloat(Float value, Object object) {
        createCollection(object).add(fromNumber(value));
    }

    @Override
    final public void fromLong(Long value, Object object) {
        createCollection(object).add(fromNumber(value));
    }

    @Override
    final public void fromInteger(Integer value, Object object) {
        createCollection(object).add(fromNumber(value));
    }

    @Override
    final public void fromShort(Short value, Object object) {
        createCollection(object).add(fromNumber(value));
    }

    @Override
    final public void fromChar(Character value, Object object) {
        createCollection(object).add(fromChar(value));
    }

    @Override
    final public void fromByte(Byte value, Object object) {
        createCollection(object).add(fromNumber(value));
    }

    @Override
    final public void fromBoolean(Boolean value, Object object) {
        createCollection(object).add(fromBoolean(value));
    }

    @Override
    final public void fromDouble(double value, Object object) {
        createCollection(object).add(fromDouble(value));
    }

    @Override
    final public void fromFloat(float value, Object object) {
        createCollection(object).add(fromFloat(value));
    }

    @Override
    final public void fromLong(long value, Object object) {
        createCollection(object).add(fromLong(value));
    }

    @Override
    final public void fromInteger(int value, Object object) {
        createCollection(object).add(fromInteger(value));
    }

    @Override
    final public void fromShort(short value, Object object) {
        createCollection(object).add(fromShort(value));
    }

    @Override
    final public void fromChar(char value, Object object) {
        createCollection(object).add(fromChar(value));
    }

    @Override
    final public void fromByte(byte value, Object object) {
        createCollection(object).add(fromByte(value));
    }

    @Override
    final public void fromBoolean(boolean value, Object object) {
        createCollection(object).add(fromBoolean(value));
    }

    private <V> void fromArray(final V[] values, final Object object, final Function<V, T> converter) {
        final Collection<T> collection = createCollection(object);
        for (V value : values)
            collection.add(converter.apply(value));
    }

    @Override
    final public void fromString(String[] values, Object object) {
        fromArray(values, object, this::fromString);
    }

    @Override
    final public void fromDouble(Double[] values, Object object) {
        fromArray(values, object, this::fromDouble);
    }

    @Override
    final public void fromFloat(Float[] values, Object object) {
        fromArray(values, object, this::fromFloat);
    }

    @Override
    final public void fromLong(Long[] values, Object object) {
        fromArray(values, object, this::fromLong);
    }

    @Override
    final public void fromInteger(Integer[] values, Object object) {
        fromArray(values, object, this::fromInteger);
    }

    @Override
    final public void fromShort(Short[] values, Object object) {
        fromArray(values, object, this::fromShort);
    }

    @Override
    final public void fromChar(Character[] values, Object object) {
        fromArray(values, object, this::fromChar);
    }

    @Override
    final public void fromByte(Byte[] values, Object object) {
        fromArray(values, object, this::fromByte);
    }

    @Override
    final public void fromBoolean(Boolean[] values, Object object) {
        fromArray(values, object, this::fromBoolean);
    }

    private <V> void fromCollection(final Collection<V> values, final Object object, final Function<V, T> converter) {
        final Collection<T> collection = createCollection(object);
        for (V value : values)
            collection.add(converter.apply(value));
    }

    @Override
    final public void fromString(List<String> values, Object object) {
        fromCollection(values, object, this::fromString);
    }

    @Override
    final public void fromDouble(List<Double> values, Object object) {
        fromCollection(values, object, this::fromDouble);
    }

    @Override
    final public void fromFloat(List<Float> values, Object object) {
        fromCollection(values, object, this::fromFloat);
    }

    @Override
    final public void fromLong(List<Long> values, Object object) {
        fromCollection(values, object, this::fromLong);
    }

    @Override
    final public void fromInteger(List<Integer> values, Object object) {
        fromCollection(values, object, this::fromInteger);
    }

    @Override
    final public void fromShort(List<Short> values, Object object) {
        fromCollection(values, object, this::fromShort);
    }

    @Override
    final public void fromChar(List<Character> values, Object object) {
        fromCollection(values, object, this::fromChar);
    }

    @Override
    final public void fromByte(List<Byte> values, Object object) {
        fromCollection(values, object, this::fromByte);
    }

    @Override
    final public void fromBoolean(List<Boolean> values, Object object) {
        fromCollection(values, object, this::fromBoolean);
    }

    @Override
    final public void fromString(Collection<String> values, Object object) {
        fromCollection(values, object, this::fromString);
    }

    @Override
    final public void fromDouble(Collection<Double> values, Object object) {
        fromCollection(values, object, this::fromDouble);
    }

    @Override
    final public void fromFloat(Collection<Float> values, Object object) {
        fromCollection(values, object, this::fromFloat);
    }

    @Override
    final public void fromLong(Collection<Long> values, Object object) {
        fromCollection(values, object, this::fromLong);
    }

    @Override
    final public void fromInteger(Collection<Integer> values, Object object) {
        fromCollection(values, object, this::fromInteger);
    }

    @Override
    final public void fromShort(Collection<Short> values, Object object) {
        fromCollection(values, object, this::fromShort);
    }

    @Override
    final public void fromChar(Collection<Character> values, Object object) {
        fromCollection(values, object, this::fromChar);
    }

    @Override
    final public void fromByte(Collection<Byte> values, Object object) {
        fromCollection(values, object, this::fromByte);
    }

    @Override
    final public void fromBoolean(Collection<Boolean> values, Object object) {
        fromCollection(values, object, this::fromBoolean);
    }

    @Override
    final public void fromObject(Collection<Object> values, Object object) {
        final Collection<T> collection = createCollection(object);
        for (Object value : values)
            collection.add((T) value);
    }

    @Override
    final public void fromDouble(double[] values, Object object) {
        final Collection<T> collection = createCollection(object);
        for (double value : values)
            collection.add(fromDouble(value));
    }

    @Override
    final public void fromFloat(float[] values, Object object) {
        final Collection<T> collection = createCollection(object);
        for (float value : values)
            collection.add(fromFloat(value));
    }

    @Override
    final public void fromLong(long[] values, Object object) {
        final Collection<T> collection = createCollection(object);
        for (long value : values)
            collection.add(fromLong(value));
    }

    @Override
    final public void fromInteger(int[] values, Object object) {
        final Collection<T> collection = createCollection(object);
        for (int value : values)
            collection.add(fromInteger(value));
    }

    @Override
    final public void fromShort(short[] values, Object object) {
        final Collection<T> collection = createCollection(object);
        for (short value : values)
            collection.add(fromShort(value));
    }

    @Override
    final public void fromChar(char[] values, Object object) {
        final Collection<T> collection = createCollection(object);
        for (char value : values)
            collection.add(fromChar(value));
    }

    @Override
    final public void fromByte(byte[] values, Object object) {
        final Collection<T> collection = createCollection(object);
        for (byte value : values)
            collection.add(fromByte(value));
    }

    @Override
    final public void fromBoolean(boolean[] values, Object object) {
        final Collection<T> collection = createCollection(object);
        for (boolean value : values)
            collection.add(fromBoolean(value));
    }

}
