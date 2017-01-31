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
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.facet.FacetField;

import java.util.Arrays;

class FacetType extends StorableFieldType {

	FacetType(final FieldMap.Item fieldMapItem) {
		super(fieldMapItem, BytesRefUtils.Converter.STRING);
	}

	@Override
	final protected void fillArray(final String fieldName, final String[] values, final FieldConsumer consumer) {
		consumer.accept(fieldName, new FacetField(fieldName, values));
		if (store != null && store == Field.Store.YES)
			consumer.accept(fieldName, new StoredField(fieldName, Arrays.toString(values)));
	}

	@Override
	final public void fillValue(final String fieldName, final Object value, final FieldConsumer consumer) {
		if (value == null)
			return;
		String stringValue = value.toString();
		if (stringValue == null)
			return;
		consumer.accept(fieldName, new FacetField(fieldName, stringValue));
		if (store != null && store == Field.Store.YES)
			consumer.accept(fieldName, new StoredField(fieldName, stringValue));
	}
}
