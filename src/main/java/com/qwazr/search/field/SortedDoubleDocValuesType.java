/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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

import com.qwazr.search.field.converters.MultiDVConverter;
import com.qwazr.search.field.converters.MultiReader;
import com.qwazr.search.field.converters.ValueConverter;
import com.qwazr.search.index.BytesRefUtils;
import com.qwazr.search.index.FieldConsumer;
import com.qwazr.utils.WildcardMatcher;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.util.NumericUtils;

final class SortedDoubleDocValuesType extends CustomFieldTypeAbstract.OneField {

	SortedDoubleDocValuesType(final String genericFieldName, final WildcardMatcher wildcardMatcher,
			final FieldDefinition definition) {
		super(of(genericFieldName, wildcardMatcher, (CustomFieldDefinition) definition).bytesRefConverter(
				BytesRefUtils.Converter.DOUBLE).sortFieldProvider(SortUtils::doubleSortField));
	}

	@Override
	final void newField(final String fieldName, final Object value, final FieldConsumer consumer) {
		final Field field;
		if (value instanceof Number)
			field = new SortedNumericDocValuesField(fieldName,
					NumericUtils.doubleToSortableLong(((Number) value).doubleValue()));
		else
			field = new SortedNumericDocValuesField(fieldName,
					NumericUtils.doubleToSortableLong(Double.parseDouble(value.toString())));
		consumer.accept(genericFieldName, fieldName, field);
	}

	@Override
	final public ValueConverter getConverter(final String fieldName, final MultiReader reader) {
		return new MultiDVConverter.DoubleSetDVConverter(reader, fieldName);
	}

}
