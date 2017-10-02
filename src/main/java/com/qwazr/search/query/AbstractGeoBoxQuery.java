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

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AbstractGeoBoxQuery<T extends AbstractGeoBoxQuery> extends AbstractFieldQuery<T> {

	final public double min_latitude;

	final public double max_latitude;

	final public double min_longitude;

	final public double max_longitude;

	public AbstractGeoBoxQuery(Class<T> queryClass, final String genericField, final String field,
			final double minLatitude, final double maxLatitude, final double minLongitude, final double maxLongitude) {
		super(queryClass, genericField, field);
		this.min_latitude = minLatitude;
		this.max_latitude = maxLatitude;
		this.min_longitude = minLongitude;
		this.max_longitude = maxLongitude;
	}

	@Override
	@JsonIgnore
	protected boolean isEqual(T q) {
		return super.isEqual(q) && min_latitude == q.min_latitude && max_latitude == q.max_latitude &&
				min_longitude == q.min_longitude && max_longitude == q.max_longitude;
	}

}
