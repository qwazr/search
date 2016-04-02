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
import jdk.nashorn.api.scripting.JSObject;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.FieldInfos;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.Collection;

abstract class FieldTypeAbstract implements FieldTypeInterface {

	final protected String fieldName;
	final protected FieldDefinition fieldDef;

	protected FieldTypeAbstract(final String fieldName, final FieldDefinition fieldDef) {
		this.fieldName = fieldName;
		this.fieldDef = fieldDef;
	}

	final protected void fillCollection(Collection<Object> values, FieldConsumer consumer) {
		for (Object value : values)
			if (value != null)
				fill(value, consumer);
	}

	final protected void fillJSObject(JSObject values, FieldConsumer consumer) {
		for (Object value : values.values())
			if (value != null)
				fill(value, consumer);
	}

	//TODO remove
	private static Number checkNumberType(String fieldName, Object value) {
		if (!(value instanceof Number))
			throw new IllegalArgumentException(
					"Wrong value type for the field: " + fieldName + " - " + value.getClass().getSimpleName());
		return (Number) value;
	}

	//TODO remove
	private static BytesRef checkStringBytesRef(Object value) {
		return new BytesRef(value.toString());
	}

	public ValueConverter getConverter(LeafReader leafReader) throws IOException {
		FieldInfos fieldInfos = leafReader.getFieldInfos();
		if (fieldInfos == null)
			return null;
		FieldInfo fieldInfo = fieldInfos.fieldInfo(fieldName);
		if (fieldInfo == null)
			return null;
		return ValueConverter.newConverter(fieldDef, leafReader, fieldInfo);
	}

}
