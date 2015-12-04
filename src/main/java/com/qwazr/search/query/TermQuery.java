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
package com.qwazr.search.query;

import com.qwazr.search.index.AnalyzerUtils;
import com.qwazr.search.index.UpdatableAnalyzer;
import org.apache.lucene.analysis.tokenattributes.*;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static com.qwazr.search.index.AnalyzerUtils.*;

public class TermQuery extends AbstractQuery {

	final public String field;
	final public String text;
	final public Boolean apply_analyzer;

	public TermQuery() {
		super(null);
		field = null;
		text = null;
		apply_analyzer = null;
	}

	TermQuery(Float boost, String field, String text, Boolean apply_analyzer) {
		super(boost);
		this.field = field;
		this.text = text;
		this.apply_analyzer = apply_analyzer;
	}

	private class AtomicString {

		private volatile String string = null;

		public synchronized void set(String string) {
			this.string = string;
		}

		public String get() {
			return this.string;
		}

	}

	@Override
	protected Query getQuery(UpdatableAnalyzer analyzer, String queryString) throws IOException {
		final AtomicString atomicString = new AtomicString();
		final String sourceText = text == null ? queryString : text;
		final String term;
		if (apply_analyzer != null && apply_analyzer) {
			forEachTerm(analyzer, field, queryString, new TermConsumer() {
				@Override
				public boolean apply(CharTermAttribute charTermAttr, FlagsAttribute flagsAttr,
						OffsetAttribute offsetAttr, PositionIncrementAttribute posIncAttr,
						PositionLengthAttribute posLengthAttr, TypeAttribute typeAttr, KeywordAttribute keywordAttr) {
					atomicString.set(charTermAttr.toString());
					return false;
				}
			});
			term = atomicString.get();
		} else
			term = sourceText;
		return new org.apache.lucene.search.TermQuery(new Term(field, term));
	}
}
