/**
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
 **/
package com.qwazr.search.index;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.qwazr.search.query.AbstractQuery;

import java.util.LinkedHashMap;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FacetDefinition {

	final public Integer top;
	final public LinkedHashMap<String, AbstractQuery> queries;

	public FacetDefinition() {
		this((Integer) null);
	}

	public FacetDefinition(Integer top) {
		this(top, null);
	}

	public FacetDefinition(Integer top, LinkedHashMap<String, AbstractQuery> queries) {
		this.top = top;
		this.queries = queries;
	}

	private FacetDefinition(final Builder builder) {
		this(builder.top, builder.queries != null && !builder.queries.isEmpty() ? builder.queries : null);
	}

	public static Builder of() {
		return new Builder();
	}

	public static Builder of(Integer top) {
		return new Builder().top(top);
	}

	public static class Builder {

		public Integer top;
		public LinkedHashMap<String, AbstractQuery> queries;

		public Builder top(Integer top) {
			this.top = top;
			return this;
		}

		public Builder query(String name, AbstractQuery query) {
			if (queries == null)
				queries = new LinkedHashMap<>();
			queries.put(name, query);
			return this;
		}

		public FacetDefinition build() {
			return new FacetDefinition(this);
		}
	}
}