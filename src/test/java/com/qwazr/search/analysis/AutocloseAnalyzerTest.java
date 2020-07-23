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

package com.qwazr.search.analysis;

import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.annotations.Index;
import com.qwazr.search.annotations.IndexField;
import com.qwazr.search.field.FieldDefinition;
import static com.qwazr.search.field.FieldDefinition.Template.StringField;
import com.qwazr.search.index.IndexManager;
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.IndexStatus;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.query.QueryParser;
import com.qwazr.utils.RandomUtils;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexOptions;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class AutocloseAnalyzerTest {

    private static ExecutorService executor;

    @BeforeClass
    public static void setup() {
        executor = Executors.newCachedThreadPool();
    }

    @AfterClass
    public static void cleanup() {
        executor.shutdownNow();
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
            Assert.assertEquals(expectedCount, status.activeAnalyzers, 1);
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
        checkActiveAnalyzers(1, master);
        master.createUpdateFields();
        checkActiveAnalyzers(1, master);

        // Get the slave index
        final AnnotatedIndexService<IndexRecord> slave =
            new AnnotatedIndexService<>(service, IndexRecord.class, "slave",
                IndexSettingsDefinition.of().master("master").build());
        slave.createUpdateIndex();
        checkActiveAnalyzers(1, slave);
        slave.createUpdateFields();
        checkActiveAnalyzers(1, slave);

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
