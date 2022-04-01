/*
 * Copyright 2015-2019 Emmanuel Keller / QWAZR
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
package com.qwazr.utils.caching;

import com.qwazr.utils.RandomUtils;
import com.qwazr.utils.concurrent.ExecutorUtils;
import com.qwazr.utils.concurrent.ThreadUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

public class KeyLockedCacheTest {

    private ExecutorService executorService;

    @Before
    public void setup() {
        executorService = Executors.newCachedThreadPool();
    }

    @After
    public void cleanup() throws InterruptedException {
        ExecutorUtils.close(executorService, 5, TimeUnit.MINUTES);
    }

    private void loop(final AtomicBoolean abort,
                      final String prefix,
                      final KeyLockedCache<String, Integer> cache) {
        int count = 0;
        while (!(abort.get() && count > 5)) {
            final Integer value = RandomUtils.nextInt(0, 5);
            final String key = prefix + value;
            final Integer insertedValue = cache.computeIfAbsent(key, k -> value);
            assertThat(value, equalTo(insertedValue));
            count++;
        }
    }

    private void consumeFutures(Collection<Future> futures) throws ExecutionException, InterruptedException {
        for (Future future : futures)
            future.get();
    }

    @Test
    public void keyLockedCacheTest() throws InterruptedException, ExecutionException {
        final CacheMap<String, Integer> cacheMap = new CacheMap<>(10);
        final KeyLockedCache<String, Integer> cache = new KeyLockedCache<>(cacheMap);


        final AtomicBoolean abort = new AtomicBoolean(false);
        final List<Future> futures = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            final String id = "p" + i;
            futures.add(executorService.submit(() -> loop(abort, id, cache)));
        }
        ThreadUtils.sleep(5, TimeUnit.SECONDS);
        abort.set(true);
        consumeFutures(futures);

        assertThat(cache.getCurrentActiveKeys(), equalTo(0));
        assertThat(cache.getMaxActiveKeys(), greaterThan(0));
        assertThat(cache.size(), greaterThanOrEqualTo(3));
        assertThat(cache.size(), lessThanOrEqualTo(10));
    }

    private void slowWrite(final String key, final KeyLockedCache<String, Integer> cache) {
        cache.computeIfAbsent(key, k -> {
            ThreadUtils.sleep(RandomUtils.nextInt(800, 1000), TimeUnit.MILLISECONDS);
            return RandomUtils.nextInt();
        });
    }

    @Test
    public void keyLockedCacheConcurrentWriteTest() throws InterruptedException, ExecutionException {
        final KeyLockedCache<String, Integer> cache = new KeyLockedCache<>(new CacheMap<>(10));
        final long startTime = System.currentTimeMillis();
        final List<Future> futures = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            final String id = "id" + i;
            futures.add(executorService.submit(() -> slowWrite(id, cache)));
            futures.add(executorService.submit(() -> slowWrite(id, cache)));
        }
        consumeFutures(futures);
        final long duration = System.currentTimeMillis() - startTime;
        assertThat(duration, lessThan(2500L));
        assertThat(cache.size(), equalTo(10));
    }

}
