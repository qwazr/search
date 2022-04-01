/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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

import org.roaringbitmap.longlong.LongIterator;
import org.roaringbitmap.longlong.Roaring64NavigableMap;

import java.util.Iterator;

public class Bitmap64Partitioner implements Iterator<Roaring64NavigableMap>, Iterable<Roaring64NavigableMap> {

    private final int batchSize;
    private final LongIterator longIterator;

    public Bitmap64Partitioner(final int batchSize, final Roaring64NavigableMap bitmap) {
        if (batchSize <= 0)
            throw new IllegalArgumentException("batchsize must be greater than zero");
        this.batchSize = batchSize;
        longIterator = bitmap.getLongIterator();
    }

    @Override
    public boolean hasNext() {
        return longIterator.hasNext();
    }

    @Override
    public Roaring64NavigableMap next() {
        final Roaring64NavigableMap partition = new Roaring64NavigableMap();
        int i = batchSize;
        while (i-- > 0 && longIterator.hasNext()) {
            partition.addLong(longIterator.next());
        }
        return partition;
    }

    @Override
    public Bitmap64Partitioner iterator() {
        return this;
    }
}
