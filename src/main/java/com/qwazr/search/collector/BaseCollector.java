/**
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
package com.qwazr.search.collector;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.ScoreMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseCollector<CollectorResult, LeafCollector extends org.apache.lucene.search.LeafCollector, BaseCollector>
        implements ParallelCollector<CollectorResult, BaseCollector> {

    private final String name;
    private final ScoreMode scoreMode;
    private final List<LeafCollector> leafCollectors;

    protected BaseCollector(final String name, final ScoreMode scoreMode) {
        this.name = name;
        this.leafCollectors = new ArrayList<>();
        this.scoreMode = scoreMode;
    }

    final public String getName() {
        return name;
    }

    protected abstract LeafCollector newLeafCollector(final LeafReaderContext context) throws IOException;

    final protected List<LeafCollector> getLeaves() {
        return leafCollectors;
    }

    @Override
    final public org.apache.lucene.search.LeafCollector getLeafCollector(final LeafReaderContext context) throws IOException {
        final LeafCollector leafCollector = newLeafCollector(context);
        if (leafCollector != null)
            leafCollectors.add(leafCollector);
        return leafCollector;
    }

    @Override
    final public ScoreMode scoreMode() {
        return scoreMode;
    }

}
