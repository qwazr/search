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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qwazr.search.field.FieldTypeInterface;
import com.qwazr.search.index.FieldMap;
import com.qwazr.utils.StringUtils;
import java.util.Objects;
import org.apache.lucene.index.Term;

public abstract class AbstractFieldSpanQuery<T extends AbstractFieldSpanQuery<T>> extends AbstractSpanQuery<T> {

    final public String genericField;
    final public String field;

    protected AbstractFieldSpanQuery(final Class<T> queryClass, final String genericField, final String field) {
        super(queryClass);
        this.field = Objects.requireNonNull(field, "The field is null");
        this.genericField = genericField;
    }

    @Override
    @JsonIgnore
    protected boolean isEqual(T q) {
        return Objects.equals(genericField, q.genericField) && Objects.equals(field, q.field);
    }
    
}
