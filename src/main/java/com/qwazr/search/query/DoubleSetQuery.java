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
package com.qwazr.search.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.index.QueryContext;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.search.Query;

import java.util.Arrays;

public class DoubleSetQuery extends AbstractFieldQuery<DoubleSetQuery> {

    final public double[] values;

    @JsonCreator
    public DoubleSetQuery(@JsonProperty("generic_field") final String genericField,
                          @JsonProperty("field") final String field,
                          @JsonProperty("values") final double... values) {
        super(DoubleSetQuery.class, genericField, field);
        this.values = values;
    }

    @Override
    @JsonIgnore
    protected boolean isEqual(DoubleSetQuery q) {
        return super.isEqual(q) && Arrays.equals(values, q.values);
    }

    public DoubleSetQuery(final String field, final double... values) {
        this(null, field, values);
    }

    @Override
    public Query getQuery(final QueryContext queryContext) {
        return DoublePoint.newSetQuery(resolveField(queryContext.getFieldMap()), values);
    }
}
