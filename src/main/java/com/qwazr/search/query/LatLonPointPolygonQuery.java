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
import org.apache.lucene.document.LatLonPoint;
import org.apache.lucene.geo.Polygon;
import org.apache.lucene.search.Query;

import java.util.Arrays;
import java.util.Objects;

public class LatLonPointPolygonQuery extends AbstractFieldQuery<LatLonPointPolygonQuery> {

	public final class GeoPolygon {

		public final double[] polyLats;
		public final double[] polyLons;
		public final GeoPolygon[] holes;

		@JsonCreator
		public GeoPolygon(@JsonProperty("polyLats") final double[] polyLats,
				@JsonProperty("polyLons") final double[] polyLons, @JsonProperty("holes") final GeoPolygon[] holes) {
			this.polyLats = polyLats;
			this.polyLons = polyLons;
			this.holes = holes;
		}

		@JsonIgnore
		public Polygon toPolygon() {
			return new Polygon(polyLats, polyLons, toPolygons(holes));
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof GeoPolygon))
				return false;
			if (o == this)
				return true;
			final GeoPolygon p = (GeoPolygon) o;
			return Arrays.equals(polyLats, p.polyLats) && Arrays.equals(polyLons, p.polyLons) &&
					Arrays.equals(holes, p.holes);
		}
	}

	final public GeoPolygon[] polygons;

	@JsonIgnore
	private Polygon[] lucenePolygons;

	@JsonCreator
	public LatLonPointPolygonQuery(@JsonProperty("generic_field") final String genericField,
			@JsonProperty("field") final String field, @JsonProperty("polygons") final GeoPolygon... polygons) {
		super(LatLonPointPolygonQuery.class, genericField, field);
		Objects.requireNonNull(polygons, "The poligons parameter is null");
		this.polygons = polygons;
		this.lucenePolygons = toPolygons(polygons);
	}

	public LatLonPointPolygonQuery(final String field, final GeoPolygon... polygons) {
		this(null, field, polygons);
	}

	@Override
	@JsonIgnore
	protected boolean isEqual(LatLonPointPolygonQuery q) {
		return super.isEqual(q) && Arrays.equals(polygons, q.polygons);
	}

	@Override
	final public Query getQuery(final QueryContext queryContext) {
		if (lucenePolygons == null)
			lucenePolygons = toPolygons(polygons);
		return LatLonPoint.newPolygonQuery(resolveField(queryContext.getFieldMap()), lucenePolygons);
	}

	public static Polygon[] toPolygons(final GeoPolygon... geoPolygons) {
		if (geoPolygons == null)
			return null;
		final Polygon[] polygons = new Polygon[geoPolygons.length];
		int i = 0;
		for (GeoPolygon geoPolygon : geoPolygons)
			polygons[i++] = geoPolygon == null ? null : geoPolygon.toPolygon();
		return polygons;
	}
}
