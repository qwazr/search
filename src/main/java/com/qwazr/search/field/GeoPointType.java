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
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.search.SortField;
import org.apache.lucene.spatial.geopoint.document.GeoPointField;

class GeoPointType extends StorableFieldType {

	GeoPointType(final String fieldName, final FieldDefinition fieldDef) {
		super(fieldName, fieldDef);
	}

	protected void fillArray(final double[] values, final FieldConsumer consumer) {
		if ((values.length & 1) != 0)
			throw new RuntimeException("Expect even double values, but got: " + values.length);
		for (int i = 0; i < values.length; )
			consumer.accept(new GeoPointField(fieldName, values[i++], values[i++], store));
	}

	protected void fillArray(final float[] values, final FieldConsumer consumer) {
		if ((values.length & 1) != 0)
			throw new RuntimeException("Expect even float values, but got: " + values.length);
		for (int i = 0; i < values.length; )
			consumer.accept(new GeoPointField(fieldName, values[i++], values[i++], store));
	}

	protected void fillArray(final Object[] values, final FieldConsumer consumer) {
		if ((values.length & 1) != 0)
			throw new RuntimeException("Expect even number values, but got: " + values.length);
		for (int i = 0; i < values.length; )
			consumer.accept(
					new GeoPointField(fieldName, ((Number) values[i++]).doubleValue(),
							((Number) values[i++]).doubleValue(),
							store));
	}

	@Override
	public void fillValue(Object value, FieldConsumer fieldConsumer) {
		throw new RuntimeException("Unsupported value type for GeoPoint: " + value.getClass() +
				". An array of numbers is expected.");
	}

	@Override
	public SortField getSortField(QueryDefinition.SortEnum sortEnum) {
		return null;
	}
}
