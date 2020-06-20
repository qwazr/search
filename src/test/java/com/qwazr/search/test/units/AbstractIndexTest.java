/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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
package com.qwazr.search.test.units;

import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.index.ExplainDefinition;
import com.qwazr.search.index.IndexManager;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.index.ResultDocumentObject;
import com.qwazr.utils.FileUtils;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.concurrent.ExecutorUtils;
import org.junit.AfterClass;
import org.junit.Assert;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public abstract class AbstractIndexTest {

    private static Path rootDirectory;
    protected static IndexManager indexManager;
    private static ExecutorService executor;

    static final Logger LOGGER = LoggerUtils.getLogger(AbstractIndexTest.class);

    protected static IndexManager initIndexManager(final boolean withExecutorService,
                                                   final Path backupDirectoryPath) {
        try {
            if (executor != null || rootDirectory != null || indexManager != null)
                throw new RuntimeException("IndexManager already setup");
            executor = withExecutorService ? Executors.newCachedThreadPool() : null;
            rootDirectory = Files.createTempDirectory("qwazr_index_test");
            return indexManager = new IndexManager(rootDirectory, executor, backupDirectoryPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static IndexManager initIndexManager(final boolean withExecutorService) {
        return initIndexManager(withExecutorService, null);
    }

    protected static IndexManager initIndexManager() {
        return initIndexManager(true);
    }

    protected static <T> AnnotatedIndexService<T> initIndexService(final boolean withExecutorService,
                                                                   final Class<T> recordClass)
        throws URISyntaxException {
        if (indexManager == null)
            initIndexManager(withExecutorService, null);
        final AnnotatedIndexService<T> indexService = indexManager.getService(recordClass);
        indexService.createUpdateIndex();
        indexService.createUpdateFields();
        return indexService;
    }

    protected static <T> AnnotatedIndexService<T> initIndexService(Class<T> recordClass) throws URISyntaxException {
        return initIndexService(true, recordClass);
    }

    <T> ResultDefinition.WithObject<T> checkQuery(AnnotatedIndexService<T> indexService, QueryDefinition queryDef,
                                                  Long hitsExpected, String queryDebug) {
        final ResultDefinition.WithObject<T> result = indexService.searchQuery(queryDef);
        Assert.assertNotNull(result);
        if (result.query != null)
            LOGGER.info(result.query);
        if (hitsExpected != null) {
            Assert.assertEquals(hitsExpected.longValue(), result.totalHits);
            if (hitsExpected > 0) {
                ExplainDefinition explain = indexService.explainQuery(queryDef, result.documents.get(0).getDoc());
                Assert.assertNotNull(explain);
            }
        }
        if (queryDebug != null)
            Assert.assertEquals(queryDebug, result.getQuery());
        return result;
    }

    <T> ResultDefinition.WithObject<T> checkQuery(AnnotatedIndexService<T> indexService, QueryDefinition queryDef) {
        return checkQuery(indexService, queryDef, 1L, null);
    }

    protected <T> void checkResult(final ResultDefinition.WithObject<T> result, long totalHits) {
        Assert.assertNotNull(result);
        Assert.assertEquals(totalHits, result.getTotalHits(), 0);
    }

    protected <T extends IndexRecord<?>> void checkRecord(final ResultDocumentObject<T> record,
                                                          final String id,
                                                          final double score) {
        Assert.assertEquals(record.getRecord().id, id);
        Assert.assertEquals(score, record.getScore(), 0.0001);
    }

    @AfterClass
    public static void releaseIndexManager() throws IOException, InterruptedException {
        if (indexManager != null) {
            indexManager.close();
            indexManager = null;
        }
        if (executor != null) {
            ExecutorUtils.close(executor, 5, TimeUnit.MINUTES);
            executor = null;
        }
        if (rootDirectory != null) {
            FileUtils.deleteDirectory(rootDirectory);
            rootDirectory = null;
        }
    }

    public static class WithIndexRecord<T extends IndexRecord<T>> extends AbstractIndexTest {

        private final Class<T> indexRecordClass;

        private AnnotatedIndexService<T> service;

        protected WithIndexRecord(Class<T> indexRecordClass) {
            this.indexRecordClass = indexRecordClass;
        }

        public synchronized AnnotatedIndexService<T> getIndexService() {
            if (service == null) {
                try {
                    service = indexManager.getService(indexRecordClass);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
            return service;
        }

        public ResultDefinition.WithObject<T> checkQuery(QueryDefinition queryDef, Long hitsExpected,
                                                         String queryDebug) {
            return checkQuery(getIndexService(), queryDef, hitsExpected, queryDebug);
        }

        public ResultDefinition.WithObject<T> checkQuery(QueryDefinition queryDef) {
            return checkQuery(queryDef, 1L, null);
        }

        public static class WithTaxonomy extends WithIndexRecord<IndexRecord.WithTaxonomy> {

            public static AnnotatedIndexService<IndexRecord.WithTaxonomy> indexService;

            public static void initIndexService() throws URISyntaxException {
                indexService = AbstractIndexTest.initIndexService(IndexRecord.WithTaxonomy.class);
            }

            protected WithTaxonomy() {
                super(IndexRecord.WithTaxonomy.class);
            }

            public IndexRecord.WithTaxonomy getNewRecord(String id) {
                return new IndexRecord.WithTaxonomy(id);
            }
        }

        public static class NoTaxonomy extends WithIndexRecord<IndexRecord.NoTaxonomy> {

            public static AnnotatedIndexService<IndexRecord.NoTaxonomy> indexService;

            protected static void initIndexService() throws URISyntaxException {
                indexService = AbstractIndexTest.initIndexService(IndexRecord.NoTaxonomy.class);
            }

            protected static void initIndexService(boolean withExecutorService) throws URISyntaxException {
                indexService = AbstractIndexTest.initIndexService(withExecutorService, IndexRecord.NoTaxonomy.class);
            }

            protected NoTaxonomy() {
                super(IndexRecord.NoTaxonomy.class);
            }

            public IndexRecord.NoTaxonomy getNewRecord(String id) {
                return new IndexRecord.NoTaxonomy(id);
            }

        }
    }
}
