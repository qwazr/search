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

import com.qwazr.search.query.DrillDown;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.search.TopDocs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

abstract class QueryCollectors {

    final QueryExecution<?> queryExecution;

    QueryCollectors(final QueryExecution<?> queryExecution) {
        this.queryExecution = queryExecution;
    }

    abstract FacetsBuilder execute() throws Exception;

    abstract Integer getTotalHits();

    abstract TopDocs getTopDocs();

    abstract FacetsCollector getFacetsCollector();

    abstract Map<String, Object> getExternalResults();

    static List<Pair<String, String[]>> getDimPathPairs(final DrillDown drillDownQuery) {
        final List<Pair<String, String[]>> dimPaths = new ArrayList<>();
        drillDownQuery.dimPath.forEach(map -> map.forEach((dim, paths) -> {
            dimPaths.add(Pair.of(dim, paths));
        }));
        return dimPaths;
    }
}
