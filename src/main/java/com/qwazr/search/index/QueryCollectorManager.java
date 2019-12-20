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
 **/
package com.qwazr.search.index;

import com.qwazr.search.collector.ParallelCollector;
import com.qwazr.utils.ExceptionUtils;
import com.qwazr.utils.LoggerUtils;
import org.apache.lucene.facet.DrillSideways;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.CollectorManager;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TimeLimitingCollector;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.TopFieldDocs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

class QueryCollectorManager extends QueryCollectors implements CollectorManager<Collector, QueryCollectors> {

    private final static Logger LOGGER = LoggerUtils.getLogger(QueryCollectorManager.class);

    private final Collection<QueryCollectorsClassic> queryCollectorsList;
    private FacetsCollector facetsCollector;

    QueryCollectorManager(final QueryExecution<?> queryExecution) {
        super(queryExecution);
        this.queryCollectorsList = new ArrayList<>();
    }

    @Override
    final public FacetsBuilder execute() throws Exception {

        final FacetsBuilder facetsBuilder;

        if (queryExecution.useDrillSideways) {

            final DrillSideways.ConcurrentDrillSidewaysResult<QueryCollectors> drillSidewaysResult =
                    new MixedDrillSideways(queryExecution).search(
                            (org.apache.lucene.facet.DrillDownQuery) queryExecution.query, this);
            facetsBuilder = new FacetsBuilder.WithSideways(queryExecution.queryContext, queryExecution.facetsConfig,
                    queryExecution.queryDef.facets, queryExecution.query, queryExecution.timeTracker,
                    drillSidewaysResult).build();

        } else {

            try {
                queryExecution.queryContext.indexSearcher.search(queryExecution.query, this);
            }
            catch (RuntimeException e) {
                if (ExceptionUtils.getRootCause(e) instanceof TimeLimitingCollector.TimeExceededException)
                    LOGGER.log(Level.WARNING, e, e::getMessage);
                else
                    throw e;
            }

            facetsCollector = getFacetsCollector();
            facetsBuilder = facetsCollector == null ?
                    null :
                    new FacetsBuilder.WithCollectors(queryExecution.queryContext, queryExecution.facetsConfig,
                            queryExecution.queryDef.facets, queryExecution.query, queryExecution.timeTracker,
                            facetsCollector).build();
        }

        return facetsBuilder;
    }

    @Override
    final public Collector newCollector() throws IOException {
        final QueryCollectorsClassic queryCollectors;
        try {
            queryCollectors = new QueryCollectorsClassic(queryExecution);
        }
        catch (ReflectiveOperationException e) {
            throw new IOException(e);
        }
        queryCollectorsList.add(queryCollectors);
        return queryCollectors.finalCollector;
    }

    @Override
    final public QueryCollectors reduce(final Collection collectors) {
        return this;
    }

    @Override
    public final Integer getTotalHits() {
        if (queryCollectorsList == null || queryCollectorsList.isEmpty())
            return 0;
        int totalHits = 0;
        for (QueryCollectorsClassic queryCollectors : queryCollectorsList) {
            if (queryCollectors.totalHitCountCollector != null)
                totalHits += queryCollectors.totalHitCountCollector.getTotalHits();
            else if (queryCollectors.topDocsCollector != null)
                totalHits += queryCollectors.topDocsCollector.getTotalHits();
        }
        return totalHits;
    }

    @Override
    public final TopDocs getTopDocs() {
        if (queryCollectorsList == null || queryCollectorsList.isEmpty())
            return null;
        if (queryExecution.sort != null)
            return getTopFieldDocs();
        final List<TopDocs> topDocsList = new ArrayList<>(queryCollectorsList.size());
        for (QueryCollectorsClassic queryCollectors : queryCollectorsList)
            if (queryCollectors.topDocsCollector != null)
                topDocsList.add(queryCollectors.topDocsCollector.topDocs());
        return TopDocs.merge(queryExecution.start, queryExecution.rows,
                topDocsList.toArray(new TopDocs[0]), true);
    }

    private TopDocs getTopFieldDocs() {
        final List<TopFieldDocs> topFieldDocsList = new ArrayList<>(queryCollectorsList.size());
        for (QueryCollectorsClassic queryCollectors : queryCollectorsList)
            if (queryCollectors.topDocsCollector != null)
                topFieldDocsList.add(((TopFieldCollector) queryCollectors.topDocsCollector).topDocs());
        return TopFieldDocs.merge(queryExecution.sort == null ? Sort.RELEVANCE : queryExecution.sort,
                queryExecution.start, queryExecution.rows,
                topFieldDocsList.toArray(new TopFieldDocs[0]), true);
    }

    private final static FacetsCollector EMPTY_FACETS_COLLECTOR = new FacetsCollector();

    @Override
    public final FacetsCollector getFacetsCollector() {
        if (facetsCollector != null) // cache
            return facetsCollector;
        if (queryExecution.queryDef.facets == null || queryExecution.queryDef.facets.isEmpty())
            return null;
        if (queryCollectorsList == null || queryCollectorsList.isEmpty())
            return EMPTY_FACETS_COLLECTOR;
        facetsCollector = new FacetsCollector();
        final List<FacetsCollector.MatchingDocs> matchingDocs = facetsCollector.getMatchingDocs();
        for (QueryCollectorsClassic queryCollectors : queryCollectorsList)
            if (queryCollectors.facetsCollector != null)
                matchingDocs.addAll(queryCollectors.facetsCollector.getMatchingDocs());
        return facetsCollector;
    }

    @Override
    public final Map<String, Object> getExternalResults() {
        if (queryCollectorsList == null || queryCollectorsList.isEmpty())
            return null;
        if (queryExecution.queryDef == null || queryExecution.queryDef.collectors == null)
            return null;
        final Map<String, Object> results = new HashMap<>();
        for (final String collectorName : queryExecution.queryDef.collectors.keySet()) {
            final List<Collector> userCollectors = new ArrayList<>();
            for (final QueryCollectorsClassic queryCollectors : queryCollectorsList)
                if (queryCollectors.userCollectors != null)
                    userCollectors.add(queryCollectors.userCollectors.get(collectorName));
            if (!userCollectors.isEmpty()) {
                final Collector userCollector = userCollectors.get(0);
                if (userCollector instanceof ParallelCollector)
                    results.put(collectorName, ((ParallelCollector) userCollector).reduce(userCollectors));
            }
        }
        return results;
    }
}
