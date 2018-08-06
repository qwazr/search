/**
 * Copyright 2017 Emmanuel Keller / QWAZR
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

import com.qwazr.search.annotations.AnnotatedIndexService;

import javax.validation.constraints.NotNull;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class QueryDocumentsIterator<T> implements Iterator<T> {

    private final AnnotatedIndexService<?> service;
    private final Class<T> recordClass;

    protected final QueryBuilder queryBuilder;

    private long count;
    private long pos;

    private List<ResultDocumentObject<T>> currentDocuments;
    private int currentPos;

    public QueryDocumentsIterator(@NotNull final AnnotatedIndexService<?> service,
                                  @NotNull final QueryDefinition queryDefinition, @NotNull final Class<T> recordClass) {
        this.service = service;
        this.recordClass = recordClass;
        this.queryBuilder = new QueryBuilder(queryDefinition);
        this.count = 0;
        this.pos = 0;
        queryBuilder.start(0);
        if (queryBuilder.rows == null || queryBuilder.rows < 1)
            queryBuilder.rows = 100;
        nextExecution();
    }

    private synchronized boolean nextExecution() {
        final ResultDefinition.WithObject<T> result = service.searchQuery(queryBuilder.build(), recordClass);
        count = result.total_hits;
        currentPos = 0;
        currentDocuments = result.documents;
        queryBuilder.start(queryBuilder.start + queryBuilder.rows);
        return currentDocuments != null && !currentDocuments.isEmpty();
    }

    @Override
    final public synchronized boolean hasNext() {
        return pos < count;
    }

    @Override
    public synchronized T next() {
        if (currentDocuments == null || currentPos >= currentDocuments.size())
            if (!nextExecution())
                throw new NoSuchElementException();
        pos++;
        return currentDocuments.get(currentPos++).record;
    }
}
