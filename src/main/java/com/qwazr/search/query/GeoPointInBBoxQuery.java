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

public class GeoPointInBBoxQuery extends AbstractGeoBoxQuery {

	public GeoPointInBBoxQuery() {
	}

	public GeoPointInBBoxQuery(final String field, final double minLatitude, final double maxLatitude,
			final double minLongitude, final double maxLongitude) {
		super(field, minLatitude, maxLatitude, minLongitude, maxLongitude);
	}

	@Override
	final public Query getQuery(final QueryContext queryContext) throws IOException {
		return new org.apache.lucene.spatial.geopoint.search.GeoPointInBBoxQuery(field, min_latitude, max_latitude,
				min_longitude, max_longitude);
	}
}
