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

import com.qwazr.utils.json.JsonMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

public class FieldDefinitionTest {

	@Test
	public void readCustomFieldDefinitionTest() throws IOException {
		Map<String, FieldDefinition> fields = JsonMapper.MAPPER.readValue(
				com.qwazr.search.test.JavaTest.class.getResourceAsStream("fields.json"),
				FieldDefinition.MapStringFieldTypeRef);
		Assert.assertNotNull(fields);
		fields.forEach((name, field) -> Assert.assertTrue(field instanceof CustomFieldDefinition));
	}

	@Test
	public void readSmartFieldDefinitionTest() throws IOException {
		Map<String, FieldDefinition> fields = JsonMapper.MAPPER.readValue(
				FieldDefinitionTest.class.getResourceAsStream("smart_fields.json"),
				FieldDefinition.MapStringFieldTypeRef);
		Assert.assertNotNull(fields);
		fields.forEach((name, field) -> Assert.assertTrue(field instanceof SmartFieldDefinition));
	}
}
