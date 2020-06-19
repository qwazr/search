/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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

import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.test.units.AbstractIndexTest;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.LinkedHashMap;

public class SmartFieldBuilderTest extends AbstractIndexTest {

    private static IndexServiceInterface indexService;

    private final static String SCHEMA = "smartFieldTestSchema";
    private final static String INDEX = "smartFieldTestIndex";

    @BeforeClass
    public static void setup() {
        initIndexManager();
        indexService = indexManager.getService();
        indexService.createUpdateSchema(SCHEMA);
        indexService.createUpdateIndex(SCHEMA, INDEX);
    }

    @Test
    public void testSetField() {
        indexService.setField(SCHEMA, INDEX, "emptyField", SmartFieldDefinition.of().build());
        indexService.setField(SCHEMA, INDEX, "doubleField",
            SmartFieldDefinition.of().type(SmartFieldDefinition.Type.DOUBLE).build());
        indexService.setField(SCHEMA, INDEX, "indexField", SmartFieldDefinition.of().index(true).build());
    }

    @Test
    public void testSetFields() {
        Map<String, FieldDefinition> fields = new LinkedHashMap<>();
        fields.put("emptyField2", SmartFieldDefinition.of().build());
        fields.put("doubleField2", SmartFieldDefinition.of().type(SmartFieldDefinition.Type.DOUBLE).build());
        fields.put("indexField2", SmartFieldDefinition.of().index(true).build());
        indexService.setFields(SCHEMA, INDEX, fields);

    }
}
