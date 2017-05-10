/**
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

import java.io.IOException;
import java.util.Objects;

public class LatLonPointPolygonQuery extends AbstractFieldQuery {

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

	}

	final public GeoPolygon[] polygons;

	@JsonIgnore
	private Polygon[] lucenePolygons;

	@JsonCreator
	public LatLonPointPolygonQuery(@JsonProperty("field") final String field,
			@JsonProperty("polygons") final GeoPolygon... poligons) {
		super(field);
		Objects.requireNonNull(poligons, "The poligons parameter is null");
		this.polygons = poligons;
		this.lucenePolygons = toPolygons(poligons);
	}

	@Override
	final public Query getQuery(final QueryContext queryContext) throws IOException {
		if (lucenePolygons == null)
			lucenePolygons = toPolygons(polygons);
		return LatLonPoint.newPolygonQuery(field, lucenePolygons);
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
