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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.index.QueryContext;
import org.apache.lucene.search.Query;
import org.apache.lucene.spatial3d.Geo3DPoint;

import java.io.IOException;

public class Geo3DBoxQuery extends AbstractGeoBoxQuery {

	@JsonCreator
	public Geo3DBoxQuery(@JsonProperty("generic_field") final String genericField,
			@JsonProperty("field") final String field, @JsonProperty("min_latitude") final double minLat,
			@JsonProperty("max_latitude") final double maxLat, @JsonProperty("min_longitude") final double minLon,
			@JsonProperty("max_longitude") final double maxLon) {
		super(genericField, field, minLat, maxLat, minLon, maxLon);
	}

	public Geo3DBoxQuery(final String field, final double minLat, final double maxLat, final double minLon,
			final double maxLon) {
		this(null, field, minLat, maxLat, minLon, maxLon);
	}

	@Override
	final public Query getQuery(final QueryContext queryContext) throws IOException {
		return Geo3DPoint.newBoxQuery(resolveField(queryContext.getFieldMap()), min_latitude, max_latitude,
				min_longitude, max_longitude);
	}
}
