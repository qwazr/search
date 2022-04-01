/**
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
package com.qwazr.utils.test;

import com.qwazr.utils.concurrent.ConcurrentQueue;
import com.qwazr.utils.concurrent.ExecutorUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ConcurrentQueueTest {

    private static ExecutorService executor;

    private final int MULTI_THREAD = Math.min(4, Runtime.getRuntime().availableProcessors());

    @BeforeClass
    public static void before() {
        executor = Executors.newCachedThreadPool();
    }

    @AfterClass
    public static void after() throws InterruptedException {
        ExecutorUtils.close(executor, 1, TimeUnit.MINUTES);
    }

    @Test
    public void quickQueue() {
        final AtomicInteger counter = new AtomicInteger();
        final int l = RandomUtils.nextInt(1000, 5000);
        try (final QuickQueue queue = new QuickQueue(counter)) {
            for (int i = 0; i < l; i++)
                queue.accept(RandomUtils.nextInt());
        }
        Assert.assertEquals(l, counter.get());
    }

    private final static Integer ENDING_ITEM = -1;

    private class QuickQueue extends ConcurrentQueue<Integer> {

        private final AtomicInteger counter;

        private QuickQueue(final AtomicInteger counter) {
            super(executor, MULTI_THREAD, ENDING_ITEM);
            this.counter = counter;
        }

        @Override
        protected Consumer<Integer> getNewConsumer() {
            return integer -> counter.incrementAndGet();
        }
    }

    @Test
    public void slowQueue() {
        final AtomicInteger counter = new AtomicInteger();
        final int l = RandomUtils.nextInt(5, 10);
        try (final SlowQueue queue = new SlowQueue(counter)) {
            for (int i = 0; i < l; i++)
                queue.accept(RandomUtils.nextInt());
        }
        Assert.assertEquals(l, counter.get());
    }

    private class SlowQueue extends ConcurrentQueue<Integer> {

        private final AtomicInteger counter;

        private SlowQueue(AtomicInteger counter) {
            super(executor, MULTI_THREAD, ENDING_ITEM);
            this.counter = counter;
        }

        @Override
        protected Consumer<Integer> getNewConsumer() {
            return integer -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                counter.incrementAndGet();
            };
        }
    }
}
