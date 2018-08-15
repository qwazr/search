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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.logging.Logger;

@RunWith(Parameterized.class)
public abstract class PriorityQueueAbstractTest {

    protected final static Logger LOGGER = LoggerUtils.getLogger(PriorityQueueAbstractTest.class);

    protected final static int COUNT = 1_000_000;

    @Parameterized.Parameters
    public static Object[][] data() {
        return new Object[5][0];
    }

    /**
     * Initialize the PriorityQueue
     */
    protected abstract Object initQueue();

    /**
     * Fill the queue with ScoreDocs
     */
    protected abstract void fillQueue();

    @Test
    final public void benchmark() {
        final Reporter reporter = new Reporter();
        final Object queue = initQueue();
        reporter.report("Init ");
        fillQueue();
        reporter.report("Fill ");
        //Ensure the queue is not garbage collected
        Assert.assertNotNull(queue);
    }

    /**
     * This class stores information about memory usage and execution time
     */
    private static class Reporter {

        private long initialMemoryUsage;
        private long startTime;

        Reporter() {
            reinit();
        }

        private long getMemoryUsage() {
            Runtime.getRuntime().gc();
            return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        }

        public void reinit() {
            initialMemoryUsage = getMemoryUsage();
            startTime = System.nanoTime();
        }

        public void report(String msg) {
            final long duration = System.nanoTime() - startTime;
            final long memoryUsage = getMemoryUsage();
            LOGGER.info(msg + "- " + FileUtils.byteCountToDisplaySize(memoryUsage - initialMemoryUsage) + " - " +
                    ((double) duration / 1_000_000) + " ms");
        }
    }
}
