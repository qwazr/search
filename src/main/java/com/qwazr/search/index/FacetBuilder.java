/**
 * Copyright 2017 Emmanuel Keller / QWAZR
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
 **/

package com.qwazr.search.index;

import com.qwazr.utils.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

final class FacetBuilder {

	private final String prefix;
	private final Map<String, Number> facetResult;

	final static FacetBuilder EMPTY = new FacetBuilder();

	private FacetBuilder() {
		prefix = null;
		facetResult = Collections.emptyMap();
	}

	FacetBuilder(final FacetDefinition facetDefinition) {
		this.prefix = StringUtils.isEmpty(facetDefinition.prefix) ? null : facetDefinition.prefix;
		this.facetResult = new LinkedHashMap<>();
	}

	void put(final String value, final Number count) {
		if (prefix != null)
			if (!value.startsWith(prefix))
				return;
		facetResult.put(value, count);
	}

	Map<String, Number> build() {
		return facetResult;
	}
}
