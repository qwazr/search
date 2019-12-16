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

package com.qwazr.search.collector;

import com.qwazr.search.query.lucene.FilteredQuery;
import it.unimi.dsi.fastutil.floats.Float2ReferenceRBTreeMap;
import it.unimi.dsi.fastutil.floats.Float2ReferenceSortedMap;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Scorable;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.RoaringDocIdSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntConsumer;

public class CollapseCollector extends BaseCollector<CollapseCollector.Query> {

    private final String fieldName;
    private final int maxRows;
    private final List<CollapseLeafCollector> leafCollectors;

    public CollapseCollector(String collectorName, final String fieldName, final Integer maxRows) {
        super(collectorName);
        this.fieldName = fieldName;
        this.maxRows = Objects.requireNonNull(maxRows, "The maxRows parameter is missing");
        this.leafCollectors = new ArrayList<>();
    }

    @Override
    public LeafCollector getLeafCollector(final LeafReaderContext context) throws IOException {
        final CollapseLeafCollector leafCollector = new CollapseLeafCollector(context);
        leafCollectors.add(leafCollector);
        return leafCollector;
    }

    @Override
    public CollapseCollector.Query getResult() {

        // Fill the priority queue wich the results of each segments
        final GroupQueue groupQueue = new GroupQueue(maxRows);
        leafCollectors.forEach(leaf -> leaf.reduce(groupQueue));

        // Stores for each doc the number of collapsed documents
        final Int2IntLinkedOpenHashMap collapsedMap = new Int2IntLinkedOpenHashMap(groupQueue.groupLeaders.size());

        // The DocID must be sorted and grouped by segment
        final Map<LeafReaderContext, IntSortedSet> sortedInts = new HashMap<>();
        long collapsedCount = 0;
        for (final GroupLeader groupLeader : groupQueue.groupLeaders.values()) {
            sortedInts.computeIfAbsent(groupLeader.context, ctx -> new IntAVLTreeSet()).add(groupLeader.doc);
            collapsedMap.addTo(groupLeader.context.docBase + groupLeader.doc, groupLeader.collapsedCount);
            collapsedCount += groupLeader.collapsedCount;
        }

        // Now we can build the bitsets
        final Map<LeafReaderContext, RoaringDocIdSet> docIdMaps = new HashMap<>();
        sortedInts.forEach((ctx, sortedInt) -> {
            final RoaringDocIdSet.Builder builder = new RoaringDocIdSet.Builder(ctx.reader().maxDoc());
            sortedInt.forEach((IntConsumer) builder::add);
            docIdMaps.put(ctx, builder.build());
        });

        // Add empty bitset for unassigned leaf
        leafCollectors.forEach(leaf -> docIdMaps.putIfAbsent(leaf.context,
                new RoaringDocIdSet.Builder(leaf.context.reader().maxDoc()).build()));

        return new Query(new FilteredQuery(docIdMaps), collapsedMap, collapsedCount);
    }

    final class CollapseLeafCollector implements LeafCollector {

        private final LeafReaderContext context;
        private final SortedDocValues sdv;
        private final Int2IntMap docIds;
        private final Int2FloatMap scores;
        private final Int2IntMap count;

        private Scorable scorer;

        CollapseLeafCollector(LeafReaderContext context) throws IOException {
            this.context = context;
            final LeafReader reader = context.reader();
            sdv = reader.getSortedDocValues(fieldName);
            final int numDocs = reader.numDocs();
            docIds = new Int2IntOpenHashMap(numDocs);
            scores = new Int2FloatOpenHashMap(numDocs);
            scores.defaultReturnValue(-1);
            count = new Int2IntOpenHashMap(numDocs);
            count.defaultReturnValue(0);
        }

        @Override
        final public void setScorer(final Scorable scorer) {
            this.scorer = scorer;
        }

        void reduce(final GroupQueue groupQueue) {
            docIds.keySet().forEach((IntConsumer) ord -> {
                try {
                    groupQueue.offer(sdv.lookupOrd(ord), scores.get(ord), count.get(ord),
                            (bytesRef, score, collapsedCount) -> new GroupLeader(context, bytesRef, docIds.get(ord),
                                    score, collapsedCount));
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        @Override
        final public void collect(final int doc) throws IOException {
            sdv.advance(doc);
            final int ord = sdv.ordValue();
            if (ord == -1)
                return;
            final float score = scorer.score();
            final float oldScore = scores.get(ord);
            if (score > oldScore) {
                scores.put(ord, score);
                docIds.put(ord, doc);
            }
            count.put(ord, count.get(ord) + 1);
        }
    }

    final static class GroupLeader extends ScoreDoc {

        final LeafReaderContext context;
        final BytesRef bytesRef;
        int collapsedCount;

        GroupLeader(final LeafReaderContext context, final BytesRef bytesRef, final int doc, final float score,
                    final int collapsedCount) {
            super(doc, score);
            this.context = context;
            this.bytesRef = bytesRef;
            this.collapsedCount = collapsedCount;
        }
    }

    @FunctionalInterface
    interface GroupLeaderProvider {
        GroupLeader get(BytesRef bytesRef, float score, int collapsedCount);
    }

    final static class GroupQueue {

        private final int maxSize;
        final Map<BytesRef, GroupLeader> groupLeaders;
        private final Float2ReferenceSortedMap<Object2BooleanLinkedOpenHashMap<BytesRef>> scoreGroups;

        GroupQueue(final int maxSize) {
            this.maxSize = maxSize;
            groupLeaders = new HashMap<>();
            scoreGroups = new Float2ReferenceRBTreeMap<>();
        }

        void offer(final BytesRef bytesRef, final float score, final int count,
                   final GroupLeaderProvider groupLeaderProvider) {

            // Do we already have a leader ? If the score is greater we can ignore the offered one
            final GroupLeader previousGroupLeader = groupLeaders.get(bytesRef);
            if (previousGroupLeader != null && score <= previousGroupLeader.score) {
                previousGroupLeader.collapsedCount += count;
                return;
            }

            final BytesRef newBytesRef =
                    previousGroupLeader == null ? BytesRef.deepCopyOf(bytesRef) : previousGroupLeader.bytesRef;
            groupLeaders.put(newBytesRef, groupLeaderProvider.get(newBytesRef, score,
                    previousGroupLeader == null ? count - 1 : previousGroupLeader.collapsedCount + count));

            Object2BooleanLinkedOpenHashMap<BytesRef> bytesRefs = scoreGroups.get(score);
            if (bytesRefs == null) {
                bytesRefs = new Object2BooleanLinkedOpenHashMap<>();
                scoreGroups.put(score, bytesRefs);
            }
            bytesRefs.put(newBytesRef, true);

            if (previousGroupLeader != null) {
                bytesRefs = scoreGroups.get(previousGroupLeader.score);
                bytesRefs.remove(newBytesRef, true);
                if (bytesRefs.isEmpty())
                    scoreGroups.remove(previousGroupLeader.score);
            }

            if (groupLeaders.size() > maxSize) {
                final float firstScoreKey = scoreGroups.firstFloatKey();
                bytesRefs = scoreGroups.get(firstScoreKey);
                final BytesRef lastKey = bytesRefs.lastKey();
                bytesRefs.removeLastBoolean();
                groupLeaders.remove(lastKey);
                if (bytesRefs.size() == 0)
                    scoreGroups.remove(firstScoreKey);
            }
        }
    }

    public static class Query extends FilterCollector.Query {

        final Int2IntLinkedOpenHashMap collapsedMap;
        final long collapsedCount;

        Query(final FilteredQuery filteredQuery, final Int2IntLinkedOpenHashMap collapsedMap,
              final long collapsedCount) {
            super(filteredQuery);
            this.collapsedMap = collapsedMap;
            this.collapsedCount = collapsedCount;
        }

        public long getCollapsed() {
            return collapsedCount;
        }

        public int getCollapsed(int docId) {
            return collapsedMap.getOrDefault(docId, -1);
        }

    }
}
