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
package com.qwazr.search.field;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.annotations.Copy;
import com.qwazr.search.annotations.IndexField;
import com.qwazr.utils.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexOptions;

import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CustomFieldDefinition extends FieldDefinition {

	public final String analyzer;
	@JsonProperty("query_analyzer")
	public final String queryAnalyzer;
	public final Boolean tokenized;
	public final Boolean stored;
	@JsonProperty("store_termvectors")
	public final Boolean storeTermVectors;
	@JsonProperty("store_termvector_offsets")
	public final Boolean storeTermVectorOffsets;
	@JsonProperty("store_termvector_positions")
	public final Boolean storeTermVectorPositions;
	@JsonProperty("store_termvector_payloads")
	public final Boolean storeTermVectorPayloads;
	@JsonProperty("omit_norms")
	public final Boolean omitNorms;
	@JsonProperty("facet_multivalued")
	public final Boolean facetMultivalued;
	@JsonProperty("facet_hierarchical")
	public final Boolean facetHierarchical;
	@JsonProperty("facet_require_dim_count")
	public final Boolean facetRequireDimCount;
	@Deprecated
	@JsonProperty("numeric_type")
	public final FieldType.LegacyNumericType numericType;
	@JsonProperty("index_options")
	public final IndexOptions indexOptions;
	@JsonProperty("docvalues_type")
	public final DocValuesType docValuesType;
	@JsonProperty("dimension_count")
	public final Integer dimensionCount;
	@JsonProperty("dimension_num_bytes")
	public final Integer dimensionNumBytes;

	@JsonCreator
	public CustomFieldDefinition(@JsonProperty("template") final Template template,
			@JsonProperty("analyzer") final String analyzer, @JsonProperty("query_analyzer") final String queryAnalyzer,
			@JsonProperty("tokenized") final Boolean tokenized, @JsonProperty("stored") final Boolean stored,
			@JsonProperty("store_termvectors") final Boolean storeTermVectors,
			@JsonProperty("store_termvector_offsets") final Boolean storeTermVectorOffsets,
			@JsonProperty("store_termvector_positions") final Boolean storeTermVectorPositions,
			@JsonProperty("store_termvector_payloads") final Boolean storeTermVectorPayloads,
			@JsonProperty("omit_norms") final Boolean omitNorms,
			@JsonProperty("facet_multivalued") final Boolean facetMultivalued,
			@JsonProperty("facet_hierarchical") final Boolean facetHierarchical,
			@JsonProperty("facet_require_dim_count") final Boolean facetRequireDimCount,
			@JsonProperty("numeric_type")            final FieldType.LegacyNumericType numericType,
			@JsonProperty("index_options")            final IndexOptions indexOptions,
			@JsonProperty("docvalues_type")    final DocValuesType docValuesType,
			@JsonProperty("dimension_count")    final Integer dimensionCount,
			@JsonProperty("dimension_num_bytes")    final Integer dimensionNumBytes,
			@JsonProperty("copy_from") String[] copyFrom) {
		super(template, copyFrom);
		this.analyzer = analyzer;
		this.queryAnalyzer = queryAnalyzer;
		this.tokenized = tokenized;
		this.stored = stored;
		this.storeTermVectors = storeTermVectors;
		this.storeTermVectorOffsets = storeTermVectorOffsets;
		this.storeTermVectorPositions = storeTermVectorPositions;
		this.storeTermVectorPayloads = storeTermVectorPayloads;
		this.omitNorms = omitNorms;
		this.facetMultivalued = facetMultivalued;
		this.facetHierarchical = facetHierarchical;
		this.facetRequireDimCount = facetRequireDimCount;
		this.numericType = numericType;
		this.indexOptions = indexOptions;
		this.docValuesType = docValuesType;
		this.dimensionCount = dimensionCount;
		this.dimensionNumBytes = dimensionNumBytes;
	}

	private CustomFieldDefinition(CustomBuilder builder) {
		super(builder);
		this.analyzer = builder.analyzer;
		this.queryAnalyzer = builder.queryAnalyzer;
		this.tokenized = builder.tokenized;
		this.stored = builder.stored;
		this.storeTermVectors = builder.storeTermVectors;
		this.storeTermVectorOffsets = builder.storeTermVectorOffsets;
		this.storeTermVectorPositions = builder.storeTermVectorPositions;
		this.storeTermVectorPayloads = builder.storeTermVectorPayloads;
		this.omitNorms = builder.omitNorms;
		this.facetMultivalued = builder.facetMultivalued;
		this.facetHierarchical = builder.facetHierarchical;
		this.facetRequireDimCount = builder.facetRequireDimCount;
		this.numericType = builder.numericType;
		this.indexOptions = builder.indexOptions;
		this.docValuesType = builder.docValuesType;
		this.dimensionCount = builder.dimensionCount;
		this.dimensionNumBytes = builder.dimensionNumBytes;
	}

	public CustomFieldDefinition(final String fieldName, final IndexField indexField, final Map<String, Copy> copyMap) {
		super(indexField.template(), from(fieldName, copyMap));
		analyzer = indexField.analyzerClass() != Analyzer.class ?
				indexField.analyzerClass().getName() :
				StringUtils.isEmpty(indexField.analyzer()) ? null : indexField.analyzer();
		queryAnalyzer = indexField.queryAnalyzerClass() != Analyzer.class ?
				indexField.queryAnalyzerClass().getName() :
				StringUtils.isEmpty(indexField.queryAnalyzer()) ? null : indexField.queryAnalyzer();
		tokenized = indexField.tokenized();
		stored = indexField.stored();
		storeTermVectors = indexField.storeTermVectors();
		storeTermVectorOffsets = indexField.storeTermVectorOffsets();
		storeTermVectorPositions = indexField.storeTermVectorPositions();
		storeTermVectorPayloads = indexField.storeTermVectorPayloads();
		omitNorms = indexField.omitNorms();
		numericType = indexField.numericType().type;
		indexOptions = indexField.indexOptions();
		docValuesType = indexField.docValuesType();
		dimensionCount = indexField.dimensionCount();
		dimensionNumBytes = indexField.dimensionNumBytes();
		facetMultivalued = indexField.facetMultivalued();
		facetHierarchical = indexField.facetHierarchical();
		facetRequireDimCount = indexField.facetRequireDimCount();
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || !(o instanceof CustomFieldDefinition))
			return false;
		if (o == this)
			return true;
		if (!super.equals(o))
			return false;
		final CustomFieldDefinition f = (CustomFieldDefinition) o;
		if (!Objects.equals(analyzer, f.analyzer))
			return false;
		if (!Objects.equals(queryAnalyzer, f.queryAnalyzer))
			return false;
		if (!Objects.equals(tokenized, f.tokenized))
			return false;
		if (!Objects.equals(stored, f.stored))
			return false;
		if (!Objects.equals(storeTermVectors, f.storeTermVectors))
			return false;
		if (!Objects.equals(storeTermVectorOffsets, f.storeTermVectorOffsets))
			return false;
		if (!Objects.equals(storeTermVectorPositions, f.storeTermVectorPositions))
			return false;
		if (!Objects.equals(storeTermVectorPayloads, f.storeTermVectorPayloads))
			return false;
		if (!Objects.equals(omitNorms, f.omitNorms))
			return false;
		if (!Objects.equals(numericType, f.numericType))
			return false;
		if (!Objects.equals(indexOptions, f.indexOptions))
			return false;
		if (!Objects.equals(docValuesType, f.docValuesType))
			return false;
		if (!Objects.equals(dimensionCount, f.dimensionCount))
			return false;
		if (!Objects.equals(dimensionNumBytes, f.dimensionNumBytes))
			return false;
		if (!Objects.equals(facetMultivalued, f.facetMultivalued))
			return false;
		if (!Objects.equals(facetHierarchical, f.facetHierarchical))
			return false;
		if (!Objects.equals(facetRequireDimCount, f.facetRequireDimCount))
			return false;
		return true;
	}

	public static CustomBuilder of() {
		return new CustomBuilder();
	}

	public static CustomBuilder of(Template template) {
		return of().template(template);
	}

	public static class CustomBuilder extends Builder {

		private String analyzer;
		private String queryAnalyzer;
		private Boolean tokenized;
		private Boolean stored;
		private Boolean storeTermVectors;
		private Boolean storeTermVectorOffsets;
		private Boolean storeTermVectorPositions;
		private Boolean storeTermVectorPayloads;
		private Boolean omitNorms;
		private FieldType.LegacyNumericType numericType;
		private IndexOptions indexOptions;
		private DocValuesType docValuesType;
		private Integer dimensionCount;
		private Integer dimensionNumBytes;
		private Boolean facetMultivalued;
		private Boolean facetHierarchical;
		private Boolean facetRequireDimCount;

		public CustomBuilder template(Template template) {
			return (CustomBuilder) super.template(template);
		}

		public CustomBuilder copyFrom(String copyFrom) {
			return (CustomBuilder) super.copyFrom(copyFrom);
		}

		public CustomBuilder analyzer(String analyzer) {
			this.analyzer = analyzer;
			return this;
		}

		public CustomBuilder queryAnalyzer(String queryAnalyzer) {
			this.queryAnalyzer = queryAnalyzer;
			return this;
		}

		public CustomBuilder tokenized(Boolean tokenized) {
			this.tokenized = tokenized;
			return this;
		}

		public CustomBuilder stored(Boolean stored) {
			this.stored = stored;
			return this;
		}

		public CustomBuilder storeTermVectors(Boolean storeTermVectors) {
			this.storeTermVectors = storeTermVectors;
			return this;
		}

		public CustomBuilder storeTermVectorOffsets(Boolean storeTermVectorOffsets) {
			this.storeTermVectorOffsets = storeTermVectorOffsets;
			return this;
		}

		public CustomBuilder storeTermVectorPositions(Boolean storeTermVectorPositions) {
			this.storeTermVectorPositions = storeTermVectorPositions;
			return this;
		}

		public CustomBuilder storeTermVectorPayloads(Boolean storeTermVectorPayloads) {
			this.storeTermVectorPayloads = storeTermVectorPayloads;
			return this;
		}

		public CustomBuilder omitNorms(Boolean omitNorms) {
			this.omitNorms = omitNorms;
			return this;
		}

		public CustomBuilder numericType(FieldType.LegacyNumericType numericType) {
			this.numericType = numericType;
			return this;
		}

		public CustomBuilder indexOptions(IndexOptions indexOptions) {
			this.indexOptions = indexOptions;
			return this;
		}

		public CustomBuilder docValuesType(DocValuesType docValuesType) {
			this.docValuesType = docValuesType;
			return this;
		}

		public CustomBuilder dimensionCount(Integer dimensionCount) {
			this.dimensionCount = dimensionCount;
			return this;
		}

		public CustomBuilder dimensionNumBytes(Integer dimensionNumBytes) {
			this.dimensionNumBytes = dimensionNumBytes;
			return this;
		}

		public CustomBuilder facetMultivalued(Boolean facetMultivalued) {
			this.facetMultivalued = facetMultivalued;
			return this;
		}

		public CustomBuilder facetHierarchical(Boolean facetHierarchical) {
			this.facetHierarchical = facetHierarchical;
			return this;
		}

		public CustomBuilder facetRequireDimCount(Boolean facetRequireDimCount) {
			this.facetRequireDimCount = facetRequireDimCount;
			return this;
		}

		public CustomFieldDefinition build() {
			return new CustomFieldDefinition(this);
		}

	}

}
