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

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.TimeLimitingCollector;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

public class TimeLimiterCollector extends BaseCollector<Boolean> {

    private final TimeLimitingCollector collector;

    public TimeLimiterCollector(final String collectorName, final Long ticksAllowed) {
        super(collectorName);
        collector = new TimeLimitingCollector(new BaseCollector<Long>(collectorName) {
            @Override
            public Long getResult() {
                return null;
            }
        }, TimeLimitingCollector.getGlobalCounter(), Objects.requireNonNull(ticksAllowed));
    }

    @Override
    public LeafCollector getLeafCollector(final LeafReaderContext context) throws IOException {
        return collector.getLeafCollector(context);
    }

    @Override
    public Boolean getResult() {
        return null;
    }

    public static class Concurrent extends TimeLimiterCollector implements ConcurrentCollector<Boolean> {

        public Concurrent(final String collectorName, final Long ticksAllowed) {
            super(collectorName, ticksAllowed);
        }

        @Override
        public Boolean getReducedResult(Collection<BaseCollector<Boolean>> baseCollectors) {
            return null;
        }
    }
}
