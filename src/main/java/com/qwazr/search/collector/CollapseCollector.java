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

package com.qwazr.search.collector;

import com.qwazr.search.query.lucene.FilteredQuery;
import com.qwazr.utils.Equalizer;
import com.qwazr.utils.HashUtils;
import com.qwazr.utils.concurrent.ThreadUtils;
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
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Scorable;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.RoaringDocIdSet;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntConsumer;
import org.roaringbitmap.RoaringBitmap;

public class CollapseCollector extends BaseCollector.Parallel<CollapseCollector.Query, CollapseCollector.Leaf, CollapseCollector> {

    private final String fieldName;
    private final int maxRows;

    public CollapseCollector(final String fieldName, final Integer maxRows) {
        super(ScoreMode.COMPLETE);
        this.fieldName = fieldName;
        this.maxRows = Objects.requireNonNull(maxRows, "The maxRows parameter is missing");
    }

    @Override
    public Leaf newLeafCollector(final LeafReaderContext context) throws IOException {
        return new Leaf(fieldName, context);
    }

    private void reduce(final GroupQueue groupQueue,
                        final Map<Integer, RoaringDocIdSet> docIdMaps,
                        final Int2IntLinkedOpenHashMap collapsedMap) {

        // Fill the priority queue which the results of each segments
        getLeaves().forEach(leaf -> leaf.reduce(groupQueue));

        // The DocID must be sorted and grouped by segment
        final Map<Integer, Pair<Integer, IntSortedSet>> sortedInts = new HashMap<>();
        for (final GroupLeader groupLeader : groupQueue.groupLeaders.values()) {
            sortedInts.computeIfAbsent(groupLeader.docBase,
                leaf -> Pair.of(groupLeader.maxDoc, new IntAVLTreeSet())).getValue().add(groupLeader.doc);
            collapsedMap.addTo(groupLeader.docBase + groupLeader.doc, groupLeader.collapsedCount);
        }

        // Now we can build the bitsets
        sortedInts.forEach((docBase, sortedInt) -> {
            final RoaringDocIdSet.Builder builder = new RoaringDocIdSet.Builder(sortedInt.getKey());
            sortedInt.getValue().forEach((IntConsumer) builder::add);
            docIdMaps.put(docBase, builder.build());
        });

        // Add empty bitset for unassigned leaf
        getLeaves().forEach(leaf -> docIdMaps.computeIfAbsent(leaf.docBase,
            ctx -> new RoaringDocIdSet.Builder(leaf.maxDoc).build()));

    }

    @Override
    final public Query reduce(final List<CollapseCollector> leafCollectors) {
        final Map<Integer, RoaringDocIdSet> docIdMaps = new ConcurrentHashMap<>();
        final GroupQueue groupQueue = new GroupQueue(maxRows);
        // Stores for each doc the number of collapsed documents
        final Int2IntLinkedOpenHashMap collapsedMap = new Int2IntLinkedOpenHashMap(groupQueue.groupLeaders.size());

        leafCollectors.forEach(collector -> collector.reduce(groupQueue, docIdMaps, collapsedMap));

        long collapsedCount = 0;
        for (final GroupLeader groupLeader : groupQueue.groupLeaders.values())
            collapsedCount += groupLeader.collapsedCount;

        return new Query(new FilteredQuery(docIdMaps), collapsedMap, collapsedCount);
    }

    final static class Leaf extends Equalizer.Immutable<Leaf> implements LeafCollector {

        private final SortedDocValues sdv;
        private final Int2IntMap docIds;
        private final Int2FloatMap scores;
        private final Int2IntMap count;
        private final int docBase;
        private final int maxDoc;

        private Scorable scorer;

        private Leaf(final String fieldName, final LeafReaderContext context) throws IOException {
            super(Leaf.class);
            final LeafReader reader = context.reader();
            sdv = reader.getSortedDocValues(fieldName);
            final int numDocs = reader.numDocs();
            docIds = new Int2IntOpenHashMap(numDocs);
            scores = new Int2FloatOpenHashMap(numDocs);
            scores.defaultReturnValue(-1);
            count = new Int2IntOpenHashMap(numDocs);
            count.defaultReturnValue(0);
            docBase = context.docBase;
            maxDoc = reader.maxDoc();
        }

        @Override
        protected boolean isEqual(Leaf leaf) {
            return leaf == this;
        }

        @Override
        protected int computeHashCode() {
            return docBase;
        }

        @Override
        final public void setScorer(final Scorable scorer) {
            this.scorer = scorer;
        }

        void reduce(final GroupQueue groupQueue) {
            docIds.keySet().forEach((IntConsumer) ord -> {
                try {
                    groupQueue.offer(sdv.lookupOrd(ord), scores.get(ord), count.get(ord),
                        (bytesRef, score, collapsedCount) -> new GroupLeader(docBase, maxDoc, bytesRef, docIds.get(ord) + docBase,
                            score, collapsedCount));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        @Override
        final public void collect(final int doc) throws IOException {
            if (sdv.advance(doc) != doc)
                return;
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

        final int docBase;
        final int maxDoc;
        final BytesRef bytesRef;
        int collapsedCount;

        GroupLeader(final int docBase,
                    final int maxDoc,
                    final BytesRef bytesRef,
                    final int doc,
                    final float score,
                    final int collapsedCount) {
            super(doc, score);
            this.maxDoc = maxDoc;
            this.docBase = docBase;
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

        synchronized void offer(final BytesRef bytesRef,
                                final float score,
                                final int count,
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

        Query(final FilteredQuery filteredQuery,
              final Int2IntLinkedOpenHashMap collapsedMap,
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
