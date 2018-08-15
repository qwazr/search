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
package com.qwazr.search.experimental;

import com.qwazr.utils.RandomUtils;
import org.junit.BeforeClass;

public class PriorityQueue2Test extends PriorityQueueAbstractTest {

    private final static ScoreDoc2[] scoreDocs2 = new ScoreDoc2[COUNT];

    private ScoreDocQueue2 scoreDocQueue2;

    @BeforeClass
    public static void setup() {
        for (int i = 0; i < COUNT; i++)
            scoreDocs2[i] = new ScoreDoc2(i, RandomUtils.nextFloat(0, 5), 1);
    }

    @Override
    public Object initQueue() {
        scoreDocQueue2 = new ScoreDocQueue2(COUNT);
        return scoreDocQueue2;
    }

    @Override
    public void fillQueue() {
        for (int i = 0; i < COUNT; i++)
            scoreDocQueue2.insertWithOverflow(scoreDocs2[i]);
    }

    public static class ScoreDocQueue2 extends PriorityQueue2<ScoreDoc2> {

        private final ScoreDoc2 sentinel = new ScoreDoc2(-2, 0, -1);
        private final ScoreDoc2 doc1 = new ScoreDoc2(-1, 0, -1);
        private final ScoreDoc2 doc2 = new ScoreDoc2(-1, 0, -1);

        private final int[] docs;
        private final float[] scores;
        private final int[] shards;

        public ScoreDocQueue2(int maxSize) {
            super(maxSize, true);
            docs = new int[heapSize];
            scores = new float[heapSize];
            shards = new int[heapSize];
        }

        @Override
        protected void setHeap(int pos, ScoreDoc2 doc) {
            docs[pos] = doc.doc;
            scores[pos] = doc.score;
            shards[pos] = doc.shardIndex;
        }

        @Override
        protected ScoreDoc2 getHeap(int pos) {
            doc1.doc = docs[pos];
            doc1.score = scores[pos];
            doc1.shardIndex = shards[pos];
            return doc1;
        }

        @Override
        protected ScoreDoc2 getHeap2(int pos) {
            doc2.doc = docs[pos];
            doc2.score = scores[pos];
            doc2.shardIndex = shards[pos];
            return doc2;
        }

        @Override
        protected boolean lessThan(ScoreDoc2 a, ScoreDoc2 b) {
            return a.score < b.score;
        }

        protected ScoreDoc2 getSentinelObject() {
            return sentinel;
        }
    }

}
