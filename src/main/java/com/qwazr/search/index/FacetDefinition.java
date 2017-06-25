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
package com.qwazr.search.index;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.qwazr.search.query.AbstractQuery;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.lucene.facet.LabelAndValue;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FacetDefinition {

	final public Integer top;

	final public LinkedHashMap<String, AbstractQuery> queries;

	final public LinkedHashSet<String[]> specific_values;

	final public String prefix;

	final public Sort sort;

	public enum Sort implements Comparator<LabelAndValue> {

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

	public FacetDefinition() {
		this((Integer) null);
	}

	public FacetDefinition(Integer top) {
		this(top, null);
	}

	public FacetDefinition(Integer top, String prefix) {
		this(top, prefix, null);
	}

	public FacetDefinition(Integer top, String prefix, Sort sort) {
		this(top, prefix, sort, null, null);
	}

	public FacetDefinition(Integer top, String prefix, Sort sort, LinkedHashMap<String, AbstractQuery> queries,
			LinkedHashSet<String[]> specificValues) {
		this.top = top;
		this.prefix = prefix;
		this.sort = sort;
		this.queries = queries;
		this.specific_values = specificValues;
	}

	private FacetDefinition(final Builder builder) {
		this(builder.top, builder.prefix, builder.sort, MapUtils.isEmpty(builder.queries) ? null : builder.queries,
				CollectionUtils.isEmpty(builder.specificValues) ? null : builder.specificValues);
	}

	public static Builder of() {
		return new Builder();
	}

	public static Builder of(Integer top) {
		return new Builder().top(top);
	}

	public static class Builder {

		public Integer top;
		public String prefix;
		public Sort sort;
		public LinkedHashMap<String, AbstractQuery> queries;
		public LinkedHashSet<String[]> specificValues;

		public Builder top(Integer top) {
			this.top = top;
			return this;
		}

		public Builder prefix(String prefix) {
			this.prefix = prefix;
			return this;
		}

		public Builder sort(Sort sort) {
			this.sort = sort;
			return this;
		}

		public Builder query(String name, AbstractQuery query) {
			if (queries == null)
				queries = new LinkedHashMap<>();
			queries.put(name, query);
			return this;
		}

		public Builder specificValues(String... path) {
			if (specificValues == null)
				specificValues = new LinkedHashSet<>();
			specificValues.add(path);
			return this;
		}

		public FacetDefinition build() {
			return new FacetDefinition(this);
		}
	}
}