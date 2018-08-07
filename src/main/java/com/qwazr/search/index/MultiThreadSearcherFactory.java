/*
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

import com.qwazr.utils.concurrent.ConsumerEx;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.SimpleMergedSegmentWarmer;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.InfoStream;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

class MultiThreadSearcherFactory extends SearcherFactory {

    private final static ConsumerEx<IndexReader, IOException> WITHOUT_WARM = reader -> {
    };

    private final static ConsumerEx<IndexReader, IOException> WITH_WARM = MultiThreadSearcherFactory::warmReader;

    static MultiThreadSearcherFactory of(final ExecutorService executorService, final boolean useWarmer,
                                         final Similarity similarity, final String stateFacetField) {
        return similarity == null ?
                new MultiThreadSearcherFactory(executorService, stateFacetField, useWarmer ? WITH_WARM : WITHOUT_WARM) :
                new WithSimilarity(executorService, similarity, stateFacetField, useWarmer ? WITH_WARM : WITHOUT_WARM);
    }

    protected final ExecutorService executorService;
    private final String stateFacetField;
    private final ConsumerEx<IndexReader, IOException> readerWarmer;

    private MultiThreadSearcherFactory(final ExecutorService executorService, final String stateFacetField,
                                       final ConsumerEx<IndexReader, IOException> readerWarmer) {
        this.executorService = executorService;
        this.stateFacetField = stateFacetField;
        this.readerWarmer = readerWarmer;
    }

    final protected StateIndexSearcher warm(final IndexReader indexReader, final StateIndexSearcher indexSearcher)
            throws IOException {
        readerWarmer.accept(indexReader);
        return indexSearcher;
    }

    public StateIndexSearcher newSearcher(final IndexReader reader, final IndexReader previousReader)
            throws IOException {
        return warm(reader, new StateIndexSearcher(reader));
    }

    static class WithSimilarity extends MultiThreadSearcherFactory {

        private final Similarity similarity;

        private WithSimilarity(final ExecutorService executorService, final Similarity similarity,
                               final String stateFacetField, final ConsumerEx<IndexReader, IOException> readerWarmer) {
            super(executorService, stateFacetField, readerWarmer);
            this.similarity = similarity;
        }

        public StateIndexSearcher newSearcher(final IndexReader reader, final IndexReader previousReader)
                throws IOException {
            final StateIndexSearcher searcher = new StateIndexSearcher(reader);
            searcher.setSimilarity(similarity);
            return warm(reader, searcher);
        }
    }

    class StateIndexSearcher extends IndexSearcher {

        final SortedSetDocValuesReaderState state;

        StateIndexSearcher(IndexReader reader) throws IOException {
            super(reader, executorService);
            state = IndexUtils.getNewFacetsState(reader, stateFacetField);
        }

    }

    private static final SimpleMergedSegmentWarmer WARMER = new SimpleMergedSegmentWarmer(InfoStream.getDefault());

    private static void warmReader(final IndexReader indexReader) throws IOException {
        for (final LeafReaderContext context : indexReader.leaves())
            WARMER.warm(context.reader());
    }

}
