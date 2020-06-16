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
package com.qwazr.search.collector;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qwazr.search.index.QueryContext;
import com.qwazr.search.query.AbstractQuery;
import com.qwazr.search.query.lucene.FilteredQuery;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Scorable;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.util.RoaringDocIdSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FilterCollector extends BaseCollector.Parallel<FilterCollector.Query, FilterCollector.Leaf, FilterCollector> {

    public FilterCollector() {
        super(ScoreMode.COMPLETE);
    }

    private Query reduce() {
        final Map<Integer, RoaringDocIdSet> docIdSetMap = new HashMap<>();
        getLeaves().forEach(leaf -> docIdSetMap.put(leaf.docBase, leaf.docIdsBuilder.build()));
        return new FilterCollector.Query(new FilteredQuery(docIdSetMap));
    }


    @Override
    final public Query reduce(final List<FilterCollector> leafCollectors) {
        final FilteredQuery filteredQuery = new FilteredQuery(new HashMap<>());
        leafCollectors.forEach(collector -> filteredQuery.merge(collector.reduce().filteredQuery));
        return new FilterCollector.Query(filteredQuery);
    }


    @Override
    protected Leaf newLeafCollector(final LeafReaderContext context) {
        return new Leaf(context);
    }

    static class Leaf implements LeafCollector {

        private final Integer docBase;
        private final RoaringDocIdSet.Builder docIdsBuilder;

        private Leaf(final LeafReaderContext context) {
            this.docBase = context.docBase;
            this.docIdsBuilder = new RoaringDocIdSet.Builder(context.reader().maxDoc());
        }

        @Override
        public void setScorer(Scorable scorer) {
        }

        @Override
        public void collect(int doc) {
            docIdsBuilder.add(doc);
        }
    }

    public static class Query extends AbstractQuery<Query> {

        private final FilteredQuery filteredQuery;

        Query(FilteredQuery filteredQuery) {
            super(Query.class);
            this.filteredQuery = filteredQuery;
        }

        @Override
        final public org.apache.lucene.search.Query getQuery(final QueryContext queryContext) {
            return filteredQuery;
        }

        @JsonIgnore
        @Override
        protected boolean isEqual(Query q) {
            return Objects.equals(filteredQuery, q.filteredQuery);
        }

        @Override
        protected int computeHashCode() {
            return Objects.hash(filteredQuery);
        }
    }
}
