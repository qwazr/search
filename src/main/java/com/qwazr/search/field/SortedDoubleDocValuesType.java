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
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortedNumericSortField;
import org.apache.lucene.util.NumericUtils;

import java.util.Collection;

class SortedDoubleDocValuesType extends FieldTypeAbstract {

	SortedDoubleDocValuesType() {
		super();
	}

	@Override
	final public void fill(final String fieldName, final Object value, FieldConsumer consumer) {
		if (value instanceof Collection)
			fillCollection(fieldName, (Collection) value, consumer);
		else if (value instanceof Number)
			consumer.accept(new SortedNumericDocValuesField(fieldName,
					NumericUtils.doubleToSortableLong(((Number) value).doubleValue())));
		else
			consumer.accept(new SortedNumericDocValuesField(fieldName,
					NumericUtils.doubleToSortableLong(Double.parseDouble(value.toString()))));

	}

	@Override
	public final SortField getSortField(String fieldName, QueryDefinition.SortEnum sortEnum) {
		final SortField sortField = new SortedNumericSortField(fieldName, SortField.Type.DOUBLE,
				FieldUtils.sortReverse(sortEnum));
		FieldUtils.sortDoubleMissingValue(sortEnum, sortField);
		return sortField;
	}

}
