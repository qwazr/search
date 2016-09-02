/**
 * Copyright 2015-2016 Emmanuel Keller / QWAZR
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

import com.qwazr.search.index.QueryContext;
import org.apache.htrace.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.lucene.geo.Polygon;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.Objects;

public class GeoPointInPolygonQuery extends AbstractQuery {

	public final class GeoPolygon {

		public final double[] polyLats;
		public final double[] polyLons;
		public final GeoPolygon[] holes;

		public GeoPolygon() {
			polyLats = null;
			polyLons = null;
			holes = null;
		}

		public GeoPolygon(final double[] polyLats, final double[] polyLons, final GeoPolygon[] holes) {
			this.polyLats = polyLats;
			this.polyLons = polyLons;
			this.holes = holes;
		}

		@JsonIgnore
		public Polygon toPolygon() {
			return new Polygon(polyLats, polyLons, toPolygons(holes));
		}

	}

	final public String field;
	final public GeoPolygon[] polygons;

	@JsonIgnore
	private Polygon[] lucenePolygons;

	public GeoPointInPolygonQuery() {
		field = null;
		polygons = null;
		lucenePolygons = null;
	}

	public GeoPointInPolygonQuery(final String field, final GeoPolygon... poligons) {
		Objects.requireNonNull(field, "The field is null");
		Objects.requireNonNull(poligons, "The poligons parameter is null");
		this.field = field;
		this.polygons = poligons;
		this.lucenePolygons = toPolygons(poligons);
	}

	@Override
	final public Query getQuery(final QueryContext queryContext) throws IOException {
		if (lucenePolygons == null)
			lucenePolygons = toPolygons(polygons);
		return new org.apache.lucene.spatial.geopoint.search.GeoPointInPolygonQuery(field, lucenePolygons);
	}

	public static Polygon[] toPolygons(GeoPolygon... geoPolygons) {
		if (geoPolygons == null)
			return null;
		final Polygon[] polygons = new Polygon[geoPolygons.length];
		int i = 0;
		for (GeoPolygon geoPolygon : geoPolygons)
			polygons[i++] = geoPolygon == null ? null : geoPolygon.toPolygon();
		return polygons;
	}
}
