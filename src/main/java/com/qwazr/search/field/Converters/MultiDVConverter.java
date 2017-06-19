/*
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
import org.apache.lucene.index.SortedNumericDocValues;
import org.apache.lucene.util.NumericUtils;

import java.lang.reflect.Field;
import java.util.Collection;

public abstract class MultiDVConverter<T extends SortedNumericDocValues, V, A> extends ValueConverter<T, A> {

	private MultiDVConverter(final T source) {
		super(source);
	}

	protected abstract V valueAt(int pos);

	@Override
	final public void fillCollection(final Object record, final Field field, final Class<?> fieldClass, final int docId)
			throws ReflectiveOperationException {
		source.setDocument(docId);
		final int count = source.count();
		if (count == 0)
			return;
		Collection<V> collection = ReflectiveUtils.getCollection(record, field, fieldClass);
		for (int i = 0; i < count; i++)
			collection.add(valueAt(i));
	}

	final public void fillSingleValue(final Object record, final Field field, final int docId)
			throws ReflectiveOperationException {
		source.setDocument(docId);
		final int count = source.count();
		if (count == 0)
			return;
		if (count > 1)
			throw new RuntimeException(
					"Cannot fill several values on this field. It should be a collection: " +
							field.getName());
		field.set(record, valueAt(0));
	}


	public static class DoubleSetDVConverter extends MultiDVConverter<SortedNumericDocValues, Double, double[]> {

		public DoubleSetDVConverter(final SortedNumericDocValues source) {
			super(source);
		}

		@Override
		final protected Double valueAt(int pos) {
			return NumericUtils.sortableLongToDouble(source.valueAt(pos));
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

	public static class FloatSetDVConverter extends MultiDVConverter<SortedNumericDocValues, Float, float[]> {

		public FloatSetDVConverter(final SortedNumericDocValues source) {
			super(source);
		}

		@Override
		final protected Float valueAt(int pos) {
			return NumericUtils.sortableIntToFloat((int) source.valueAt(pos));
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

	public static class LongSetDVConverter extends MultiDVConverter<SortedNumericDocValues, Long, long[]> {

		public LongSetDVConverter(final SortedNumericDocValues source) {
			super(source);
		}

		@Override
		final protected Long valueAt(int pos) {
			return source.valueAt(pos);
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

	public static class IntegerSetDVConverter extends MultiDVConverter<SortedNumericDocValues, Integer, int[]> {

		public IntegerSetDVConverter(final SortedNumericDocValues source) {
			super(source);
		}

		@Override
		final protected Integer valueAt(int pos) {
			return (int) source.valueAt(pos);
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
	
}
