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
package com.qwazr.search.field;

import com.qwazr.search.index.BytesRefUtils;
import com.qwazr.search.index.FieldConsumer;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.utils.WildcardMatcher;
import org.apache.lucene.document.StringField;
import org.apache.lucene.search.SortField;

class StringFieldType extends StorableFieldType {

	StringFieldType(final WildcardMatcher wildcardMatcher, final FieldDefinition definition) {
		super(wildcardMatcher, definition, BytesRefUtils.Converter.STRING);
	}

	@Override
	final public void fillValue(final String fieldName, final Object value, final Float boost,
			final FieldConsumer consumer) {
		consumer.accept(fieldName, new StringField(fieldName, value.toString(), store), boost);
	}

	@Override
	public final SortField getSortField(final String fieldName, final QueryDefinition.SortEnum sortEnum) {
		final SortField sortField = new SortField(fieldName, SortField.Type.STRING, SortUtils.sortReverse(sortEnum));
		SortUtils.sortStringMissingValue(sortEnum, sortField);
		return sortField;
	}

}
