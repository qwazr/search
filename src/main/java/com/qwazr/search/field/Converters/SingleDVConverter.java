/*
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
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;

public abstract class SingleDVConverter<T, V> extends ValueConverter<T, V> {

	private SingleDVConverter(final T source) {
		super(source);
	}

	final public static class DoubleDVConverter extends SingleDVConverter<NumericDocValues, Double> {

		public DoubleDVConverter(final NumericDocValues source) {
			super(source);
		}

		@Override
		final public Double convert(final int docId) {
			return NumericUtils.sortableLongToDouble(source.get(docId));
		}

		@Override
		public void fill(Object record, FieldSetter fieldSetter, int docId) {
			fieldSetter.fromDouble(NumericUtils.sortableLongToDouble(source.get(docId)), record);
		}
	}

	final public static class FloatDVConverter extends SingleDVConverter<NumericDocValues, Float> {

		public FloatDVConverter(final NumericDocValues source) {
			super(source);
		}

		@Override
		final public Float convert(final int docId) {
			return NumericUtils.sortableIntToFloat((int) source.get(docId));
		}

		@Override
		public void fill(Object record, FieldSetter fieldSetter, int docId) {
			fieldSetter.fromFloat(NumericUtils.sortableIntToFloat((int) source.get(docId)), record);
		}
	}

	final public static class LongDVConverter extends SingleDVConverter<NumericDocValues, Long> {

		public LongDVConverter(final NumericDocValues source) {
			super(source);
		}

		@Override
		final public Long convert(int docId) {
			return source.get(docId);
		}

		@Override
		public void fill(Object record, FieldSetter fieldSetter, int docId) {
			fieldSetter.fromLong(source.get(docId), record);
		}
	}

	final public static class IntegerDVConverter extends SingleDVConverter<NumericDocValues, Integer> {

		public IntegerDVConverter(final NumericDocValues source) {
			super(source);
		}

		@Override
		final public Integer convert(final int docId) {
			return (int) source.get(docId);
		}

		@Override
		public void fill(Object record, FieldSetter fieldSetter, int docId) {
			fieldSetter.fromInteger((int) source.get(docId), record);
		}
	}

	final public static class BinaryDVConverter extends SingleDVConverter<BinaryDocValues, String> {

		public BinaryDVConverter(final BinaryDocValues source) {
			super(source);
		}

		@Override
		final public String convert(final int docId) {
			final BytesRef bytesRef = source.get(docId);
			if (bytesRef == null)
				return null;
			return bytesRef.utf8ToString();
		}

		@Override
		public void fill(final Object record, final FieldSetter fieldSetter, final int docId) {
			fieldSetter.fromString(convert(docId), record);
		}
	}

	final public static class SortedDVConverter extends SingleDVConverter<SortedDocValues, String> {

		public SortedDVConverter(final SortedDocValues source) {
			super(source);
		}

		@Override
		final public String convert(final int docId) {
			BytesRef bytesRef = source.get(docId);
			if (bytesRef == null)
				return null;
			return bytesRef.utf8ToString();
		}

		@Override
		public void fill(final Object record, final FieldSetter fieldSetter, final int docId) {
			fieldSetter.fromString(convert(docId), record);
		}
	}
}
