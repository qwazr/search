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
package com.qwazr.utils;

import com.qwazr.utils.concurrent.ReadWriteLock;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AccessTimeCacheMap<K, V> {

    private volatile int size;

    private final EldestMap entryMap;
    private final ReadWriteLock rwl = ReadWriteLock.stamped();

    private final long msTimeOut;

    public AccessTimeCacheMap(int secondsTimeOut) {
        entryMap = new EldestMap();
        msTimeOut = secondsTimeOut * 1000;
        size = 0;
    }

    private V getSynchronizedValue(K key, long nextExpirationTime) {
        final AccessTimeCacheEntry<V> entry = entryMap.get(key);
        if (entry == null)
            return null;
        synchronized (entry) {
            return entry.getValue(nextExpirationTime);
        }
    }

    public V getOrCreate(K key, Supplier<V> supplier) {
        final long nextExpirationTime = System.currentTimeMillis() + msTimeOut;
        final V val = rwl.read(() -> getSynchronizedValue(key, nextExpirationTime));
        if (val != null)
            return val;
        final AccessTimeCacheEntry<V> entry = new AccessTimeCacheEntry(nextExpirationTime);
        synchronized (entry) {
            return rwl.write(() -> {
                V value = getSynchronizedValue(key, nextExpirationTime);
                if (value != null)
                    return value;
                entryMap.put(key, entry);
                size = entryMap.size();
                entry.setValue(supplier);
                return entry.value;
            });
        }
    }

    public V remove(K key) {
        final V val = rwl.read(() -> getSynchronizedValue(key, 0));
        if (val == null)
            return null;

        return rwl.write(() -> {
            final V value = getSynchronizedValue(key, 0);
            if (value == null)
                return null;
            entryMap.remove(key);
            size = entryMap.size();
            return value;
        });
    }

    public int size() {
        return size;
    }

    private class EldestMap extends LinkedHashMap<K, AccessTimeCacheEntry<V>> {

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, AccessTimeCacheEntry<V>> eldest) {
            return eldest.getValue().isExpired(System.currentTimeMillis());
        }

    }

    private static class AccessTimeCacheEntry<T> {

        private volatile long expirationTime;

        private T value;

        AccessTimeCacheEntry(long newExpirationTime) {
            this.expirationTime = newExpirationTime;
            this.value = null;
        }

        void setValue(Supplier<T> supplier) {
            value = supplier.get();
        }

        T getValue(long newExpirationTime) {
            expirationTime = newExpirationTime;
            return value;
        }

        boolean isExpired(long compareTime) {
            return expirationTime < compareTime;
        }

    }

}
