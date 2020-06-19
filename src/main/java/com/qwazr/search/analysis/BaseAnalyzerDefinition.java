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
package com.qwazr.search.analysis;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.utils.Equalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public class BaseAnalyzerDefinition extends Equalizer.Immutable<BaseAnalyzerDefinition> implements AnalyzerDefinition {

    public final LinkedHashMap<String, Integer> positionIncrementGap;
    public final LinkedHashMap<String, Integer> offsetGap;
    public final LinkedHashMap<String, String> tokenizer;
    public final List<LinkedHashMap<String, String>> filters;

    @JsonCreator
    public BaseAnalyzerDefinition(@JsonProperty("position_increment_gap") final LinkedHashMap<String, Integer> positionIncrementGap,
                                  @JsonProperty("offset_gap") final LinkedHashMap<String, Integer> offsetGap,
                                  @JsonProperty("tokenizer") final LinkedHashMap<String, String> tokenizer,
                                  @JsonProperty("filters") final List<LinkedHashMap<String, String>> filters) {
        super(BaseAnalyzerDefinition.class);
        this.positionIncrementGap = positionIncrementGap;
        this.offsetGap = offsetGap;
        this.tokenizer = tokenizer;
        this.filters = filters;
    }


    @Override
    public LinkedHashMap<String, Integer> getPositionIncrementGap() {
        return positionIncrementGap;
    }

    @Override
    public LinkedHashMap<String, Integer> getOffsetGap() {
        return offsetGap;
    }

    @Override
    public LinkedHashMap<String, String> getTokenizer() {
        return tokenizer;
    }

    @Override
    public List<LinkedHashMap<String, String>> getFilters() {
        return filters;
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(tokenizer, filters, positionIncrementGap, offsetGap);
    }

    @Override
    protected boolean isEqual(final BaseAnalyzerDefinition other) {
        return Objects.equals(tokenizer, other.tokenizer)
            && Objects.equals(filters, other.filters)
            && Objects.equals(positionIncrementGap, other.positionIncrementGap)
            && Objects.equals(offsetGap, other.offsetGap);
    }
}
