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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.field.FieldTypeInterface;
import com.qwazr.search.index.QueryContext;
import org.apache.lucene.search.Query;
import org.apache.lucene.spatial3d.Geo3DPoint;
import org.apache.lucene.spatial3d.geom.PlanetModel;

public class Geo3DBoxQuery extends AbstractGeoBoxQuery<Geo3DBoxQuery> {

    public enum PlanetModelEnum {

        SPHERE(PlanetModel.SPHERE),
        WGS84(PlanetModel.WGS84),
        CLARKE_1866(PlanetModel.CLARKE_1866);

        final PlanetModel planetModel;

        PlanetModelEnum(PlanetModel planetModel) {
            this.planetModel = planetModel;
        }
    }

    @JsonProperty("planet_model")
    final public PlanetModelEnum planetModel;

    @JsonCreator
    public Geo3DBoxQuery(@JsonProperty("generic_field") final String genericField,
                         @JsonProperty("field") final String field,
                         @JsonProperty("planet_model") final PlanetModelEnum planetModel,
                         @JsonProperty("min_latitude") final double minLat,
                         @JsonProperty("max_latitude") final double maxLat,
                         @JsonProperty("min_longitude") final double minLon,
                         @JsonProperty("max_longitude") final double maxLon) {
        super(Geo3DBoxQuery.class, genericField, field, minLat, maxLat, minLon, maxLon);
        this.planetModel = planetModel;
    }

    public Geo3DBoxQuery(final String field,
                         final PlanetModelEnum planetModel,
                         final double minLat,
                         final double maxLat,
                         final double minLon,
                         final double maxLon) {
        this(null, field, planetModel, minLat, maxLat, minLon, maxLon);
    }

    @Override
    final public Query getQuery(final QueryContext queryContext) {
        return Geo3DPoint.newBoxQuery(
            resolvePointField(queryContext.getFieldMap(), 0D, FieldTypeInterface.ValueType.doubleType),
            planetModel.planetModel, minLatitude, maxLatitude, minLongitude, maxLongitude
        );
    }
}
