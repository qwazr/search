/**
 * Copyright 2015 Emmanuel Keller / QWAZR
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.index;

import org.apache.lucene.index.*;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;

import java.io.IOException;

class DocValueUtils {

	static abstract class DVConverter<T, V> {

		protected final T source;

		private DVConverter(T source) {
			this.source = source;
		}

		protected abstract V convert(int docId);
	}

	static class BinaryDVConverter extends DVConverter<BinaryDocValues, String> {

		private BinaryDVConverter(BinaryDocValues source) {
			super(source);
		}

		@Override
		protected String convert(int docId) {
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
		DocValuesType type = fieldInfo.getDocValuesType();
		if (type == null)
			return null;
		switch (type) {
		case BINARY:
			return new BinaryDVConverter(dvReader.getBinaryDocValues(fieldInfo.name));
		case NONE:
			break;
		case NUMERIC:
			NumericDocValues numericDocValues = dvReader.getNumericDocValues(fieldInfo.name);
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
		case SORTED:
			break;
		case SORTED_NUMERIC:
			break;
		case SORTED_SET:
			break;
		default:
			throw new IOException("Unsupported doc value type: " + type + " for field: " + fieldInfo.name);
		}
		return null;
	}
}
