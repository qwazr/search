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
package com.qwazr.search.collector;

import com.qwazr.search.field.Converters.SingleDVConverter;
import com.qwazr.search.field.Converters.ValueConverter;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.search.LeafCollector;

import java.io.IOException;

public abstract class MaxNumericCollector<R> extends DocValuesCollector.Numeric<R> {

	public volatile R max;
	private Long current;
	private ValueConverter<?, R> converter;

	public MaxNumericCollector(final String collectorName, final String fieldName) {
		super(collectorName, fieldName);
		max = null;
	}

	public final R getResult() {
		return max;
	}

	@Override
	public boolean needsScores() {
		return false;
	}

	protected class MaxNumericLeafCollector extends DocValuesLeafCollector.Numeric<ValueConverter<?, R>> {

		protected MaxNumericLeafCollector(final ValueConverter<?, R> converter) throws IOException {
			super(converter);
		}

		@Override
		final public void collect(final int doc) throws IOException {
			final long value = docValues.get(doc);
			if (current == null || current < value) {
				current = value;
				max = converter.convert(doc);
			}
		}
	}

	public static class MaxLong extends MaxNumericCollector<Long> {

		public MaxLong(final String collectorName, final String fieldName) {
			super(collectorName, fieldName);
		}

		@Override
		protected LeafCollector newLeafCollector(final LeafReader leafReader, final NumericDocValues docValues)
				throws IOException {
			return docValues == null ? DoNothingCollector.INSTANCE :
					new MaxNumericLeafCollector(
							new SingleDVConverter.LongDVConverter(docValues));
		}
	}

	public static class MaxInteger extends MaxNumericCollector<Integer> {

		public MaxInteger(final String collectorName, final String fieldName) {
			super(collectorName, fieldName);
		}

		@Override
		protected LeafCollector newLeafCollector(final LeafReader leafReader, final NumericDocValues docValues)
				throws IOException {
			return docValues == null ? DoNothingCollector.INSTANCE :
					new MaxNumericLeafCollector(
							new SingleDVConverter.IntegerDVConverter(docValues));
		}
	}

	public static class MaxFloat extends MaxNumericCollector<Float> {

		public MaxFloat(final String collectorName, final String fieldName) {
			super(collectorName, fieldName);
		}

		@Override
		protected LeafCollector newLeafCollector(final LeafReader leafReader, final NumericDocValues docValues)
				throws IOException {
			return docValues == null ? DoNothingCollector.INSTANCE :
					new MaxNumericLeafCollector(
							new SingleDVConverter.FloatDVConverter(docValues));
		}
	}

	public static class MaxDouble extends MaxNumericCollector<Double> {

		public MaxDouble(final String collectorName, final String fieldName) {
			super(collectorName, fieldName);
		}

		@Override
		protected LeafCollector newLeafCollector(final LeafReader leafReader, final NumericDocValues docValues)
				throws IOException {
			return docValues == null ? DoNothingCollector.INSTANCE :
					new MaxNumericLeafCollector(
							new SingleDVConverter.DoubleDVConverter(docValues));
		}
	}


}
