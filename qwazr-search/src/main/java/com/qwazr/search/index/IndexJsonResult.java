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
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.qwazr.utils.Equalizer;
import java.util.Objects;
import java.util.SortedMap;
import java.util.SortedSet;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(
    creatorVisibility = JsonAutoDetect.Visibility.NONE,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    fieldVisibility = JsonAutoDetect.Visibility.NONE)
public class IndexJsonResult extends Equalizer.Immutable<IndexJsonResult> {

    @JsonProperty("count")
    public Integer count;

    @JsonProperty("field_types")
    public SortedMap<String, SortedSet<JsonNodeType>> fieldTypes;

    @JsonCreator
    public IndexJsonResult(final @JsonProperty("count") Integer count,
                           final @JsonProperty("field_types") SortedMap<String, SortedSet<JsonNodeType>> fieldTypes) {
        super(IndexJsonResult.class);
        this.count = count;
        this.fieldTypes = fieldTypes;
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(count, fieldTypes);
    }

    @Override
    protected boolean isEqual(final IndexJsonResult o) {
        return Objects.equals(count, o.count)
            && Objects.equals(fieldTypes, o.fieldTypes);
    }

}
