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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractMultiRangeQuery<T> extends AbstractQuery {

	final public String field;

	public AbstractMultiRangeQuery() {
		field = null;

	}

	protected AbstractMultiRangeQuery(final String field) {
		this.field = field;
	}

	public abstract static class AbstractBuilder<T> {

		private final String field;
		private final List<T> lowerValues;
		private final List<T> upperValues;

		protected AbstractBuilder(String field) {
			this.field = field;
			lowerValues = new ArrayList<>();
			upperValues = new ArrayList<>();
		}

		public AbstractBuilder<T> addRange(T lowerValue, T upperValue) {
			lowerValues.add(lowerValue);
			upperValues.add(upperValue);
			return this;
		}

		protected abstract AbstractMultiRangeQuery<T> build(String field, Collection<T> lowerValues, Collection<T> upperValues);

		public AbstractMultiRangeQuery<T> build() {
			return build(field, lowerValues, upperValues);
		}

	}
}
