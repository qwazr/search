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
package com.qwazr.utils.test;

import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.concurrent.ExecutorUtils;
import com.qwazr.utils.concurrent.ReadWriteLock;
import com.qwazr.utils.concurrent.ThreadUtils;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;
import java.util.logging.Logger;

public class ReadWriteLockTest {

    final static Logger LOGGER = LoggerUtils.getLogger(ReadWriteLockTest.class);

    private void test(ReadWriteLock rwl) throws InterruptedException {
        final AtomicLong writeTime = new AtomicLong();
        final AtomicLong readTime = new AtomicLong();
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        rwl.writeEx(() -> {

            final Runnable runnable = () -> rwl.readEx(() -> {
                ThreadUtils.sleep(1, TimeUnit.MILLISECONDS);
                readTime.set(System.currentTimeMillis());
            });

            executor.submit(runnable);
            Thread.sleep(100);
            writeTime.set(System.currentTimeMillis());
        });
        ExecutorUtils.close(executor, 1, TimeUnit.MINUTES);
        Assert.assertTrue(writeTime.get() < readTime.get());
    }

    @Test
    public void testReadWriteLock() throws InterruptedException {
        test(ReadWriteLock.reentrant(false));
        test(ReadWriteLock.reentrant(true));
        test(ReadWriteLock.of(new StampedLock().asReadWriteLock()));
        test(ReadWriteLock.stamped());
    }

    private class Benchmark {

        final String name;
        final ReadWriteLock rwl;
        final long timeLimit;
        final long readSleep;
        final long writeSleep;
        final AtomicLong writeCount = new AtomicLong();
        final AtomicLong readCount = new AtomicLong();
        final AtomicLong writeLockTime = new AtomicLong();
        final AtomicLong readLockTime = new AtomicLong();
        final long totalTime;

        Benchmark(String name, ReadWriteLock rwl, Duration duration, long readSleep, long writeSleep)
                throws InterruptedException, ExecutionException {
            this.name = name;
            this.rwl = rwl;
            this.readSleep = readSleep;
            this.writeSleep = writeSleep;

            final long startTime = System.currentTimeMillis();
            timeLimit = startTime + duration.toMillis();
            execute();
            totalTime = System.currentTimeMillis() - startTime;
        }

        private void doRead() {
            final long startTime = System.nanoTime();
            rwl.readEx(() -> {
                readLockTime.addAndGet(System.nanoTime() - startTime);
                if (readSleep != 0)
                    ThreadUtils.sleep(readSleep, TimeUnit.MILLISECONDS);
                readCount.incrementAndGet();
            });
        }

        private void doWrite() {
            final long startTime = System.nanoTime();
            rwl.writeEx(() -> {
                writeLockTime.addAndGet(System.nanoTime() - startTime);
                if (writeSleep != 0)
                    ThreadUtils.sleep(writeSleep, TimeUnit.MILLISECONDS);
                writeCount.incrementAndGet();
            });
        }

        private void execute() throws InterruptedException, ExecutionException {
            final ExecutorService executor = Executors.newCachedThreadPool();

            final List<Future> futures = new ArrayList<>();

            for (int i = 0; i < 10; i++) {
                futures.add(executor.submit(() -> {
                    while (System.currentTimeMillis() < timeLimit) {
                        doRead();
                        doWrite();
                    }
                }));
            }

            for (Future future : futures)
                future.get();

            ExecutorUtils.close(executor, 1, TimeUnit.MINUTES);
        }

        float getReadRate() {
            return (float) readCount.get() * 1000 / totalTime;
        }

        float getWriteRate() {
            return (float) writeCount.get() * 1000 / totalTime;
        }

        @Override
        public String toString() {
            return " | " + name + " => " + getReadRate();
        }
    }

    private void benchmarkSuite(Duration duration, long readSleep, long writeSleep)
            throws InterruptedException, ExecutionException {
        for (int i = 0; i < 5; i++) {
            List<Benchmark> results = new ArrayList<>();
            results.add(new Benchmark("Unfair", ReadWriteLock.reentrant(false), duration, readSleep, writeSleep));
            results.add(new Benchmark("Fair", ReadWriteLock.reentrant(true), duration, readSleep, writeSleep));
            results.add(new Benchmark("StampedAsRw", ReadWriteLock.of(new StampedLock().asReadWriteLock()), duration,
                    readSleep, writeSleep));
            results.add(new Benchmark("Stamped", ReadWriteLock.stamped(), duration, readSleep, writeSleep));

            if (readSleep == 0 || writeSleep != 0)
                dumpResult("READ: ", results, b -> " | " + b.name + " => " + +b.getReadRate(),
                        (o1, o2) -> Double.compare(o2.getReadRate(), o1.getReadRate()));
            if (writeSleep == 0 || readSleep != 0)
                dumpResult("WRITE: ", results, b -> " | " + b.name + " => " + b.getWriteRate(),
                        (o1, o2) -> Double.compare(o2.getWriteRate(), o1.getWriteRate()));
        }
    }

    private void dumpResult(String prefix, List<Benchmark> results, Function<Benchmark, String> display,
                            Comparator<Benchmark> comparator) {
        Collections.sort(results, comparator);
        StringBuilder sb = new StringBuilder(prefix);
        results.forEach(b -> sb.append(display.apply(b)));
        LOGGER.info(sb.toString());
    }

    @Test
    public void benchmarkNoSleep() throws InterruptedException, ExecutionException {
        benchmarkSuite(Duration.ofMillis(250), 0, 0);
    }

    @Test
    public void benchmarkWithSleepBestRead() throws InterruptedException, ExecutionException {
        benchmarkSuite(Duration.ofMillis(250), 0, 20);
    }

    @Test
    public void benchmarkWithSleepBestWrite() throws InterruptedException, ExecutionException {
        benchmarkSuite(Duration.ofMillis(250), 20, 0);
    }

    @Test
    public void benchmarkWithRealSleep() throws InterruptedException, ExecutionException {
        benchmarkSuite(Duration.ofMillis(250), 5, 20);
    }
}
