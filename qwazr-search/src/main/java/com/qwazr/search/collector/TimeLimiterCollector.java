/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Scorable;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.TimeLimitingCollector;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class TimeLimiterCollector extends BaseCollector.Parallel<Boolean, TimeLimiterCollector.Leaf, TimeLimiterCollector> {

    private final TimeLimitingCollector collector;

    public TimeLimiterCollector(final Long ticksAllowed) {
        super(ScoreMode.COMPLETE_NO_SCORES);
        collector = new TimeLimitingCollector(new Collector() {
            @Override
            public LeafCollector getLeafCollector(LeafReaderContext context) {
                return DoNothingCollector.Leaf.INSTANCE;
            }

            @Override
            public ScoreMode scoreMode() {
                return ScoreMode.COMPLETE_NO_SCORES;
            }
        }, TimeLimitingCollector.getGlobalCounter(), Objects.requireNonNull(ticksAllowed));
    }

    @Override
    public Leaf newLeafCollector(final LeafReaderContext context) throws IOException {
        return new Leaf(collector.getLeafCollector(context));
    }

    @Override
    public Boolean reduce(final List<TimeLimiterCollector> collectors) {
        for (final TimeLimiterCollector collector : collectors)
            if (!collector.reduceLeaf())
                return Boolean.FALSE;
        return Boolean.TRUE;
    }

    private Boolean reduceLeaf() {
        for (final Leaf leaf : getLeaves())
            if (!leaf.result)
                return Boolean.FALSE;
        return Boolean.TRUE;
    }

    static class Leaf implements LeafCollector {

        private final LeafCollector parent;

        private boolean result;

        private Leaf(final LeafCollector parent) {
            this.parent = parent;
            this.result = true;
        }

        @Override
        public void setScorer(Scorable scorer) throws IOException {
            parent.setScorer(scorer);
        }

        @Override
        public void collect(int doc) throws IOException {
            try {
                parent.collect(doc);
            }
            catch (TimeLimitingCollector.TimeExceededException e) {
                result = Boolean.FALSE;
                throw e;
            }
        }
    }

    public static class Lucene extends TimeLimitingCollector {

        /**
         * Create a TimeLimitedCollector wrapper over another {@link Collector} with a specified timeout.
         *
         * @param ticksAllowed max time allowed for collecting
         *                     hits after which {@link TimeExceededException} is thrown
         */
        public Lucene(final Long ticksAllowed) {
            super(new DoNothingCollector(), TimeLimitingCollector.getGlobalCounter(), Objects.requireNonNull(ticksAllowed));
        }

    }
}
