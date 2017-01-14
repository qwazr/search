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
import com.qwazr.search.index.QueryDefinition;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetField;
import org.apache.lucene.search.SortField;

class SortedSetDocValuesFacetType extends StorableFieldType {

	SortedSetDocValuesFacetType(final FieldMap.Item fieldMapItem) {
		super(fieldMapItem, BytesRefUtils.Converter.STRING);
	}

	@Override
	final public void fillValue(final String fieldName, final Object value, final FieldConsumer consumer) {
		String stringValue = value.toString();
		consumer.accept(fieldName, new SortedSetDocValuesFacetField(fieldName, stringValue));
		if (store == Field.Store.YES)
			consumer.accept(fieldName, new StoredField(fieldName, stringValue));
	}

	@Override
	final public SortField getSortField(final String fieldName, final QueryDefinition.SortEnum sortEnum) {
		return null;
	}

}
