/*
 *  Copyright 2015-2018 Emmanuel Keller / QWAZR
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.qwazr.search.query.lucene;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.BulkScorer;
import org.apache.lucene.search.ConstantScoreScorer;
import org.apache.lucene.search.ConstantScoreWeight;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.RoaringDocIdSet;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public final class FilteredQuery extends org.apache.lucene.search.Query {

    private final Map<LeafReaderContext, RoaringDocIdSet> docIdSetMap;

    public FilteredQuery(final Map<LeafReaderContext, RoaringDocIdSet> docIdSetMap) {
        this.docIdSetMap = docIdSetMap;
    }

    public void merge(final FilteredQuery filteredQuery) {
        docIdSetMap.putAll(filteredQuery.docIdSetMap);
    }

    @Override
    final public Weight createWeight(final IndexSearcher searcher, final ScoreMode scoreMode, final float boost) {
        return new ConstantScoreWeight(this, boost) {
            @Override
            public boolean isCacheable(LeafReaderContext ctx) {
                return false;
            }

            @Override
            final public String toString() {
                return "weight(" + FilteredQuery.this + ")";
            }

            @Override
            final public Scorer scorer(final LeafReaderContext context) throws IOException {
                final RoaringDocIdSet docIdSet = docIdSetMap.get(context);
                final DocIdSetIterator docIdSetIterator = docIdSet == null ? null : docIdSet.iterator();
                return new ConstantScoreScorer(this, score(), scoreMode,
                        docIdSetIterator == null ? DocIdSetIterator.empty() : docIdSetIterator);
            }

            @Override
            final public BulkScorer bulkScorer(final LeafReaderContext context) {

                final float score = score();
                final int maxDoc = context.reader().maxDoc();
                final RoaringDocIdSet docIdSet = docIdSetMap.get(context);
                final int cost = docIdSet.cardinality();
                final Weight weight = this;

                return new BulkScorer() {
                    @Override
                    final public int score(final LeafCollector collector, final Bits acceptDocs, final int min, int max)
                            throws IOException {
                        max = Math.min(max, maxDoc);
                            final FakeScorer scorer = new FakeScorer(weight);
                        scorer.score = score;
                        collector.setScorer(scorer);
                        int doc;
                        final DocIdSetIterator it = docIdSet.iterator();
                        if (it != null) {
                            while ((doc = it.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
                                if (acceptDocs == null || acceptDocs.get(doc)) {
                                    scorer.doc = doc;
                                    collector.collect(doc);
                                }
                            }
                        }
                        return max == maxDoc ? DocIdSetIterator.NO_MORE_DOCS : max;
                    }

                    @Override
                    final public long cost() {
                        return cost;
                    }
                };
            }
        };
    }

    @Override
    public String toString(String field) {
        return "(f)";
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FilteredQuery))
            return false;
        return Objects.equals(((FilteredQuery) o).docIdSetMap, docIdSetMap);
    }

    @Override
    public int hashCode() {
        return classHash();
    }

}
