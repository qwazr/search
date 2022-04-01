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

import com.qwazr.search.query.QueryInterface;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class FacetDefinitionBuilder {

    Integer top;
    String prefix;
    FacetDefinition.Sort sort;
    LinkedHashMap<String, QueryInterface> queries;
    LinkedHashSet<String[]> specificValues;
    String genericFieldName;

    public FacetDefinitionBuilder top(Integer top) {
        this.top = top;
        return this;
    }

    public FacetDefinitionBuilder prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public FacetDefinitionBuilder sort(FacetDefinition.Sort sort) {
        this.sort = sort;
        return this;
    }

    public FacetDefinitionBuilder query(String name, QueryInterface query) {
        if (queries == null)
            queries = new LinkedHashMap<>();
        queries.put(name, query);
        return this;
    }

    public FacetDefinitionBuilder specificValues(String... path) {
        if (specificValues == null)
            specificValues = new LinkedHashSet<>();
        specificValues.add(path);
        return this;
    }

    public FacetDefinitionBuilder genericFieldName(String genericFieldName) {
        this.genericFieldName = genericFieldName;
        return this;
    }

    public FacetDefinition build() {
        return new BaseFacetDefinition(this);
    }
}
