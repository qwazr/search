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
import com.qwazr.search.annotations.IndexField;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.json.JsonMapper;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexOptions;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Objects;

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
	@Deprecated
	public final FieldType.LegacyNumericType numeric_type;
	public final IndexOptions index_options;
	public final DocValuesType docvalues_type;
	public final Integer dimension_count;
	public final Integer dimension_num_bytes;

	public enum Template {
		NONE,
		DoublePoint,
		FloatPoint,
		IntPoint,
		LongPoint,
		DoubleField,
		FloatField,
		IntField,
		LongField,
		LongDocValuesField,
		IntDocValuesField,
		FloatDocValuesField,
		DoubleDocValuesField,
		GeoPointField,
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
		dimension_count = null;
		dimension_num_bytes = null;
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
		dimension_count = builder.dimension_count;
		dimension_num_bytes = builder.dimension_num_bytes;
		template = builder.template;
	}

	public FieldDefinition(IndexField indexField) {
		analyzer = indexField.analyzerClass() != Analyzer.class ?
				indexField.analyzerClass().getName() :
				indexField.analyzer();
		query_analyzer = indexField.queryAnalyzerClass() != Analyzer.class ?
				indexField.queryAnalyzerClass().getName() :
				indexField.queryAnalyzer();
		tokenized = indexField.tokenized();
		stored = indexField.stored();
		store_termvectors = indexField.storeTermVectors();
		store_termvector_offsets = indexField.storeTermVectorOffsets();
		store_termvector_positions = indexField.storeTermVectorPositions();
		store_termvector_payloads = indexField.storeTermVectorPayloads();
		omit_norms = indexField.omitNorms();
		numeric_type = indexField.numericType().type;
		index_options = indexField.indexOptions();
		docvalues_type = indexField.docValuesType();
		dimension_count = indexField.dimensionCount();
		dimension_num_bytes = indexField.dimensionNumBytes();
		template = indexField.template();
	}

	public boolean equals(final Object o) {
		if (o == null || !(o instanceof FieldDefinition))
			return false;
		final FieldDefinition f = (FieldDefinition) o;
		if (!Objects.equals(template, f.template))
			return false;
		if (!Objects.equals(analyzer, f.analyzer))
			return false;
		if (!Objects.equals(query_analyzer, f.query_analyzer))
			return false;
		if (!Objects.equals(tokenized, f.tokenized))
			return false;
		if (!Objects.equals(stored, f.stored))
			return false;
		if (!Objects.equals(store_termvectors, f.store_termvectors))
			return false;
		if (!Objects.equals(store_termvector_offsets, f.store_termvector_offsets))
			return false;
		if (!Objects.equals(store_termvector_positions, f.store_termvector_positions))
			return false;
		if (!Objects.equals(store_termvector_payloads, f.store_termvector_payloads))
			return false;
		if (!Objects.equals(omit_norms, f.omit_norms))
			return false;
		if (!Objects.equals(numeric_type, f.numeric_type))
			return false;
		if (!Objects.equals(index_options, f.index_options))
			return false;
		if (!Objects.equals(docvalues_type, f.docvalues_type))
			return false;
		if (!Objects.equals(dimension_count, f.dimension_count))
			return false;
		if (!Objects.equals(dimension_num_bytes, f.dimension_num_bytes))
			return false;
		return true;
	}

	public final static TypeReference<LinkedHashMap<String, FieldDefinition>> MapStringFieldTypeRef =
			new TypeReference<LinkedHashMap<String, FieldDefinition>>() {
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

	public static final String[] RESERVED_NAMES = {ID_FIELD, FACET_FIELD, SCORE_FIELD, DOC_FIELD};

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
		private FieldType.LegacyNumericType numeric_type = null;
		private IndexOptions index_options = null;
		private DocValuesType docvalues_type = null;
		private Integer dimension_count = null;
		private Integer dimension_num_bytes = null;
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

		public Builder setNumericType(FieldType.LegacyNumericType numeric_type) {
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

		public Builder setDimensionCount(Integer dimension_count) {
			this.dimension_count = dimension_count;
			return this;
		}

		public Builder setDimensionNumBytes(Integer dimension_num_bytes) {
			this.dimension_num_bytes = dimension_num_bytes;
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
