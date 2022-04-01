/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.annotations.Copy;
import com.qwazr.search.annotations.IndexField;
import com.qwazr.utils.WildcardMatcher;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexOptions;

public class CustomFieldDefinition extends BaseFieldDefinition<CustomFieldDefinition> {

    @JsonProperty("template")
    public final Template template;
    @JsonProperty("tokenized")
    public final Boolean tokenized;
    @JsonProperty("stored")
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
    @JsonProperty("index_options")
    public final IndexOptions indexOptions;
    @JsonProperty("docvalues_type")
    public final DocValuesType docValuesType;
    @JsonProperty("data_dimension_count")
    public final Integer dataDimensionCount;
    @JsonProperty("index_dimension_count")
    public final Integer indexDimensionCount;
    @JsonProperty("dimension_num_bytes")
    public final Integer dimensionNumBytes;
    @JsonProperty("attributes")
    public final Map<String, String> attributes;

    @JsonCreator
    public CustomFieldDefinition(@JsonProperty("template") final Template template,
                                 @JsonProperty("analyzer") final String analyzer,
                                 @JsonProperty("index_analyzer") final String indexAnalyzer,
                                 @JsonProperty("query_analyzer") final String queryAnalyzer,
                                 @JsonProperty("tokenized") final Boolean tokenized,
                                 @JsonProperty("stored") final Boolean stored,
                                 @JsonProperty("store_termvectors") final Boolean storeTermVectors,
                                 @JsonProperty("store_termvector_offsets") final Boolean storeTermVectorOffsets,
                                 @JsonProperty("store_termvector_positions") final Boolean storeTermVectorPositions,
                                 @JsonProperty("store_termvector_payloads") final Boolean storeTermVectorPayloads,
                                 @JsonProperty("omit_norms") final Boolean omitNorms,
                                 @JsonProperty("facet_multivalued") final Boolean facetMultivalued,
                                 @JsonProperty("facet_hierarchical") final Boolean facetHierarchical,
                                 @JsonProperty("facet_require_dim_count") final Boolean facetRequireDimCount,
                                 @JsonProperty("index_options") final IndexOptions indexOptions,
                                 @JsonProperty("docvalues_type") final DocValuesType docValuesType,
                                 @JsonProperty("index_dimension_count") final Integer indexDimensionCount,
                                 @JsonProperty("data_dimension_count") final Integer dataDimensionCount,
                                 @JsonProperty("dimension_num_bytes") final Integer dimensionNumBytes,
                                 @JsonProperty("attributes") final Map<String, String> attributes,
                                 @JsonProperty("copy_from") String[] copyFrom) {
        super(CustomFieldDefinition.class, null, analyzer, indexAnalyzer, queryAnalyzer, copyFrom);
        this.template = template;
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
        this.indexOptions = indexOptions;
        this.docValuesType = docValuesType;
        this.indexDimensionCount = indexDimensionCount;
        this.dataDimensionCount = dataDimensionCount;
        this.dimensionNumBytes = dimensionNumBytes;
        this.attributes = attributes;
    }

    private CustomFieldDefinition(final CustomBuilder builder) {
        super(CustomFieldDefinition.class, builder);
        this.template = builder.template;
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
        this.indexOptions = builder.indexOptions;
        this.docValuesType = builder.docValuesType;
        this.indexDimensionCount = builder.indexDimensionCount;
        this.dataDimensionCount = builder.dataDimensionCount;
        this.attributes = builder.attributes == null || builder.attributes.isEmpty() ?
            null :
            Collections.unmodifiableMap(new LinkedHashMap<>(builder.attributes));
        this.dimensionNumBytes = builder.dimensionNumBytes;
    }

    public CustomFieldDefinition(final String fieldName, final IndexField indexField, final Map<String, Copy> copyMap) {
        super(CustomFieldDefinition.class, null,
            from(indexField.analyzer(), indexField.analyzerClass()),
            from(indexField.indexAnalyzer(), indexField.indexAnalyzerClass()),
            from(indexField.queryAnalyzer(), indexField.queryAnalyzerClass()),
            from(fieldName, copyMap));
        template = indexField.template();
        tokenized = indexField.tokenized();
        stored = indexField.stored();
        storeTermVectors = indexField.storeTermVectors();
        storeTermVectorOffsets = indexField.storeTermVectorOffsets();
        storeTermVectorPositions = indexField.storeTermVectorPositions();
        storeTermVectorPayloads = indexField.storeTermVectorPayloads();
        omitNorms = indexField.omitNorms();
        indexOptions = indexField.indexOptions();
        docValuesType = indexField.docValuesType();
        indexDimensionCount = indexField.indexDimensionCount();
        dataDimensionCount = indexField.dataDimensionCount();
        dimensionNumBytes = indexField.dimensionNumBytes();
        facetMultivalued = indexField.facetMultivalued();
        facetHierarchical = indexField.facetHierarchical();
        facetRequireDimCount = indexField.facetRequireDimCount();
        attributes = null;
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(template, tokenized, stored, indexOptions, type, analyzer, indexAnalyzer, queryAnalyzer);
    }

    @Override
    protected boolean isEqual(final CustomFieldDefinition f) {
        return super.isEqual(f) &&
            Objects.equals(template, f.template) &&
            Objects.equals(tokenized, f.tokenized) &&
            Objects.equals(stored, f.stored) &&
            Objects.equals(storeTermVectors, f.storeTermVectors) &&
            Objects.equals(storeTermVectorOffsets, f.storeTermVectorOffsets) &&
            Objects.equals(storeTermVectorPositions, f.storeTermVectorPositions) &&
            Objects.equals(storeTermVectorPayloads, f.storeTermVectorPayloads) &&
            Objects.equals(omitNorms, f.omitNorms) &&
            Objects.equals(facetMultivalued, f.facetMultivalued) &&
            Objects.equals(facetHierarchical, f.facetHierarchical) &&
            Objects.equals(facetRequireDimCount, f.facetRequireDimCount) &&
            Objects.equals(indexOptions, f.indexOptions) &&
            Objects.equals(docValuesType, f.docValuesType) &&
            Objects.equals(dataDimensionCount, f.dataDimensionCount) &&
            Objects.equals(indexDimensionCount, f.indexDimensionCount) &&
            Objects.equals(dimensionNumBytes, f.dimensionNumBytes) &&
            Objects.equals(attributes, f.attributes);
    }

    @Override
    public FieldTypeInterface newFieldType(final String genericFieldName,
                                           final WildcardMatcher wildcardMatcher,
                                           final String primaryKey) {
        return template == null ?
            CustomFieldType.of(genericFieldName, wildcardMatcher, this) :
            template.newFieldType(genericFieldName, wildcardMatcher, this);
    }

    public static CustomBuilder of() {
        return new CustomBuilder();
    }

    static CustomBuilder of(Template template) {
        return new CustomBuilder().template(template);
    }

    public static class CustomBuilder extends AbstractBuilder<CustomBuilder> {

        private Template template;
        private Boolean tokenized;
        private Boolean stored;
        private Boolean storeTermVectors;
        private Boolean storeTermVectorOffsets;
        private Boolean storeTermVectorPositions;
        private Boolean storeTermVectorPayloads;
        private Boolean omitNorms;
        private IndexOptions indexOptions;
        private DocValuesType docValuesType;
        private Integer indexDimensionCount;
        private Integer dataDimensionCount;
        private Integer dimensionNumBytes;
        private Map<String, String> attributes;
        private Boolean facetMultivalued;
        private Boolean facetHierarchical;
        private Boolean facetRequireDimCount;

        public CustomBuilder template(Template template) {
            this.template = template;
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

        public CustomBuilder indexOptions(IndexOptions indexOptions) {
            this.indexOptions = indexOptions;
            return this;
        }

        public CustomBuilder docValuesType(DocValuesType docValuesType) {
            this.docValuesType = docValuesType;
            return this;
        }

        public CustomBuilder indexDimensionCount(Integer indexDimensionCount) {
            this.indexDimensionCount = indexDimensionCount;
            return this;
        }

        public CustomBuilder dataDimensionCount(Integer dataDimensionCount) {
            this.dataDimensionCount = dataDimensionCount;
            return this;
        }

        public CustomBuilder dimensionNumBytes(Integer dimensionNumBytes) {
            this.dimensionNumBytes = dimensionNumBytes;
            return this;
        }

        public CustomBuilder attribute(String name, String value) {
            if (this.attributes == null)
                this.attributes = new LinkedHashMap<>();
            this.attributes.put(name, value);
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

        @Override
        protected CustomBuilder me() {
            return this;
        }
    }

}
