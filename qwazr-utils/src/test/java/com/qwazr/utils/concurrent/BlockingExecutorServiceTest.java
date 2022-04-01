/*
 * Copyright 2016-2020 Emmanuel Keller / QWAZR
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
package com.qwazr.utils.concurrent;

import com.qwazr.utils.RandomUtils;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import org.hamcrest.MatcherAssert;
import static org.hamcrest.Matchers.lessThan;
import org.junit.Assert;
import org.junit.Test;

public class BlockingExecutorServiceTest {

    public static class Counters {
        final AtomicInteger concurrent = new AtomicInteger(0);
        final AtomicInteger start = new AtomicInteger(0);
        final AtomicInteger end = new AtomicInteger(0);
        final AtomicInteger max = new AtomicInteger(0);

        private void randomEx() {
            if (RandomUtils.nextBoolean()) {
                throw new RuntimeException();
            }
        }

        public void run() {
            synchronized (max) {
                max.set(Math.max(concurrent.incrementAndGet(), max.get()));
            }
            start.incrementAndGet();
            ThreadUtils.sleep(500, TimeUnit.MILLISECONDS);
            concurrent.decrementAndGet();
            end.incrementAndGet();
        }

        public void runEx() {
            run();
            randomEx();
        }

        public Object callable() {
            run();
            return null;
        }

        public Object callableEx() {
            runEx();
            return null;
        }

        public void checkCounters(int poolSize, int startEnd) {
            Assert.assertEquals(concurrent.get(), 0);
            Assert.assertEquals(max.get(), poolSize);
            Assert.assertEquals(start.get(), startEnd);
            Assert.assertEquals(end.get(), startEnd);
        }
    }

    protected void testLoop(int poolSize, int count, BiConsumer<BlockingExecutorService, Counters> task) throws InterruptedException {
        final BlockingExecutorService executorService = new BlockingExecutorService(poolSize);
        final Counters counters = new Counters();
        for (int i = 0; i < count; i++) {
            task.accept(executorService, counters);
        }
        MatcherAssert.assertThat(executorService.availablePermits(), lessThan(poolSize));
        executorService.shutdown();
        Assert.assertTrue(executorService.awaitTermination(1, TimeUnit.MINUTES));
        Assert.assertEquals(poolSize, executorService.availablePermits());
        counters.checkCounters(poolSize, count);
    }

    @Test
    public void testSubmitRunnable() throws InterruptedException {
        testLoop(10, 50, (executorService, counters) -> executorService.submit(counters::run));
    }

    @Test
    public void testSubmitCallable() throws InterruptedException {
        testLoop(10, 50, (executorService, counters) -> executorService.submit(counters::callable));
    }

    @Test
    public void testSubmitRunnableWithResult() throws InterruptedException {
        testLoop(10, 50, (executorService, counters) -> executorService.submit(counters::run, new Object()));
    }

    @Test
    public void testExecute() throws InterruptedException {
        testLoop(10, 50, (executorService, counters) -> executorService.execute(counters::run));
    }

    @Test
    public void testSubmitRunnableExceptions() throws InterruptedException {
        testLoop(10, 50, (executorService, counters) -> executorService.submit(counters::runEx));
    }

    @Test
    public void testSubmitCallableExceptions() throws InterruptedException {
        testLoop(10, 50, (executorService, counters) -> executorService.submit(counters::callableEx));
    }

    @Test
    public void testSubmitRunnableWithResultExceptions() throws InterruptedException {
        testLoop(10, 50, (executorService, counters) -> executorService.submit(counters::runEx, new Object()));
    }

    @Test
    public void testExecuteExceptions() throws InterruptedException {
        testLoop(10, 50, (executorService, counters) -> executorService.execute(counters::runEx));
    }
    
}
