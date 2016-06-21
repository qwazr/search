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
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Scorer;

import java.io.IOException;

public class DocValuesLeafCollector {

	public static abstract class ConverterLeafCollector<T extends ValueConverter> implements LeafCollector {

		protected final T converter;

		protected ConverterLeafCollector(final T converter) throws IOException {
			this.converter = converter;
		}

		@Override
		public void setScorer(Scorer scorer) throws IOException {
		}

	}

	public static abstract class Numeric<T extends ValueConverter> extends ConverterLeafCollector<T> {

		protected final NumericDocValues docValues;

		protected Numeric(T converter) throws IOException {
			super(converter);
			docValues = (NumericDocValues) converter.source;
		}
	}

	public static abstract class Binary extends ConverterLeafCollector<SingleDVConverter.BinaryDVConverter> {

		protected final BinaryDocValues docValues;

		protected Binary(SingleDVConverter.BinaryDVConverter converter) throws IOException {
			super(converter);
			docValues = converter.source;
		}
	}
}