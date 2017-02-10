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
import com.qwazr.utils.WildcardMatcher;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.facet.FacetField;

import java.util.Arrays;

class FacetType extends StorableFieldType {

	FacetType(final WildcardMatcher wildcardMatcher, final FieldDefinition definition) {
		super(wildcardMatcher, definition, BytesRefUtils.Converter.STRING);
	}

	@Override
	final protected void fillArray(final String fieldName, final String[] values, final Float boost,
			final FieldConsumer consumer) {
		consumer.accept(fieldName, new FacetField(fieldName, values), boost);
		if (store != null && store == Field.Store.YES)
			consumer.accept(fieldName, new StoredField(fieldName, Arrays.toString(values)), boost);
	}

	@Override
	final public void fillValue(final String fieldName, final Object value, final Float boost,
			final FieldConsumer consumer) {
		if (value == null)
			return;
		final String stringValue = value.toString();
		if (stringValue == null || stringValue.isEmpty())
			return;
		consumer.accept(fieldName, new FacetField(fieldName, stringValue), boost);
		if (store != null && store == Field.Store.YES)
			consumer.accept(fieldName, new StoredField(fieldName, stringValue), boost);
	}
}
