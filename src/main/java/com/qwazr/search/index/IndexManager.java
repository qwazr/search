/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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
package com.qwazr.search.index;

import com.qwazr.search.analysis.AnalyzerFactory;
import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.server.ServerException;
import com.qwazr.utils.FileUtils;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.concurrent.ReadWriteLock;
import com.qwazr.utils.reflection.ConstructorParameters;
import com.qwazr.utils.reflection.ConstructorParametersImpl;
import java.io.Closeable;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.Response;
import org.apache.lucene.search.Sort;

public class IndexManager extends ConstructorParametersImpl implements IndexInstance.Provider, Closeable {

    public final static String INDEXES_DIRECTORY = "indexes";
    public final static String BACKUPS_DIRECTORY = "backups";

    private final ConcurrentHashMap<String, IndexInstanceManager> indexMap;

    private final Path indexesDirectory;

    private final Path backupRootDirectory;
    private final ReadWriteLock backupLock = ReadWriteLock.stamped();

    private volatile SortedMap<String, UUID> indexSortedMap;

    private final IndexServiceInterface service;

    private final ConcurrentHashMap<String, SimilarityFactory> similarityFactoryMap;
    private final ConcurrentHashMap<String, Sort> sortMap;
    private final ConcurrentHashMap<String, AnalyzerFactory> analyzerFactoryMap;

    private final ExecutorService executorService;

    public IndexManager(final Path indexesDirectory,
                        final ExecutorService executorService,
                        final ConstructorParameters constructorParameters,
                        final Path backupRootDirectory) {
        super(constructorParameters == null ? new ConcurrentHashMap<>() : constructorParameters.getMap());
        this.indexesDirectory = indexesDirectory;
        this.executorService = executorService;
        this.backupRootDirectory = backupRootDirectory;

        service = new IndexServiceImpl(this);
        indexMap = new ConcurrentHashMap<>();
        similarityFactoryMap = new ConcurrentHashMap<>();
        sortMap = new ConcurrentHashMap<>();
        analyzerFactoryMap = new ConcurrentHashMap<>();

        try (final Stream<Path> stream = Files.list(indexesDirectory)) {
            stream.filter(path -> Files.isDirectory(path))
                .forEach(indexPath -> indexMap.put(indexPath.toFile().getName(),
                    new IndexInstanceManager(this, similarityFactoryMap, analyzerFactoryMap,
                        sortMap, executorService, service, indexPath)));
        } catch (IOException e) {
            throw new InternalServerErrorException("Issue while reading the index directory: " + indexesDirectory, e);
        }
        buildIndexNameMap();
    }

    public IndexManager(final Path indexesDirectory,
                        final ExecutorService executorService,
                        final Path backupDirectoryPath) {
        this(indexesDirectory, executorService, null, backupDirectoryPath);
    }


    public IndexManager(final Path indexesDirectory,
                        final ExecutorService executorService) {
        this(indexesDirectory, executorService, null);
    }

    public static Path checkSubDirectory(final Path dataDirectory, final String subDirectoryName) throws IOException {
        final Path subDirectory = dataDirectory.resolve(subDirectoryName);
        if (!Files.exists(subDirectory))
            Files.createDirectory(subDirectory);
        if (!Files.isDirectory(subDirectory))
            throw new IOException("This name is not valid. No directory exists for this location: " + subDirectory);
        return subDirectory;
    }

    public IndexManager registerSimilarityFactory(final String name, final SimilarityFactory factory) {
        similarityFactoryMap.put(name, factory);
        return this;
    }

    public IndexManager registerAnalyzerFactory(final String name, final AnalyzerFactory factory) {
        analyzerFactoryMap.put(name, factory);
        return this;
    }

    public IndexManager registerSort(final String name, final Sort sort) {
        sortMap.put(name, sort);
        return this;
    }

    final public IndexServiceInterface getService() {
        return service;
    }

    final public <T> AnnotatedIndexService<T> getService(final Class<T> indexClass) throws URISyntaxException {
        return getService(indexClass, null, null);
    }

    final public <T> AnnotatedIndexService<T> getService(final Class<T> indexClass,
                                                         final String indexName,
                                                         final IndexSettingsDefinition settings)
        throws URISyntaxException {
        return new AnnotatedIndexService<>(service, indexClass, indexName, settings);
    }

    @Override
    public void close() {
        indexMap.values().forEach(IOUtils::closeQuietly);
    }

    IndexInstance createUpdate(final String indexName, final IndexSettingsDefinition settings) {
        Objects.requireNonNull(settings, "The settings cannot be null");
        final IndexInstanceManager indexInstanceManager = indexMap.computeIfAbsent(indexName,
            name -> new IndexInstanceManager(this, similarityFactoryMap, analyzerFactoryMap,
                sortMap, executorService, service, indexesDirectory.resolve(name)));
        buildIndexNameMap();
        return indexInstanceManager.createUpdate(settings);
    }

    private IndexInstanceManager checkIndexExists(final String indexName,
                                                  final IndexInstanceManager indexInstanceManager) {
        if (indexInstanceManager == null)
            throw new ServerException(Response.Status.NOT_FOUND, "Index not found: " + indexName);
        return indexInstanceManager;
    }

    /**
     * Returns the indexInstance. If the index does not exists, an exception it
     * thrown. This method never returns a null value.
     *
     * @param indexName The name of the index
     * @return the indexInstance
     */
    public IndexInstance get(final String indexName) {
        final IndexInstanceManager indexInstanceManager = checkIndexExists(indexName, indexMap.get(indexName));
        try {
            final IndexInstance indexInstance = indexInstanceManager.getIndexInstance();
            if (indexInstance != null)
                return indexInstance;
            return indexInstanceManager.open();
        } catch (Exception e) {
            throw ServerException.of(e);
        }
    }

    void delete(final String indexName) throws ServerException {
        indexMap.compute(indexName, (name, indexInstanceManager) -> {
            checkIndexExists(indexName, indexInstanceManager).delete();
            return null;
        });
        buildIndexNameMap();
    }

    private synchronized void buildIndexNameMap() {
        final TreeMap<String, UUID> map = new TreeMap<>();
        indexMap.forEach((name, index) -> map.put(name, index.getIndexUuid()));
        indexSortedMap = Collections.unmodifiableSortedMap(map);
    }

    Map<String, UUID> getIndexMap() {
        return indexSortedMap;
    }

    final private static Pattern backupNameMatcher = Pattern.compile("[^a-zA-Z0-9-_]");

    private void checkBackupConfig() throws IOException {
        if (backupRootDirectory == null)
            throw new IOException("The backup root directory is not set");
        if (Files.notExists(backupRootDirectory) || !Files.isDirectory(backupRootDirectory))
            throw new IOException("The backup root directory does not exists: " + backupRootDirectory);
    }

    private Path getBackupDirectory(final String backupName, boolean createIfNotExists) throws IOException {
        if (StringUtils.isEmpty(backupName))
            throw new IOException("The backup name is empty");
        if (backupNameMatcher.matcher(backupName).find())
            throw new IOException(
                "The backup name should only contains alphanumeric characters, dash, or underscore : " +
                    backupName);
        final Path backupDirectory = backupRootDirectory.resolve(backupName);
        if (createIfNotExists && Files.notExists(backupDirectory))
            Files.createDirectory(backupDirectory);
        return backupDirectory;
    }

    private void indexIterator(final String indexName, final BiConsumer<String, IndexInstance> consumer) {
        if ("*".equals(indexName)) {
            indexMap.forEach((name, indexInstanceManager) -> {
                try {
                    consumer.accept(name, indexInstanceManager.open());
                } catch (Exception e) {
                    throw ServerException.of(e);
                }
            });
        } else
            consumer.accept(indexName, get(indexName));
    }

    SortedMap<String, BackupStatus> backups(final String indexName, final String backupName) {
        return backupLock.write(() -> {
            checkBackupConfig();
            final Path backupDirectory = getBackupDirectory(backupName, true);
            final SortedMap<String, BackupStatus> results = Collections.synchronizedSortedMap(new TreeMap<>());
            indexIterator(indexName, (idxName, indexInstance) -> {
                try {
                    results.put(idxName, indexInstance.backup(backupDirectory.resolve(idxName)));
                } catch (IOException e) {
                    throw ServerException.of(e);
                }
            });
            return results;
        });
    }

    private void backupIterator(final String backupName, final Consumer<Path> consumer) {
        try {
            if ("*".equals(backupName)) {
                try (final Stream<Path> stream = Files.list(backupRootDirectory)) {
                    stream.filter(path -> Files.isDirectory(path)).forEach(consumer);
                }
            } else
                consumer.accept(getBackupDirectory(backupName, false));
        } catch (IOException e) {
            throw ServerException.of(e);
        }
    }

    SortedMap<String, SortedMap<String, BackupStatus>> getBackups(final String indexName,
                                                                  final String backupName,
                                                                  final boolean extractVersion) {
        return backupLock.read(() -> {
            checkBackupConfig();
            final SortedMap<String, SortedMap<String, BackupStatus>> results =
                Collections.synchronizedSortedMap(new TreeMap<>());

            backupIterator(backupName, backupDirectory -> {

                final SortedMap<String, BackupStatus> backupResults =
                    Collections.synchronizedSortedMap(new TreeMap<>());

                indexIterator(indexName, (idxName, indexInstance) -> {
                    try {
                        final Path backupIndexDirectory = backupDirectory.resolve(idxName);
                        if (Files.exists(backupIndexDirectory) && Files.isDirectory(backupIndexDirectory))
                            backupResults.put(idxName, indexInstance.getBackup(backupIndexDirectory, extractVersion));
                    } catch (IOException e) {
                        throw ServerException.of(e);
                    }
                });
                if (!backupResults.isEmpty())
                    results.put(backupDirectory.toFile().getName(), backupResults);
            });
            return results;
        });
    }

    private void backupIndexDirectoryIterator(final Path backupDirectory,
                                              final String indexName,
                                              final Consumer<Path> consumer) {
        if (Files.notExists(backupRootDirectory) || !Files.isDirectory(backupRootDirectory))
            return;
        if ("*".equals(indexName)) {
            try {
                if (Files.exists(backupDirectory)) {
                    try (final Stream<Path> stream = Files.list(backupDirectory)) {
                        stream.filter(path -> Files.isDirectory(path)).forEach(consumer);
                    }
                }
            } catch (IOException e) {
                throw ServerException.of(e);
            }
        } else {
            final Path indexPath = backupDirectory.resolve(indexName);
            if (Files.exists(indexPath))
                consumer.accept(indexPath);
        }
    }

    int deleteBackups(final String indexName, final String backupName) {
        return backupLock.write(() -> {
            checkBackupConfig();

            final Map<String, Path> indexesToDelete = new LinkedHashMap<>();
            backupIterator(backupName, backupDirectory -> backupIndexDirectoryIterator(backupDirectory, indexName,
                indexDir -> indexesToDelete.put(indexDir.getFileName().toString(), indexDir)));

            final AtomicInteger counter = new AtomicInteger();
            indexesToDelete.forEach((indexToDelete, backupDirectory) -> {
                try {
                    final IndexInstance indexInstance = get(indexToDelete);
                    if (indexInstance != null)
                        indexInstance.deleteBackup(backupDirectory);
                    else
                        FileUtils.deleteDirectory(backupDirectory);
                    counter.incrementAndGet();

                    if (Files.exists(backupDirectory)) {
                        final long childCount;
                        try (final Stream<Path> stream = Files.list(backupDirectory)) {
                            childCount = stream.count();
                        }
                        if (childCount == 0)
                            Files.deleteIfExists(backupDirectory);
                    }
                } catch (IOException e) {
                    throw ServerException.of(e);
                }

            });
            return counter.get();
        });
    }

    IndexCheckStatus checkIndex(final String indexName) throws Exception {
        final IndexInstanceManager indexInstanceManager = indexMap.get(indexName);
        return indexInstanceManager == null ? null : new IndexCheckStatus(indexInstanceManager.check());
    }

    IndexStatus mergeIndex(final String indexName, final String mergedIndexName,
                           final Map<String, String> commitUserData) throws IOException {
        return get(indexName).merge(get(mergedIndexName), commitUserData);
    }
}
