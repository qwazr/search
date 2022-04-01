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
 **/
package com.qwazr.search.index;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

abstract class ResultDocumentsList<T extends ResultDocumentAbstract>
    implements ResultDocuments<T>, ResultDocumentsInterface {

    private final List<ResultDocumentBuilder<T>> documentsBuilder;
    private final ReturnedFieldStrategy returnedFieldStrategy;
    protected final int start;

    ResultDocumentsList(final int start, final ReturnedFieldStrategy returnedFieldStrategy) {
        this.start = start;
        this.returnedFieldStrategy = returnedFieldStrategy;
        this.documentsBuilder = new ArrayList<>();
    }

    protected abstract ResultDocumentBuilder<T> newResultDocumentBuilder(final int absolutePos,
                                                                         final ScoreDoc scoreDoc);

    protected abstract ResultDefinition<T> newResultDefinition(final ResultDocumentsBuilder resultDocumentsBuilder,
                                                               final List<T> documents);

    @Override
    final public void doc(final IndexSearcher searcher, final int pos, final ScoreDoc scoreDoc) throws IOException {
        final ResultDocumentBuilder<T> builder = newResultDocumentBuilder(start + pos, scoreDoc);
        if (builder == null)
            return;
        returnedFieldStrategy.extract(searcher, builder);
        documentsBuilder.add(builder);
    }

    @Override
    final public void highlight(int pos, String name, String snippet) {
        documentsBuilder.get(pos).setHighlight(name, snippet);
    }

    @Override
    final public ResultDefinition<T> apply(ResultDocumentsBuilder resultDocumentsBuilder) {
        final List<T> documents = new ArrayList<>(documentsBuilder.size());
        documentsBuilder.forEach(builder -> documents.add(builder.build()));
        return newResultDefinition(resultDocumentsBuilder, documents);
    }

    @Override
    final public ResultDocumentsInterface getResultDocuments() {
        return this;
    }

}
