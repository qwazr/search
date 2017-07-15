/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.field;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.annotations.Copy;
import com.qwazr.search.annotations.SmartField;

import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SmartFieldDefinition extends FieldDefinition {

	final public Boolean index;
	final public Boolean facet;
	final public Boolean sort;
	final public Boolean stored;

	public enum Type {
		TEXT, LONG, INTEGER, DOUBLE, FLOAT
	}

	@JsonCreator
	SmartFieldDefinition(@JsonProperty("type") Type type, @JsonProperty("facet") Boolean facet,
			@JsonProperty("index") Boolean index, @JsonProperty("analyzer") final String analyzer,
			@JsonProperty("query_analyzer") final String queryAnalyzer, @JsonProperty("sort") Boolean sort,
			@JsonProperty("stored") Boolean stored, @JsonProperty("copy_from") String[] copyFrom) {
		super(type, analyzer, queryAnalyzer, copyFrom);
		this.facet = facet;
		this.index = index;
		this.sort = sort;
		this.stored = stored;
	}

	private SmartFieldDefinition(SmartBuilder builder) {
		super(builder);
		facet = builder.facet;
		index = builder.index;
		sort = builder.sort;
		stored = builder.stored;
	}

	public SmartFieldDefinition(final String fieldName, final SmartField smartField, final Map<String, Copy> copyMap) {
		super(smartField.type(), from(smartField.analyzer(), smartField.analyzerClass()),
				from(smartField.queryAnalyzer(), smartField.queryAnalyzerClass()), from(fieldName, copyMap));
		facet = smartField.facet();
		index = smartField.index();
		sort = smartField.sort();
		stored = smartField.stored();
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || !(o instanceof SmartFieldDefinition))
			return false;
		if (o == this)
			return true;
		if (!super.equals(o))
			return false;
		final SmartFieldDefinition f = (SmartFieldDefinition) o;
		if (!Objects.equals(facet, f.facet))
			return false;
		if (!Objects.equals(index, f.index))
			return false;
		if (!Objects.equals(sort, f.sort))
			return false;
		if (!Objects.equals(stored, f.stored))
			return false;
		return true;
	}

	public static SmartBuilder of() {
		return new SmartBuilder();
	}

	public static class SmartBuilder extends Builder {

		public Boolean facet;
		public Boolean index;
		public Boolean sort;
		public Boolean stored;

		@Override
		public SmartBuilder type(Type type) {
			super.type(type);
			return this;
		}

		@Override
		public SmartBuilder analyzer(String analyzer) {
			return (SmartBuilder) super.analyzer(analyzer);
		}

		@Override
		public SmartBuilder queryAnalyzer(String queryAnalyzer) {
			return (SmartBuilder) super.queryAnalyzer(queryAnalyzer);
		}

		@Override
		public SmartBuilder copyFrom(String copyFrom) {
			return (SmartBuilder) super.copyFrom(copyFrom);
		}

		public SmartBuilder facet(Boolean facet) {
			this.facet = facet;
			return this;
		}

		public SmartBuilder index(Boolean index) {
			this.index = index;
			return this;
		}

		public SmartBuilder sort(Boolean sort) {
			this.sort = sort;
			return this;
		}

		public SmartBuilder stored(Boolean stored) {
			this.stored = stored;
			return this;
		}

		public SmartFieldDefinition build() {
			return new SmartFieldDefinition(this);
		}

	}

}
