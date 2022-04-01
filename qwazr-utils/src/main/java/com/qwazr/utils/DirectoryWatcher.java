/*
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
package com.qwazr.utils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DirectoryWatcher implements Runnable, Closeable, AutoCloseable {

    private final static Logger LOGGER = LoggerUtils.getLogger(DirectoryWatcher.class);

    private final Path rootPath;
    private final WatchService watcher;
    private final HashSet<Consumer<Path>> consumers;

    private volatile List<Consumer<Path>> consumersCache;

    private final HashMap<WatchKey, Path> keys;

    private DirectoryWatcher(Path rootPath) throws IOException {
        FileSystem fs = FileSystems.getDefault();
        this.watcher = fs.newWatchService();
        this.rootPath = rootPath;
        this.keys = new HashMap<>();
        this.consumers = new HashSet<>();
        this.consumersCache = new ArrayList<>();
    }

    private final static HashMap<Path, DirectoryWatcher> watchers = new HashMap<>();

    /**
     * <p>Create a new DirectoryWatcher instance.</p>
     * <p>A DirectoryWatcher is a running thread listening for events in the file system.</p>
     *
     * @param rootPath The path of the monitored directory
     * @param consumer The consumer called each time a file event occurs
     * @return a new DirectoryWatcher
     * @throws IOException if any I/O error occurs
     */
    public static DirectoryWatcher register(final Path rootPath, final Consumer<Path> consumer) throws IOException {
        synchronized (watchers) {

            DirectoryWatcher watcher = watchers.get(rootPath);
            if (watcher == null) {
                LOGGER.info(() -> "New directory watcher: " + rootPath);
                watcher = new DirectoryWatcher(rootPath);
                watchers.put(rootPath, watcher);
            }
            watcher.register(consumer);
            return watcher;
        }
    }

    private synchronized void register(final Consumer<Path> consumer) {
        synchronized (consumers) {
            if (consumers.add(consumer))
                consumersCache = new ArrayList<>(consumers);
        }
    }

    public synchronized void unregister(final Consumer<Path> consumer) throws IOException {
        synchronized (consumers) {
            if (consumers.remove(consumer))
                consumersCache = new ArrayList<>(consumers);
            synchronized (watchers) {
                if (consumersCache.isEmpty()) {
                    watchers.remove(rootPath);
                    close();
                }
            }
        }
    }

    public static void registerDirectory(final Path rootPath, final WatchService watcher,
                                         final HashMap<WatchKey, Path> keys) throws IOException {
        Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
            @Override
            final public FileVisitResult preVisitDirectory(Path file, BasicFileAttributes attrs) throws IOException {
                if (attrs.isDirectory()) {
                    keys.put(file.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY), file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Override
    public void run() {
        try {
            registerDirectory(rootPath, watcher, keys);
            // Infinite loop.
            for (; ; ) {
                WatchKey key = watcher.take();
                Path dir = keys.get(key);
                if (dir != null) {

                    for (WatchEvent<?> watchEvent : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = watchEvent.kind();
                        if (kind == StandardWatchEventKinds.OVERFLOW)
                            continue;
                        Object o = watchEvent.context();
                        Path file = (o instanceof Path) ? (Path) o : null;
                        if (file == null)
                            continue;

                        Path child = dir.resolve(file);
                        // If this is a new directory, we have to register it
                        if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS))
                            if (kind == StandardWatchEventKinds.ENTRY_CREATE)
                                registerDirectory(child, watcher, keys);
                    }
                    for (Consumer consumer : consumersCache)
                        consumer.accept(dir.toAbsolutePath());
                }
                if (!key.reset()) {
                    keys.remove(key);
                    if (keys.isEmpty())
                        break;
                }
            }
        } catch (ClosedWatchServiceException e1) {
            LOGGER.log(Level.FINER, e1, () -> "Directory watcher ends: " + rootPath);
        } catch (IOException | InterruptedException e2) {
            LOGGER.log(Level.WARNING, e2, () -> "Directory watcher ends: " + rootPath);
        }
    }

    @Override
    public void close() throws IOException {
        if (watcher != null)
            watcher.close();
    }
}