/*
 * Copyright 2017-2020 Emmanuel Keller / QWAZR
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
import com.qwazr.server.ServerException;
import com.qwazr.utils.FileUtils;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.concurrent.ReadWriteLock;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import org.apache.lucene.index.CheckIndex;
import org.apache.lucene.search.Sort;
import org.apache.lucene.store.Directory;

class IndexInstanceManager implements Closeable {

    private final ReadWriteLock rwl;

    private final IndexManager indexManager;
    private final ExecutorService executorService;
    private final IndexServiceInterface indexServiceInterface;
    private final IndexFileSet fileSet;
    private final Map<String, SimilarityFactory> similarityFactoryMap;
    private final Map<String, AnalyzerFactory> analyzerFactoryMap;
    private final Map<String, Sort> sortMap;

    private final UUID indexUuid;
    private final String indexName;
    private IndexSettingsDefinition settings;
    private IndexInstance indexInstance;

    IndexInstanceManager(final IndexManager indexManager,
                         final Map<String, SimilarityFactory> similarityFactoryMap,
                         final Map<String, AnalyzerFactory> analyzerFactoryMap,
                         final Map<String, Sort> sortMap,
                         final ExecutorService executorService,
                         final IndexServiceInterface indexServiceInterface,
                         final Path indexDirectory) {
        try {
            rwl = ReadWriteLock.stamped();
            this.indexManager = indexManager;
            this.executorService = executorService;
            this.indexServiceInterface = indexServiceInterface;
            this.fileSet = new IndexFileSet(indexDirectory);
            this.similarityFactoryMap = similarityFactoryMap;
            this.analyzerFactoryMap = analyzerFactoryMap;
            this.sortMap = sortMap;
            this.indexName = fileSet.checkIndexDirectory();
            this.indexUuid = fileSet.checkUuid();
            this.settings = fileSet.loadSettings();
        } catch (IOException e) {
            throw ServerException.of(e);
        }
    }

    private IndexInstance ensureOpen() {
        if (indexInstance == null)
            indexInstance =
                new IndexInstanceBuilder(indexManager, similarityFactoryMap, analyzerFactoryMap,
                    sortMap, executorService, indexServiceInterface, fileSet, settings,
                    indexUuid, indexName).build();
        return indexInstance;
    }

    IndexInstance open() {
        return rwl.write(this::ensureOpen);
    }

    IndexInstance createUpdate(final IndexSettingsDefinition newSettings) {
        return rwl.write(() -> {
            final boolean same = Objects.equals(newSettings, settings);
            if (same && indexInstance != null)
                return indexInstance;
            closeIndex();
            if (!same) {
                fileSet.writeSettings(newSettings);
                settings = newSettings;
            }
            return ensureOpen();
        });
    }

    CheckIndex.Status check() throws Exception {
        return rwl.writeEx(() -> {
            closeIndex();
            try (final Directory directory = IndexInstanceBuilder.getDirectory(settings, fileSet.dataDirectory)) {
                try (final CheckIndex checkIndex = new CheckIndex(directory)) {
                    return checkIndex.checkIndex();
                }
            } finally {
                ensureOpen();
            }
        });
    }

    /**
     * Return the loaded instance or null if the index cannot be loaded
     *
     * @return the loaded instance
     */
    IndexInstance getIndexInstance() {
        return rwl.read(() -> indexInstance);
    }

    UUID getIndexUuid() {
        return indexUuid;
    }

    private void closeIndex() {
        if (indexInstance == null)
            return;
        IOUtils.closeQuietly(indexInstance);
        indexInstance = null;
    }

    @Override
    public void close() {
        rwl.writeEx(this::closeIndex);
    }

    public void delete() {
        rwl.writeEx(() -> {
            closeIndex();
            if (Files.exists(fileSet.mainDirectory)) {
                try {
                    FileUtils.deleteDirectory(fileSet.mainDirectory);
                } catch (IOException e) {
                    throw ServerException.of(e);
                }
            }
        });
    }

}
