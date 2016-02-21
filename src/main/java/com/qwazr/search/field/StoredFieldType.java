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
import org.apache.lucene.document.StoredField;
import org.apache.lucene.search.SortField;

import java.util.Collection;

class StoredFieldType extends FieldTypeAbstract {

	StoredFieldType(final String fieldName, final FieldDefinition fieldDef) {
		super(fieldName, fieldDef);
	}

	@Override
	final public void fill(final Object value, final FieldConsumer consumer) {
		if (value instanceof Collection)
			fillCollection((Collection) value, consumer);
		else if (value instanceof String)
			consumer.accept(new StoredField(fieldName, (String) value));
		else if (value instanceof Integer)
			consumer.accept(new StoredField(fieldName, (int) value));
		else if (value instanceof Long)
			consumer.accept(new StoredField(fieldName, (long) value));
		else if (value instanceof Float)
			consumer.accept(new StoredField(fieldName, (float) value));
		else
			consumer.accept(new StoredField(fieldName, value.toString()));
	}

	@Override
	final public SortField getSortField(final QueryDefinition.SortEnum sortEnum) {
		return null;
	}

}
