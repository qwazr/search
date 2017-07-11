/*
 * Copyright 2017 Emmanuel Keller / QWAZR
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
package com.qwazr.search.test;

import com.qwazr.search.field.CustomFieldDefinition;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.utils.json.JsonMapper;
import org.apache.commons.lang3.RandomUtils;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexOptions;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@FixMethodOrder
public class CustomFieldDefinitionTest {

	private static List<FieldDefinition> fields;
	private static CustomFieldDefinition.CustomBuilder builder;
	private static Map<String, Object> map;

	@BeforeClass
	public static void before() {
		fields = new ArrayList<>();
		builder = CustomFieldDefinition.of();
		map = new LinkedHashMap<>();
	}

	public void checkFields(CustomFieldDefinition.CustomBuilder builder, String key, Object value) throws IOException {
		map.put(key, value);
		String jsonString = JsonMapper.MAPPER.writeValueAsString(map);
		final FieldDefinition fieldBuilder = builder.build();
		final FieldDefinition fieldJson = FieldDefinition.newField(jsonString);
		Assert.assertEquals(fieldBuilder, fieldJson);
		fields.forEach(field -> Assert.assertNotEquals(field, fieldJson));
		fields.add(fieldJson);
	}

	@Test
	public void check001Template() throws IOException {
		checkFields(builder.template(FieldDefinition.Template.TextField), "template", "TextField");
	}

	@Test
	public void check002Store() throws IOException {
		checkFields(builder.stored(true), "stored", true);
	}

	@Test
	public void check003Analyzer() throws IOException {
		checkFields(builder.analyzer("EnglishAnalyzer"), "analyzer", "EnglishAnalyzer");
		checkFields(builder.queryAnalyzer("EnglishMinimalAnalyzer"), "query_analyzer", "EnglishMinimalAnalyzer");
	}

	@Test
	public void check004TokenizedNorms() throws IOException {
		boolean v1 = RandomUtils.nextBoolean();
		checkFields(builder.tokenized(v1), "tokenized", v1);
		boolean v2 = RandomUtils.nextBoolean();
		checkFields(builder.omitNorms(v2), "omit_norms", v2);
	}

	@Test
	public void check005StoreTermVector() throws IOException {
		checkFields(builder.storeTermVectorOffsets(true), "store_termvector_offsets", true);
		checkFields(builder.storeTermVectorPayloads(true), "store_termvector_payloads", true);
		checkFields(builder.storeTermVectorPositions(true), "store_termvector_positions", true);
		checkFields(builder.storeTermVectors(true), "store_termvectors", true);
	}

	@Test
	public void check006Dimension() throws IOException {
		int v1 = RandomUtils.nextInt(1, 10);
		checkFields(builder.dimensionCount(v1), "dimension_count", v1);
		int v2 = RandomUtils.nextInt(1, 3) * 4;
		checkFields(builder.dimensionNumBytes(v2), "dimension_num_bytes", v2);
	}

	@Test
	public void check007Facet() throws IOException {
		boolean v1 = RandomUtils.nextBoolean();
		checkFields(builder.facetHierarchical(v1), "facet_hierarchical", v1);
		boolean v2 = RandomUtils.nextBoolean();
		checkFields(builder.facetMultivalued(v2), "facet_multivalued", v2);
		boolean v3 = RandomUtils.nextBoolean();
		checkFields(builder.facetRequireDimCount(v3), "facet_require_dim_count", v3);
	}

	@Test
	public void check008DocValueType() throws IOException {
		DocValuesType v1 = DocValuesType.values()[RandomUtils.nextInt(0, DocValuesType.values().length)];
		checkFields(builder.docValuesType(v1), "docvalues_type", v1);
	}

	@Test
	public void check009IndexOptions() throws IOException {
		IndexOptions v1 = IndexOptions.values()[RandomUtils.nextInt(0, IndexOptions.values().length)];
		checkFields(builder.indexOptions(v1), "index_options", v1);
	}
}
