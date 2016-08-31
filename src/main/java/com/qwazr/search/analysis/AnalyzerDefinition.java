/**
 * Copyright 2015-2016 Emmanuel Keller / QWAZR
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
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.json.JsonMapper;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AnalyzerDefinition {

	public final LinkedHashMap<String, String> tokenizer;
	public final List<LinkedHashMap<String, String>> filters;

	public AnalyzerDefinition() {
		tokenizer = null;
		filters = null;
	}

	public AnalyzerDefinition(final LinkedHashMap<String, String> tokenizer,
			List<LinkedHashMap<String, String>> filters) {
		this.tokenizer = tokenizer;
		this.filters = filters;
	}

	public final static TypeReference<LinkedHashMap<String, AnalyzerDefinition>> MapStringAnalyzerTypeRef =
			new TypeReference<LinkedHashMap<String, AnalyzerDefinition>>() {
			};

	public final static LinkedHashMap<String, AnalyzerDefinition> newAnalyzerMap(String jsonString) throws IOException {
		if (StringUtils.isEmpty(jsonString))
			return null;
		return JsonMapper.MAPPER.readValue(jsonString, MapStringAnalyzerTypeRef);
	}

	public final static AnalyzerDefinition newAnalyzer(String jsonString) throws IOException {
		return JsonMapper.MAPPER.readValue(jsonString, AnalyzerDefinition.class);
	}
}
