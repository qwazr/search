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

import com.qwazr.utils.FileUtils;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.RandomUtils;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.PriorityQueue;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.logging.Logger;

@RunWith(Parameterized.class)
public class PriorityQueueTest {

    private final static Logger LOGGER = LoggerUtils.getLogger(PriorityQueueTest.class);

    private final static int COUNT = 100;

    @Parameterized.Parameters
    public static Object[][] data() {
        return new Object[10][0];
    }

    private long getMemoryUsage() {
        Runtime.getRuntime().gc();
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    private final static ScoreDoc[] ScoreDocs = new ScoreDoc[COUNT];

    @BeforeClass
    public static void setup() {
        for (int i = 0; i < COUNT; i++)
            ScoreDocs[i] = new ScoreDoc(i, RandomUtils.nextFloat(0, 5), 1);
    }

    @Test
    public void testScoreDocArray() {
        final long before = getMemoryUsage();
        final long start = System.nanoTime();

        final ScoreDocQueue scoreDocQueue = new ScoreDocQueue(COUNT);

        for (int i = 0; i < COUNT; i++)
            scoreDocQueue.insertWithOverflow(ScoreDocs[i]);

        final long duration = System.nanoTime() - start;
        final long after = getMemoryUsage();

        LOGGER.info("SCOREDOC: " + FileUtils.byteCountToDisplaySize(after - before) + " - " + duration + " ns");
        Assert.assertNotNull(scoreDocQueue);
    }

    @Test
    public void testPrimitiveArrays() {
        final long before = getMemoryUsage();
        final long start = System.nanoTime();

        int docs[] = new int[COUNT];
        float scores[] = new float[COUNT];
        int shards[] = new int[COUNT];
        for (int i = 0; i < COUNT; i++) {
            docs[i] = 1;
            scores[i] = 1f;
            shards[i] = 0;
        }

        final long duration = System.nanoTime() - start;
        final long after = getMemoryUsage();

        LOGGER.info("ARRAYS: " + FileUtils.byteCountToDisplaySize(after - before) + " - " + duration + " ns");
        Assert.assertNotNull(docs);
        Assert.assertNotNull(scores);
        Assert.assertNotNull(shards);
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
