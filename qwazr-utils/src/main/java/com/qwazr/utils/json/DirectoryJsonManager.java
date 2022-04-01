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
package com.qwazr.utils.json;

import com.qwazr.utils.ObjectMappers;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DirectoryJsonManager<T> {

    private final ReadWriteLock rwl = new ReentrantReadWriteLock();

    protected final File directory;

    private final Map<String, Pair<Long, T>> instancesMap;

    private volatile Map<String, Pair<Long, T>> instancesCache;

    private final Class<T> instanceClass;

    protected DirectoryJsonManager(File directory, Class<T> instanceClass) throws IOException {
        this.instanceClass = instanceClass;
        this.directory = directory;
        this.instancesMap = new LinkedHashMap<>();
        load();
    }

    private File getFile(String name) {
        return new File(directory, name + ".json");
    }

    protected void load() throws IOException {
        try {
            File[] files = directory.listFiles(JsonFileFilter.INSTANCE);
            if (files == null)
                return;
            for (File file : files) {
                String name = file.getName();
                name = name.substring(0, name.length() - 5);
                loadItem(name, file, file.lastModified());
            }
        } finally {
            buildCache();
        }
    }

    private Pair<Long, T> loadItem(String name, File file, long lastModified) throws IOException {
        T item = ObjectMappers.JSON.readValue(file, instanceClass);
        return put(name, lastModified, item);
    }

    private void buildCache() {
        instancesCache = new LinkedHashMap<>(instancesMap);
    }

    protected T delete(String name) throws IOException {
        if (StringUtils.isEmpty(name))
            return null;
        name = name.intern();
        rwl.writeLock().lock();
        try {
            Files.deleteIfExists(getFile(name).toPath());
            Pair<Long, T> instance = instancesMap.remove(name);
            buildCache();
            return instance.getRight();
        } finally {
            rwl.writeLock().unlock();
        }
    }

    private Pair<Long, T> put(String name, long lastModified, T instance) {
        name = name.intern();
        Pair<Long, T> item = Pair.of(lastModified, instance);
        instancesMap.put(name, item);
        return item;
    }

    protected void set(String name, T instance) throws IOException {
        if (instance == null)
            return;
        if (StringUtils.isEmpty(name))
            return;
        rwl.writeLock().lock();
        try {
            File destFile = getFile(name);
            ObjectMappers.JSON.writeValue(destFile, instance);
            put(name, destFile.lastModified(), instance);
            buildCache();
        } finally {
            rwl.writeLock().unlock();
        }
    }

    private T getNoLock(File file, String name, AtomicBoolean mustBeEvaluated) throws IOException {
        Pair<Long, T> item = instancesCache.get(name);
        long lastModified = file.lastModified();
        if (file.exists()) {
            if (item != null && item.getLeft() == lastModified)
                return item.getRight();
            if (mustBeEvaluated == null) {
                item = loadItem(name, file, lastModified);
                buildCache();
                return item.getRight();
            }
        } else {
            if (item == null)
                return null;
            if (mustBeEvaluated == null) {
                instancesMap.remove(name);
                buildCache();
                return null;
            }
        }
        mustBeEvaluated.set(true);
        return null;
    }

    protected T get(String name) throws IOException {
        File file = getFile(name);
        rwl.readLock().lock();
        try {
            AtomicBoolean mustBeEvaluated = new AtomicBoolean(false);
            T item = getNoLock(file, name, mustBeEvaluated);
            if (!mustBeEvaluated.get())
                return item;
        } finally {
            rwl.readLock().unlock();
        }
        rwl.writeLock().lock();
        try {
            return getNoLock(file, name, null);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    protected Set<String> nameSet() {
        return instancesCache.keySet();
    }
}
