/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qwazr.search.test;

import com.qwazr.search.analysis.UpdatableAnalyzers;
import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.annotations.Index;
import com.qwazr.search.annotations.IndexField;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.IndexManager;
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.IndexStatus;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.query.QueryParser;
import com.qwazr.utils.RandomUtils;
import com.qwazr.utils.concurrent.ThreadUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexOptions;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.qwazr.search.field.FieldDefinition.Template.StringField;

public class UpdatableAnalyzersTest {

    private static ExecutorService executor;

    @BeforeClass
    public static void setup() {
        executor = Executors.newCachedThreadPool();
    }

    @AfterClass
    public static void cleanup() {
        executor.shutdownNow();
    }

    @Test
    public void testConcurrencyUpdates() throws ExecutionException, InterruptedException {

        final UpdatableAnalyzers updatableAnalyzers = new UpdatableAnalyzers(new HashMap<>());
        Assert.assertEquals(1, updatableAnalyzers.getActiveAnalyzers());

        final AtomicBoolean run = new AtomicBoolean(true);

        final List<Future> futures = new ArrayList<>();

        // 4 threads update the analyzers
        for (int i = 0; i < 4; i++) {
            futures.add(executor.submit(() -> {
                while (run.get()) {
                    updatableAnalyzers.update(new HashMap<>());
                    ThreadUtils.sleep(RandomUtils.nextLong(0, 50), TimeUnit.MILLISECONDS);
                }
            }));
        }

        // 4 threads acquire the analyzers
        for (int i = 0; i < 4; i++) {
            futures.add(executor.submit(() -> {
                while (run.get()) {
                    try (UpdatableAnalyzers.Analyzers analyzers = updatableAnalyzers.getAnalyzers()) {
                        Assert.assertNotNull(analyzers);
                        ThreadUtils.sleep(RandomUtils.nextLong(0, 50), TimeUnit.MILLISECONDS);
                    }
                }
            }));
        }

        // Let's run for 10 seconds
        ThreadUtils.sleep(10, TimeUnit.SECONDS);
        run.set(false);

        for (Future future : futures)
            future.get();

        // At the end, we should only have 1 active analyzer
        Assert.assertEquals(1, updatableAnalyzers.getActiveAnalyzers());
        updatableAnalyzers.close();
        Assert.assertEquals(0, updatableAnalyzers.getActiveAnalyzers());
    }

    @Index(name = "index")
    public static class IndexRecord {

        @IndexField(name = FieldDefinition.ID_FIELD, template = StringField, stored = true)
        final public String id = RandomUtils.alphanumeric(10);

        @IndexField(analyzer = "en.EnglishAnalyzer", indexOptions = IndexOptions.DOCS_AND_FREQS_AND_POSITIONS)
        final public String title = RandomUtils.alphanumeric(10);

        @IndexField(analyzerClass = StandardAnalyzer.class, indexOptions = IndexOptions.DOCS_AND_FREQS_AND_POSITIONS)
        final public String titleStd = RandomUtils.alphanumeric(10);
    }

    public void checkActiveAnalyzers(int expectedCount, AnnotatedIndexService<IndexRecord>... services) {
        for (AnnotatedIndexService<IndexRecord> service : services) {
            final IndexStatus status = service.getIndexStatus();
            Assert.assertEquals(expectedCount, status.activeIndexAnalyzers, 0);
            Assert.assertEquals(expectedCount, status.activeQueryAnalyzers, 0);
        }
    }

    @Test
    public void replication() throws IOException, URISyntaxException {

        final Path path = Files.createTempDirectory("updatable-replication");

        final IndexManager indexManager = new IndexManager(path, executor);
        final IndexServiceInterface service = indexManager.getService();

        // Get the master index service
        final AnnotatedIndexService<IndexRecord> master =
            new AnnotatedIndexService<>(service, IndexRecord.class, "master", null);
        master.createUpdateIndex();
        master.createUpdateFields();

        // Get the slave index
        final AnnotatedIndexService<IndexRecord> slave =
            new AnnotatedIndexService<>(service, IndexRecord.class, "slave",
                IndexSettingsDefinition.of().master("master").build());
        slave.createUpdateIndex();
        slave.createUpdateFields();

        checkActiveAnalyzers(1, master, slave);

        final QueryDefinition query =
            QueryDefinition.of(QueryParser.of("title").setQueryString("a").build()).returnedField("*").build();

        master.postDocument(new IndexRecord());
        master.searchQuery(query);
        slave.searchQuery(query);
        checkActiveAnalyzers(1, master, slave);
        slave.replicationCheck();

        checkActiveAnalyzers(1, master, slave);

    }
}
