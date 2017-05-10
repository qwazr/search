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

public abstract class AbstractRangeQuery<T> extends AbstractFieldQuery {

	final public T lower_value;
	final public T upper_value;

	protected AbstractRangeQuery(final String field, final T lowerValue, final T upperValue) {
		super(field);
		this.lower_value = lowerValue;
		this.upper_value = upperValue;
	}

}
