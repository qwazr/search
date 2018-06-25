/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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

package com.qwazr.search.backups;

import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.index.SchemaSettingsDefinition;
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.search.test.units.IndexRecord;
import com.qwazr.utils.LoggerUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class BackupConcurrentTests extends AbstractIndexTest {

    private final static String SCHEMA_NAME = "backup_schema";

    private static Path backupPath;

    private static IndexServiceInterface service;

    private final static Logger LOGGER = LoggerUtils.getLogger(BackupConcurrentTests.class);

    @BeforeClass
    public static void setup() throws IOException, InterruptedException, URISyntaxException {
        initIndexManager();
        service = indexManager.getService();
        backupPath = Files.createTempDirectory("backup");
        service.createUpdateSchema(SCHEMA_NAME,
                SchemaSettingsDefinition.of().backupDirectoryPath(backupPath.toAbsolutePath().toString()).build());

        AnnotatedIndexService<IndexRecord.NoTaxonomy> indexNoTaxo =
                new AnnotatedIndexService<>(service, IndexRecord.NoTaxonomy.class, SCHEMA_NAME, "test1", null);
        indexNoTaxo.createUpdateIndex();
        indexNoTaxo.createUpdateFields();

        Collection<IndexRecord.NoTaxonomy> indexNoTaxonomyRecords = new ArrayList<>();
        for (int i = 0; i < 1000; i++)
            indexNoTaxonomyRecords.add(new IndexRecord.NoTaxonomy("id" + i));
        indexNoTaxo.postDocuments(indexNoTaxonomyRecords);

        AnnotatedIndexService<IndexRecord.WithTaxonomy> indexWithTaxo =
                new AnnotatedIndexService<>(service, IndexRecord.WithTaxonomy.class, SCHEMA_NAME, "test1", null);
        indexNoTaxo.createUpdateIndex();
        indexNoTaxo.createUpdateFields();

        Collection<IndexRecord.WithTaxonomy> indexWithTaxonomyRecords = new ArrayList<>();
        for (int i = 0; i < 1000; i++)
            indexWithTaxonomyRecords.add(new IndexRecord.WithTaxonomy("id" + i));
        indexWithTaxo.postDocuments(indexWithTaxonomyRecords);
    }

    private void doBackup() {
        Assert.assertFalse(service.doBackup("*", "*", "backup1").isEmpty());
        Assert.assertFalse(service.doBackup("*", "*", "backup2").isEmpty());
        service.deleteBackups("*", "*", "backup2");
        service.deleteBackups("*", "*", "backup1");
        Assert.assertNotNull(service.getBackups("*", "*", "*", false));
    }

    @Test
    public void test() throws InterruptedException, ExecutionException {
        final ExecutorService executors = Executors.newCachedThreadPool();
        final AtomicInteger counter = new AtomicInteger(100);
        final long endTime = System.currentTimeMillis() + 1000 * 10;
        final Collection<Future> futures = new ArrayList<>();
        try {
            for (int i = 0; i < 4; i++) {
                final int threadId = i;
                futures.add(executors.submit(() -> {
                    int count = 0;
                    while (System.currentTimeMillis() < endTime || counter.get() > 0) {
                        doBackup();
                        counter.decrementAndGet();
                        count++;
                    }
                    LOGGER.info("Done: " + threadId + " - " + count);
                }));
            }
            for (Future future : futures)
                future.get();
        } finally {
            executors.shutdown();
            executors.awaitTermination(1, TimeUnit.MINUTES);
        }
    }
}
