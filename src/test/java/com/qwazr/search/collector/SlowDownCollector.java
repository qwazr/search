/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.collector;

import com.qwazr.utils.concurrent.ThreadUtils;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Scorable;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class SlowDownCollector extends BaseCollector<Long> {

    private final int perRecordMsPause;

    public SlowDownCollector(final String collectorName, final Integer perRecordMsPause) {
        super(collectorName);
        this.perRecordMsPause = Objects.requireNonNull(perRecordMsPause);
    }

    @Override
    public LeafCollector getLeafCollector(final LeafReaderContext context) {
        return new SlowDownLeafCollector();
    }

    @Override
    public Long getResult() {
        return null;
    }

    private class SlowDownLeafCollector implements LeafCollector {

        @Override
        final public void setScorer(final Scorable scorer) {
        }

        @Override
        final public void collect(final int doc) {
            ThreadUtils.sleep(perRecordMsPause, TimeUnit.MILLISECONDS);
        }
    }

    public static class Concurrent extends SlowDownCollector implements ConcurrentCollector<Long> {

        public Concurrent(final String collectorName, final Integer perRecordMsPause) {
            super(collectorName, perRecordMsPause);
        }

        @Override
        public Long getReducedResult(Collection<BaseCollector<Long>> baseCollectors) {
            return null;
        }
    }
}
