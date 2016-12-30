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

import org.apache.lucene.index.*;
import org.apache.lucene.search.LeafCollector;

import java.io.IOException;

public abstract class DocValuesCollector<R, D> extends BaseCollector<R> implements ConcurrentCollector<R> {

	protected final String fieldName;
	protected int count;

	private DocValuesCollector(final String collectorName, final String fieldName) {
		super(collectorName);
		this.fieldName = fieldName;
		this.count = 0;
	}

	protected abstract LeafCollector newLeafCollector(final LeafReader leafReader, final D docValues)
			throws IOException;

	protected abstract D getDocValues(final LeafReader leafReader) throws IOException;

	@Override
	final public LeafCollector getLeafCollector(final LeafReaderContext context) throws IOException {
		final LeafReader leafReader = context.reader();
		final FieldInfo fieldInfo = leafReader.getFieldInfos().fieldInfo(fieldName);
		if (fieldInfo == null)
			return DoNothingCollector.INSTANCE;
		final DocValuesType type = fieldInfo.getDocValuesType();
		if (type == null)
			return DoNothingCollector.INSTANCE;
		final D docValues = getDocValues(leafReader);
		if (docValues == null)
			return DoNothingCollector.INSTANCE;
		return newLeafCollector(leafReader, docValues);
	}

	public static abstract class Binary<R> extends DocValuesCollector<R, BinaryDocValues> {

		protected Binary(String collectorName, String fieldName) {
			super(collectorName, fieldName);
		}

		final protected BinaryDocValues getDocValues(final LeafReader leafReader) throws IOException {
			return leafReader.getBinaryDocValues(fieldName);
		}
	}

	public static abstract class Sorted<R> extends DocValuesCollector<R, SortedDocValues> {

		protected Sorted(String collectorName, String fieldName) {
			super(collectorName, fieldName);
		}

		final protected SortedDocValues getDocValues(final LeafReader leafReader) throws IOException {
			return leafReader.getSortedDocValues(fieldName);
		}
	}

	public static abstract class SortedSet<R> extends DocValuesCollector<R, SortedSetDocValues> {

		protected SortedSet(String collectorName, String fieldName) {
			super(collectorName, fieldName);
		}

		final protected SortedSetDocValues getDocValues(final LeafReader leafReader) throws IOException {
			return leafReader.getSortedSetDocValues(fieldName);
		}
	}

	public static abstract class Numeric<R> extends DocValuesCollector<R, NumericDocValues> {

		protected Numeric(final String collectorName, final String fieldName) {
			super(collectorName, fieldName);
		}

		final protected NumericDocValues getDocValues(final LeafReader leafReader) throws IOException {
			return leafReader.getNumericDocValues(fieldName);
		}
	}

	public static abstract class SortedNumeric<R> extends DocValuesCollector<R, SortedNumericDocValues> {

		protected SortedNumeric(final String collectorName, final String fieldName) {
			super(collectorName, fieldName);
		}

		final protected SortedNumericDocValues getDocValues(final LeafReader leafReader) throws IOException {
			return leafReader.getSortedNumericDocValues(fieldName);
		}
	}
}
