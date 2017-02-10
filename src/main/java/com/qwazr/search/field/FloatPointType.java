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
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;

class FloatPointType extends StorableFieldType {

	FloatPointType(final WildcardMatcher wildcardMatcher, final FieldDefinition definition) {
		super(wildcardMatcher, definition, BytesRefUtils.Converter.FLOAT_POINT);
	}

	@Override
	final public void fillValue(final String fieldName, final Object value, final Float boost,
			final FieldConsumer consumer) {
		final float floatValue =
				value instanceof Number ? ((Number) value).floatValue() : Float.parseFloat(value.toString());
		consumer.accept(fieldName, new DoublePoint(fieldName, floatValue), boost);
		if (store == Field.Store.YES)
			consumer.accept(fieldName, new StoredField(fieldName, floatValue), boost);
	}

}
