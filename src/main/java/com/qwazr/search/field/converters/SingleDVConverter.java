/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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
package com.qwazr.search.field.converters;

import com.qwazr.binder.setter.FieldSetter;
import org.apache.lucene.util.NumericUtils;

import java.io.IOException;

public abstract class SingleDVConverter<T> extends ValueConverter<T> {

	private SingleDVConverter(final MultiReader reader, final String field) {
		super(reader, field);
	}

	final public static class DoubleDVConverter extends SingleDVConverter<Double> {

		public DoubleDVConverter(final MultiReader reader, final String field) {
			super(reader, field);
		}

		@Override
		final public Double convert(final int docId) throws IOException {
			return NumericUtils.sortableLongToDouble(reader.getNumericDocValues(docId, field));
		}

		@Override
		public void fill(Object record, FieldSetter fieldSetter, int docId) throws IOException {
			fieldSetter.fromDouble(convert(docId), record);
		}
	}

	final public static class FloatDVConverter extends SingleDVConverter<Float> {

		public FloatDVConverter(final MultiReader reader, final String field) {
			super(reader, field);
		}

		@Override
		final public Float convert(final int docId) throws IOException {
			return NumericUtils.sortableIntToFloat((int) reader.getNumericDocValues(docId, field));
		}

		@Override
		public void fill(Object record, FieldSetter fieldSetter, int docId) throws IOException {
			fieldSetter.fromFloat(convert(docId), record);
		}
	}

	final public static class LongDVConverter extends SingleDVConverter<Long> {

		public LongDVConverter(final MultiReader reader, final String field) {
			super(reader, field);
		}

		@Override
		final public Long convert(int docId) throws IOException {
			return reader.getNumericDocValues(docId, field);
		}

		@Override
		public void fill(Object record, FieldSetter fieldSetter, int docId) throws IOException {
			fieldSetter.fromLong(convert(docId), record);
		}
	}

	final public static class IntegerDVConverter extends SingleDVConverter<Integer> {

		public IntegerDVConverter(final MultiReader reader, final String field) {
			super(reader, field);
		}

		@Override
		final public Integer convert(final int docId) throws IOException {
			return (int) reader.getNumericDocValues(docId, field);
		}

		@Override
		public void fill(Object record, FieldSetter fieldSetter, int docId) throws IOException {
			fieldSetter.fromInteger(convert(docId), record);
		}
	}

	final public static class BinaryDVConverter extends SingleDVConverter<String> {

		public BinaryDVConverter(final MultiReader reader, final String field) {
			super(reader, field);
		}

		@Override
		final public String convert(final int docId) throws IOException {
			return reader.getBinaryDocValues(docId, field).utf8ToString();
		}

		@Override
		public void fill(final Object record, final FieldSetter fieldSetter, final int docId) throws IOException {
			fieldSetter.fromString(convert(docId), record);
		}
	}

	final public static class SortedDVConverter extends SingleDVConverter<String> {

		public SortedDVConverter(final MultiReader reader, final String field) {
			super(reader, field);
		}

		@Override
		final public String convert(final int docId) throws IOException {
			return reader.getSortedDocValues(docId, field).utf8ToString();
		}

		@Override
		public void fill(final Object record, final FieldSetter fieldSetter, final int docId) throws IOException {
			fieldSetter.fromString(convert(docId), record);
		}
	}
}
