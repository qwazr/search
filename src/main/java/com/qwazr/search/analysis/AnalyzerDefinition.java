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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.qwazr.utils.ObjectMappers;
import com.qwazr.utils.StringUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(
    creatorVisibility = JsonAutoDetect.Visibility.NONE,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    fieldVisibility = JsonAutoDetect.Visibility.NONE)
@JsonDeserialize(as = BaseAnalyzerDefinition.class)
public interface AnalyzerDefinition {

    @JsonProperty("position_increment_gap")
    LinkedHashMap<String, Integer> getPositionIncrementGap();

    @JsonProperty("offset_gap")
    LinkedHashMap<String, Integer> getOffsetGap();

    @JsonProperty("tokenizer")
    LinkedHashMap<String, String> getTokenizer();

    @JsonProperty("filters")
    List<LinkedHashMap<String, String>> getFilters();

    TypeReference<LinkedHashMap<String, AnalyzerDefinition>> mapStringAnalyzerTypeRef =
        new TypeReference<>() {
        };

    static LinkedHashMap<String, AnalyzerDefinition> newAnalyzerMap(final String jsonString) throws IOException {
        if (StringUtils.isEmpty(jsonString))
            return null;
        return ObjectMappers.JSON.readValue(jsonString, mapStringAnalyzerTypeRef);
    }

    static AnalyzerDefinition newAnalyzer(String jsonString) throws IOException {
        return ObjectMappers.JSON.readValue(jsonString, AnalyzerDefinition.class);
    }

    static LinkedHashMap<String, AnalyzerDefinition> loadMap(final File mapFile,
                                                             final Supplier<LinkedHashMap<String, AnalyzerDefinition>> defaultMap) throws IOException {
        return mapFile != null && mapFile.exists() && mapFile.isFile() ?
            ObjectMappers.JSON.readValue(mapFile, AnalyzerDefinition.mapStringAnalyzerTypeRef) :
            defaultMap == null ? null : defaultMap.get();
    }

    static void saveMap(final LinkedHashMap<String, AnalyzerDefinition> definitionMap, final File mapFile)
        throws IOException {
        if (definitionMap == null)
            Files.deleteIfExists(mapFile.toPath());
        else
            ObjectMappers.JSON.writeValue(mapFile, definitionMap);
    }
}
