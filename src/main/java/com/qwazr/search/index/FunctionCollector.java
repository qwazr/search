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

import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Scorer;

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
		switch (function.function) {
		case max:
			return new MaxFunctionCollector(parentLeafCollector, context, fieldDef, function.field);
		case min:
			return new MinFunctionCollector(parentLeafCollector, context, fieldDef, function.field);
		}
		return parentLeafCollector;
	}

	public static abstract class LeafFunctionCollector implements LeafCollector {

		protected final LeafCollector parentLeafCollector;
		protected final DocValueUtils.DVConverter<?, ?> dvConverter;

		protected LeafFunctionCollector(LeafCollector parentLeafCollector, LeafReaderContext context,
				FieldDefinition fieldDef, String field) throws IOException {
			this.parentLeafCollector = parentLeafCollector;
			final LeafReader leafReader = context.reader();
			FieldInfo fieldInfo = leafReader.getFieldInfos().fieldInfo(field);
			dvConverter = DocValueUtils.newConverter(fieldDef, leafReader, fieldInfo);
		}

		@Override
		public void setScorer(Scorer scorer) throws IOException {
			if (parentLeafCollector != null)
				parentLeafCollector.setScorer(scorer);
		}
	}

	private static class MaxFunctionCollector extends LeafFunctionCollector {

		private Comparable max;

		private MaxFunctionCollector(LeafCollector parentLeafCollector, LeafReaderContext context,
				FieldDefinition fieldDef, String field) throws IOException {
			super(parentLeafCollector, context, fieldDef, field);
			max = null;
		}

		@Override
		public void collect(int doc) throws IOException {
			if (parentLeafCollector != null)
				parentLeafCollector.collect(doc);
			Comparable value = dvConverter.convert(doc);
			if (value == null)
				return;
			if (max == null)
				max = value;
			else if (value.compareTo(max) > 0)
				max = value;
		}
	}

	private static class MinFunctionCollector extends LeafFunctionCollector {

		private Comparable min;

		private MinFunctionCollector(LeafCollector parentLeafCollector, LeafReaderContext context,
				FieldDefinition fieldDef, String field) throws IOException {
			super(parentLeafCollector, context, fieldDef, field);
			min = null;
		}

		@Override
		public void collect(int doc) throws IOException {
			if (parentLeafCollector != null)
				parentLeafCollector.collect(doc);
			if (parentLeafCollector != null)
				parentLeafCollector.collect(doc);
			Comparable value = (Comparable) dvConverter.convert(doc);
			if (value == null)
				return;
			if (min == null)
				min = value;
			else if (value.compareTo(min) < 0)
				min = value;
		}
	}
}
