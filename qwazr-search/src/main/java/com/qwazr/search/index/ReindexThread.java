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

import com.fasterxml.jackson.databind.JsonNode;
import com.qwazr.search.collector.BaseCollector;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.query.MatchAllDocs;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.ObjectMappers;
import com.qwazr.utils.StringUtils;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotFoundException;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.StoredFieldVisitor;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Scorable;
import org.apache.lucene.search.ScoreMode;

class ReindexThread {

    private final static Logger LOGGER = LoggerUtils.getLogger(ReindexThread.class);

    private final ExecutorService executorService;
    private final IndexInstance indexInstance;
    private final AtomicReference<ReindexProcess> currentTask;

    ReindexThread(final ExecutorService executorService, final IndexInstance indexInstance) {
        this.executorService = executorService;
        this.indexInstance = indexInstance;
        currentTask = new AtomicReference<>();
    }

    private ReindexDefinition compute(final Function<ReindexProcess, ReindexProcess> compute) {
        synchronized (currentTask) {
            final ReindexProcess oldIndexProcess = currentTask.get();
            final ReindexProcess newIndexProcess = compute.apply(oldIndexProcess);
            if (newIndexProcess != null && newIndexProcess != oldIndexProcess) {
                currentTask.set(newIndexProcess);
                return newIndexProcess.getStatus();
            }
            return oldIndexProcess == null ? ReindexDefinition.EMPTY : oldIndexProcess.getStatus();
        }
    }

    ReindexDefinition start(int bufferSize) {
        return compute(current -> {
            if (current != null)
                if (!current.future.isDone())
                    throw new NotAcceptableException("A reindexing process is currently running.");
            return new ReindexProcess(executorService, indexInstance, bufferSize);
        });
    }

    ReindexDefinition abort() {
        return compute(current -> {
            if (current == null)
                throw new NotFoundException("There is no reindexing process currently running.");
            if (current.future.isDone())
                throw new NotAcceptableException("The reindexing process is not running.");
            current.abort();
            return current;
        });
    }

    ReindexDefinition getStatus() {
        return compute(current -> null);
    }

    private static class ReindexProcess implements Runnable {

        private final AtomicBoolean abort;
        private final CompletableFuture<Void> future;
        private final Date startTime;
        private final String recordField;
        private final IndexInstance indexInstance;
        private volatile ReindexDefinition.Status status;
        private volatile Date endTime;
        private final AtomicLong completedRecords;
        private final AtomicLong numDocs;
        private volatile String error;
        private final List<JsonNode> buffer;
        private final int maxBufferSize;

        private ReindexProcess(final ExecutorService executorService,
                               final IndexInstance indexInstance,
                               final int maxBufferSize) {
            this.maxBufferSize = maxBufferSize;
            this.status = ReindexDefinition.Status.initialized;
            this.startTime = Date.from(Instant.now());
            this.indexInstance = indexInstance;
            this.abort = new AtomicBoolean(false);
            this.completedRecords = new AtomicLong(0);
            final IndexStatus indexStatus;
            try {
                indexStatus = indexInstance.getStatus();
            } catch (IOException e) {
                throw new InternalServerErrorException("Error while getting the index status: " + e.getMessage(), e);
            }
            if (StringUtils.isBlank(indexStatus.settings.primaryKey))
                throw new NotAcceptableException("Reindex requires a primary key. Check that your index configuration defines a primary key.");
            if (StringUtils.isBlank(indexStatus.settings.recordField))
                throw new NotAcceptableException("Reindex requires a record field. Check that your index configuration defines a record field.");
            this.recordField = indexStatus.settings.recordField;
            this.numDocs = new AtomicLong(indexStatus.numDocs == null ? 0 : indexStatus.numDocs);
            this.buffer = new ArrayList<>();
            this.future = CompletableFuture.runAsync(this, executorService);
        }


        private void abort() {
            abort.set(true);
        }

        private void indexBuffer() {
            if (buffer.isEmpty())
                return;
            try {
                completedRecords.addAndGet(indexInstance.postJsonNodes(buffer).count);
                buffer.clear();
            } catch (IOException e) {
                throw new InternalServerErrorException("Error while reindexing: " + e.getMessage(), e);
            }
        }

        private void newRecord(final byte[] recordBytes) {
            try {
                final JsonNode jsonNode = ObjectMappers.SMILE.readTree(recordBytes);
                synchronized (buffer) {
                    buffer.add(jsonNode);
                    if (buffer.size() >= maxBufferSize)
                        indexBuffer();
                }
            } catch (IOException e) {
                throw new InternalServerErrorException("Error while reading a record: " + e.getMessage(), e);
            }
        }

        @Override
        public void run() {
            try {
                final QueryDefinition recordsQuery =
                    QueryDefinition.of(MatchAllDocs.INSTANCE)
                        .rows(0)
                        .sort(FieldDefinition.DOC_FIELD, QueryDefinition.SortEnum.ascending)
                        .collector("records", ReindexCollector.class, recordField, this)
                        .build();
                status = ReindexDefinition.Status.running;
                indexInstance.query(queryContext -> {
                    queryContext.searchInterface(recordsQuery, ResultDocumentsInterface.NOPE);
                    return null;
                });
                indexBuffer();
                status = abort.get() ? ReindexDefinition.Status.aborted : ReindexDefinition.Status.done;
            } catch (Exception e) {
                error = e.getMessage();
                LOGGER.log(Level.SEVERE, e, () -> "Error while reindexing: " + error);
            } finally {
                endTime = Date.from(Instant.now());
            }
        }

        private ReindexDefinition getStatus() {
            final float completed = completedRecords.get();
            final float total = numDocs.get();
            final float completion = total == 0 ? 1 : completed == 0 ? 0 : total / completed;
            return new ReindexDefinition(startTime, endTime, completion * 100, status, error);
        }

    }

    public final static class ReindexCollector extends BaseCollector {

        private final String recordField;
        private final ReindexProcess reindexProcess;

        public ReindexCollector(final String recordField, final ReindexProcess reindexProcess) {
            super(ScoreMode.COMPLETE_NO_SCORES);
            this.recordField = recordField;
            this.reindexProcess = reindexProcess;
        }

        @Override
        protected LeafCollector newLeafCollector(final LeafReaderContext context) {
            return new Leaf(context.reader(), recordField, reindexProcess);
        }

        private final static class Leaf extends StoredFieldVisitor implements LeafCollector {

            private final String recordField;
            private final LeafReader leafReader;
            private final ReindexProcess reindexProcess;

            private Leaf(final LeafReader leafReader, final String recordField, final ReindexProcess reindexProcess) {
                this.leafReader = leafReader;
                this.recordField = recordField;
                this.reindexProcess = reindexProcess;
            }

            @Override
            final public void setScorer(final Scorable scorer) {

            }

            @Override
            final public void collect(int doc) throws IOException {
                leafReader.document(doc, this);
            }

            @Override
            public void binaryField(FieldInfo fieldInfo, byte[] value) {
                reindexProcess.newRecord(value);
            }

            @Override
            public Status needsField(final FieldInfo fieldInfo) {
                return fieldInfo != null && recordField.equals(fieldInfo.name) ? Status.YES : Status.NO;
            }
        }
    }
}
