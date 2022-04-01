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
package com.qwazr.search.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.annotations.QuerySampleCreator;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.FieldMap;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.QueryContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;

public class DrillDown extends AbstractQuery<DrillDown> {

    final public AbstractQuery<?> baseQuery;
    final public List<Map<String, String[]>> dimPath;
    final public Map<String, String> genericFieldNames;
    final public Boolean useDrillSideways;

    @JsonCreator
    public DrillDown(@JsonProperty("baseQuery") final AbstractQuery<?> baseQuery,
                     @JsonProperty("useDrillSideways") final boolean useDrillSideways,
                     @JsonProperty("dimPath") final List<Map<String, String[]>> dimPath,
                     @JsonProperty("genericFieldNames") final Map<String, String> genericFieldNames) {
        super(DrillDown.class);
        this.baseQuery = baseQuery;
        this.useDrillSideways = useDrillSideways;
        this.dimPath = dimPath == null ? new ArrayList<>() : dimPath;
        this.genericFieldNames = genericFieldNames == null ? new LinkedHashMap<>() : genericFieldNames;
    }

    public DrillDown(final AbstractQuery<?> baseQuery, final boolean useDrillSideways) {
        this(baseQuery, useDrillSideways, null, null);
    }

    @QuerySampleCreator(docUri = CORE_BASE_DOC_URI + "facet/org/apache/lucene/facet/DrillDownQuery.html")
    public DrillDown(final IndexSettingsDefinition settings,
                     final Map<String, AnalyzerDefinition> analyzers,
                     final Map<String, FieldDefinition> fields) {
        this(MatchAllDocs.INSTANCE, true, Collections.singletonList(Map.of("category", new String[]{"search-engine"})), null);
    }

    public DrillDown dynamicFilter(final String genericFieldName, final String dim, final String... path) {
        final LinkedHashMap<String, String[]> map = new LinkedHashMap<>();
        map.put(dim, path);
        dimPath.add(map);
        if (genericFieldName != null)
            genericFieldNames.put(dim, genericFieldName);
        return this;
    }

    public DrillDown filter(final String dim, final String... path) {
        return dynamicFilter(null, dim, path);
    }

    @Override
    final public org.apache.lucene.facet.DrillDownQuery getQuery(final QueryContext queryContext)
        throws IOException, ParseException, ReflectiveOperationException, QueryNodeException {

        final FieldMap fieldMap = queryContext.getFieldMap();
        final Map<String, String> resolvedDimensions = new HashMap<>();
        dimPath.forEach(map -> map.keySet().forEach(concreteField -> {
            final String genericField = genericFieldNames.getOrDefault(concreteField, concreteField);
            final String facetField = FieldResolver.resolveFacetField(fieldMap, genericField, concreteField);
            resolvedDimensions.put(concreteField, facetField);
        }));

        final FacetsConfig facetsConfig = queryContext.getFacetsConfig(resolvedDimensions);
        Objects.requireNonNull(facetsConfig, "FacetsConfig is null");
        final org.apache.lucene.facet.DrillDownQuery drillDownQuery;
        if (baseQuery == null)
            drillDownQuery = new org.apache.lucene.facet.DrillDownQuery(facetsConfig);
        else
            drillDownQuery = new org.apache.lucene.facet.DrillDownQuery(facetsConfig, baseQuery.getQuery(queryContext));

        dimPath.forEach(dimPath -> dimPath.forEach(
            (dim, path) -> drillDownQuery.add(resolvedDimensions.getOrDefault(dim, dim), path)));

        return drillDownQuery;
    }

    @Override
    protected boolean isEqual(final DrillDown q) {
        if (!Objects.equals(baseQuery, q.baseQuery) || !Objects.equals(genericFieldNames, q.genericFieldNames) ||
            !Objects.equals(useDrillSideways, q.useDrillSideways))
            return false;
        if (dimPath == q.dimPath)
            return true;
        if (dimPath == null || q.dimPath == null)
            return false;
        if (dimPath.size() != q.dimPath.size())
            return false;
        final Iterator<Map<String, String[]>> mapIterator = q.dimPath.iterator();
        for (final Map<String, String[]> map1 : dimPath) {
            final Map<String, String[]> map2 = mapIterator.next();
            for (Map.Entry<String, String[]> entry1 : map1.entrySet()) {
                if (!Arrays.equals(entry1.getValue(), map2.get(entry1.getKey())))
                    return false;
            }
        }
        return true;
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(baseQuery, genericFieldNames, useDrillSideways, dimPath);
    }

}
