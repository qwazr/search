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
package com.qwazr.search.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.index.QueryContext;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

public class SortedDocValuesExactQuery extends AbstractExactQuery<String, SortedDocValuesExactQuery> {

    @JsonCreator
    public SortedDocValuesExactQuery(@JsonProperty("generic_field") final String genericField,
            @JsonProperty("field") final String field, @JsonProperty("value") final String value) {
        super(SortedDocValuesExactQuery.class, genericField, field, value);
    }

    public SortedDocValuesExactQuery(final String field, final String value) {
        this(null, field, value);
    }

    @Override
    public Query getQuery(final QueryContext queryContext) throws IOException {
        return SortedDocValuesField.newSlowExactQuery(resolveField(queryContext.getFieldMap()), new BytesRef(value));
    }
}
