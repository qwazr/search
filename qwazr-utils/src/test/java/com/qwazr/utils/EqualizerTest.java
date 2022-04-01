/*
 * Copyright 2016-2017 Emmanuel Keller / QWAZR
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
package com.qwazr.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;

import java.util.Objects;

public class EqualizerTest {

    @Test
    public void testEquals() {
        Assert.assertEquals(new Same(2), new Same(2));
    }

    @Test
    public void testNonEquals() {
        Assert.assertNotEquals(new Same(2), new Same(3));
    }

    public static class Same extends Equalizer<Same> {

        private final Integer value;

        Same(Integer value) {
            super(Same.class);
            this.value = value;
        }

        @Override
        protected boolean isEqual(Same query) {
            return Objects.equals(value, query.value);
        }
    }

    @Test
    public void testImmutableEquals() {
        Assert.assertEquals(new SameImmutable(Long.MAX_VALUE), new SameImmutable(Long.MAX_VALUE));
    }

    @Test
    public void testImmutableNonEquals() {
        Assert.assertNotEquals(new SameImmutable(Long.MAX_VALUE), new SameImmutable(Long.MIN_VALUE));
    }

    @Test
    public void testImmutableHashCode() {
        Assert.assertEquals(new SameImmutable(Long.MAX_VALUE).hashCode(), Objects.hash(Long.MAX_VALUE));
        Assert.assertEquals(new SameImmutable(Long.MIN_VALUE).hashCode(), Objects.hash(Long.MIN_VALUE));
        Assert.assertEquals(new SameImmutable(0L).hashCode(), Objects.hash(0L));
    }

    @Test
    public void testConcurrentImmutableHashCode() {
        final SameImmutable sameImmutable = new SameImmutable(RandomUtils.nextLong());
        final List<CompletableFuture<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 100; i++)
            tasks.add(CompletableFuture.supplyAsync(sameImmutable::hashCode));
        CompletableFuture.allOf(tasks.toArray(new CompletableFuture<?>[0])).join();
        Assert.assertEquals(sameImmutable.countCompute.get(), 1);
        for (final CompletableFuture<Integer> task : tasks)
            Assert.assertEquals(task.getNow(null), Integer.valueOf(sameImmutable.hashCode()));
    }


    public static class SameImmutable extends Equalizer.Immutable<SameImmutable> {

        private final Long value;
        private final AtomicInteger countCompute;

        SameImmutable(Long value) {
            super(SameImmutable.class);
            this.value = value;
            this.countCompute = new AtomicInteger(0);
        }

        @Override
        protected boolean isEqual(SameImmutable query) {
            return Objects.equals(value, query.value);
        }

        @Override
        protected int computeHashCode() {
            countCompute.incrementAndGet();
            return Objects.hash(value);
        }
    }
}
