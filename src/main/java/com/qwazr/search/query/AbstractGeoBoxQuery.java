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

public abstract class AbstractGeoBoxQuery<T extends AbstractGeoBoxQuery<T>> extends AbstractFieldQuery<T> {

    @JsonProperty("min_latitude")
    final public double minLatitude;

    @JsonProperty("max_latitude")
    final public double maxLatitude;

    @JsonProperty("min_longitude")
    final public double minLongitude;

    @JsonProperty("max_longitude")
    final public double maxLongitude;

    public AbstractGeoBoxQuery(final Class<T> queryClass,
                               final String genericField,
                               final String field,
                               final double minLatitude,
                               final double maxLatitude,
                               final double minLongitude,
                               final double maxLongitude) {
        super(queryClass, genericField, field);
        this.minLatitude = minLatitude;
        this.maxLatitude = maxLatitude;
        this.minLongitude = minLongitude;
        this.maxLongitude = maxLongitude;
    }

    @Override
    @JsonIgnore
    protected boolean isEqual(T q) {
        return super.isEqual(q) && minLatitude == q.minLatitude && maxLatitude == q.maxLatitude &&
            minLongitude == q.minLongitude && maxLongitude == q.maxLongitude;
    }

}
