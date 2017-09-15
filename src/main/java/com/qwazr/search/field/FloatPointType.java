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
package com.qwazr.search.field;

import com.qwazr.search.index.BytesRefUtils;
import com.qwazr.search.index.FieldConsumer;
import com.qwazr.utils.WildcardMatcher;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.document.StoredField;

final class FloatPointType extends StorableFieldType {

	FloatPointType(final String genericFieldName, final WildcardMatcher wildcardMatcher,
			final FieldDefinition definition) {
		super(of(genericFieldName, wildcardMatcher, (CustomFieldDefinition) definition).bytesRefConverter(
				BytesRefUtils.Converter.FLOAT_POINT));
	}

	@Override
	void newFieldNoStore(final String fieldName, final Object value, final FieldConsumer consumer) {
		consumer.accept(genericFieldName, fieldName, new FloatPoint(fieldName, FieldUtils.getFloatValue(value)));
	}

	@Override
	void newFieldWithStore(final String fieldName, final Object value, final FieldConsumer consumer) {
		final float floatValue = FieldUtils.getFloatValue(value);
		consumer.accept(genericFieldName, fieldName, new FloatPoint(fieldName, floatValue));
		consumer.accept(genericFieldName, fieldName, new StoredField(fieldName, floatValue));
	}

}
