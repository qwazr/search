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
package com.qwazr.utils.concurrent;

import com.qwazr.utils.IOUtils;
import com.qwazr.utils.RandomUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ReferenceCounterTest {

    private static ExecutorService executorService;

    @BeforeClass
    public static void setup() {
        executorService = Executors.newFixedThreadPool(4);
    }

    @AfterClass
    public static void cleanup() throws InterruptedException {
        ExecutorUtils.close(executorService, 1, TimeUnit.MINUTES);
    }

    private final static int MAX_ITERATIONS = 2000;

    @Test
    public void multiThreadTest() throws InterruptedException, IOException, ExecutionException {

        final Item item = new Item();
        item.acquire();

        final List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            futures.add(executorService.submit(() -> {
                while (!item.done()) {
                    item.acquire();
                    try {
                        item.action();
                    } finally {
                        IOUtils.closeQuietly(item);
                    }
                }
            }));
        }

        item.close();

        for (Future future : futures)
            future.get();

        Assert.assertFalse(item.open);
        Assert.assertTrue(item.counter.get() >= MAX_ITERATIONS);
    }

    @Test(expected = IOException.class)
    public void doubleReleaseExceptionTest() throws IOException {
        final Item item = new Item();
        item.acquire();
        item.close();
        item.close();
    }

    public static class Item implements Closeable {

        private final AtomicInteger counter = new AtomicInteger();
        private final ReferenceCounter.Impl refCounter = new ReferenceCounter.Impl();

        private boolean open = true;

        public void action() {
            ThreadUtils.sleep(RandomUtils.nextLong(0, 10), TimeUnit.MILLISECONDS);
            counter.incrementAndGet();
        }

        public Item acquire() {
            refCounter.acquire();
            return this;
        }

        public boolean done() {
            return counter.get() > MAX_ITERATIONS;
        }

        @Override
        public synchronized void close() throws IOException {
            if (refCounter.release() > 0)
                return;
            if (!open)
                throw new IOException("Already close");
            open = false;
        }
    }
}
