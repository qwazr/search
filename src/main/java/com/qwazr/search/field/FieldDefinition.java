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

	public static final String[] RESERVED_NAMES = { ID_FIELD, FACET_FIELD };
}
