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
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.qwazr.search.analysis.AnalyzerContext;
import com.qwazr.search.annotations.Copy;
import com.qwazr.search.annotations.IndexField;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.WildcardMatcher;
import com.qwazr.utils.json.JsonMapper;
import org.apache.lucene.facet.FacetsConfig;

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
		include = JsonTypeInfo.As.PROPERTY,
		property = "type",
		defaultImpl = CustomFieldDefinition.class)
public abstract class FieldDefinition {

	public enum Template {
		NONE(CustomFieldType::new),
		DoublePoint(DoublePointType::new),
		FloatPoint(FloatPointType::new),
		IntPoint(IntPointType::new),
		LongPoint(LongPointType::new),
		DoubleField(DoublePointType::new),
		FloatField(FloatPointType::new),
		IntField(IntPointType::new),
		LongField(LongPointType::new),
		LongDocValuesField(LongDocValuesType::new),
		IntDocValuesField(IntDocValuesType::new),
		FloatDocValuesField(FloatDocValuesType::new),
		DoubleDocValuesField(DoubleDocValuesType::new),
		LatLonPoint(LatLonPointType::new),
		Geo3DPoint(Geo3DPointType::new),
		SortedDocValuesField(SortedDocValuesType::new),
		SortedLongDocValuesField(SortedLongDocValuesType::new),
		SortedIntDocValuesField(SortedIntDocValuesType::new),
		SortedDoubleDocValuesField(SortedDoubleDocValuesType::new),
		SortedFloatDocValuesField(SortedFloatDocValuesType::new),
		SortedSetDocValuesField(SortedSetDocValuesType::new),
		BinaryDocValuesField(BinaryDocValuesType::new),
		StoredField(StoredFieldType::new),
		StringField(StringFieldType::new),
		TextField(TextFieldType::new),
		FacetField(FacetType::new),
		IntAssociatedField(IntAssociationFacetType::new),
		FloatAssociatedField(FloatAssociationFacetType::new),
		SortedSetDocValuesFacetField(SortedSetDocValuesFacetType::new),
		SmartField(SmartField::new);

		public final FieldBuilder builder;

		Template(FieldBuilder builder) {
			this.builder = builder;
		}

	}

	@FunctionalInterface
	public interface FieldBuilder {
		FieldTypeInterface build(final WildcardMatcher wildcardMatcher, final FieldDefinition fieldDefinition);
	}

	public final Template template;

	@JsonProperty("copy_from")
	public final String[] copyFrom;

	@JsonCreator
	FieldDefinition(@JsonProperty("template") final Template template,
			@JsonProperty("copy_from") final String[] copyFrom) {
		this.template = template;
		this.copyFrom = copyFrom;
	}

	FieldDefinition(final AbstractBuilder builder) {
		this.template = builder.template;
		this.copyFrom = builder.copyFrom == null || builder.copyFrom.isEmpty() ? null : builder.copyFrom.toArray(
				new String[builder.copyFrom.size()]);
	}

	FieldDefinition(final String fieldName, final IndexField indexField, final Map<String, Copy> copyMap) {
		template = indexField.template();
		copyFrom = from(fieldName, copyMap);
	}

	public abstract void setFacetsConfig(String fieldName, FacetsConfig facetsConfig);

	public abstract void setIndexAnalyzer(String fieldName, AnalyzerContext.Builder builder)
			throws ReflectiveOperationException, IOException;

	public abstract void setQueryAnalyzer(String fieldName, AnalyzerContext.Builder builder)
			throws ReflectiveOperationException, IOException;

	@Override
	public boolean equals(final Object o) {
		if (o == null || !(o instanceof FieldDefinition))
			return false;
		if (o == this)
			return true;
		final FieldDefinition f = (FieldDefinition) o;
		return Objects.equals(template, f.template);
	}

	public final static TypeReference<LinkedHashMap<String, FieldDefinition>> MapStringFieldTypeRef =
			new TypeReference<LinkedHashMap<String, FieldDefinition>>() {
			};

	public static LinkedHashMap<String, FieldDefinition> newFieldMap(final String jsonString) throws IOException {
		if (StringUtils.isEmpty(jsonString))
			return null;
		return JsonMapper.MAPPER.readValue(jsonString, MapStringFieldTypeRef);
	}

	public static FieldDefinition newField(final String jsonString) throws IOException {
		return JsonMapper.MAPPER.readValue(jsonString, FieldDefinition.class);
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
			JsonMapper.MAPPER.writeValue(fieldMapFile, fieldDefinitionMap);
	}

	static public LinkedHashMap<String, FieldDefinition> loadMap(final File fieldMapFile,
			final Supplier<LinkedHashMap<String, FieldDefinition>> defaultMap) throws IOException {
		return fieldMapFile != null && fieldMapFile.exists() && fieldMapFile.isFile() ? JsonMapper.MAPPER.readValue(
				fieldMapFile, FieldDefinition.MapStringFieldTypeRef) : defaultMap == null ? null : defaultMap.get();
	}

	private static String[] from(final String fieldName, final Map<String, Copy> copyMap) {
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
		return globalCopyFromList.toArray(new String[globalCopyFromList.size()]);
	}

	abstract static class AbstractBuilder {

		private Template template;
		private LinkedHashSet<String> copyFrom;

		AbstractBuilder template(Template template) {
			this.template = template;
			return this;
		}

		AbstractBuilder copyFrom(String field) {
			if (copyFrom == null)
				copyFrom = new LinkedHashSet<>();
			copyFrom.add(field);
			return this;
		}

	}

}
