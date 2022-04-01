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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.index.QueryContext;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.search.SortedNumericSelector;

public class MultiValuedIntFieldSource extends AbstractFieldSelectorSource<MultiValuedIntFieldSource> {

	@JsonCreator
	public MultiValuedIntFieldSource(@JsonProperty("field") String field,
			@JsonProperty("selector") SortedNumericSelector.Type selector) {
		super(MultiValuedIntFieldSource.class, field, selector);
	}

	@Override
	public ValueSource getValueSource(QueryContext queryContext) {
		return new org.apache.lucene.queries.function.valuesource.MultiValuedIntFieldSource(field, selector);
	}

}
