/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.qwazr.search.annotations.Copy;
import com.qwazr.search.field.converters.MultiDVConverter;
import com.qwazr.search.field.converters.MultiReader;
import com.qwazr.search.field.converters.SingleDVConverter;
import com.qwazr.search.field.converters.ValueConverter;
import com.qwazr.utils.ArrayUtils;
import com.qwazr.utils.ObjectMappers;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.WildcardMatcher;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.facet.FacetsConfig;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Supplier;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
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
//@JsonDeserialize(using = FieldDefinition.FieldDeserializer.class)
public abstract class FieldDefinition {

    /* Used by CustomFieldDefinition */
    public enum Template implements ValueConverter.Supplier, FieldTypeInterface.Supplier {
        NONE(CustomFieldType::new),
        DoublePoint(DoublePointType::new),
        FloatPoint(FloatPointType::new),
        IntPoint(IntPointType::new),
        LongPoint(LongPointType::new),
        DoubleField(DoublePointType::new),
        FloatField(FloatPointType::new),
        IntField(IntPointType::new),
        LongField(LongPointType::new),
        LongDocValuesField(LongDocValuesType::new, SingleDVConverter.LongDVConverter::new),
        IntDocValuesField(IntDocValuesType::new, SingleDVConverter.IntegerDVConverter::new),
        FloatDocValuesField(FloatDocValuesType::new, SingleDVConverter.FloatDVConverter::new),
        DoubleDocValuesField(DoubleDocValuesType::new, SingleDVConverter.DoubleDVConverter::new),
        LatLonPoint(LatLonPointType::new),
        Geo3DPoint(Geo3DPointType::new),
        SortedDocValuesField(SortedDocValuesType::new, SingleDVConverter.SortedDVConverter::new),
        SortedLongDocValuesField(SortedLongDocValuesType::new, MultiDVConverter.LongSetDVConverter::new),
        SortedIntDocValuesField(SortedIntDocValuesType::new, MultiDVConverter.IntegerSetDVConverter::new),
        SortedDoubleDocValuesField(SortedDoubleDocValuesType::new, MultiDVConverter.DoubleSetDVConverter::new),
        SortedFloatDocValuesField(SortedFloatDocValuesType::new, MultiDVConverter.FloatSetDVConverter::new),
        SortedSetDocValuesField(SortedSetDocValuesType::new, MultiDVConverter.SortedSetDVConverter::new),
        BinaryDocValuesField(BinaryDocValuesType::new, SingleDVConverter.BinaryDVConverter::new),
        StoredField(StoredFieldType::new),
        StringField(StringFieldType::new),
        TextField(TextFieldType::new),
        FacetField(FacetType::new),
        IntAssociatedField(IntAssociationFacetType::new),
        FloatAssociatedField(FloatAssociationFacetType::new),
        SortedSetDocValuesFacetField(SortedSetDocValuesFacetType::new);

        @NotNull
        private final FieldTypeInterface.Supplier fieldTypeSupplier;

        @NotNull
        private final ValueConverter.Supplier valueConverterSupplier;

        Template(final FieldTypeInterface.Supplier fieldTypeSupplier) {
            this(fieldTypeSupplier, (reader, field) -> null);
        }

        Template(final FieldTypeInterface.Supplier fieldTypeSupplier,
                 final ValueConverter.Supplier valueConverterSupplier) {
            this.fieldTypeSupplier = fieldTypeSupplier;
            this.valueConverterSupplier = valueConverterSupplier;
        }

        @Override
        final public ValueConverter getConverter(final MultiReader reader, final String field) {
            return valueConverterSupplier.getConverter(reader, field);
        }

        @Override
        final public FieldTypeInterface newFieldType(final String genericFieldName,
                                                     final WildcardMatcher wildcardMatcher, final FieldDefinition definition) {
            return fieldTypeSupplier.newFieldType(genericFieldName, wildcardMatcher, definition);
        }
    }

    public final String analyzer;

    /**
     * This property is present for polymorphism
     */
    public final SmartFieldDefinition.Type type;

    @JsonProperty("query_analyzer")
    public final String queryAnalyzer;

    @JsonProperty("copy_from")
    public final String[] copyFrom;

    @JsonCreator
    FieldDefinition(@JsonProperty("type") SmartFieldDefinition.Type type,
                    @JsonProperty("analyzer") final String analyzer, @JsonProperty("query_analyzer") final String queryAnalyzer,
                    @JsonProperty("copy_from") final String[] copyFrom) {
        this.type = type;
        this.analyzer = analyzer;
        this.queryAnalyzer = queryAnalyzer;
        this.copyFrom = copyFrom;
    }

    FieldDefinition(final AbstractBuilder<? extends AbstractBuilder> builder) {
        this.type = builder.type;
        this.analyzer = builder.analyzer;
        this.queryAnalyzer = builder.queryAnalyzer;
        this.copyFrom = builder.copyFrom == null || builder.copyFrom.isEmpty() ?
                null :
                builder.copyFrom.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type);
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof FieldDefinition))
            return false;
        if (o == this)
            return true;
        final FieldDefinition f = (FieldDefinition) o;
        return Objects.equals(type, f.type) && Objects.equals(analyzer, f.analyzer) &&
                Objects.equals(queryAnalyzer, f.queryAnalyzer);
    }

    public final static TypeReference<LinkedHashMap<String, FieldDefinition>> mapStringFieldTypeRef =
            new TypeReference<LinkedHashMap<String, FieldDefinition>>() {
            };

    public static LinkedHashMap<String, FieldDefinition> newFieldMap(final String jsonString) throws IOException {
        if (StringUtils.isEmpty(jsonString))
            return null;
        return ObjectMappers.JSON.readValue(jsonString, mapStringFieldTypeRef);
    }

    public static FieldDefinition newField(final String jsonString) throws IOException {
        return ObjectMappers.JSON.readValue(jsonString, FieldDefinition.class);
    }

    public final static String ID_FIELD = "$id$";

    public final static String TAXONOMY_FACET_FIELD = FacetsConfig.DEFAULT_INDEX_FIELD_NAME;

    public final static String TAXONOMY_INT_ASSOC_FACET_FIELD = FacetsConfig.DEFAULT_INDEX_FIELD_NAME + "$int";

    public final static String TAXONOMY_FLOAT_ASSOC_FACET_FIELD = FacetsConfig.DEFAULT_INDEX_FIELD_NAME + "$float";

    public final static String TAXONOMY_STRING_ASSOC_FACET_FIELD = FacetsConfig.DEFAULT_INDEX_FIELD_NAME + "$string";

    public final static String DEFAULT_SORTEDSET_FACET_FIELD = FacetsConfig.DEFAULT_INDEX_FIELD_NAME + "$sdv";

    public final static String SCORE_FIELD = "$score";

    public final static String DOC_FIELD = "$doc";

    static public void saveMap(final LinkedHashMap<String, FieldDefinition> fieldDefinitionMap, final File fieldMapFile)
            throws IOException {
        if (fieldDefinitionMap == null)
            Files.deleteIfExists(fieldMapFile.toPath());
        else
            ObjectMappers.JSON.writeValue(fieldMapFile, fieldDefinitionMap);
    }

    static public LinkedHashMap<String, FieldDefinition> loadMap(final File fieldMapFile,
                                                                 final Supplier<LinkedHashMap<String, FieldDefinition>> defaultMap) throws IOException {
        return fieldMapFile != null && fieldMapFile.exists() && fieldMapFile.isFile() ?
                ObjectMappers.JSON.readValue(fieldMapFile, FieldDefinition.mapStringFieldTypeRef) :
                defaultMap == null ? null : defaultMap.get();
    }

    protected static String from(String analyzerName, Class<? extends Analyzer> analyzerClass) {
        return analyzerClass != Analyzer.class ?
                analyzerClass.getName() :
                StringUtils.isEmpty(analyzerName) ? null : analyzerName;
    }

    protected static String[] from(final String fieldName, final Map<String, Copy> copyMap) {
        if (copyMap == null || copyMap.isEmpty())
            return null;
        final TreeMap<Integer, List<String>> map = new TreeMap<>();
        copyMap.forEach((name, copy) -> {
            for (Copy.To to : copy.to())
                if (fieldName.equals(to.field()))
                    map.computeIfAbsent(to.order(), order -> new ArrayList<>()).add(name);
        });

        final List<String> globalCopyFromList = new ArrayList<>();
        map.forEach((order, copyFromList) -> globalCopyFromList.addAll(copyFromList));
        return globalCopyFromList.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    public abstract FieldTypeInterface newFieldType(final String genericFieldName,
                                                    final WildcardMatcher wildcardMatcher);

    public static abstract class AbstractBuilder<B extends AbstractBuilder> {

        SmartFieldDefinition.Type type;
        String analyzer;
        String queryAnalyzer;
        LinkedHashSet<String> copyFrom;

        protected abstract B me();

        public B type(SmartFieldDefinition.Type type) {
            this.type = type;
            return me();
        }

        final public B analyzer(String analyzer) {
            this.analyzer = analyzer;
            return me();
        }

        final public B queryAnalyzer(String queryAnalyzer) {
            this.queryAnalyzer = queryAnalyzer;
            return me();
        }

        final public B copyFrom(String field) {
            if (copyFrom == null)
                copyFrom = new LinkedHashSet<>();
            copyFrom.add(field);
            return me();
        }
    }

    final public static class Builder extends AbstractBuilder<Builder> {

        @Override
        protected Builder me() {
            return this;
        }
    }

}
