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
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.PriorityQueue;
import org.junit.BeforeClass;

public class PriorityQueue1Test extends PriorityQueueAbstractTest {

    private final static ScoreDoc[] ScoreDocs = new ScoreDoc[COUNT];

    private ScoreDocQueue scoreDocQueue;

    @BeforeClass
    public static void setup() {
        for (int i = 0; i < COUNT; i++)
            ScoreDocs[i] = new ScoreDoc(i, RandomUtils.nextFloat(0, 5), 1);
    }

    @Override
    public Object initQueue() {
        scoreDocQueue = new ScoreDocQueue(COUNT);
        return scoreDocQueue;
    }

    @Override
    public void fillQueue() {
        for (int i = 0; i < COUNT; i++)
            scoreDocQueue.insertWithOverflow(ScoreDocs[i]);
    }

    public class ScoreDocQueue extends PriorityQueue<ScoreDoc> {

        public ScoreDocQueue(int maxSize) {
            super(maxSize, true);
        }

        @Override
        protected boolean lessThan(ScoreDoc a, ScoreDoc b) {
            return a.score < b.score;
        }

        protected ScoreDoc getSentinelObject() {
            return new ScoreDoc(1, 1f);
        }
    }

}
