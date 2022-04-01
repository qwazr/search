/*
 * Copyright 2017-2020 Emmanuel Keller / QWAZR
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
package com.qwazr.search.test;

import com.qwazr.search.collector.BaseCollector;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Scorable;
import org.apache.lucene.search.ScoreMode;

import java.io.IOException;
import java.util.List;

public class ClassicMaxCollector extends BaseCollector.Parallel<Long, ClassicMaxCollector.RandomCollector, ClassicMaxCollector> {

    public ClassicMaxCollector() {
        super(ScoreMode.COMPLETE_NO_SCORES);
    }

    @Override
    public RandomCollector newLeafCollector(final LeafReaderContext context) throws IOException {
        return new RandomCollector(context.reader());
    }

    @Override
    public Long reduce(final List<ClassicMaxCollector> randomCollectors) {
        long result = 0;
        for (final ClassicMaxCollector collector : randomCollectors) {
            for (final RandomCollector leaf : collector.getLeaves())
                if (leaf.max > result)
                    result = leaf.max;
        }
        return result;
    }

    public static class RandomCollector implements LeafCollector {

        private final NumericDocValues qtyDocValues;

        private long max = 0;

        RandomCollector(final LeafReader reader) throws IOException {
            qtyDocValues = reader.getNumericDocValues(AnnotatedRecord.DV_QUANTITY_FIELD);
        }

        @Override
        final public void setScorer(final Scorable scorer) {
        }

        @Override
        final public void collect(final int doc) throws IOException {
            if (qtyDocValues == null)
                return;
            qtyDocValues.advance(doc);
            final long m = qtyDocValues.longValue();
            if (m > max)
                max = m;
        }
    }
}
