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

import com.qwazr.search.collector.ClassicCollector;
import com.qwazr.search.collector.ParallelCollector;
import com.qwazr.search.field.SortUtils;
import com.qwazr.search.query.DrillDown;
import com.qwazr.search.query.QueryInterface;
import com.qwazr.utils.ClassLoaderUtils;
import com.qwazr.utils.TimeTracker;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.ws.rs.InternalServerErrorException;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;

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

        this.timeTracker = Boolean.TRUE.equals(queryDefinition.getQueryDebug())
            ? TimeTracker.withDurations() : TimeTracker.noDurations();

        this.queryContext = queryContext;
        this.queryDef = queryDefinition;

        final Query luceneQuery = queryDef.getLuceneQuery();
        final QueryInterface query = queryDef.getQuery();
        this.query = luceneQuery != null ? luceneQuery :
            query == null ? new MatchAllDocsQuery() : query.getQuery(queryContext);

        final LinkedHashMap<String, QueryDefinition.SortEnum> sorts = queryDef.getSorts();
        this.sort = sorts == null ? null : SortUtils.buildSort(queryContext.fieldMap, sorts);

        final LinkedHashMap<String, FacetDefinition> facets = queryDef.getFacets();
        this.dimensions = facets == null ? null : FacetsBuilder.getFields(facets);
        this.facetsConfig = dimensions == null ? null : queryContext.fieldMap.getFacetsConfig(dimensions);

        this.start = queryDef.getStartValue();
        this.rows = queryDef.getRowsValue();
        this.end = Math.min(start + rows, queryContext.indexReader.numDocs());

        this.useDrillSideways = query instanceof DrillDown && ((DrillDown) query).useDrillSideways && facets != null;
        final LinkedHashMap<String, QueryDefinition.CollectorDefinition> collectors = queryDef.getCollectors();
        if (collectors != null && !collectors.isEmpty()) {
            collectorConstructors = new LinkedHashMap<>();
            isConcurrent = buildExternalCollectors(collectors, collectorConstructors);
        } else {
            collectorConstructors = null;
            isConcurrent = true;
        }
    }

    enum CollectorType {
        LUCENE, CLASSIC, PARALLEL

    }

    private static Constructor<?> getConstructor(final Class<? extends Collector> collectorClass,
                                                 final Object[] arguments)
        throws NoSuchMethodException {
        final Class<?>[] classes = new Class[arguments.length];
        int i = 0;
        for (final Object arg : arguments)
            classes[i++] = arg == null ? Object.class : arg.getClass();
        return collectorClass.getConstructor(classes);
    }

    static class CollectorConstructor {

        private final Constructor<?> constructor;
        private final Object[] arguments;
        private final CollectorType collectorType;

        private CollectorConstructor(final QueryDefinition.CollectorDefinition collector) throws ReflectiveOperationException {
            final Class<? extends Collector> collectorClass = ClassLoaderUtils.findClass(collector.getClassname());
            if (ParallelCollector.class.isAssignableFrom(collectorClass)) {
                collectorType = CollectorType.PARALLEL;
            } else if (ClassicCollector.class.isAssignableFrom(collectorClass)) {
                collectorType = CollectorType.CLASSIC;
            } else if (Collector.class.isAssignableFrom(collectorClass)) {
                collectorType = CollectorType.LUCENE;
            } else {
                throw new InternalServerErrorException("The type of the collector class is not valid: " + collectorClass);
            }
            final Constructor<?>[] constructors = collectorClass.getConstructors();
            if (constructors.length == 0)
                throw new ReflectiveOperationException("No constructor for class: " + collectorClass);
            final Object[] collectorArguments = collector.getArguments();
            if (collectorArguments == null || collectorArguments.length == 0) {
                constructor = collectorClass.getConstructor();
                arguments = null;
            } else {
                constructor = getConstructor(collectorClass, collectorArguments);
                arguments = collectorArguments;
            }
        }

        Collector newInstance() {
            try {
                if (arguments != null)
                    return (Collector) constructor.newInstance(arguments);
                else
                    return (Collector) constructor.newInstance();
            } catch (ReflectiveOperationException e) {
                throw new InternalServerErrorException("Cannot create the collector " + constructor, e);
            }
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
            final CollectorConstructor collectorConstructor = new CollectorConstructor(collector);
            collectorConstructors.put(collectorName, collectorConstructor);
            if (collectorConstructor.collectorType != CollectorType.PARALLEL)
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
        final Map<String, HighlighterDefinition> queryHighlighters = queryDef.getHighlighters();
        if (queryHighlighters != null && topDocs != null) {
            highlighters = new LinkedHashMap<>();
            queryHighlighters.forEach((name, highlighterDefinition) -> highlighters.put(name,
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
