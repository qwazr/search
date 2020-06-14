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
import com.qwazr.search.index.QueryContext;
import java.util.Map;

public class QueryContextTest implements QueryContext {

    private final FieldMap fieldMap;

    public QueryContextTest(final String primaryKey,
                            final String recordField,
                            final String sortedSetFacetField,
                            final Map<String, FieldDefinition> fields) {
        fieldMap = new FieldMap(primaryKey, fields, sortedSetFacetField, recordField);
    }

    @Override
    public FieldMap getFieldMap() {
        return fieldMap;
    }

    @Override
    public void close() {
    }

    public static QueryContextTest of(Map<String, FieldDefinition> fields) {
        return new QueryContextTest(FieldDefinition.ID_FIELD,
            FieldDefinition.RECORD_FIELD,
            FieldDefinition.DEFAULT_SORTEDSET_FACET_FIELD,
            fields
        );
    }

    public static final QueryContextTest DEFAULT = of(Map.of());
}
