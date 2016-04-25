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

import org.apache.lucene.index.*;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;

import java.io.IOException;

public abstract class ValueConverter<T, V> {

	final public boolean isNumeric;

	final public T source;

	private ValueConverter(final T source) {
		this.source = source;
		isNumeric = source instanceof NumericDocValues || source instanceof SortedNumericDocValues;
	}

	public abstract V convert(final int docId);

	static class BinaryDVConverter extends ValueConverter<BinaryDocValues, String> {

		BinaryDVConverter(final BinaryDocValues source) {
			super(source);
		}

		@Override
		final public String convert(final int docId) {
			final BytesRef bytesRef = source.get(docId);
			if (bytesRef == null)
				return null;
			return bytesRef.utf8ToString();
		}

	}

	static class SortedDVConverter extends ValueConverter<SortedDocValues, String> {

		SortedDVConverter(final SortedDocValues source) {
			super(source);
		}

		@Override
		final public String convert(final int docId) {
			BytesRef bytesRef = source.get(docId);
			if (bytesRef == null)
				return null;
			return bytesRef.utf8ToString();
		}
	}

	static class DoubleDVConverter extends ValueConverter<NumericDocValues, Double> {

		DoubleDVConverter(final NumericDocValues source) {
			super(source);
		}

		@Override
		final public Double convert(final int docId) {
			return NumericUtils.sortableLongToDouble(source.get(docId));
		}
	}

	static class FloatDVConverter extends ValueConverter<NumericDocValues, Float> {

		FloatDVConverter(final NumericDocValues source) {
			super(source);
		}

		@Override
		final public Float convert(final int docId) {
			return NumericUtils.sortableIntToFloat((int) source.get(docId));
		}
	}

	static class LongDVConverter extends ValueConverter<NumericDocValues, Long> {

		LongDVConverter(final NumericDocValues source) {
			super(source);
		}

		@Override
		final public Long convert(int docId) {
			return source.get(docId);
		}
	}

	static class IntegerDVConverter extends ValueConverter<NumericDocValues, Integer> {

		IntegerDVConverter(final NumericDocValues source) {
			super(source);
		}

		@Override
		final public Integer convert(final int docId) {
			return (int) source.get(docId);
		}
	}

	static class DoubleSetDVConverter extends ValueConverter<SortedNumericDocValues, double[]> {

		DoubleSetDVConverter(final SortedNumericDocValues source) {
			super(source);
		}

		@Override
		final public double[] convert(final int docId) {
			source.setDocument(docId);
			final double[] set = new double[source.count()];
			for (int i = 0; i < set.length; i++)
				set[i] = NumericUtils.sortableLongToDouble(source.valueAt(i));
			return set;
		}
	}

	static class FloatSetDVConverter extends ValueConverter<SortedNumericDocValues, float[]> {

		FloatSetDVConverter(final SortedNumericDocValues source) {
			super(source);
		}

		@Override
		final public float[] convert(final int docId) {
			source.setDocument(docId);
			final float[] set = new float[source.count()];
			for (int i = 0; i < set.length; i++)
				set[i] = NumericUtils.sortableIntToFloat((int) source.valueAt(i));
			return set;
		}
	}

	static class LongSetDVConverter extends ValueConverter<SortedNumericDocValues, long[]> {

		LongSetDVConverter(final SortedNumericDocValues source) {
			super(source);
		}

		@Override
		final public long[] convert(final int docId) {
			source.setDocument(docId);
			final long[] set = new long[source.count()];
			for (int i = 0; i < set.length; i++)
				set[i] = source.valueAt(i);
			return set;
		}
	}

	static class IntegerSetDVConverter extends ValueConverter<SortedNumericDocValues, int[]> {

		IntegerSetDVConverter(final SortedNumericDocValues source) {
			super(source);
		}

		@Override
		final public int[] convert(final int docId) {
			source.setDocument(docId);
			final int[] set = new int[source.count()];
			for (int i = 0; i < set.length; i++)
				set[i] = (int) source.valueAt(i);
			return set;
		}
	}

	private final static ValueConverter newNumericConverter(FieldDefinition fieldDef, NumericDocValues numericDocValues)
			throws IOException {
		if (fieldDef.numeric_type == null) {
			if (fieldDef.template == null)
				return null;
			switch (fieldDef.template) {
				case DoubleDocValuesField:
					return new DoubleDVConverter(numericDocValues);
				case FloatDocValuesField:
					return new FloatDVConverter(numericDocValues);
				case IntDocValuesField:
					return new IntegerDVConverter(numericDocValues);
				case LongDocValuesField:
					return new LongDVConverter(numericDocValues);
				default:
					return null;
			}
		}
		if (fieldDef.docvalues_type != DocValuesType.NUMERIC)
			return null;
		switch (fieldDef.numeric_type) {
			case DOUBLE:
				return new DoubleDVConverter(numericDocValues);
			case FLOAT:
				return new FloatDVConverter(numericDocValues);
			case LONG:
				return new LongDVConverter(numericDocValues);
			case INT:
				return new IntegerDVConverter(numericDocValues);
		}
		return null;
	}

	private final static ValueConverter newSortedNumericConverter(FieldDefinition fieldDef,
			SortedNumericDocValues sortedNumericDocValues) throws IOException {
		if (fieldDef.numeric_type == null) {
			if (fieldDef.template == null)
				return null;
			switch (fieldDef.template) {
				case DoubleDocValuesField:
					return new DoubleSetDVConverter(sortedNumericDocValues);
				case FloatDocValuesField:
					return new FloatSetDVConverter(sortedNumericDocValues);
				case IntDocValuesField:
					return new IntegerSetDVConverter(sortedNumericDocValues);
				case LongDocValuesField:
					return new LongSetDVConverter(sortedNumericDocValues);
				default:
					return null;
			}
		}
		if (fieldDef.docvalues_type != DocValuesType.SORTED_SET)
			return null;
		switch (fieldDef.numeric_type) {
			case DOUBLE:
				return new DoubleSetDVConverter(sortedNumericDocValues);
			case FLOAT:
				return new FloatSetDVConverter(sortedNumericDocValues);
			case LONG:
				return new LongSetDVConverter(sortedNumericDocValues);
			case INT:
				return new IntegerSetDVConverter(sortedNumericDocValues);
		}
		return null;
	}

	final static ValueConverter newConverter(String fieldName, final FieldDefinition fieldDef, final IndexReader reader)
			throws IOException {
		if (fieldDef == null)
			return null;
		final DocValuesType type = fieldDef.docvalues_type;
		if (type != null && type != DocValuesType.NONE)
			return newDocValueConverter(reader, fieldName, fieldDef, type);
		return null;
	}

	final static ValueConverter newDocValueConverter(final IndexReader reader, final String fieldName,
			final FieldDefinition fieldDef, final DocValuesType type) throws IOException {
		switch (type) {
			case BINARY:
				BinaryDocValues binaryDocValue = MultiDocValues.getBinaryValues(reader, fieldName);
				if (binaryDocValue == null)
					return null;
				return new BinaryDVConverter(binaryDocValue);
			case SORTED:
				SortedDocValues sortedDocValues = MultiDocValues.getSortedValues(reader, fieldName);
				if (sortedDocValues == null)
					return null;
				return new SortedDVConverter(sortedDocValues);
			case NONE:
				break;
			case NUMERIC:
				NumericDocValues numericDocValues = MultiDocValues.getNumericValues(reader, fieldName);
				if (numericDocValues == null)
					return null;
				return newNumericConverter(fieldDef, numericDocValues);
			case SORTED_NUMERIC:
				SortedNumericDocValues sortedNumericDocValues =
						MultiDocValues.getSortedNumericValues(reader, fieldName);
				if (sortedNumericDocValues == null)
					return null;
				return newSortedNumericConverter(fieldDef, sortedNumericDocValues);
			case SORTED_SET:
				SortedSetDocValues sortedSetDocValues = MultiDocValues.getSortedSetValues(reader, fieldName);
				if (sortedSetDocValues == null)
					return null;
				return null;
		}
		throw new IOException("Unsupported doc value type: " + type + " for field: " + fieldName);
	}
}
