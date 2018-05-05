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
package com.qwazr.search.field.Converters;

import com.qwazr.binder.setter.FieldSetter;
import org.apache.lucene.util.NumericUtils;

import java.io.IOException;
import java.util.List;

public abstract class MultiDVConverter<T, A> extends ValueConverter<A> {

	private MultiDVConverter(final MultiReader reader, final String field) {
		super(reader, field);
	}

	final public static class DoubleSetDVConverter extends MultiDVConverter<Double, double[]> {

		public DoubleSetDVConverter(final MultiReader reader, final String field) {
			super(reader, field);
		}

		final static double[] EMPTY = new double[0];

		@Override
		final public double[] convert(final int docId) throws IOException {
			final long[] sources = reader.getSortedNumericDocValues(docId, field);
			if (sources.length == 0)
				return EMPTY;
			final double[] set = new double[sources.length];
			for (int i = 0; i < set.length; i++)
				set[i] = NumericUtils.sortableLongToDouble(sources[i]);
			return set;
		}

		@Override
		final public void fill(final Object record, final FieldSetter fieldSetter, final int docId) throws IOException {
			final double[] values = convert(docId);
			if (values.length == 0) {
				fieldSetter.fromNull(record);
			} else if (values.length == 1) {
				fieldSetter.fromDouble(values[0], record);
			} else
				fieldSetter.fromDouble(values, record);
		}

	}

	final public static class FloatSetDVConverter extends MultiDVConverter<Float, float[]> {

		public FloatSetDVConverter(final MultiReader reader, final String field) {
			super(reader, field);
		}

		@Override
		final public float[] convert(final int docId) throws IOException {
			final long[] sources = reader.getSortedNumericDocValues(docId, field);
			final float[] set = new float[sources.length];
			for (int i = 0; i < set.length; i++)
				set[i] = NumericUtils.sortableIntToFloat((int) sources[i]);
			return set;
		}

		@Override
		final public void fill(final Object record, final FieldSetter fieldSetter, final int docId) throws IOException {
			final float[] values = convert(docId);
			if (values.length == 0) {
				fieldSetter.fromNull(record);
			} else if (values.length == 1) {
				fieldSetter.fromFloat(values[0], record);
			} else
				fieldSetter.fromFloat(values, record);
		}

	}

	final public static class LongSetDVConverter extends MultiDVConverter<Long, long[]> {

		public LongSetDVConverter(final MultiReader reader, final String field) {
			super(reader, field);
		}

		@Override
		final public long[] convert(final int docId) throws IOException {
			return reader.getSortedNumericDocValues(docId, field);
		}

		@Override
		final public void fill(final Object record, final FieldSetter fieldSetter, final int docId) throws IOException {
			final long[] values = convert(docId);
			if (values.length == 0) {
				fieldSetter.fromNull(record);
			} else if (values.length == 1) {
				fieldSetter.fromLong(values[0], record);
			} else
				fieldSetter.fromLong(values, record);
		}

	}

	final public static class IntegerSetDVConverter extends MultiDVConverter<Integer, int[]> {

		public IntegerSetDVConverter(final MultiReader reader, final String field) {
			super(reader, field);
		}

		@Override
		final public int[] convert(final int docId) throws IOException {
			final long[] sources = reader.getSortedNumericDocValues(docId, field);
			final int[] set = new int[sources.length];
			for (int i = 0; i < set.length; i++)
				set[i] = (int) sources[i];
			return set;
		}

		@Override
		final public void fill(final Object record, final FieldSetter fieldSetter, final int docId) throws IOException {
			final int[] values = convert(docId);
			if (values.length == 0) {
				fieldSetter.fromNull(record);
			} else if (values.length == 1) {
				fieldSetter.fromInteger(values[0], record);
			} else
				fieldSetter.fromInteger(values, record);
		}

	}

	final public static class SortedSetDVConverter extends MultiDVConverter<String, List<String>> {

		public SortedSetDVConverter(final MultiReader reader, final String field) {
			super(reader, field);
		}

		@Override
		final public List<String> convert(final int docId) throws IOException {
			return reader.getSortedSetDocValues(docId, field);
		}

		@Override
		final public void fill(final Object record, final FieldSetter fieldSetter, final int docId) throws IOException {
			final List<String> values = convert(docId);
			if (values.size() == 0) {
				fieldSetter.fromNull(record);
			} else if (values.size() == 1) {
				fieldSetter.fromString(values.get(0), record);
			} else
				fieldSetter.fromString(values, record);
		}

	}

}
