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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qwazr.search.field.Converters.SingleDVConverter;
import org.apache.lucene.index.*;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

public abstract class MinCollector<T> implements Collector {

	public final String fieldName;

	public volatile T min;

	public MinCollector(final String fieldName) {
		this.fieldName = fieldName;
		min = null;
	}

	@Override
	@JsonIgnore
	public boolean needsScores() {
		return false;
	}

	@Override
	@JsonIgnore
	public LeafCollector getLeafCollector(final LeafReaderContext context) throws IOException {
		final LeafReader leafReader = context.reader();
		FieldInfo fieldInfo = leafReader.getFieldInfos().fieldInfo(fieldName);
		if (fieldInfo == null)
			throw new IOException("Field not found " + fieldName);

		final DocValuesType type = fieldInfo.getDocValuesType();
		if (type == null)
			throw new IOException("Wrong type (DocValues expected): " + fieldName);

		return newLeafCollector(leafReader, fieldName);
	}

	protected abstract LeafCollector newLeafCollector(final LeafReader leafReader, final String fieldName)
			throws IOException;

	public class MinBinaryCollector extends MinCollector<String> {

		private BytesRef current;

		public MinBinaryCollector(final String fieldName) {
			super(fieldName);
			current = null;
		}

		protected synchronized void checkMin(final BytesRef value, final SingleDVConverter.BinaryDVConverter converter,
				final int doc) {
			if (current == null || value.compareTo(current) > 0) {
				current = value;
				min = converter.convert(doc);
			}
		}

		final protected LeafCollector newLeafCollector(final LeafReader leafReader, final String fieldName)
				throws IOException {
			final BinaryDocValues source = leafReader.getBinaryDocValues(fieldName);
			return source == null ?
					DoNothingCollector.INSTANCE :
					new MinBinaryLeafCollector(new SingleDVConverter.BinaryDVConverter(source));
		}

		public class MinBinaryLeafCollector extends DocValuesLeafCollector.Binary {

			public MinBinaryLeafCollector(final SingleDVConverter.BinaryDVConverter converter) throws IOException {
				super(converter);
			}

			@Override
			final public void collect(int doc) throws IOException {
				checkMin(docValues.get(doc), converter, doc);
			}
		}
	}

	public class MinLongCollector extends MinCollector<Long> {

		private Long current;

		public MinLongCollector(final String fieldName) {
			super(fieldName);
		}

		protected synchronized void checkMin(final long value, final SingleDVConverter.LongDVConverter converter,
				final int doc) {
			if (current == null || current > value) {
				current = value;
				min = converter.convert(doc);
			}
		}

		final protected LeafCollector newLeafCollector(final LeafReader leafReader, final String fieldName)
				throws IOException {
			final NumericDocValues source = leafReader.getNumericDocValues(fieldName);
			return source == null ?
					DoNothingCollector.INSTANCE :
					new MinNumericLeafCollector(new SingleDVConverter.LongDVConverter(source));
		}

		public class MinNumericLeafCollector extends DocValuesLeafCollector.Numeric<SingleDVConverter.LongDVConverter> {

			private MinNumericLeafCollector(final SingleDVConverter.LongDVConverter converter) throws IOException {
				super(converter);
			}

			@Override
			final public void collect(final int doc) throws IOException {
				checkMin(docValues.get(doc), converter, doc);
			}
		}
	}
}
