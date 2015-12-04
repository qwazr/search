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

import com.qwazr.utils.server.ServerException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.DelegatingAnalyzerWrapper;
import org.apache.lucene.analysis.core.KeywordAnalyzer;

import java.util.Map;

final public class UpdatableAnalyzer extends DelegatingAnalyzerWrapper {

	private final Analyzer defaultAnalyzer = new KeywordAnalyzer();

	private volatile AnalyzerContext context;

	private volatile Map<String, Analyzer> analyzerMap;

	UpdatableAnalyzer(AnalyzerContext context, Map<String, Analyzer> analyzerMap) throws ServerException {
		super(PER_FIELD_REUSE_STRATEGY);
		update(context, analyzerMap);
	}

	final synchronized void update(AnalyzerContext context, Map<String, Analyzer> analyzerMap) throws ServerException {
		this.context = context;
		this.analyzerMap = analyzerMap;
	}

	@Override
	final public void close() {
		if (analyzerMap != null)
			analyzerMap.forEach((s, analyzer) -> analyzer.close());
		super.close();
	}

	final AnalyzerContext getContext() {
		return context;
	}

	@Override
	final protected Analyzer getWrappedAnalyzer(String fieldName) {
		Analyzer analyzer = analyzerMap.get(fieldName);
		return analyzer == null ? defaultAnalyzer : analyzer;
	}

}
