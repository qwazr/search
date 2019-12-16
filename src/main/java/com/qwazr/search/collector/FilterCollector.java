/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Scorable;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.util.RoaringDocIdSet;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FilterCollector extends BaseCollector<FilterCollector.Query>
        implements ConcurrentCollector<FilterCollector.Query> {

    private final Map<LeafReaderContext, RoaringDocIdSet.Builder> docIdSetMapBuilders;

    public FilterCollector(final String collectorName) {
        super(collectorName);
        this.docIdSetMapBuilders = new HashMap<>();
    }

    @Override
    public FilterCollector.Query getResult() {
        final Map<LeafReaderContext, RoaringDocIdSet> docIdSetMap = new HashMap<>();
        docIdSetMapBuilders.forEach((ctx, builder) -> docIdSetMap.put(ctx, builder.build()));
        return new FilterCollector.Query(new FilteredQuery(docIdSetMap));
    }

    @Override
    final public LeafCollector getLeafCollector(final LeafReaderContext context) throws IOException {

        final RoaringDocIdSet.Builder builder = new RoaringDocIdSet.Builder(context.reader().maxDoc());
        docIdSetMapBuilders.put(context, builder);

        return new LeafCollector() {

            @Override
            public void setScorer(final Scorable scorer) {
            }

            @Override
            final public void collect(final int doc) {
                builder.add(doc);
            }
        };
    }

    @Override
    public ScoreMode scoreMode() {
        return ScoreMode.COMPLETE;
    }

    @Override
    final public FilterCollector.Query getReducedResult(
            final Collection<BaseCollector<FilterCollector.Query>> baseCollectors) {
        final FilteredQuery filteredQuery = new FilteredQuery(new HashMap<>());
        baseCollectors.forEach(collector -> filteredQuery.merge(collector.getResult().filteredQuery));
        return new FilterCollector.Query(filteredQuery);
    }

    public static class Query extends AbstractQuery<Query> {

        private final FilteredQuery filteredQuery;

        Query(FilteredQuery filteredQuery) {
            super(Query.class);
            this.filteredQuery = filteredQuery;
        }

        @Override
        final public org.apache.lucene.search.Query getQuery(final QueryContext queryContext)
                throws IOException, ParseException, QueryNodeException, ReflectiveOperationException {
            return filteredQuery;
        }

        @JsonIgnore
        @Override
        protected boolean isEqual(Query q) {
            return Objects.equals(filteredQuery, q.filteredQuery);
        }
    }
}
