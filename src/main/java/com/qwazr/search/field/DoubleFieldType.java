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
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.search.SortField;

import java.util.Collection;

class DoubleFieldType extends StorableFieldType {

	DoubleFieldType(FieldDefinition fieldDefinition) {
		super(fieldDefinition);
	}

	@Override
	final public void fillDocument(final String fieldName, final Object value, Document doc) {
		if (value instanceof Collection)
			addCollection(fieldName, (Collection) value, doc);
		else if (value instanceof Number)
			doc.add(new DoubleField(fieldName, ((Number) value).doubleValue(), store));
		else
			doc.add(new DoubleField(fieldName, Double.parseDouble(value.toString()), store));
	}

	private final void addCollection(String fieldName, Collection<Object> values, Document doc) {
		for (Object value : values)
			fillDocument(fieldName, value, doc);
	}

	@Override
	public final SortField getSortField(String fieldName, QueryDefinition.SortEnum sortEnum) {
		final SortField sortField = new SortField(fieldName, SortField.Type.DOUBLE, FieldUtils.sortReverse(sortEnum));
		FieldUtils.sortDoubleMissingValue(sortEnum, sortField);
		return sortField;
	}

}
