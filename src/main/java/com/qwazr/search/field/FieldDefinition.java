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
package com.qwazr.search.field;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.json.JsonMapper;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexOptions;

import java.io.IOException;
import java.util.LinkedHashMap;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FieldDefinition {

	public final String analyzer;
	public final String query_analyzer;
	public final Boolean tokenized;
	public final Boolean stored;
	public final Boolean store_termvectors;
	public final Boolean store_termvector_offsets;
	public final Boolean store_termvector_positions;
	public final Boolean store_termvector_payloads;
	public final Boolean omit_norms;
	public final FieldType.NumericType numeric_type;
	public final IndexOptions index_options;
	public final DocValuesType docvalues_type;

	public enum Template {
		DoubleField,
		FloatField,
		IntField,
		LongField,
		LongDocValuesField,
		IntDocValuesField,
		FloatDocValuesField,
		DoubleDocValuesField,
		SortedDocValuesField,
		SortedLongDocValuesField,
		SortedIntDocValuesField,
		SortedDoubleDocValuesField,
		SortedFloatDocValuesField,
		SortedSetDocValuesField,
		BinaryDocValuesField,
		StoredField,
		StringField,
		TextField,
		FacetField,
		MultiFacetField,
		SortedSetDocValuesFacetField,
		SortedSetMultiDocValuesFacetField
	}

	public final Template template;

	public FieldDefinition() {
		analyzer = null;
		query_analyzer = null;
		tokenized = null;
		stored = null;
		store_termvectors = null;
		store_termvector_offsets = null;
		store_termvector_positions = null;
		store_termvector_payloads = null;
		omit_norms = null;
		numeric_type = null;
		index_options = null;
		docvalues_type = null;
		template = null;
	}

	private FieldDefinition(Builder builder) {
		analyzer = builder.analyzer;
		query_analyzer = builder.query_analyzer;
		tokenized = builder.tokenized;
		stored = builder.stored;
		store_termvectors = builder.store_termvectors;
		store_termvector_offsets = builder.store_termvector_offsets;
		store_termvector_positions = builder.store_termvector_positions;
		store_termvector_payloads = builder.store_termvector_payloads;
		omit_norms = builder.omit_norms;
		numeric_type = builder.numeric_type;
		index_options = builder.index_options;
		docvalues_type = builder.docvalues_type;
		template = builder.template;
	}

	public final static TypeReference<LinkedHashMap<String, FieldDefinition>> MapStringFieldTypeRef = new TypeReference<LinkedHashMap<String, FieldDefinition>>() {
	};

	public final static LinkedHashMap<String, FieldDefinition> newFieldMap(String jsonString) throws IOException {
		if (StringUtils.isEmpty(jsonString))
			return null;
		return JsonMapper.MAPPER.readValue(jsonString, MapStringFieldTypeRef);
	}

	public final static FieldDefinition newField(String jsonString) throws IOException {
		return JsonMapper.MAPPER.readValue(jsonString, FieldDefinition.class);
	}

	public final static String ID_FIELD = "$id$";

	public final static String FACET_FIELD = FacetsConfig.DEFAULT_INDEX_FIELD_NAME;

	public final static String SCORE_FIELD = "$score";

	public final static String DOC_FIELD = "$doc";

	public static final String[] RESERVED_NAMES = { ID_FIELD, FACET_FIELD, SCORE_FIELD, DOC_FIELD };

	public final static Builder builder() {
		return new Builder();
	}

	public final static Builder builder(Template template) {
		return new Builder(template);
	}

	public static class Builder {

		private String analyzer = null;
		private String query_analyzer = null;
		private Boolean tokenized = null;
		private Boolean stored = null;
		private Boolean store_termvectors = null;
		private Boolean store_termvector_offsets = null;
		private Boolean store_termvector_positions = null;
		private Boolean store_termvector_payloads = null;
		private Boolean omit_norms = null;
		private FieldType.NumericType numeric_type = null;
		private IndexOptions index_options = null;
		private DocValuesType docvalues_type = null;
		private Template template = null;

		public Builder() {
		}

		public Builder(Template template) {
			this.template = template;
		}

		public Builder setAnalyzer(String analyzer) {
			this.analyzer = analyzer;
			return this;
		}

		public Builder setQueryAnalyzer(String query_analyzer) {
			this.query_analyzer = query_analyzer;
			return this;
		}

		public Builder setTokenized(Boolean tokenized) {
			this.tokenized = tokenized;
			return this;
		}

		public Builder setStored(Boolean stored) {
			this.stored = stored;
			return this;
		}

		public Builder setStoreTermVectors(Boolean store_termvectors) {
			this.store_termvectors = store_termvectors;
			return this;
		}

		public Builder setStoreTermVectorOffsets(Boolean store_termvector_offsets) {
			this.store_termvector_offsets = store_termvector_offsets;
			return this;
		}

		public Builder setStoreTermVectorPositions(Boolean store_termvector_positions) {
			this.store_termvector_positions = store_termvector_positions;
			return this;
		}

		public Builder setStoreTermVectorPayloads(Boolean store_termvector_payloads) {
			this.store_termvector_payloads = store_termvector_payloads;
			return this;
		}

		public Builder setOmitNorms(Boolean omit_norms) {
			this.omit_norms = omit_norms;
			return this;
		}

		public Builder setNumericType(FieldType.NumericType numeric_type) {
			this.numeric_type = numeric_type;
			return this;
		}

		public Builder setIndexOptions(IndexOptions index_options) {
			this.index_options = index_options;
			return this;
		}

		public Builder setDocValuesType(DocValuesType docvalues_type) {
			this.docvalues_type = docvalues_type;
			return this;
		}

		public Builder setTemplate(Template template) {
			this.template = template;
			return this;
		}

		public FieldDefinition build() {
			return new FieldDefinition(this);
		}

	}
}
