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
import com.qwazr.search.index.FieldMap;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.util.BytesRef;

class IntPointType extends StorableFieldType {

	IntPointType(final FieldMap.Item fieldMapItem) {
		super(fieldMapItem);
	}

	@Override
	final public void fillValue(final String fieldName, final Object value, final FieldConsumer consumer) {
		int intValue = value instanceof Number ? ((Number) value).intValue() : Integer.parseInt(value.toString());
		consumer.accept(fieldName, new IntPoint(fieldName, intValue));
		if (store == Field.Store.YES)
			consumer.accept(fieldName, new StoredField(fieldName, intValue));
	}

	@Override
	public final Object toTerm(final BytesRef bytesRef) {
		if (bytesRef == null || bytesRef.bytes == null)
			return null;
		return IntPoint.decodeDimension(bytesRef.bytes, 0);
	}
}
