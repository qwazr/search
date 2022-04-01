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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.qwazr.search.query.QueryInterface;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.apache.lucene.facet.LabelAndValue;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(
    creatorVisibility = JsonAutoDetect.Visibility.NONE,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    fieldVisibility = JsonAutoDetect.Visibility.NONE)
@JsonDeserialize(as = BaseFacetDefinition.class)
public interface FacetDefinition {

    @JsonProperty("top")
    Integer getTop();

    int DEFAULT_TOP = 10;

    @JsonProperty("generic_field_name")
    String getGenericFieldName();

    @JsonProperty("queries")
    @NotNull
    Map<String, QueryInterface> getQueries();

    @JsonProperty("specific_values")
    @NotNull
    Set<String[]> getSpecificValues();

    @JsonProperty("prefix")
    String getPrefix();

    @JsonProperty("sort")
    Sort getSort();

    enum Sort implements Comparator<LabelAndValue> {

        value_descending(FacetBuilder.VALUE_DESCENDING),
        value_ascending(FacetBuilder.VALUE_ASCENDING),
        label_descending(FacetBuilder.LABEL_DESCENDING),
        label_ascending(FacetBuilder.LABEL_ASCENDING);

        private final Comparator<LabelAndValue> comparator;

        Sort(Comparator<LabelAndValue> comparator) {
            this.comparator = comparator;
        }

        @Override
        public int compare(LabelAndValue o1, LabelAndValue o2) {
            return comparator.compare(o1, o2);
        }
    }

    FacetDefinition EMPTY = new BaseFacetDefinition();

    static FacetDefinition create(int top) {
        return new BaseFacetDefinition(top);
    }

    static FacetDefinitionBuilder of() {
        return new FacetDefinitionBuilder();
    }

    static FacetDefinitionBuilder of(Integer top) {
        return new FacetDefinitionBuilder().top(top);
    }

}
