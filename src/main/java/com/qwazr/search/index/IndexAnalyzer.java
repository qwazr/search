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

import com.qwazr.utils.FileClassCompilerLoader;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.server.ServerException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.DelegatingAnalyzerWrapper;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.facet.FacetsConfig;

import javax.script.ScriptException;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final public class IndexAnalyzer extends DelegatingAnalyzerWrapper {

	private final Analyzer defaultAnalyzer = new KeywordAnalyzer();

	private volatile AnalyzerContext context;

	IndexAnalyzer(FileClassCompilerLoader compilerLoader, Map<String, FieldDefinition> fields) throws ServerException {
		super(PER_FIELD_REUSE_STRATEGY);
		update(compilerLoader, fields);
	}

	synchronized void update(FileClassCompilerLoader compilerLoader, Map<String, FieldDefinition> fields)
			throws ServerException {
		context = new AnalyzerContext(compilerLoader, fields);
	}

	final AnalyzerContext getContext() {
		return context;
	}

	@Override
	final protected Analyzer getWrappedAnalyzer(String fieldName) {
		Analyzer analyzer = context.analyzerMap.get(fieldName);
		return analyzer == null ? defaultAnalyzer : analyzer;
	}

}
