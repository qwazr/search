/**
 * Copyright 2015 Emmanuel Keller / QWAZR
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

import org.apache.lucene.index.*;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

public class FunctionCollector implements Collector {

	private final FieldDefinition fieldDef;

	final QueryDefinition.Function function;

	protected Comparable runningValue;
	protected Comparable finalValue;

	FunctionCollector(QueryDefinition.Function function, FieldDefinition fieldDef) {
		this.function = function;
		this.fieldDef = fieldDef;
		this.finalValue = null;
		this.runningValue = null;
	}

	@Override
	public boolean needsScores() {
		return false;
	}

	public Comparable getValue() {
		return finalValue;
	}

	@Override
	public LeafCollector getLeafCollector(LeafReaderContext context) throws IOException {
		final LeafReader leafReader = context.reader();
		FieldInfo fieldInfo = leafReader.getFieldInfos().fieldInfo(function.field);
		DocValueUtils.DVConverter dvConverter = DocValueUtils.newConverter(fieldDef, leafReader, fieldInfo);
		if (dvConverter == null)
			throw new IOException("Function " + function.function + " not available for the field: " + function.field);
		switch (function.function) {
		case max:
			if (dvConverter.isNumeric)
				return new MaxNumericFunctionCollector(dvConverter);
			else
				return new MaxBinaryFunctionCollector(dvConverter);
		case min:
			if (dvConverter.isNumeric)
				return new MinNumericFunctionCollector(dvConverter);
			else
				return new MinBinaryFunctionCollector(dvConverter);
		default:
			throw new IOException("Unknown function for field " + function.field);
		}
	}

	static abstract class LeafFunctionCollector implements LeafCollector {

		protected final DocValueUtils.DVConverter dvConverter;

		protected LeafFunctionCollector(DocValueUtils.DVConverter dvConverter) throws IOException {
			this.dvConverter = dvConverter;
		}

		@Override
		final public void setScorer(Scorer scorer) throws IOException {
		}

	}

	private static abstract class LeafNumericFunctionCollector extends LeafFunctionCollector {

		protected final NumericDocValues docValues;

		protected LeafNumericFunctionCollector(DocValueUtils.DVConverter dvConverter) throws IOException {
			super(dvConverter);
			docValues = (NumericDocValues) dvConverter.source;
		}
	}

	private static abstract class LeafBinaryFunctionCollector extends LeafFunctionCollector {

		protected final BinaryDocValues docValues;

		protected LeafBinaryFunctionCollector(DocValueUtils.DVConverter dvConverter) throws IOException {
			super(dvConverter);
			docValues = (BinaryDocValues) dvConverter.source;
		}
	}

	private class MaxNumericFunctionCollector extends LeafNumericFunctionCollector {

		private Long max;

		private MaxNumericFunctionCollector(DocValueUtils.DVConverter dvConverter) throws IOException {
			super(dvConverter);
			this.max = (Long) runningValue;
		}

		@Override
		final public void collect(int doc) throws IOException {
			long value = docValues.get(doc);
			if (max == null || value > max) {
				max = value;
				runningValue = value;
				finalValue = dvConverter.convert(doc);
			}
		}
	}

	private class MinNumericFunctionCollector extends LeafNumericFunctionCollector {

		private Long min;

		private MinNumericFunctionCollector(DocValueUtils.DVConverter dvConverter) throws IOException {
			super(dvConverter);
			min = (Long) runningValue;
		}

		@Override
		final public void collect(int doc) throws IOException {
			long value = docValues.get(doc);
			if (min == null || value < min) {
				min = value;
				runningValue = value;
				finalValue = dvConverter.convert(doc);
			}
		}
	}

	private class MaxBinaryFunctionCollector extends LeafBinaryFunctionCollector {

		private BytesRef max;

		private MaxBinaryFunctionCollector(DocValueUtils.DVConverter dvConverter) throws IOException {
			super(dvConverter);
			max = (BytesRef) runningValue;
		}

		@Override
		final public void collect(int doc) throws IOException {
			BytesRef value = docValues.get(doc);
			if (max == null || value.compareTo(max) > 0) {
				max = value;
				runningValue = value;
				finalValue = dvConverter.convert(doc);
			}
		}
	}

	private class MinBinaryFunctionCollector extends LeafBinaryFunctionCollector {

		private BytesRef min;

		private MinBinaryFunctionCollector(DocValueUtils.DVConverter dvConverter) throws IOException {
			super(dvConverter);
			min = (BytesRef) runningValue;
		}

		@Override
		final public void collect(int doc) throws IOException {
			BytesRef value = docValues.get(doc);
			if (min == null || value.compareTo(min) < 0) {
				min = value;
				runningValue = value;
				finalValue = dvConverter.convert(doc);

			}
		}
	}
}
