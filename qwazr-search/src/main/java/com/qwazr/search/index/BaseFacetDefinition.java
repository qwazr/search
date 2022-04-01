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
import com.qwazr.utils.StringUtils;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class BaseFacetDefinition extends Equalizer.Immutable<BaseFacetDefinition> implements FacetDefinition {

    final public Integer top;

    final public String genericFieldName;

    final public Map<String, QueryInterface> queries;

    final public Set<String[]> specificValues;

    final public String prefix;

    final public Sort sort;

    protected BaseFacetDefinition() {
        this((Integer) null);
    }

    protected BaseFacetDefinition(Integer top) {
        this(top, null);
    }

    protected BaseFacetDefinition(Integer top, String prefix) {
        this(top, prefix, null);
    }

    protected BaseFacetDefinition(Integer top, String prefix, Sort sort) {
        this(top, prefix, sort, null, null, null);
    }

    @JsonCreator
    public BaseFacetDefinition(@JsonProperty("top") Integer top,
                               @JsonProperty("prefix") String prefix,
                               @JsonProperty("sort") Sort sort,
                               @JsonProperty("queries") LinkedHashMap<String, QueryInterface> queries,
                               @JsonProperty("specific_values") LinkedHashSet<String[]> specificValues,
                               @JsonProperty("genericFieldName") String genericFieldName) {
        super(BaseFacetDefinition.class);
        this.top = top;
        this.prefix = prefix == null ? null : StringUtils.isBlank(prefix) ? null : prefix;
        this.sort = sort;
        this.queries = queries == null ? Collections.emptyMap() : queries;
        this.specificValues = specificValues == null ? Collections.emptySet() : specificValues;
        this.genericFieldName = genericFieldName;
    }

    BaseFacetDefinition(final FacetDefinitionBuilder builder) {
        this(builder.top,
            builder.prefix,
            builder.sort,
            builder.queries,
            builder.specificValues,
            builder.genericFieldName);
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(genericFieldName, top);
    }

    @Override
    protected boolean isEqual(final BaseFacetDefinition f) {
        return Objects.equals(top, f.top)
            && Objects.equals(queries, f.queries)
            && Objects.equals(specificValues, f.specificValues)
            && Objects.equals(prefix, f.prefix)
            && Objects.equals(sort, f.sort)
            && Objects.equals(genericFieldName, f.genericFieldName);
    }

    @Override
    public Integer getTop() {
        return top;
    }

    @Override
    public String getGenericFieldName() {
        return genericFieldName;
    }

    @Override
    public Map<String, QueryInterface> getQueries() {
        return queries;
    }

    @Override
    public Set<String[]> getSpecificValues() {
        return specificValues;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public Sort getSort() {
        return sort;
    }
}
