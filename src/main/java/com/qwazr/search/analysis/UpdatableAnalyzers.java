/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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
package com.qwazr.search.analysis;

import com.qwazr.server.ServerException;
import com.qwazr.utils.concurrent.ReferenceCounter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.AnalyzerWrapper;
import org.apache.lucene.analysis.core.KeywordAnalyzer;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

final public class UpdatableAnalyzers extends AnalyzerWrapper {

	private final AtomicInteger activeAnalyzers = new AtomicInteger();

	private volatile Analyzers analyzers;

	public UpdatableAnalyzers(final Map<String, Analyzer> analyzerMap) throws ServerException {
		super(PER_FIELD_REUSE_STRATEGY);
		update(analyzerMap);
	}

	final public synchronized void update(final Map<String, Analyzer> analyzerMap) throws ServerException {
		if (analyzers != null && analyzerMap == analyzers.analyzerMap)
			return;
		final Analyzers oldAnalyzers = analyzers;
		analyzers = new Analyzers(analyzerMap).acquire();
		if (oldAnalyzers != null)
			oldAnalyzers.close();
	}

	public int getActiveAnalyzers() {
		return activeAnalyzers.get();
	}

	@Override
	public void close() {
		if (analyzers != null)
			analyzers.close();
		analyzers = null;
		super.close();
	}

	/**
	 * The given Analyzers object must be closed after usage
	 *
	 * @return an Analyzer
	 */
	public Analyzers getAnalyzers() {
		return analyzers.acquire();
	}

	@Override
	protected Analyzer getWrappedAnalyzer(String fieldName) {
		return analyzers.getWrappedAnalyzer(fieldName);
	}

	private static final Analyzer defaultAnalyzer = new KeywordAnalyzer();

	public class Analyzers extends AnalyzerWrapper {

		private final ReferenceCounter refCounter;
		private final Map<String, Analyzer> analyzerMap;

		private Analyzers(Map<String, Analyzer> analyzerMap) {
			super(PER_FIELD_REUSE_STRATEGY);
			refCounter = new ReferenceCounter.Impl();
			this.analyzerMap = analyzerMap;
			activeAnalyzers.incrementAndGet();
		}

		private Analyzers acquire() {
			refCounter.acquire();
			return this;
		}

		@Override
		public void close() {
			if (refCounter.release() > 0)
				return;
			activeAnalyzers.decrementAndGet();
			if (analyzerMap != null)
				analyzerMap.forEach((s, analyzer) -> analyzer.close());
		}

		@Override
		public Analyzer getWrappedAnalyzer(final String fieldName) {
			final Analyzer analyzer = analyzerMap.get(fieldName);
			return analyzer == null ? defaultAnalyzer : analyzer;
		}

	}

}
