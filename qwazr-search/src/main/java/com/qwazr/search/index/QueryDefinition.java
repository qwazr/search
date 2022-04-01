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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.qwazr.search.query.QueryInterface;
import com.qwazr.utils.ObjectMappers;
import com.qwazr.utils.StringUtils;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import org.apache.lucene.search.Query;

@JsonInclude(Include.NON_EMPTY)
@JsonAutoDetect(
    creatorVisibility = JsonAutoDetect.Visibility.NONE,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    fieldVisibility = JsonAutoDetect.Visibility.NONE)
@JsonDeserialize(as = BaseQueryDefinition.class)
public interface QueryDefinition {

    @JsonProperty("query")
    QueryInterface getQuery();

    @JsonProperty("start")
    Integer getStart();

    @JsonProperty("rows")
    Integer getRows();

    int DEFAULT_START = 0;

    @JsonIgnore
    int getStartValue();

    int DEFAULT_ROWS = 10;

    @JsonIgnore
    int getRowsValue();

    @JsonIgnore
    int getEndValue();


    @JsonProperty("returned_fields")
    LinkedHashSet<String> getReturnedFields();

    @JsonProperty("query_debug")
    Boolean getQueryDebug();

    enum SortEnum {

        ascending,

        descending,

        ascending_missing_first,

        ascending_missing_last,

        descending_missing_first,

        descending_missing_last
    }

    @JsonProperty("sorts")
    LinkedHashMap<String, QueryDefinition.SortEnum> getSorts();

    @JsonInclude(Include.NON_EMPTY)
    @JsonDeserialize(as = BaseCollectorDefinition.class)
    interface CollectorDefinition {

        @JsonProperty("class")
        String getClassname();

        @JsonProperty("arguments")
        Object[] getArguments();
    }

    @JsonProperty("collectors")
    LinkedHashMap<String, CollectorDefinition> getCollectors();

    @JsonProperty("facets")
    LinkedHashMap<String, FacetDefinition> getFacets();

    @JsonProperty("highlighters")
    LinkedHashMap<String, HighlighterDefinition> getHighlighters();

    @JsonProperty("commit_user_data")
    Map<String, String> getCommitUserData();

    @JsonIgnore
    Query getLuceneQuery();

    @JsonIgnore
    QueryBuilder of();

    static QueryBuilder of(final Query query) {
        return new QueryBuilder(query);
    }

    static QueryBuilder of(final QueryInterface query) {
        return new QueryBuilder(query);
    }

    static QueryDefinition newQuery(final String jsonString) throws IOException {
        if (StringUtils.isEmpty(jsonString))
            return null;
        return ObjectMappers.JSON.readValue(jsonString, QueryDefinition.class);
    }

}
