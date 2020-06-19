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
package com.qwazr.search.query;

import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.FieldMap;
import com.qwazr.search.index.FieldsContext;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.QueryContext;
import java.util.Map;

public class QueryContextTest implements QueryContext {

    private final FieldMap fieldMap;

    public QueryContextTest(final IndexSettingsDefinition indexSettings,
                            final Map<String, FieldDefinition> fields) {
        fieldMap = new FieldMap(new FieldsContext(indexSettings, fields));
    }

    @Override
    public FieldMap getFieldMap() {
        return fieldMap;
    }

    @Override
    public void close() {
    }

    public static QueryContextTest of(final Map<String, FieldDefinition> fields) {
        return new QueryContextTest(IndexSettingsDefinition.of().build(), fields);
    }

    public static final QueryContextTest DEFAULT = of(Map.of());
}
