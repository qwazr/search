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
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public abstract class AbstractExactQuery<V, T extends AbstractExactQuery<V, T>> extends AbstractFieldQuery<T> {

    final public V value;

    protected AbstractExactQuery(final Class<T> queryClass,
                                 @JsonProperty("field") final String field,
                                 @JsonProperty("value") final V value) {
        this(queryClass, (String) null, field, value);
    }

    protected AbstractExactQuery(final Class<T> queryClass,
                                 final String genericField,
                                 @JsonProperty("field") final String field,
                                 @JsonProperty("value") final V value) {
        super(queryClass, genericField, field);
        this.value = Objects.requireNonNull(value, "The value is missing");
    }

    @Override
    @JsonIgnore
    final protected boolean isEqual(T q) {
        return super.isEqual(q) && Objects.equals(value, q.value);
    }

    @Override
    final protected int computeHashCode() {
        return Objects.hash(field, value);
    }
}

