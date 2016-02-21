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
package com.qwazr.search.index;

import com.qwazr.search.field.FieldTypeInterface;
import com.qwazr.search.field.ValueConverter;
import org.apache.lucene.index.*;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

public class FunctionCollector implements Collector {

	private final FieldTypeInterface fieldType;

	final QueryDefinition.Function function;

	protected Object runningValue;
	protected Object finalValue;

	FunctionCollector(QueryDefinition.Function function, FieldTypeInterface fieldType) {
		this.function = function;
		this.fieldType = fieldType;
		this.finalValue = null;
		this.runningValue = null;
	}

	@Override
	public boolean needsScores() {
		return false;
	}

	public Object getValue() {
		return finalValue;
	}

	@Override
	public LeafCollector getLeafCollector(LeafReaderContext context) throws IOException {
		final LeafReader leafReader = context.reader();
		FieldInfo fieldInfo = leafReader.getFieldInfos().fieldInfo(function.field);
		ValueConverter converter = fieldType.getConverter(leafReader);
		if (converter == null)
			return DoNothingCollector.INSTANCE;
		switch (function.function) {
		case max:
			if (converter.isNumeric)
				return new MaxNumericFunctionCollector(converter);
			else
				return new MaxBinaryFunctionCollector(converter);
		case min:
			if (converter.isNumeric)
				return new MinNumericFunctionCollector(converter);
			else
				return new MinBinaryFunctionCollector(converter);
		default:
			throw new IOException("Unknown function for field " + function.field);
		}
	}

	static abstract class LeafFunctionCollector implements LeafCollector {

		protected final ValueConverter converter;

		protected LeafFunctionCollector(ValueConverter converter) throws IOException {
			this.converter = converter;
		}

		@Override
		final public void setScorer(Scorer scorer) throws IOException {
		}

	}

	private static abstract class LeafNumericFunctionCollector extends LeafFunctionCollector {

		protected final NumericDocValues docValues;

		protected LeafNumericFunctionCollector(ValueConverter converter) throws IOException {
			super(converter);
			docValues = (NumericDocValues) converter.source;
		}
	}

	private static abstract class LeafBinaryFunctionCollector extends LeafFunctionCollector {

		protected final BinaryDocValues docValues;

		protected LeafBinaryFunctionCollector(ValueConverter converter) throws IOException {
			super(converter);
			docValues = (BinaryDocValues) converter.source;
		}
	}

	private class MaxNumericFunctionCollector extends LeafNumericFunctionCollector {

		private Long max;

		private MaxNumericFunctionCollector(ValueConverter converter) throws IOException {
			super(converter);
			this.max = (Long) runningValue;
		}

		@Override
		final public void collect(int doc) throws IOException {
			long value = docValues.get(doc);
			if (max == null || value > max) {
				max = value;
				runningValue = value;
				finalValue = converter.convert(doc);
			}
		}
	}

	private class MinNumericFunctionCollector extends LeafNumericFunctionCollector {

		private Long min;

		private MinNumericFunctionCollector(ValueConverter converter) throws IOException {
			super(converter);
			min = (Long) runningValue;
		}

		@Override
		final public void collect(int doc) throws IOException {
			long value = docValues.get(doc);
			if (min == null || value < min) {
				min = value;
				runningValue = value;
				finalValue = converter.convert(doc);
			}
		}
	}

	private class MaxBinaryFunctionCollector extends LeafBinaryFunctionCollector {

		private BytesRef max;

		private MaxBinaryFunctionCollector(ValueConverter converter) throws IOException {
			super(converter);
			max = (BytesRef) runningValue;
		}

		@Override
		final public void collect(int doc) throws IOException {
			BytesRef value = docValues.get(doc);
			if (max == null || value.compareTo(max) > 0) {
				max = value;
				runningValue = value;
				finalValue = converter.convert(doc);
			}
		}
	}

	private class MinBinaryFunctionCollector extends LeafBinaryFunctionCollector {

		private BytesRef min;

		private MinBinaryFunctionCollector(ValueConverter converter) throws IOException {
			super(converter);
			min = (BytesRef) runningValue;
		}

		@Override
		final public void collect(int doc) throws IOException {
			BytesRef value = docValues.get(doc);
			if (min == null || value.compareTo(min) < 0) {
				min = value;
				runningValue = value;
				finalValue = converter.convert(doc);

			}
		}
	}

	private static class DoNothingCollector implements LeafCollector {

		private static final DoNothingCollector INSTANCE = new DoNothingCollector();

		@Override
		final public void setScorer(Scorer scorer) throws IOException {

		}

		@Override
		final public void collect(int doc) throws IOException {
		}
	}
}
