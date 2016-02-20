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
package com.qwazr.search.field;

import com.qwazr.search.index.QueryDefinition;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortedNumericSortField;
import org.apache.lucene.util.NumericUtils;

import java.util.Collection;

class SortedFloatDocValuesType implements FieldTypeInterface {

	SortedFloatDocValuesType() {
		super();
	}

	@Override
	final public void fillDocument(final String fieldName, final Object value, Document doc) {
		if (value instanceof Collection)
			addCollection(fieldName, (Collection) value, doc);
		else if (value instanceof Number)
			doc.add(new SortedNumericDocValuesField(fieldName,
					NumericUtils.floatToSortableInt(((Number) value).floatValue())));
		else
			doc.add(new SortedNumericDocValuesField(fieldName,
					NumericUtils.floatToSortableInt(Float.parseFloat(value.toString()))));
	}

	private final void addCollection(String fieldName, Collection<Object> values, Document doc) {
		for (Object value : values)
			fillDocument(fieldName, value, doc);
	}

	@Override
	public final SortField getSortField(String fieldName, QueryDefinition.SortEnum sortEnum) {
		final SortField sortField = new SortedNumericSortField(fieldName, SortField.Type.FLOAT,
				FieldUtils.sortReverse(sortEnum));
		FieldUtils.sortFloatMissingValue(sortEnum, sortField);
		return sortField;
	}

}
