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
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.NumericUtils;

import java.io.IOException;

public class ValueUtils {

	static abstract class DVConverter<T, V extends Comparable<V>> {

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
		case SORTED_NUMERIC:
			break;
		case SORTED_SET:
			break;
		default:
			throw new IOException("Unsupported doc value type: " + type + " for field: " + fieldInfo.name);
		}
		return null;
	}

	final public static BytesRef getNewBytesRef(String text) {
		return text == null ? null : new BytesRef(text);
	}

	final public static BytesRef getNewBytesRef(long value) {
		final BytesRefBuilder bytes = new BytesRefBuilder();
		NumericUtils.longToPrefixCoded(value, 0, bytes);
		return bytes.get();
	}

	final public static BytesRef getNewBytesRef(int value) {
		final BytesRefBuilder bytes = new BytesRefBuilder();
		NumericUtils.intToPrefixCoded(value, 0, bytes);
		return bytes.get();
	}

	final public static BytesRef getNewBytesRef(double value) {
		final BytesRefBuilder bytes = new BytesRefBuilder();
		NumericUtils.longToPrefixCoded(NumericUtils.doubleToSortableLong(value), 0, bytes);
		return bytes.get();
	}

	final public static BytesRef getNewBytesRef(float value) {
		final BytesRefBuilder bytes = new BytesRefBuilder();
		NumericUtils.intToPrefixCoded(NumericUtils.floatToSortableInt(value), 0, bytes);
		return bytes.get();
	}

}
