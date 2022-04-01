/*
 * Copyright 2017-2020 Emmanuel Keller / QWAZR
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

import com.qwazr.utils.StringUtils;
import org.apache.lucene.facet.LabelAndValue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class FacetBuilder {

    private final FacetDefinition.Sort sort;
    private final String prefix;
    private final List<LabelAndValue> facetResult;

    FacetBuilder(final FacetDefinition facetDefinition) {
        final String facetPrefix = facetDefinition.getPrefix();
        prefix = StringUtils.isBlank(facetPrefix) ? null : facetPrefix;
        sort = facetDefinition.getSort();
        facetResult = new ArrayList<>();
    }

    void put(final LabelAndValue labelAndValue) {
        if (prefix != null)
            if (!labelAndValue.label.startsWith(prefix))
                return;
        facetResult.add(labelAndValue);
    }

    Map<String, Number> build() {
        final Map<String, Number> result = new LinkedHashMap<>();
        if (sort != null && facetResult.size() > 1)
            facetResult.sort(sort);
        facetResult.forEach((labelAndValue) -> result.put(labelAndValue.label, labelAndValue.value));
        return result;
    }

    final static Comparator<LabelAndValue> LABEL_ASCENDING = Comparator.comparing(o -> o.label);
    final static Comparator<LabelAndValue> LABEL_DESCENDING = (o1, o2) -> o2.label.compareTo(o1.label);
    final static Comparator<LabelAndValue> VALUE_ASCENDING = Comparator.comparingLong(o -> o.value.longValue());
    final static Comparator<LabelAndValue> VALUE_DESCENDING = (o1, o2) -> Long.compare(o2.value.longValue(), o1.value.longValue());
}
