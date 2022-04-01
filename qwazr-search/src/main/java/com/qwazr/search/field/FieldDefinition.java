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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.qwazr.search.field.converters.MultiDVConverter;
import com.qwazr.search.field.converters.MultiReader;
import com.qwazr.search.field.converters.SingleDVConverter;
import com.qwazr.search.field.converters.ValueConverter;
import com.qwazr.utils.ObjectMappers;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.WildcardMatcher;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.function.Supplier;
import javax.validation.constraints.NotNull;
import org.apache.lucene.facet.FacetsConfig;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(
    creatorVisibility = JsonAutoDetect.Visibility.NONE,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    fieldVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    visible = true,
    property = "type",
    defaultImpl = CustomFieldDefinition.class)
@JsonSubTypes({@JsonSubTypes.Type(value = SmartFieldDefinition.class, name = "TEXT"),
    @JsonSubTypes.Type(value = SmartFieldDefinition.class, name = "INTEGER"),
    @JsonSubTypes.Type(value = SmartFieldDefinition.class, name = "LONG"),
    @JsonSubTypes.Type(value = SmartFieldDefinition.class, name = "FLOAT"),
    @JsonSubTypes.Type(value = SmartFieldDefinition.class, name = "DOUBLE")})
public interface FieldDefinition {

    /**
     * This property is present for polymorphism
     *
     * @return the type of the field
     */
    @JsonProperty("type")
    SmartFieldDefinition.Type getType();

    @JsonProperty("analyzer")
    String getAnalyzer();

    @JsonProperty("index_analyzer")
    String getIndexAnalyzer();

    @JsonProperty("query_analyzer")
    String getQueryAnalyzer();

    @JsonProperty("copy_from")
    @NotNull
    String[] getCopyFrom();


    String ID_FIELD = "$id$";

    String RECORD_FIELD = "$record$";

    String TAXONOMY_FACET_FIELD = FacetsConfig.DEFAULT_INDEX_FIELD_NAME;

    String TAXONOMY_INT_ASSOC_FACET_FIELD = FacetsConfig.DEFAULT_INDEX_FIELD_NAME + "$int";

    String TAXONOMY_FLOAT_ASSOC_FACET_FIELD = FacetsConfig.DEFAULT_INDEX_FIELD_NAME + "$float";

    String TAXONOMY_STRING_ASSOC_FACET_FIELD = FacetsConfig.DEFAULT_INDEX_FIELD_NAME + "$string";

    String DEFAULT_SORTEDSET_FACET_FIELD = FacetsConfig.DEFAULT_INDEX_FIELD_NAME + "$sdv";

    String SCORE_FIELD = "$score";

    String DOC_FIELD = "$doc";

    @JsonIgnore
    FieldTypeInterface newFieldType(final String genericFieldName,
                                    final WildcardMatcher wildcardMatcher,
                                    final String primaryKey);

    @JsonIgnore
    boolean hasFullTextAnalyzer();

    @JsonIgnore
    String resolvedIndexAnalyzer();

    @JsonIgnore
    String resolvedQueryAnalyzer();

    /* Used by CustomFieldDefinition */
    enum Template implements ValueConverter.Supplier, FieldTypeInterface.Supplier<CustomFieldDefinition> {
        NONE(CustomFieldType::of),
        DoublePoint(DoublePointType::of),
        FloatPoint(FloatPointType::of),
        IntPoint(IntPointType::of),
        LongPoint(LongPointType::of),
        DoubleField(DoublePointType::of),
        FloatField(FloatPointType::of),
        IntField(IntPointType::of),
        LongField(LongPointType::of),
        LongDocValuesField(LongDocValuesType::of, SingleDVConverter.LongDVConverter::new),
        IntDocValuesField(IntDocValuesType::of, SingleDVConverter.IntegerDVConverter::new),
        FloatDocValuesField(FloatDocValuesType::of, SingleDVConverter.FloatDVConverter::new),
        DoubleDocValuesField(DoubleDocValuesType::of, SingleDVConverter.DoubleDVConverter::new),
        LatLonPoint(LatLonPointType::of),
        Geo3DPoint(Geo3DPointType::of),
        SortedDocValuesField(SortedDocValuesType::of, SingleDVConverter.SortedDVConverter::new),
        SortedLongDocValuesField(SortedLongDocValuesType::of, MultiDVConverter.LongSetDVConverter::new),
        SortedIntDocValuesField(SortedIntDocValuesType::of, MultiDVConverter.IntegerSetDVConverter::new),
        SortedDoubleDocValuesField(SortedDoubleDocValuesType::of, MultiDVConverter.DoubleSetDVConverter::new),
        SortedFloatDocValuesField(SortedFloatDocValuesType::of, MultiDVConverter.FloatSetDVConverter::new),
        SortedSetDocValuesField(SortedSetDocValuesType::of, MultiDVConverter.SortedSetDVConverter::new),
        BinaryDocValuesField(BinaryDocValuesType::of, SingleDVConverter.BinaryDVConverter::new),
        StoredField(StoredFieldType::of),
        StringField(StringFieldType::of),
        TextField(TextFieldType::of),
        FacetField(FacetType::of),
        IntAssociatedField(IntAssociationFacetType::of),
        FloatAssociatedField(FloatAssociationFacetType::of),
        SortedSetDocValuesFacetField(SortedSetDocValuesFacetType::of);

        @NotNull
        private final FieldTypeInterface.Supplier<CustomFieldDefinition> fieldTypeSupplier;

        @NotNull
        private final ValueConverter.Supplier valueConverterSupplier;

        Template(final FieldTypeInterface.Supplier<CustomFieldDefinition> fieldTypeSupplier) {
            this(fieldTypeSupplier, (reader, field) -> null);
        }

        Template(final FieldTypeInterface.Supplier<CustomFieldDefinition> fieldTypeSupplier,
                 final ValueConverter.Supplier valueConverterSupplier) {
            this.fieldTypeSupplier = fieldTypeSupplier;
            this.valueConverterSupplier = valueConverterSupplier;
        }

        @Override
        final public ValueConverter<?> getConverter(final MultiReader reader, final String field) {
            return valueConverterSupplier.getConverter(reader, field);
        }

        @Override
        final public FieldTypeInterface newFieldType(final String genericFieldName,
                                                     final WildcardMatcher wildcardMatcher,
                                                     final CustomFieldDefinition definition) {
            return fieldTypeSupplier.newFieldType(genericFieldName, wildcardMatcher, definition);
        }
    }

    TypeReference<Map<String, FieldDefinition>> mapStringFieldTypeRef = new TypeReference<>() {
    };

    static Map<String, FieldDefinition> newFieldMap(final String jsonString) throws IOException {
        if (StringUtils.isEmpty(jsonString))
            return null;
        return ObjectMappers.JSON.readValue(jsonString, mapStringFieldTypeRef);
    }

    static FieldDefinition newField(final String jsonString) throws IOException {
        return ObjectMappers.JSON.readValue(jsonString, BaseFieldDefinition.class);
    }

    static void saveMap(final Map<String, FieldDefinition> fieldDefinitionMap, final File fieldMapFile)
        throws IOException {
        if (fieldDefinitionMap == null)
            Files.deleteIfExists(fieldMapFile.toPath());
        else
            ObjectMappers.JSON.writeValue(fieldMapFile, fieldDefinitionMap);
    }

    static Map<String, FieldDefinition> loadMap(final File fieldMapFile,
                                                final Supplier<Map<String, FieldDefinition>> defaultMap) throws IOException {
        return fieldMapFile != null && fieldMapFile.exists() && fieldMapFile.isFile() ?
            ObjectMappers.JSON.readValue(fieldMapFile, mapStringFieldTypeRef) :
            defaultMap == null ? null : defaultMap.get();
    }
}
