/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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
import org.apache.lucene.search.Query;
import org.apache.lucene.spatial3d.Geo3DPoint;

import java.io.IOException;

public class Geo3DDistanceQuery extends AbstractFieldQuery<Geo3DDistanceQuery> {

	public final double latitude;

	public final double longitude;

	public final double radius_meters;

	@JsonCreator
	public Geo3DDistanceQuery(@JsonProperty("generic_field") final String genericField,
			@JsonProperty("field") final String field, @JsonProperty("latitude") final double latitude,
			@JsonProperty("longitude") final double longitude,
			@JsonProperty("radius_meters") final double radiusMeters) {
		super(Geo3DDistanceQuery.class, genericField, field);
		this.latitude = latitude;
		this.longitude = longitude;
		this.radius_meters = radiusMeters;
	}

	public Geo3DDistanceQuery(final String field, final double latitude, final double longitude,
			final double radiusMeters) {
		this(null, field, latitude, longitude, radiusMeters);
	}

	@Override
	@JsonIgnore
	protected boolean isEqual(Geo3DDistanceQuery q) {
		return super.isEqual(q) && latitude == q.latitude && longitude == q.longitude &&
				radius_meters == q.radius_meters;
	}

	@Override
	final public Query getQuery(final QueryContext queryContext) throws IOException {
		return Geo3DPoint.newDistanceQuery(resolveField(queryContext.getFieldMap()), latitude, longitude,
				radius_meters);
	}
}
