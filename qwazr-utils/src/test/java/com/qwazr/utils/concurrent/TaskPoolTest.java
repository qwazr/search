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

import com.qwazr.utils.RandomUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TaskPoolTest {

    private static ExecutorService executorService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void setup() {
        executorService = Executors.newCachedThreadPool();
    }

    @AfterClass
    public static void cleanup() throws InterruptedException {
        ExecutorUtils.close(executorService, 1, TimeUnit.MINUTES);
    }

    void test(final int poolSize, final int valueNumber, boolean withResult) {
        final int[] values = new int[valueNumber];
        for (int i = 0; i < valueNumber; i++)
            values[i] = RandomUtils.nextInt(1, 100);

        final Set<Integer> resultValues = withResult ? ConcurrentHashMap.newKeySet() : null;

        final Set<Integer> returned = ConcurrentHashMap.newKeySet();
        try (final TaskPool pool = TaskPool.of(poolSize)) {
            for (int value : values)
                pool.submit(() -> {
                    returned.add(value);
                    if (resultValues != null)
                        resultValues.add(value);
                    return value;
                });
            while (pool.getConcurrentTasks() > 0)
                ThreadUtils.sleep(1, TimeUnit.SECONDS);
            pool.close();
            Assert.assertEquals(0, pool.getConcurrentTasks());
        }

        for (int value : values)
            Assert.assertTrue(returned.contains(value));
        if (resultValues != null)
            for (int value : values)
                Assert.assertTrue(resultValues.contains(value));
    }

    @Test
    public void oneThreadOneValueNoResults() {
        test(1, 1, false);
    }

    @Test
    public void oneThreadMultipleValuesNoResults() {
        test(1, 5, false);
    }

    @Test
    public void multipleThreadOneValueNoResults() {
        test(2, 1, false);
    }

    @Test
    public void multipleThreadMutipleValuesNoResults() {
        test(2, 5, false);
    }

    @Test
    public void oneThreadOneValueWithResults() {
        test(1, 1, true);
    }

    @Test
    public void oneThreadMultipleValuesWithResults() {
        test(1, 5, true);
    }

    @Test
    public void multipleThreadOneValueWithResults() {
        test(2, 1, true);
    }

    @Test
    public void multipleThreadMutipleValuesWithResults() {
        test(2, 5, true);
    }

    private void test(final TaskPool pool, final int count, final int expectedTotal) {
        final AtomicInteger counter = new AtomicInteger();
        final AtomicInteger total = new AtomicInteger();
        for (int i = 0; i < count; i++) {
            pool.submit(() -> {
                ThreadUtils.sleep(1, TimeUnit.SECONDS);
                total.addAndGet(counter.incrementAndGet());
            });
        }
        assertFalse(pool.isShutdown());
        assertEquals(pool.shutdown(), pool);
        assertTrue(pool.isShutdown());
        assertEquals(pool.awaitCompletion(), pool);
        assertEquals(count, counter.get());
        assertEquals(expectedTotal, total.get());
    }

    @Test
    public void internalExecutorTest() {
        try (final TaskPool pool = TaskPool.of()) {
            test(pool, 8, 36);
        }
    }

    @Test
    public void externalExecutorTest() {
        try (final TaskPool pool = TaskPool.of(executorService)) {
            test(pool, 10, 55);
        }
        assertFalse(executorService.isShutdown());
    }

    @Test
    public void externalExecutorWithMaxConcurrentTasksTest() {
        try (final TaskPool pool = TaskPool.of(executorService, 2)) {
            test(pool, 10, 55);
        }
        assertFalse(executorService.isShutdown());
    }

    @Test
    public void exceptionsCollectTest() {
        thrown.expectMessage("exceptionCollectTest");
        try (final TaskPool pool = TaskPool.of()) {
            pool.submit(() -> {
                throw new RuntimeException("exceptionCollectTest");
            }).join();
        }
    }

    @Test
    public void awaitCompletionWithoutShutdownTest() {
        thrown.expect(IllegalStateException.class);
        try (final TaskPool pool = TaskPool.of()) {
            pool.submit(() -> ThreadUtils.sleep(1, TimeUnit.SECONDS));
            pool.awaitCompletion();
        }
    }

}
