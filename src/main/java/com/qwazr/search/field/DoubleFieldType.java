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

import com.qwazr.search.index.FieldConsumer;
import com.qwazr.search.index.QueryDefinition;
import jdk.nashorn.api.scripting.JSObject;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.search.SortField;

import java.util.Collection;

class DoubleFieldType extends StorableFieldType {

	DoubleFieldType(final String fieldName, final FieldDefinition fieldDef) {
		super(fieldName, fieldDef);
	}

	@Override
	final public void fill(final Object value, final FieldConsumer consumer) {
		if (value instanceof Collection)
			fillCollection((Collection) value, consumer);
		else if (value instanceof JSObject)
			fillJSObject((JSObject) value, consumer);
		else if (value instanceof Number)
			consumer.accept(new DoubleField(fieldName, ((Number) value).doubleValue(), store));
		else
			consumer.accept(new DoubleField(fieldName, Double.parseDouble(value.toString()), store));
	}

	@Override
	public final SortField getSortField(final QueryDefinition.SortEnum sortEnum) {
		final SortField sortField = new SortField(fieldName, SortField.Type.DOUBLE, SortUtils.sortReverse(sortEnum));
		SortUtils.sortDoubleMissingValue(sortEnum, sortField);
		return sortField;
	}

}
