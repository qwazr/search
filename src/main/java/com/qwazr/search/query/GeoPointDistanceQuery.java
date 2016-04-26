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
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.Objects;

public class GeoPointDistanceQuery extends AbstractQuery {

	final public String field;

	final public double center_lat;

	final public double center_lon;

	final public double radius_meter;

	public GeoPointDistanceQuery() {
		field = null;
		center_lat = 0;
		center_lon = 0;
		radius_meter = 0;
	}

	public GeoPointDistanceQuery(final String field, final double centerLat, final double centerLon,
			final double radiusMeter) {
		Objects.requireNonNull(field, "The field is null");
		this.field = field;
		this.center_lat = centerLat;
		this.center_lon = centerLon;
		this.radius_meter = radiusMeter;
	}

	@Override
	final public Query getQuery(QueryContext queryContext) throws IOException {
		return new org.apache.lucene.spatial.geopoint.search.GeoPointDistanceQuery(field, center_lat, center_lon,
				radius_meter);
	}
}
