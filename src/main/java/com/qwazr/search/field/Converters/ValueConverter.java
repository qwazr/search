/**
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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
package com.qwazr.search.field.Converters;

import com.qwazr.binder.setter.FieldSetter;
import com.qwazr.search.field.CustomFieldDefinition;
import com.qwazr.search.field.FieldDefinition;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiDocValues;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.index.SortedNumericDocValues;
import org.apache.lucene.index.SortedSetDocValues;

import java.io.IOException;

public abstract class ValueConverter<T, V> {

	final public boolean isNumeric;

	final public T source;

	protected ValueConverter(final T source) {
		this.source = source;
		isNumeric = source instanceof NumericDocValues || source instanceof SortedNumericDocValues;
	}

	public abstract V convert(final int docId);

	public abstract void fill(final Object record, final FieldSetter fieldSetter, final int docId);

	private final static ValueConverter newNumericConverter(CustomFieldDefinition fieldDef,
			NumericDocValues numericDocValues) throws IOException {
		if (fieldDef.numeric_type == null) {
			if (fieldDef.template == null)
				return null;
			switch (fieldDef.template) {
			case DoubleDocValuesField:
				return new SingleDVConverter.DoubleDVConverter(numericDocValues);
			case FloatDocValuesField:
				return new SingleDVConverter.FloatDVConverter(numericDocValues);
			case IntDocValuesField:
				return new SingleDVConverter.IntegerDVConverter(numericDocValues);
			case LongDocValuesField:
				return new SingleDVConverter.LongDVConverter(numericDocValues);
			default:
				return null;
			}
		}
		if (fieldDef.docvalues_type != DocValuesType.NUMERIC)
			return null;
		switch (fieldDef.numeric_type) {
		case DOUBLE:
			return new SingleDVConverter.DoubleDVConverter(numericDocValues);
		case FLOAT:
			return new SingleDVConverter.FloatDVConverter(numericDocValues);
		case LONG:
			return new SingleDVConverter.LongDVConverter(numericDocValues);
		case INT:
			return new SingleDVConverter.IntegerDVConverter(numericDocValues);
		}
		return null;
	}

	private final static ValueConverter newSortedNumericConverter(CustomFieldDefinition fieldDef,
			SortedNumericDocValues sortedNumericDocValues) throws IOException {
		if (fieldDef.numeric_type == null) {
			if (fieldDef.template == null)
				return null;
			switch (fieldDef.template) {
			case DoubleDocValuesField:
				return new MultiDVConverter.DoubleSetDVConverter(sortedNumericDocValues);
			case FloatDocValuesField:
				return new MultiDVConverter.FloatSetDVConverter(sortedNumericDocValues);
			case IntDocValuesField:
				return new MultiDVConverter.IntegerSetDVConverter(sortedNumericDocValues);
			case LongDocValuesField:
				return new MultiDVConverter.LongSetDVConverter(sortedNumericDocValues);
			default:
				return null;
			}
		}
		if (fieldDef.docvalues_type != DocValuesType.SORTED_SET)
			return null;
		switch (fieldDef.numeric_type) {
		case DOUBLE:
			return new MultiDVConverter.DoubleSetDVConverter(sortedNumericDocValues);
		case FLOAT:
			return new MultiDVConverter.FloatSetDVConverter(sortedNumericDocValues);
		case LONG:
			return new MultiDVConverter.LongSetDVConverter(sortedNumericDocValues);
		case INT:
			return new MultiDVConverter.IntegerSetDVConverter(sortedNumericDocValues);
		}
		return null;
	}

	public static ValueConverter newConverter(String fieldName, final FieldDefinition fieldDefinition,
			final IndexReader reader) throws IOException {
		if (fieldDefinition == null || !(fieldDefinition instanceof CustomFieldDefinition))
			return null;
		final CustomFieldDefinition fieldDef = (CustomFieldDefinition) fieldDefinition;
		final DocValuesType type = fieldDef.docvalues_type;
		if (type != null && type != DocValuesType.NONE)
			return newDocValueConverter(reader, fieldName, fieldDef, type);
		return null;
	}

	static ValueConverter newDocValueConverter(final IndexReader reader, final String fieldName,
			final CustomFieldDefinition fieldDef, final DocValuesType type) throws IOException {
		switch (type) {
		case BINARY:
			final BinaryDocValues binaryDocValue = MultiDocValues.getBinaryValues(reader, fieldName);
			if (binaryDocValue == null)
				return null;
			return new SingleDVConverter.BinaryDVConverter(binaryDocValue);
		case SORTED:
			SortedDocValues sortedDocValues = MultiDocValues.getSortedValues(reader, fieldName);
			if (sortedDocValues == null)
				return null;
			return new SingleDVConverter.SortedDVConverter(sortedDocValues);
		case NONE:
			break;
		case NUMERIC:
			final NumericDocValues numericDocValues = MultiDocValues.getNumericValues(reader, fieldName);
			if (numericDocValues == null)
				return null;
			return newNumericConverter(fieldDef, numericDocValues);
		case SORTED_NUMERIC:
			final SortedNumericDocValues sortedNumericDocValues = MultiDocValues.getSortedNumericValues(reader,
					fieldName);
			if (sortedNumericDocValues == null)
				return null;
			return newSortedNumericConverter(fieldDef, sortedNumericDocValues);
		case SORTED_SET:
			final SortedSetDocValues sortedSetDocValues = MultiDocValues.getSortedSetValues(reader, fieldName);
			if (sortedSetDocValues == null)
				return null;
			return null;
		}
		throw new IOException("Unsupported doc value type: " + type + " for field: " + fieldName);
	}

}
