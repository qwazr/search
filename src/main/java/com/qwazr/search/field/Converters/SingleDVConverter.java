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
package com.qwazr.search.field.Converters;


import com.qwazr.utils.ReflectiveUtils;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;

import java.lang.reflect.Field;

public abstract class SingleDVConverter<T, V> extends ValueConverter<T, V> {

	private SingleDVConverter(final T source) {
		super(source);
	}

	@Override
	final public void fillCollection(final Object record, final Field field, final Class<?> fieldClass, final int docId)
			throws ReflectiveOperationException {
		final V value = convert(docId);
		if (value == null)
			return;
		ReflectiveUtils.<V>getCollection(record, field, fieldClass).add(value);
	}

	final public void fillSingleValue(final Object record, final Field field, final int docId)
			throws ReflectiveOperationException {
		final V value = convert(docId);
		if (value == null)
			return;
		field.set(record, value);
	}

	public static class DoubleDVConverter extends SingleDVConverter<NumericDocValues, Double> {

		public DoubleDVConverter(final NumericDocValues source) {
			super(source);
		}

		@Override
		final public Double convert(final int docId) {
			return NumericUtils.sortableLongToDouble(source.get(docId));
		}
	}

	public static class FloatDVConverter extends SingleDVConverter<NumericDocValues, Float> {

		public FloatDVConverter(final NumericDocValues source) {
			super(source);
		}

		@Override
		final public Float convert(final int docId) {
			return NumericUtils.sortableIntToFloat((int) source.get(docId));
		}
	}

	public static class LongDVConverter extends SingleDVConverter<NumericDocValues, Long> {

		public LongDVConverter(final NumericDocValues source) {
			super(source);
		}

		@Override
		final public Long convert(int docId) {
			return source.get(docId);
		}
	}

	public static class IntegerDVConverter extends SingleDVConverter<NumericDocValues, Integer> {

		public IntegerDVConverter(final NumericDocValues source) {
			super(source);
		}

		@Override
		final public Integer convert(final int docId) {
			return (int) source.get(docId);
		}
	}

	public static class BinaryDVConverter extends SingleDVConverter<BinaryDocValues, String> {

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
	}

	public static class SortedDVConverter extends SingleDVConverter<SortedDocValues, String> {

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
	}
}
