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
import org.apache.lucene.index.SortedNumericDocValues;
import org.apache.lucene.util.NumericUtils;

public abstract class MultiDVConverter<T extends SortedNumericDocValues, V, A> extends ValueConverter<T, A> {

	private MultiDVConverter(final T source) {
		super(source);
	}

	protected abstract void fillFirst(FieldSetter fieldSetter, Object record);

	protected abstract void fillAll(int count, FieldSetter fieldSetter, Object record);

	@Override
	final public void fill(final Object record, final FieldSetter fieldSetter, final int docId) {
		source.setDocument(docId);
		final int count = source.count();
		if (count == 0) {
			fieldSetter.fromNull(record);
			return;
		}
		if (count == 1) {
			fillFirst(fieldSetter, record);
			return;
		}
		fillAll(count, fieldSetter, record);
	}

	final public static class DoubleSetDVConverter extends MultiDVConverter<SortedNumericDocValues, Double, double[]> {

		public DoubleSetDVConverter(final SortedNumericDocValues source) {
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

		@Override
		protected void fillFirst(final FieldSetter fieldSetter, final Object record) {
			fieldSetter.fromDouble(NumericUtils.sortableLongToDouble(source.valueAt(0)), record);
		}

		@Override
		protected void fillAll(int count, FieldSetter fieldSetter, Object record) {
			final double[] set = new double[source.count()];
			for (int i = 0; i < set.length; i++)
				set[i] = NumericUtils.sortableLongToDouble(source.valueAt(i));
			fieldSetter.fromDouble(set, record);
		}

	}

	final public static class FloatSetDVConverter extends MultiDVConverter<SortedNumericDocValues, Float, float[]> {

		public FloatSetDVConverter(final SortedNumericDocValues source) {
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

		@Override
		protected void fillFirst(final FieldSetter fieldSetter, final Object record) {
			fieldSetter.fromFloat(NumericUtils.sortableIntToFloat((int) source.valueAt(0)), record);
		}

		@Override
		protected void fillAll(final int count, final FieldSetter fieldSetter, final Object record) {
			final float[] set = new float[count];
			for (int i = 0; i < set.length; i++)
				set[i] = NumericUtils.sortableIntToFloat((int) source.valueAt(i));
			fieldSetter.fromFloat(set, record);
		}
	}

	final public static class LongSetDVConverter extends MultiDVConverter<SortedNumericDocValues, Long, long[]> {

		public LongSetDVConverter(final SortedNumericDocValues source) {
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

		@Override
		protected void fillFirst(final FieldSetter fieldSetter, final Object record) {
			fieldSetter.fromLong(source.valueAt(0), record);
		}

		@Override
		protected void fillAll(final int count, final FieldSetter fieldSetter, final Object record) {
			final long[] set = new long[count];
			for (int i = 0; i < set.length; i++)
				set[i] = source.valueAt(i);
			fieldSetter.fromLong(set, record);
		}
	}

	final public static class IntegerSetDVConverter extends MultiDVConverter<SortedNumericDocValues, Integer, int[]> {

		public IntegerSetDVConverter(final SortedNumericDocValues source) {
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

		@Override
		protected void fillFirst(final FieldSetter fieldSetter, final Object record) {
			fieldSetter.fromInteger((int) source.valueAt(0), record);
		}

		@Override
		protected void fillAll(final int count, final FieldSetter fieldSetter, final Object record) {
			final int[] set = new int[count];
			for (int i = 0; i < set.length; i++)
				set[i] = (int) source.valueAt(i);
			fieldSetter.fromInteger(set, record);
		}
	}

}
