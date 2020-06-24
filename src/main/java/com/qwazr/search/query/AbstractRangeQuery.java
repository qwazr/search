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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public abstract class AbstractRangeQuery<V, T extends AbstractRangeQuery<V, T>> extends AbstractFieldQuery<T> {

    @JsonProperty("lower_value")
    final public V lowerValue;
    @JsonProperty("upper_value")
    final public V upperValue;

    protected AbstractRangeQuery(final Class<T> queryClass,
                                 final String genericField,
                                 final String field,
                                 final V lowerValue,
                                 final V upperValue) {
        super(queryClass, genericField, field);
        this.lowerValue = lowerValue;
        this.upperValue = upperValue;
    }

    @JsonIgnore
    @Override
    final protected boolean isEqual(T q) {
        return super.isEqual(q) && Objects.equals(lowerValue, q.lowerValue) &&
            Objects.equals(upperValue, q.upperValue);
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(lowerValue, upperValue);
    }

}
