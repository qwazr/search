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

import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.field.FieldTypeInterface;
import com.qwazr.search.query.QueryInterface;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.TimeTracker;
import com.qwazr.utils.concurrent.BiConsumerEx;
import com.qwazr.utils.concurrent.ConcurrentUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.facet.DrillSideways;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetCounts;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.facet.taxonomy.TaxonomyFacetSumFloatAssociations;
import org.apache.lucene.facet.taxonomy.TaxonomyFacetSumIntAssociations;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

abstract class FacetsBuilder {

    protected final QueryContextImpl queryContext;
    protected final String sortedSetFacetField;
    private final LinkedHashMap<String, FacetDefinition> facetsDef;
    protected final HashMap<String, String> resolvedDimensions;
    private final Query searchQuery;
    private final TimeTracker timeTracker;

    final LinkedHashMap<String, Map<String, Number>> results = new LinkedHashMap<>();

    private FacetsBuilder(final QueryContextImpl queryContext,
                          final LinkedHashMap<String, FacetDefinition> facetsDef,
                          final Query searchQuery,
                          final TimeTracker timeTracker) {
        this.facetsDef = facetsDef;
        this.queryContext = queryContext;
        this.sortedSetFacetField = queryContext.fieldMap.fieldsContext.sortedSetFacetField;
        this.resolvedDimensions = new HashMap<>();
        getFields(facetsDef).forEach((concrete, generic) -> resolvedDimensions.put(concrete,
            queryContext.fieldMap.getFieldType(generic, concrete)
                .resolveFieldName(concrete, FieldTypeInterface.FieldType.facetField, FieldTypeInterface.ValueType.textType)));
        this.searchQuery = searchQuery;
        this.timeTracker = timeTracker;
    }

    final FacetsBuilder build() throws Exception {
        for (final Map.Entry<String, FacetDefinition> entry : facetsDef.entrySet()) {
            final String dimension = entry.getKey();
            final String resolvedDimension = resolvedDimensions.get(dimension);
            final FacetDefinition facet = entry.getValue();
            final FacetBuilder facetBuilder = new FacetBuilder(facet);
            final Map<String, QueryInterface> queries = facet.getQueries();
            final Set<String[]> specificValues = facet.getSpecificValues();
            final Integer topdef = facet.getTop();
            final Integer top = topdef != null ? topdef : (queries.isEmpty() && specificValues.isEmpty()) ? FacetDefinition.DEFAULT_TOP : null;
            if (!specificValues.isEmpty() || top != null)
                buildFacetState(resolvedDimension, top, specificValues, facetBuilder);
            if (!queries.isEmpty())
                buildFacetQueries(queries, facetBuilder);
            results.put(dimension, facetBuilder.build());
        }

        if (timeTracker != null)
            timeTracker.next("facet_count");
        return this;
    }

    protected abstract Facets getFacets(final String dim);

    private void buildFacetState(final String resolvedDimension, final Integer top, final Set<String[]> specificValues,
                                 final FacetBuilder facetBuilder) throws IOException {
        final Facets facets = getFacets(resolvedDimension);
        if (facets == null)
            return;
        if (top != null && top > 0) {
            final FacetResult facetResult = facets.getTopChildren(top, resolvedDimension);
            if (facetResult != null && facetResult.labelValues != null)
                for (LabelAndValue lv : facetResult.labelValues)
                    facetBuilder.put(lv);
        }
        if (specificValues != null) {
            for (String[] path : specificValues) {
                final Number count = facets.getSpecificValue(resolvedDimension, path);
                facetBuilder.put(new LabelAndValue(StringUtils.join(path, '/'),
                    count == null || count.longValue() <= 0 ? 0 : count));
            }
        }
    }

    private void buildFacetQueries(final Map<String, QueryInterface> queries, final FacetBuilder facetBuilder)
        throws Exception {
        final BiConsumerEx<String, QueryInterface, Exception> consumer = (name, facetQuery) -> {
            final BooleanQuery.Builder builder = new BooleanQuery.Builder();
            builder.add(searchQuery, BooleanClause.Occur.FILTER);
            builder.add(facetQuery.getQuery(queryContext), BooleanClause.Occur.FILTER);
            facetBuilder.put(new LabelAndValue(name, queryContext.indexSearcher.count(builder.build())));
        };
        ConcurrentUtils.forEachEx(queries, consumer);
    }

    static Map<String, String> getFields(LinkedHashMap<String, FacetDefinition> facets) {
        if (facets == null || facets.isEmpty())
            return null;
        final Map<String, String> fields = new HashMap<>();
        facets.forEach((field, facetDefinition) -> {
            if (facetDefinition.getQueries().isEmpty()) {
                final String genericFieldName = facetDefinition.getGenericFieldName();
                fields.put(field, genericFieldName == null ? field : genericFieldName);
            }
        });
        return fields;
    }

    static class WithCollectors extends FacetsBuilder {

        private final SortedSetDocValuesFacetCounts sortedSetCounts;
        private final FastTaxonomyFacetCounts taxonomyCounts;
        private final TaxonomyFacetSumFloatAssociations floatTaxonomyCounts;
        private final TaxonomyFacetSumIntAssociations intTaxonomyCounts;
        private final FacetsConfig facetsConfig;

        WithCollectors(final QueryContextImpl queryContext, final FacetsConfig facetsConfig,
                       final LinkedHashMap<String, FacetDefinition> facetsDef, final Query searchQuery,
                       final TimeTracker timeTracker, final FacetsCollector facetsCollector)
            throws IOException {
            super(queryContext, facetsDef, searchQuery, timeTracker);
            this.facetsConfig = facetsConfig;
            int facetFlag = checkFacetTypeFlags(facetsConfig, facetsDef);
            this.sortedSetCounts = queryContext.docValueReaderState == null ?
                null :
                (facetFlag & FACET_IS_SORTED) == FACET_IS_SORTED ?
                    new SortedSetDocValuesFacetCounts(queryContext.docValueReaderState, facetsCollector) :
                    null;
            this.taxonomyCounts = (facetFlag & FACET_IS_TAXO) == FACET_IS_TAXO ?
                new FastTaxonomyFacetCounts(queryContext.taxonomyReader, facetsConfig, facetsCollector) :
                null;
            this.floatTaxonomyCounts = (facetFlag & FACET_IS_TAXO_FLOAT) == FACET_IS_TAXO_FLOAT ?
                new TaxonomyFacetSumFloatAssociations(FieldDefinition.TAXONOMY_FLOAT_ASSOC_FACET_FIELD,
                    queryContext.taxonomyReader, facetsConfig, facetsCollector) :
                null;
            this.intTaxonomyCounts = (facetFlag & FACET_IS_TAXO_INT) == FACET_IS_TAXO_INT ?
                new TaxonomyFacetSumIntAssociations(FieldDefinition.TAXONOMY_INT_ASSOC_FACET_FIELD,
                    queryContext.taxonomyReader, facetsConfig, facetsCollector) :
                null;
        }

        final private static int FACET_IS_SORTED = 1;
        final private static int FACET_IS_TAXO = 2;
        final private static int FACET_IS_TAXO_INT = 4;
        final private static int FACET_IS_TAXO_FLOAT = 8;

        private int checkFacetTypeFlags(final FacetsConfig facetsConfig,
                                        final LinkedHashMap<String, FacetDefinition> facetsDef) {
            int flag = 0;
            for (String dimName : facetsDef.keySet()) {
                final String resolvedDimension = resolvedDimensions.get(dimName);
                if (resolvedDimension == null)
                    continue;
                final String indexField = facetsConfig.getDimConfig(resolvedDimension).indexFieldName;
                if (indexField == null)
                    continue;
                if (indexField.equals(sortedSetFacetField)) {
                    flag = flag | FACET_IS_SORTED;
                } else {
                    switch (indexField) {
                        case FieldDefinition.TAXONOMY_FACET_FIELD:
                            flag = flag | FACET_IS_TAXO;
                            break;
                        case FieldDefinition.TAXONOMY_INT_ASSOC_FACET_FIELD:
                            flag = flag | FACET_IS_TAXO_INT;
                            break;
                        case FieldDefinition.TAXONOMY_FLOAT_ASSOC_FACET_FIELD:
                            flag = flag | FACET_IS_TAXO_FLOAT;
                            break;
                        default:
                            break;
                    }
                }
            }
            return flag;
        }

        @Override
        final protected Facets getFacets(final String dimension) {
            final String indexFieldName = facetsConfig.getDimConfig(dimension).indexFieldName;
            if (indexFieldName == null)
                return null;
            if (indexFieldName.equals(sortedSetFacetField)) {
                if (queryContext.docValueReaderState != null)
                    if (queryContext.docValueReaderState.getOrdRange(dimension) != null)
                        return sortedSetCounts;
            } else {
                switch (indexFieldName) {
                    case FieldDefinition.TAXONOMY_FACET_FIELD:
                        return taxonomyCounts;
                    case FieldDefinition.TAXONOMY_INT_ASSOC_FACET_FIELD:
                        return intTaxonomyCounts;
                    case FieldDefinition.TAXONOMY_FLOAT_ASSOC_FACET_FIELD:
                        return floatTaxonomyCounts;
                    default:
                        break;
                }
            }
            return null;
        }
    }

    static class WithSideways extends FacetsBuilder {

        final DrillSideways.DrillSidewaysResult results;
        private final FacetsConfig facetsConfig;

        WithSideways(final QueryContextImpl queryContext, final FacetsConfig facetsConfig,
                     final LinkedHashMap<String, FacetDefinition> facetsDef, final Query searchQuery,
                     final TimeTracker timeTracker, final DrillSideways.DrillSidewaysResult results) {
            super(queryContext, facetsDef, searchQuery, timeTracker);
            this.facetsConfig = facetsConfig;
            this.results = results;
        }

        @Override
        final protected Facets getFacets(final String dimension) {
            if (sortedSetFacetField.equals(facetsConfig.getDimConfig(dimension).indexFieldName)) {
                if (queryContext.docValueReaderState == null)
                    return null;
                if (queryContext.docValueReaderState.getOrdRange(dimension) == null)
                    return null;
            }
            return results.facets;
        }
    }
}
