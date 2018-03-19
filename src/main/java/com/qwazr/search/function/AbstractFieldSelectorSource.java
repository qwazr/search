/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.function;

import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.search.SortedNumericSelector;

import java.util.Objects;

public abstract class AbstractFieldSelectorSource<T extends AbstractFieldSelectorSource>
		extends AbstractFieldSource<T> {

	public final SortedNumericSelector.Type selector;

	protected AbstractFieldSelectorSource(final Class<T> ownClass, final String field,
			final SortedNumericSelector.Type selector, final ValueSource valueSource) {
		super(ownClass, field, valueSource);
		this.selector = Objects.requireNonNull(selector, "The selector is missing");
	}

}
