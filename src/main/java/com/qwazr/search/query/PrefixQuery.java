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
import com.qwazr.utils.StringUtils;
import java.util.Objects;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;

public class PrefixQuery extends AbstractFieldQuery<PrefixQuery> {

    final public String text;

    @JsonCreator
    public PrefixQuery(@JsonProperty("generic_field") final String genericField,
                       @JsonProperty("field") final String field, @JsonProperty("text") final String text) {
        super(PrefixQuery.class, genericField, field);
        this.text = text;
    }

    public PrefixQuery(final String field, final String text) {
        this(null, field, text);
    }

    @Override
    @JsonIgnore
    protected boolean isEqual(final PrefixQuery q) {
        return super.isEqual(q) && Objects.equals(text, q.text);
    }

    @Override
    final public Query getQuery(final QueryContext queryContext) {
        return new org.apache.lucene.search.PrefixQuery(
            new Term(resolveIndexTextField(queryContext.getFieldMap(), StringUtils.EMPTY), text)
        );
    }
}
