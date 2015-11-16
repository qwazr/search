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
	private final Collector parentCollector;

	private final QueryDefinition.Function function;

	FunctionCollector(Collector parentCollector, QueryDefinition.Function function, FieldDefinition fieldDef) {
		this.parentCollector = parentCollector;
		this.function = function;
		this.fieldDef = fieldDef;
	}

	@Override
	public boolean needsScores() {
		return false;
	}

	@Override
	public LeafCollector getLeafCollector(LeafReaderContext context) throws IOException {
		final LeafCollector parentLeafCollector =
						parentCollector != null ? parentCollector.getLeafCollector(context) : null;

		final LeafReader leafReader = context.reader();
		FieldInfo fieldInfo = leafReader.getFieldInfos().fieldInfo(function.field);
		DocValueUtils.DVConverter dvConverter = DocValueUtils.newConverter(fieldDef, leafReader, fieldInfo);
		if (dvConverter == null)
			throw new IOException("Function " + function.function + " not available for the field: " + function.field);
		switch (function.function) {
		case max:
			if (dvConverter.isNumeric)
				return new MaxNumericFunctionCollector(parentLeafCollector, dvConverter);
			else
				return new MaxBinaryFunctionCollector(parentLeafCollector, dvConverter);
		case min:
			if (dvConverter.isNumeric)
				return new MinNumericFunctionCollector(parentLeafCollector, dvConverter);
			else
				return new MinBinaryFunctionCollector(parentLeafCollector, dvConverter);
		}
		return parentLeafCollector;
	}

	static abstract class LeafFunctionCollector implements LeafCollector {

		protected final LeafCollector parentLeafCollector;
		protected final DocValueUtils.DVConverter dvConverter;

		protected LeafFunctionCollector(LeafCollector parentLeafCollector, DocValueUtils.DVConverter dvConverter)
						throws IOException {
			this.parentLeafCollector = parentLeafCollector;
			this.dvConverter = dvConverter;
		}

		@Override
		public void setScorer(Scorer scorer) throws IOException {
			if (parentLeafCollector != null)
				parentLeafCollector.setScorer(scorer);
		}
	}

	private static abstract class LeafNumericFunctionCollector extends LeafFunctionCollector {

		protected final NumericDocValues docValues;

		protected LeafNumericFunctionCollector(LeafCollector parentLeafCollector, DocValueUtils.DVConverter dvConverter)
						throws IOException {
			super(parentLeafCollector, dvConverter);
			docValues = (NumericDocValues) dvConverter.source;
		}
	}

	private static abstract class LeafBinaryFunctionCollector extends LeafFunctionCollector {

		protected final BinaryDocValues docValues;

		protected LeafBinaryFunctionCollector(LeafCollector parentLeafCollector, DocValueUtils.DVConverter dvConverter)
						throws IOException {
			super(parentLeafCollector, dvConverter);
			docValues = (BinaryDocValues) dvConverter.source;
		}
	}

	private static class MaxNumericFunctionCollector extends LeafNumericFunctionCollector {

		private Long max;

		private MaxNumericFunctionCollector(LeafCollector parentLeafCollector, DocValueUtils.DVConverter dvConverter)
						throws IOException {
			super(parentLeafCollector, dvConverter);
			max = null;
		}

		@Override
		public void collect(int doc) throws IOException {
			if (parentLeafCollector != null)
				parentLeafCollector.collect(doc);
			long value = docValues.get(doc);
			if (max == null || value > max)
				max = value;
		}
	}

	private static class MinNumericFunctionCollector extends LeafNumericFunctionCollector {

		private Long min;

		private MinNumericFunctionCollector(LeafCollector parentLeafCollector, DocValueUtils.DVConverter dvConverter)
						throws IOException {
			super(parentLeafCollector, dvConverter);
			min = null;
		}

		@Override
		public void collect(int doc) throws IOException {
			if (parentLeafCollector != null)
				parentLeafCollector.collect(doc);
			if (parentLeafCollector != null)
				parentLeafCollector.collect(doc);
			long value = docValues.get(doc);
			if (min == null || value < min)
				min = value;
		}
	}

	private static class MaxBinaryFunctionCollector extends LeafBinaryFunctionCollector {

		private BytesRef max;

		private MaxBinaryFunctionCollector(LeafCollector parentLeafCollector, DocValueUtils.DVConverter dvConverter)
						throws IOException {
			super(parentLeafCollector, dvConverter);
			max = null;
		}

		@Override
		public void collect(int doc) throws IOException {
			if (parentLeafCollector != null)
				parentLeafCollector.collect(doc);
			BytesRef value = docValues.get(doc);
			if (max == null || value.compareTo(max) > 0)
				max = value;
		}
	}

	private static class MinBinaryFunctionCollector extends LeafBinaryFunctionCollector {

		private BytesRef min;

		private MinBinaryFunctionCollector(LeafCollector parentLeafCollector, DocValueUtils.DVConverter dvConverter)
						throws IOException {
			super(parentLeafCollector, dvConverter);
			min = null;
		}

		@Override
		public void collect(int doc) throws IOException {
			if (parentLeafCollector != null)
				parentLeafCollector.collect(doc);
			BytesRef value = docValues.get(doc);
			if (min == null || value.compareTo(min) < 0)
				min = value;
		}
	}
}
