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

import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.BytesRef;

import java.util.Collection;

abstract class FieldTypeAbstract implements FieldTypeInterface {

	final protected void fillCollection(String fieldName, Collection<Object> values, FieldConsumer consumer) {
		for (Object value : values)
			if (value != null)
				fill(fieldName, value, consumer);
	}

	private static Number checkNumberType(String fieldName, Object value) {
		if (!(value instanceof Number))
			throw new IllegalArgumentException(
					"Wrong value type for the field: " + fieldName + " - " + value.getClass().getSimpleName());
		return (Number) value;
	}

	private static BytesRef checkStringBytesRef(Object value) {
		return new BytesRef(value.toString());
	}

	public final static Object getValue(IndexableField field) {
		if (field == null)
			return null;
		String s = field.stringValue();
		if (s != null)
			return s;
		Number n = field.numericValue();
		if (n != null)
			return n;
		return null;
	}

}
