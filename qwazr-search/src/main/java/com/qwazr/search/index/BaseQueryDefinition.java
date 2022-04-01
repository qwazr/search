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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.query.QueryInterface;
import com.qwazr.utils.Equalizer;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import org.apache.lucene.search.Query;

public class BaseQueryDefinition extends Equalizer.Immutable<BaseQueryDefinition> implements QueryDefinition {

    final public QueryInterface query;

    final public LinkedHashMap<String, QueryDefinition.SortEnum> sorts;
    final public LinkedHashMap<String, QueryDefinition.CollectorDefinition> collectors;

    final public Integer start;
    final public Integer rows;

    final public LinkedHashSet<String> returnedFields;

    final public Boolean queryDebug;

    final public LinkedHashMap<String, FacetDefinition> facets;

    final public LinkedHashMap<String, HighlighterDefinition> highlighters;

    final public Map<String, String> commitUserData;

    final public Query luceneQuery;

    @JsonCreator
    public BaseQueryDefinition(@JsonProperty("start") Integer start,
                               @JsonProperty("rows") Integer rows,
                               @JsonProperty("returned_fields") LinkedHashSet<String> returnedFields,
                               @JsonProperty("query_debug") Boolean queryDebug,
                               @JsonProperty("sorts") LinkedHashMap<String, SortEnum> sorts,
                               @JsonProperty("collectors") LinkedHashMap<String, CollectorDefinition> collectors,
                               @JsonProperty("facets") LinkedHashMap<String, FacetDefinition> facets,
                               @JsonProperty("highlighters") LinkedHashMap<String, HighlighterDefinition> highlighters,
                               @JsonProperty("query") QueryInterface query,
                               @JsonProperty("commit_user_data") Map<String, String> commitUserData) {
        super(BaseQueryDefinition.class);
        this.start = start;
        this.rows = rows;
        this.returnedFields = returnedFields == null || returnedFields.isEmpty() ? null : returnedFields;
        this.queryDebug = queryDebug;
        this.sorts = sorts;
        this.collectors = collectors == null || collectors.isEmpty() ? null : collectors;
        this.facets = facets == null ? null : facets.isEmpty() ? null : facets;
        this.highlighters = highlighters == null || highlighters.isEmpty() ? null : highlighters;
        this.query = query;
        this.commitUserData = commitUserData == null || commitUserData.isEmpty() ? null : commitUserData;
        this.luceneQuery = null;
    }

    BaseQueryDefinition(final QueryBuilder builder) {
        super(BaseQueryDefinition.class);
        start = builder.start;
        rows = builder.rows;
        returnedFields = builder.returnedFields == null || builder.returnedFields.isEmpty() ? null : builder.returnedFields;
        queryDebug = builder.queryDebug;
        facets = builder.facets == null || builder.facets.isEmpty() ? null : builder.facets;
        sorts = builder.sorts;
        collectors = builder.collectors == null || builder.collectors.isEmpty() ? null : builder.collectors;
        highlighters = builder.highlighters == null || builder.highlighters.isEmpty() ? null : builder.highlighters;
        query = builder.query;
        luceneQuery = builder.luceneQuery;
        commitUserData = builder.commitUserData == null || builder.commitUserData.isEmpty() ? null : builder.commitUserData;
    }

    @Override
    final public int getStartValue() {
        return start == null ? DEFAULT_START : start;
    }

    @Override
    final public int getRowsValue() {
        return rows == null ? DEFAULT_ROWS : rows;
    }

    @Override
    final public int getEndValue() {
        return getStartValue() + getRowsValue();
    }


    @Override
    protected int computeHashCode() {
        return Objects.hashCode(query);
    }

    @Override
    protected boolean isEqual(BaseQueryDefinition q) {
        return Objects.equals(query, q.query)
            && Objects.equals(sorts, q.sorts)
            && Objects.equals(collectors, q.collectors)
            && Objects.equals(start, q.start)
            && Objects.equals(rows, q.rows)
            && Objects.equals(returnedFields, q.returnedFields)
            && Objects.equals(queryDebug, q.queryDebug)
            && Objects.equals(facets, q.facets)
            && Objects.equals(highlighters, q.highlighters)
            && Objects.equals(commitUserData, q.commitUserData)
            && Objects.equals(luceneQuery, q.luceneQuery);
    }


    @Override
    public QueryInterface getQuery() {
        return query;
    }

    @Override
    public Integer getStart() {
        return start;
    }

    @Override
    public Integer getRows() {
        return rows;
    }

    @Override
    public LinkedHashSet<String> getReturnedFields() {
        return returnedFields;
    }

    @Override
    public Boolean getQueryDebug() {
        return queryDebug;
    }

    @Override
    public LinkedHashMap<String, SortEnum> getSorts() {
        return sorts;
    }

    @Override
    public LinkedHashMap<String, CollectorDefinition> getCollectors() {
        return collectors;
    }

    @Override
    public LinkedHashMap<String, FacetDefinition> getFacets() {
        return facets;
    }

    @Override
    public LinkedHashMap<String, HighlighterDefinition> getHighlighters() {
        return highlighters;
    }

    @Override
    public Map<String, String> getCommitUserData() {
        return commitUserData;
    }

    @Override
    public Query getLuceneQuery() {
        return luceneQuery;
    }

    @Override
    public QueryBuilder of() {
        return new QueryBuilder()
            .start(start)
            .rows(rows)
            .returnedField(returnedFields)
            .queryDebug(queryDebug)
            .sorts(sorts)
            .collectors(collectors)
            .facets(facets)
            .highlighters(highlighters)
            .query(query)
            .commitUserData(commitUserData)
            .query(luceneQuery);
    }
}
