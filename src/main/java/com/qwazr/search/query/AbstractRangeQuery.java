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

import java.util.Objects;

public abstract class AbstractRangeQuery<V, T extends AbstractRangeQuery> extends AbstractFieldQuery<T> {

	final public V lower_value;
	final public V upper_value;

	protected AbstractRangeQuery(final Class<T> queryClass, final String genericField, final String field,
			final V lowerValue, final V upperValue) {
		super(queryClass, genericField, field);
		this.lower_value = lowerValue;
		this.upper_value = upperValue;
	}

	@JsonIgnore
	@Override
	protected boolean isEqual(T q) {
		return super.isEqual(q) && Objects.equals(lower_value, q.lower_value) &&
				Objects.equals(upper_value, q.upper_value);
	}

}
