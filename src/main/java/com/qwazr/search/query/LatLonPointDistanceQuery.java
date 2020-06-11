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
import com.qwazr.search.field.FieldTypeInterface;
import com.qwazr.search.index.QueryContext;
import org.apache.lucene.document.LatLonPoint;
import org.apache.lucene.search.Query;

public class LatLonPointDistanceQuery extends AbstractFieldQuery<LatLonPointDistanceQuery> {

    @JsonProperty("center_latitude")
    final public double centerLatitude;

    @JsonProperty("center_longitude")
    final public double centerLongitude;

    @JsonProperty("radius_meters")
    final public double radiusMeters;

    @JsonCreator
    public LatLonPointDistanceQuery(@JsonProperty("generic_field") final String genericField,
                                    @JsonProperty("field") final String field,
                                    @JsonProperty("center_latitude") final double centerLatitude,
                                    @JsonProperty("center_longitude") final double centerLongitude,
                                    @JsonProperty("radius_meters") final double radiusMeters) {
        super(LatLonPointDistanceQuery.class, genericField, field);
        this.centerLatitude = centerLatitude;
        this.centerLongitude = centerLongitude;
        this.radiusMeters = radiusMeters;
    }

    @Override
    @JsonIgnore
    protected boolean isEqual(final LatLonPointDistanceQuery q) {
        return super.isEqual(q) && centerLatitude == q.centerLatitude && centerLongitude == q.centerLongitude &&
            radiusMeters == q.radiusMeters;
    }

    @Override
    final public Query getQuery(final QueryContext queryContext) {
        return LatLonPoint.newDistanceQuery(
            resolveField(queryContext.getFieldMap(), FieldTypeInterface.LuceneFieldType.point),
            centerLatitude, centerLongitude, radiusMeters);
    }
}
