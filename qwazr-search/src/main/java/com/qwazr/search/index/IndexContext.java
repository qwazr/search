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

import java.io.Closeable;
import java.util.Map;
import org.apache.lucene.facet.FacetsConfig;

public interface IndexContext extends Closeable {

    default IndexInstance getIndex(String indexName) {
        return null;
    }

    FacetsConfig DEFAULT_FACETS_CONFIG = new FacetsConfig();

    default FacetsConfig getFacetsConfig(String genericFieldName, String concreteFieldName) {
        return DEFAULT_FACETS_CONFIG;
    }

    default FacetsConfig getFacetsConfig(Map<String, String> fieldNames) {
        return DEFAULT_FACETS_CONFIG;
    }

}
