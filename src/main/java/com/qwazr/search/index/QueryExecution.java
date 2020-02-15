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
package com.qwazr.search.index;

import com.qwazr.search.collector.ParallelCollector;
import com.qwazr.search.field.SortUtils;
import com.qwazr.search.query.DrillDownQuery;
import com.qwazr.utils.ClassLoaderUtils;
import com.qwazr.utils.TimeTracker;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.Map;

final class QueryExecution<T extends ResultDocumentAbstract> {

    final QueryContextImpl queryContext;
    final QueryDefinition queryDef;
    final TimeTracker timeTracker;
    final Map<String, String> dimensions;
    final FacetsConfig facetsConfig;
    final int start;
    final int rows;
    final int end;
    final Sort sort;
    final boolean useDrillSideways;
    final Query query;
    final Map<String, CollectorConstructor> collectorConstructors;

    private final boolean isConcurrent;

    QueryExecution(final QueryContextImpl queryContext, final QueryDefinition queryDefinition)
            throws QueryNodeException, ReflectiveOperationException, ParseException, IOException {

        this.timeTracker = new TimeTracker();

        this.queryContext = queryContext;
        this.queryDef = queryDefinition;

        this.query = queryDef.luceneQuery != null ?
                queryDef.luceneQuery :
                queryDef.query == null ? new MatchAllDocsQuery() : queryDef.query.getQuery(queryContext);

        this.sort = queryDef.sorts == null ? null : SortUtils.buildSort(queryContext.fieldMap, queryDef.sorts);

        this.dimensions = queryDef.facets == null ? null : FacetsBuilder.getFields(queryDef.facets);
        this.facetsConfig = dimensions == null ? null : queryContext.fieldMap.getFacetsConfig(dimensions);

        this.start = queryDef.getStartValue();
        this.rows = queryDef.getRowsValue();
        this.end = Math.min(start + rows, queryContext.indexReader.numDocs());

        this.useDrillSideways =
                queryDef.query instanceof DrillDownQuery && ((DrillDownQuery) queryDef.query).useDrillSideways &&
                        queryDef.facets != null;
        if (queryDef.collectors != null && !queryDef.collectors.isEmpty()) {
            collectorConstructors = new LinkedHashMap<>();
            isConcurrent = buildExternalCollectors(queryDef.collectors, collectorConstructors);
        } else {
            collectorConstructors = null;
            isConcurrent = true;
        }
    }

    static class CollectorConstructor {

        private final Constructor<?> constructor;
        private final Object[] arguments;
        private final boolean isParallel;

        private CollectorConstructor(final String collectorName, final QueryDefinition.CollectorDefinition collector) throws ReflectiveOperationException {
            final Class<? extends Collector> collectorClass = ClassLoaderUtils.findClass(collector.classname);
            isParallel = ParallelCollector.class.isAssignableFrom(collectorClass);
            final Constructor<?>[] constructors = collectorClass.getConstructors();
            if (constructors.length == 0)
                throw new ReflectiveOperationException("No constructor for class: " + collectorClass);
            if (collector.arguments == null || collector.arguments.length == 0) {
                constructor = collectorClass.getConstructor(String.class);
                arguments = new Object[]{collectorName};
            } else {
                arguments = new Object[collector.arguments.length + 1];
                arguments[0] = collectorName;
                System.arraycopy(collector.arguments, 0, arguments, 1, collector.arguments.length);
                final Class<?>[] classes = new Class[arguments.length];
                int i = 0;
                for (Object arg : arguments)
                    classes[i++] = arg.getClass();
                constructor = collectorClass.getConstructor(classes);
            }
        }

        Collector newInstance() throws ReflectiveOperationException {
            return (Collector) constructor.newInstance(arguments);
        }
    }

    private static boolean buildExternalCollectors(final Map<String, QueryDefinition.CollectorDefinition> collectors,
                                                   final Map<String, CollectorConstructor> collectorConstructors)
            throws ReflectiveOperationException {
        if (collectors == null || collectors.isEmpty())
            return true;
        int classicCollectors = 0;
        for (Map.Entry<String, QueryDefinition.CollectorDefinition> entry : collectors.entrySet()) {
            final String collectorName = entry.getKey();
            final QueryDefinition.CollectorDefinition collector = entry.getValue();
            final CollectorConstructor collectorConstructor = new CollectorConstructor(collectorName, collector);
            collectorConstructors.put(collectorName, collectorConstructor);
            if (!collectorConstructor.isParallel)
                classicCollectors++;
        }
        return classicCollectors == 0;
    }

    final ResultDefinition<T> execute(final ResultDocuments<T> resultDocuments) throws Exception {

        final ResultDocumentsInterface resultDocumentsInterface = resultDocuments.getResultDocuments();

        final QueryCollectors queryCollectors =
                isConcurrent ? new QueryCollectorManager(this) : new QueryCollectorsClassic(this);

        final FacetsBuilder facetsBuilder = queryCollectors.execute();

        final TopDocs topDocs = queryCollectors.getTopDocs();
        final Integer totalHits = queryCollectors.getTotalHits();

        final Map<String, HighlighterImpl> highlighters;
        if (queryDef.highlighters != null && topDocs != null) {
            highlighters = new LinkedHashMap<>();
            queryDef.highlighters.forEach((name, highlighterDefinition) -> highlighters.put(name,
                    new HighlighterImpl(name, highlighterDefinition, queryContext)));
        } else
            highlighters = null;

        timeTracker.next("search_query");

        final ResultDocumentsBuilder resultBuilder =
                new ResultDocumentsBuilder(queryDef, topDocs, queryContext.indexSearcher, query, highlighters,
                        queryCollectors.getExternalResults(), timeTracker, facetsBuilder,
                        totalHits == null ? 0 : totalHits, resultDocumentsInterface);

        return resultDocuments.apply(resultBuilder);
    }

    final Explanation explain(final int docId) throws IOException {
        return queryContext.indexSearcher.explain(query, docId);
    }
}
