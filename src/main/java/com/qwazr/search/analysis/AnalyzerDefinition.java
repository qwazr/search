/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.qwazr.utils.ObjectMappers;
import com.qwazr.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AnalyzerDefinition {

	public final LinkedHashMap<String, Integer> position_increment_gap;
	public final LinkedHashMap<String, Integer> offset_gap;
	public final LinkedHashMap<String, String> tokenizer;
	public final List<LinkedHashMap<String, String>> filters;

	public AnalyzerDefinition() {
		tokenizer = null;
		filters = null;
		position_increment_gap = null;
		offset_gap = null;
	}

	public AnalyzerDefinition(final LinkedHashMap<String, Integer> positionIncrementGaps,
			final LinkedHashMap<String, Integer> offsetGaps, final LinkedHashMap<String, String> tokenizer,
			final List<LinkedHashMap<String, String>> filters) {
		this.position_increment_gap = positionIncrementGaps;
		this.offset_gap = offsetGaps;
		this.tokenizer = tokenizer;
		this.filters = filters;
	}

	public final static TypeReference<LinkedHashMap<String, AnalyzerDefinition>> MapStringAnalyzerTypeRef =
			new TypeReference<LinkedHashMap<String, AnalyzerDefinition>>() {
			};

	public static LinkedHashMap<String, AnalyzerDefinition> newAnalyzerMap(String jsonString) throws IOException {
		if (StringUtils.isEmpty(jsonString))
			return null;
		return ObjectMappers.JSON.readValue(jsonString, MapStringAnalyzerTypeRef);
	}

	public static AnalyzerDefinition newAnalyzer(String jsonString) throws IOException {
		return ObjectMappers.JSON.readValue(jsonString, AnalyzerDefinition.class);
	}

	public static LinkedHashMap<String, AnalyzerDefinition> loadMap(final File mapFile,
			final Supplier<LinkedHashMap<String, AnalyzerDefinition>> defaultMap) throws IOException {
		return mapFile != null && mapFile.exists() && mapFile.isFile() ? ObjectMappers.JSON.readValue(mapFile,
				AnalyzerDefinition.MapStringAnalyzerTypeRef) : defaultMap == null ? null : defaultMap.get();
	}

	public static void saveMap(final LinkedHashMap<String, AnalyzerDefinition> definitionMap, final File mapFile)
			throws IOException {
		if (definitionMap == null)
			Files.deleteIfExists(mapFile.toPath());
		else
			ObjectMappers.JSON.writeValue(mapFile, definitionMap);
	}
}
