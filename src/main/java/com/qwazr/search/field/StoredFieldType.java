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

import com.qwazr.search.index.BytesRefUtils;
import com.qwazr.search.index.FieldConsumer;
import com.qwazr.search.index.FieldMap;
import org.apache.lucene.document.StoredField;

import java.io.Externalizable;
import java.io.Serializable;

class StoredFieldType extends FieldTypeAbstract {

	StoredFieldType(final FieldMap.Item fieldMapItem) {
		super(fieldMapItem, BytesRefUtils.Converter.STRING);
	}

	@Override
	final public void fillValue(final String fieldName, final Object value, final FieldConsumer consumer) {
		if (value instanceof String)
			consumer.accept(fieldName, new StoredField(fieldName, (String) value));
		else if (value instanceof Integer)
			consumer.accept(fieldName, new StoredField(fieldName, (int) value));
		else if (value instanceof Long)
			consumer.accept(fieldName, new StoredField(fieldName, (long) value));
		else if (value instanceof Float)
			consumer.accept(fieldName, new StoredField(fieldName, (float) value));
		else if (value instanceof Externalizable)
			consumer.accept(fieldName, new StoredField(fieldName, toBytes(fieldName, (Externalizable) value)));
		else if (value instanceof Serializable)
			consumer.accept(fieldName, new StoredField(fieldName, toBytes(fieldName, (Serializable) value)));
		else
			consumer.accept(fieldName, new StoredField(fieldName, value.toString()));
	}

}
