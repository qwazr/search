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
import org.apache.lucene.search.Query;
import org.apache.lucene.spatial3d.Geo3DPoint;

public class Geo3DDistanceQuery extends AbstractFieldQuery<Geo3DDistanceQuery> {

    @JsonProperty("planet_model")
    public final Geo3DBoxQuery.PlanetModelEnum planetModel;

    public final double latitude;

    public final double longitude;

    @JsonProperty("radius_meters")
    public final double radiusMeters;

    @JsonCreator
    public Geo3DDistanceQuery(@JsonProperty("generic_field") final String genericField,
                              @JsonProperty("field") final String field,
                              @JsonProperty("planet_model") final Geo3DBoxQuery.PlanetModelEnum planetModel,
                              @JsonProperty("latitude") final double latitude,
                              @JsonProperty("longitude") final double longitude,
                              @JsonProperty("radius_meters") final double radiusMeters) {
        super(Geo3DDistanceQuery.class, genericField, field);
        this.planetModel = planetModel;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusMeters = radiusMeters;
    }

    public Geo3DDistanceQuery(final String field,
                              final Geo3DBoxQuery.PlanetModelEnum planetModel,
                              final double latitude,
                              final double longitude,
                              final double radiusMeters) {
        this(null, field, planetModel, latitude, longitude, radiusMeters);
    }

    @Override
    @JsonIgnore
    protected boolean isEqual(final Geo3DDistanceQuery q) {
        return super.isEqual(q) && latitude == q.latitude && longitude == q.longitude &&
            radiusMeters == q.radiusMeters;
    }

    @Override
    final public Query getQuery(final QueryContext queryContext) {
        return Geo3DPoint.newDistanceQuery(
            resolveField(queryContext.getFieldMap(), FieldTypeInterface.LuceneFieldType.point),
            planetModel.planetModel, latitude, longitude, radiusMeters);
    }
}
