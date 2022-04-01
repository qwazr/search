/*
 * Copyright 2015-2019 Emmanuel Keller / QWAZR
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
package com.qwazr.utils.caching;

import com.qwazr.utils.concurrent.ReadWriteLock;

import javax.annotation.concurrent.ThreadSafe;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@ThreadSafe
public class KeyLockedCache<KEY, VALUE> {

    private final ReadWriteLock mapLock;
    private final Map<KEY, Locker<KEY>> keyMap;
    private final Map<KEY, VALUE> map;
    private volatile int currentActiveKeys;
    private volatile int maxActiveKeys;

    public KeyLockedCache(final Map<KEY, VALUE> map) {
        this.keyMap = new HashMap<>();
        this.map = map;
        this.currentActiveKeys = 0;
        this.maxActiveKeys = 0;
        this.mapLock = ReadWriteLock.reentrant(true);
    }

    private Locker<KEY> getKeyLock(final KEY key) {
        final Locker<KEY> locker;
        synchronized (keyMap) {
            // We do not use computeIfAbsent due to activeKeys computation optimisation
            // We don't want to call Map::size() on each call
            final Locker<KEY> existingLocker = keyMap.get(key);
            if (existingLocker != null) {
                locker = existingLocker;
            } else {
                locker = new Locker<>(key);
                keyMap.put(key, locker);
                locker.lock();
                currentActiveKeys = keyMap.size();
                maxActiveKeys = Math.max(currentActiveKeys, maxActiveKeys);
            }
        }
        return locker;
    }

    private void release(Locker<KEY> locker) {
        synchronized (keyMap) {
            if (locker.release() == 0) {
                keyMap.remove(locker.key);
                currentActiveKeys = keyMap.size();
            }
        }
    }

    public VALUE computeIfAbsent(final KEY key,
                                 final Function<KEY, VALUE> supplier) {
        final VALUE value;
        final Locker<KEY> locker = getKeyLock(key);
        try {
            synchronized (locker.key) {
                final VALUE existingValue = mapLock.read(() -> map.get(locker.key));
                if (existingValue != null) {
                    value = existingValue;
                } else {
                    value = supplier.apply(locker.key);
                    mapLock.write(() -> map.put(locker.key, value));
                }
            }
        } finally {
            release(locker);
        }
        return value;
    }

    /**
     * @return the size of the backed map
     */
    public int size() {
        return mapLock.read(map::size);
    }

    /**
     * @return the number of active keys
     */
    public int getCurrentActiveKeys() {
        return currentActiveKeys;
    }

    /**
     * @return the highest number of active keys
     */
    public int getMaxActiveKeys() {
        return maxActiveKeys;
    }

    private static class Locker<KEY> {

        private final KEY key;
        private int count;

        private Locker(final KEY key) {
            this.key = key;
            this.count = 0;
        }

        void lock() {
            synchronized (this) {
                count++;
            }
        }

        int release() {
            final int result;
            synchronized (this) {
                result = --count;
            }
            return result;
        }
    }
}
