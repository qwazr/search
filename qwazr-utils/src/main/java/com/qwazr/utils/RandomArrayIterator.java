/*
 * Copyright 2014-2018 Emmanuel Keller / QWAZR
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
package com.qwazr.utils;

import org.apache.commons.lang3.RandomUtils;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class RandomArrayIterator<T> implements Iterator<T> {

    private final T[] objects;

    private int pos;

    private int count;

    public RandomArrayIterator(final T[] objects) {
        this.objects = objects;
        if (objects != null) {
            pos = RandomUtils.nextInt(0, objects.length);
            count = objects.length;
        } else {
            pos = 0;
            count = 0;
        }
    }

    @Override
    public boolean hasNext() {
        return count > 0;
    }

    @Override
    public T next() {
        if (count == 0)
            throw new NoSuchElementException();
        final T object = objects[pos++];
        if (pos == objects.length)
            pos = 0;
        count--;
        return object;
    }

    @Override
    public void remove() {
        throw new RuntimeException("Not available");
    }

}
