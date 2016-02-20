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
package com.qwazr.search.index;

import com.qwazr.search.field.FieldDefinition;
import org.apache.lucene.index.*;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;

import java.io.IOException;

class ValueUtils {

	static abstract class DVConverter<T, V> {

		final boolean isNumeric;

		protected final T source;

		private DVConverter(T source) {
			this.source = source;
			isNumeric = source instanceof NumericDocValues;
		}

		abstract V convert(int docId);
	}

	static class BinaryDVConverter extends DVConverter<BinaryDocValues, String> {

		private BinaryDVConverter(BinaryDocValues source) {
			super(source);
		}

		@Override
		final protected String convert(int docId) {
			BytesRef bytesRef = source.get(docId);
			if (bytesRef == null)
				return null;
			return bytesRef.utf8ToString();
		}

	}

	static class SortedDVConverter extends DVConverter<SortedDocValues, String> {

		private SortedDVConverter(SortedDocValues source) {
			super(source);
		}

		@Override
		final protected String convert(int docId) {
			BytesRef bytesRef = source.get(docId);
			if (bytesRef == null)
				return null;
			return bytesRef.utf8ToString();
		}
	}

	private static class DoubleDVConverter extends DVConverter<NumericDocValues, Double> {

		private DoubleDVConverter(NumericDocValues source) {
			super(source);
		}

		@Override
		protected Double convert(int docId) {
			return NumericUtils.sortableLongToDouble(source.get(docId));
		}
	}

	private static class FloatDVConverter extends DVConverter<NumericDocValues, Float> {

		private FloatDVConverter(NumericDocValues source) {
			super(source);
		}

		@Override
		protected Float convert(int docId) {
			return NumericUtils.sortableIntToFloat((int) source.get(docId));
		}
	}

	private static class LongDVConverter extends DVConverter<NumericDocValues, Long> {

		private LongDVConverter(NumericDocValues source) {
			super(source);
		}

		@Override
		protected Long convert(int docId) {
			return source.get(docId);
		}
	}

	private static class IntegerDVConverter extends DVConverter<NumericDocValues, Integer> {

		private IntegerDVConverter(NumericDocValues source) {
			super(source);
		}

		@Override
		protected Integer convert(int docId) {
			return (int) source.get(docId);
		}
	}

	private static class DoubleSetDVConverter extends DVConverter<SortedNumericDocValues, double[]> {

		private DoubleSetDVConverter(SortedNumericDocValues source) {
			super(source);
		}

		@Override
		protected double[] convert(int docId) {
			source.setDocument(docId);
			double[] set = new double[source.count()];
			for (int i = 0; i < set.length; i++)
				set[i] = NumericUtils.sortableLongToDouble(source.valueAt(i));
			return set;
		}
	}

	private static class FloatSetDVConverter extends DVConverter<SortedNumericDocValues, float[]> {

		private FloatSetDVConverter(SortedNumericDocValues source) {
			super(source);
		}

		@Override
		protected float[] convert(int docId) {
			source.setDocument(docId);
			float[] set = new float[source.count()];
			for (int i = 0; i < set.length; i++)
				set[i] = NumericUtils.sortableIntToFloat((int) source.valueAt(i));
			return set;
		}
	}

	private static class LongSetDVConverter extends DVConverter<SortedNumericDocValues, long[]> {

		private LongSetDVConverter(SortedNumericDocValues source) {
			super(source);
		}

		@Override
		protected long[] convert(int docId) {
			source.setDocument(docId);
			long[] set = new long[source.count()];
			for (int i = 0; i < set.length; i++)
				set[i] = source.valueAt(i);
			return set;
		}
	}

	private static class IntegerSetDVConverter extends DVConverter<SortedNumericDocValues, int[]> {

		private IntegerSetDVConverter(SortedNumericDocValues source) {
			super(source);
		}

		@Override
		protected int[] convert(int docId) {
			source.setDocument(docId);
			int[] set = new int[source.count()];
			for (int i = 0; i < set.length; i++)
				set[i] = (int) source.valueAt(i);
			return set;
		}
	}

	static DVConverter newNumericConverter(FieldDefinition fieldDef, NumericDocValues numericDocValues)
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

	static DVConverter newSortedNumericConverter(FieldDefinition fieldDef,
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

	static DVConverter newConverter(FieldDefinition fieldDef, LeafReader dvReader, FieldInfo fieldInfo)
			throws IOException {
		if (fieldInfo == null)
			return null;
		DocValuesType type = fieldInfo.getDocValuesType();
		if (type == null)
			return null;
		switch (type) {
		case BINARY:
			BinaryDocValues binaryDocValue = dvReader.getBinaryDocValues(fieldInfo.name);
			if (binaryDocValue == null)
				return null;
			return new BinaryDVConverter(binaryDocValue);
		case SORTED:
			SortedDocValues sortedDocValues = dvReader.getSortedDocValues(fieldInfo.name);
			if (sortedDocValues == null)
				return null;
			return new SortedDVConverter(sortedDocValues);
		case NONE:
			break;
		case NUMERIC:
			NumericDocValues numericDocValues = dvReader.getNumericDocValues(fieldInfo.name);
			if (numericDocValues == null)
				return null;
			return newNumericConverter(fieldDef, numericDocValues);
		case SORTED_NUMERIC:
			SortedNumericDocValues sortedNumericDocValues = dvReader.getSortedNumericDocValues(fieldInfo.name);
			if (sortedNumericDocValues == null)
				return null;
			return newSortedNumericConverter(fieldDef, sortedNumericDocValues);
		case SORTED_SET:
			SortedSetDocValues sortedSetDocValues = dvReader.getSortedSetDocValues(fieldInfo.name);
			if (sortedSetDocValues == null)
				return null;
			return null;
		default:
			throw new IOException("Unsupported doc value type: " + type + " for field: " + fieldInfo.name);
		}
		return null;
	}

}
