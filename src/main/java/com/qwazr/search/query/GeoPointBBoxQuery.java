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

public class GeoPointBBoxQuery extends AbstractQuery {

	final public String field;

	final public double min_lat;

	final public double max_lat;

	final public double min_lon;

	final public double max_lon;

	public GeoPointBBoxQuery() {
		field = null;
		min_lat = 0;
		max_lat = 0;
		min_lon = 0;
		max_lon = 0;
	}

	public GeoPointBBoxQuery(final String field, final double minLat, final double maxLat,
			final double minLon, final double maxLon) {
		Objects.requireNonNull(field, "The field is null");
		this.field = field;
		min_lat = minLat;
		max_lat = maxLat;
		min_lon = minLon;
		max_lon = maxLon;
	}

	@Override
	final public Query getQuery(QueryContext queryContext) throws IOException {
		return new org.apache.lucene.spatial.geopoint.search.GeoPointInBBoxQuery(field, min_lat, max_lat,
				min_lon, max_lon);
	}
}
