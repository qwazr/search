/**
 * Copyright 2015 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.index;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class ResultDefinition {

	final public long number_of_documents;
	final public List<Map<String, List<String>>> documents;
	final public Map<String, Map<String, Long>> facets;

	ResultDefinition(long number_of_documents,
			List<Map<String, List<String>>> documents,
			Map<String, Map<String, Long>> facets) {
		this.number_of_documents = number_of_documents;
		this.documents = documents;
		this.facets = facets;
	}

	public ResultDefinition() {
		this(0, null, null);
	}
}
