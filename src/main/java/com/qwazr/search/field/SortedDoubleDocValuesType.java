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

import com.qwazr.search.field.Converters.MultiDVConverter;
import com.qwazr.search.field.Converters.ValueConverter;
import com.qwazr.search.index.BytesRefUtils;
import com.qwazr.search.index.FieldConsumer;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.utils.WildcardMatcher;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiDocValues;
import org.apache.lucene.index.SortedNumericDocValues;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortedNumericSortField;
import org.apache.lucene.util.NumericUtils;

import java.io.IOException;

class SortedDoubleDocValuesType extends FieldTypeAbstract {

	SortedDoubleDocValuesType(final WildcardMatcher wildcardMatcher, final FieldDefinition definition) {
		super(of(wildcardMatcher, (CustomFieldDefinition) definition).bytesRefConverter(
				BytesRefUtils.Converter.DOUBLE));
	}

	@Override
	final public void fillValue(final String fieldName, final Object value, final Float boost,
			final FieldConsumer consumer) {
		if (value instanceof Number)
			consumer.accept(fieldName, new SortedNumericDocValuesField(fieldName,
					NumericUtils.doubleToSortableLong(((Number) value).doubleValue())), boost);
		else
			consumer.accept(fieldName, new SortedNumericDocValuesField(fieldName,
					NumericUtils.doubleToSortableLong(Double.parseDouble(value.toString()))), boost);

	}

	@Override
	final public SortField getSortField(final String fieldName, final QueryDefinition.SortEnum sortEnum) {
		final SortField sortField = new SortedNumericSortField(fieldName, SortField.Type.DOUBLE,
				SortUtils.sortReverse(sortEnum));
		SortUtils.sortDoubleMissingValue(sortEnum, sortField);
		return sortField;
	}

	@Override
	final public ValueConverter getConverter(final String fieldName, final IndexReader reader) throws IOException {
		final SortedNumericDocValues docValues = MultiDocValues.getSortedNumericValues(reader, fieldName);
		if (docValues == null)
			return super.getConverter(fieldName, reader);
		return new MultiDVConverter.DoubleSetDVConverter(docValues);
	}

}
