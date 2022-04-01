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
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

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

    @Test
    final public void functionalTest() {
        initQueue();
        scoreDocQueue2.insertWithOverflow(scoreDocs2[0]);
        scoreDocQueue2.insertWithOverflow(scoreDocs2[1]);
        scoreDocQueue2.insertWithOverflow(scoreDocs2[2]);
        Assert.assertEquals(3, scoreDocQueue2.size());
        int i = 0;
        for (ScoreDoc2 scoreDoc2 : scoreDocQueue2) {
            Assert.assertTrue(scoreDoc2.doc >= 0);
            Assert.assertTrue(scoreDoc2.doc < COUNT);
            i++;
        }
        Assert.assertEquals(3, i);
    }

}
