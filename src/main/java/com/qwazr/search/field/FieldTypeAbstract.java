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
import org.apache.lucene.index.IndexReader;

import java.io.IOException;
import java.util.Collection;

abstract class FieldTypeAbstract implements FieldTypeInterface {

	final protected String fieldName;
	final protected FieldDefinition fieldDef;

	protected FieldTypeAbstract(final String fieldName, final FieldDefinition fieldDef) {
		this.fieldName = fieldName;
		this.fieldDef = fieldDef;
	}

	protected void fillArray(final int[] values, final FieldConsumer consumer) {
		for (int value : values)
			fill(value, consumer);
	}

	protected void fillArray(final long[] values, final FieldConsumer consumer) {
		for (long value : values)
			fill(value, consumer);
	}

	protected void fillArray(final double[] values, final FieldConsumer consumer) {
		for (double value : values)
			fill(value, consumer);
	}

	protected void fillArray(final float[] values, final FieldConsumer consumer) {
		for (float value : values)
			fill(value, consumer);
	}

	protected void fillArray(final Object[] values, final FieldConsumer consumer) {
		for (Object value : values)
			fill(value, consumer);
	}

	protected void fillArray(final String[] values, final FieldConsumer consumer) {
		for (String value : values)
			fill(value, consumer);
	}

	protected void fillCollection(final Collection<Object> values, final FieldConsumer consumer) {
		values.forEach(value -> {
			if (value != null)
				fill(value, consumer);
		});
	}

	protected void fillJSObject(final JSObject values, final FieldConsumer consumer) {
		fillCollection(values.values(), consumer);
	}

	final public void fill(final Object value, final FieldConsumer fieldConsumer) {
		if (value == null)
			return;
		if (value instanceof String[])
			fillArray((String[]) value, fieldConsumer);
		else if (value instanceof int[])
			fillArray((int[]) value, fieldConsumer);
		else if (value instanceof long[])
			fillArray((long[]) value, fieldConsumer);
		else if (value instanceof double[])
			fillArray((double[]) value, fieldConsumer);
		else if (value instanceof float[])
			fillArray((float[]) value, fieldConsumer);
		else if (value instanceof Object[])
			fillArray((Object[]) value, fieldConsumer);
		else if (value instanceof Collection)
			fillCollection((Collection) value, fieldConsumer);
		else if (value instanceof JSObject)
			fillJSObject((JSObject) value, fieldConsumer);
		else
			fillValue(value, fieldConsumer);
	}

	public ValueConverter getConverter(final IndexReader reader) throws IOException {
		return ValueConverter.newConverter(fieldName, fieldDef, reader);
	}

}
