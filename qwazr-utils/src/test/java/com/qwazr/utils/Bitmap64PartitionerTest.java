/*
 * Copyright 2016-2019 Emmanuel Keller / QWAZR
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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.roaringbitmap.longlong.Roaring64NavigableMap;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

public class Bitmap64PartitionerTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    public static Roaring64NavigableMap getRandom(int size) {
        final Roaring64NavigableMap bitmap = new Roaring64NavigableMap();
        long pos = 0;
        for (int i = 0; i < size; i++) {
            pos += RandomUtils.nextInt(1, 10);
            bitmap.addLong(pos);
        }
        return bitmap;
    }

    private void test(int bitmapSize, int batchSize, int expectedIterations) {
        final Roaring64NavigableMap bitmap = getRandom(bitmapSize);
        final Roaring64NavigableMap checker = new Roaring64NavigableMap();
        final Bitmap64Partitioner partitioner = new Bitmap64Partitioner(batchSize, bitmap);
        int iterationCount = 0;
        for (final Roaring64NavigableMap partition : partitioner) {
            iterationCount++;
            checker.or(partition);
            assertThat(partition.getIntCardinality(), lessThanOrEqualTo(batchSize));
        }
        assertThat(expectedIterations, equalTo(iterationCount));
        assertThat(bitmap, equalTo(checker));
    }

    @Test
    public void emptyPartitionTest() {
        test(0, 50, 0);
    }

    @Test
    public void onePartitionTest() {
        test(1, 50, 1);
        test(25, 50, 1);
        test(50, 50, 1);
    }

    @Test
    public void exactPartitionTest() {
        test(1000, 50, 20);
    }

    @Test
    public void realisticPartitionTest() {
        test(999, 50, 20);
        test(1001, 50, 21);
    }

    @Test
    public void unrealisticPartitionTest() {
        test(42, 1, 42);
    }

    @Test
    public void zeroBatchSizePartitionTest() {
        exceptionRule.expectMessage("batchsize must be greater than zero");
        exceptionRule.expect(IllegalArgumentException.class);
        test(1000, 0, 50);
    }

    @Test
    public void negativeBatchSizePartitionTest() {
        exceptionRule.expectMessage("batchsize must be greater than zero");
        exceptionRule.expect(IllegalArgumentException.class);
        test(1000, -1, 50);
    }
}
