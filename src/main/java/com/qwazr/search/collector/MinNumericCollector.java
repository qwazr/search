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
import org.apache.lucene.index.*;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

public abstract class MinNumericCollector<R> extends DocValuesCollector.Numeric<R> {

	public volatile R min;
	private Long current;

	public MinNumericCollector(final String collectorName, final String fieldName) {
		super(collectorName, fieldName);
		min = null;
	}

	public final R getResult() {
		return min;
	}

	@Override
	public boolean needsScores() {
		return false;
	}

	protected class MinNumericLeafCollector extends DocValuesLeafCollector.Numeric<ValueConverter<?, R>> {

		protected MinNumericLeafCollector(final ValueConverter<?, R> converter) throws IOException {
			super(converter);
		}

		@Override
		final public void collect(final int doc) throws IOException {
			long value = docValues.get(doc);
			if (current == null || current > value) {
				current = value;
				min = converter.convert(doc);
			}
		}
	}

	public static class MinLong extends MinNumericCollector<Long> {

		public MinLong(final String collectorName, final String fieldName) {
			super(collectorName, fieldName);
		}

		@Override
		protected LeafCollector newLeafCollector(final LeafReader leafReader, final NumericDocValues docValues)
				throws IOException {
			return docValues == null ? DoNothingCollector.INSTANCE :
					new MinNumericLeafCollector(
							new SingleDVConverter.LongDVConverter(docValues));
		}
	}

	public static class MinInteger extends MinNumericCollector<Integer> {

		public MinInteger(final String collectorName, final String fieldName) {
			super(collectorName, fieldName);
		}

		@Override
		protected LeafCollector newLeafCollector(final LeafReader leafReader, final NumericDocValues docValues)
				throws IOException {
			return docValues == null ? DoNothingCollector.INSTANCE :
					new MinNumericLeafCollector(
							new SingleDVConverter.IntegerDVConverter(docValues));
		}
	}

	public static class MinFloat extends MinNumericCollector<Float> {

		public MinFloat(final String collectorName, final String fieldName) {
			super(collectorName, fieldName);
		}

		@Override
		protected LeafCollector newLeafCollector(final LeafReader leafReader, final NumericDocValues docValues)
				throws IOException {
			return docValues == null ? DoNothingCollector.INSTANCE :
					new MinNumericLeafCollector(
							new SingleDVConverter.FloatDVConverter(docValues));
		}
	}

	public static class MinDouble extends MinNumericCollector<Double> {

		public MinDouble(final String collectorName, final String fieldName) {
			super(collectorName, fieldName);
		}

		@Override
		protected LeafCollector newLeafCollector(final LeafReader leafReader, final NumericDocValues docValues)
				throws IOException {
			return docValues == null ? DoNothingCollector.INSTANCE :
					new MinNumericLeafCollector(
							new SingleDVConverter.DoubleDVConverter(docValues));
		}
	}


}
