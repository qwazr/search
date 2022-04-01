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
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Scorable;
import org.apache.lucene.search.ScoreMode;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public interface SlowDownCollector {

    class Classic extends BaseCollector.Classic<Integer, SlowDownCollector.Leaf> {

        private final int perRecordMsPause;

        public Classic(final Integer perRecordMsPause) {
            super(ScoreMode.COMPLETE_NO_SCORES);
            this.perRecordMsPause = Objects.requireNonNull(perRecordMsPause);
        }

        @Override
        protected Leaf newLeafCollector(LeafReaderContext context) {
            return new Leaf(perRecordMsPause);
        }

        @Override
        public Integer reduce() {
            return perRecordMsPause;
        }
    }

    class Concurrent extends BaseCollector.Parallel<Integer, SlowDownCollector.Leaf, SlowDownCollector> {

        private final int perRecordMsPause;

        public Concurrent(final Integer perRecordMsPause) {
            super(ScoreMode.COMPLETE_NO_SCORES);
            this.perRecordMsPause = Objects.requireNonNull(perRecordMsPause);
        }

        @Override
        public Leaf newLeafCollector(final LeafReaderContext context) {
            return new Leaf(perRecordMsPause);
        }

        @Override
        public Integer reduce(final List<SlowDownCollector> collectors) {
            return perRecordMsPause;
        }

    }

    class Leaf implements LeafCollector {

        private final int perRecordMsPause;

        private Leaf(final int perRecordMsPause) {
            this.perRecordMsPause = perRecordMsPause;
        }

        @Override
        final public void setScorer(final Scorable scorer) {
        }

        @Override
        final public void collect(final int doc) {
            ThreadUtils.sleep(perRecordMsPause, TimeUnit.MILLISECONDS);
        }
    }

    class Lucene implements Collector {

        private final int perRecordMsPause;

        public Lucene(final Integer perRecordMsPause) {
            this.perRecordMsPause = Objects.requireNonNull(perRecordMsPause);
        }

        @Override
        public LeafCollector getLeafCollector(LeafReaderContext context) {
            return new Leaf(perRecordMsPause);
        }

        @Override
        public ScoreMode scoreMode() {
            return ScoreMode.COMPLETE_NO_SCORES;
        }
    }

}
