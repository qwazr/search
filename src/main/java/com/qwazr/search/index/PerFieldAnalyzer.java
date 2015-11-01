/**
 * Copyright 2015 Emmanuel Keller / QWAZR
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
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

final public class PerFieldAnalyzer extends DelegatingAnalyzerWrapper {

	private final Analyzer defaultAnalyzer = new KeywordAnalyzer();

	static class AnalyzerContext {

		final Map<String, FieldDefinition> fields;
		final FacetsConfig facetsConfig;
		final Map<String, Analyzer> analyzerMap;

		private AnalyzerContext(FileClassCompilerLoader compilerLoader, Map<String, FieldDefinition> fields)
						throws ServerException {
			this.fields = fields;
			this.facetsConfig = new FacetsConfig();
			if (fields == null || fields.size() == 0) {
				this.analyzerMap = Collections.<String, Analyzer>emptyMap();
				return;
			}
			this.analyzerMap = new HashMap<String, Analyzer>();
			for (Map.Entry<String, FieldDefinition> field : fields.entrySet()) {
				String fieldName = field.getKey();
				FieldDefinition fieldDef = field.getValue();
				if (fieldDef.template == FieldDefinition.Template.SortedSetMultiDocValuesFacetField)
					facetsConfig.setMultiValued(fieldName, true);
				else if (fieldDef.template == FieldDefinition.Template.SortedSetDocValuesFacetField)
					facetsConfig.setMultiValued(fieldName, false);
				try {
					if (!StringUtils.isEmpty(fieldDef.analyzer))
						analyzerMap.put(field.getKey(), findAnalyzer(compilerLoader, fieldDef.analyzer).newInstance());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InterruptedException | ScriptException | IOException e) {
					throw new ServerException(Response.Status.NOT_ACCEPTABLE,
									"Class " + fieldDef.analyzer + " not known for the field " + fieldName, e);
				}
			}
		}

		final Field getNewLuceneField(String fieldName, Object value) throws IOException {
			FieldDefinition fieldDef = fields == null ? null : fields.get(fieldName);
			if (fieldDef == null)
				throw new IOException("No field definition for the field: " + fieldName);
			return fieldDef.newField(fieldName, value);
		}
	}

	private volatile AnalyzerContext context;

	PerFieldAnalyzer(FileClassCompilerLoader compilerLoader, Map<String, FieldDefinition> fields)
					throws ServerException {
		super(PER_FIELD_REUSE_STRATEGY);
		update(compilerLoader, fields);
	}

	private final static String[] classPrefixes = { "", "com.qwazr.search.analysis.", "org.apache.lucene.analysis." };

	private static Class<Analyzer> findAnalyzerClass(String analyzer) throws ClassNotFoundException {
		ClassNotFoundException firstClassException = null;
		for (String prefix : classPrefixes) {
			try {
				return (Class<Analyzer>) Class.forName(prefix + analyzer);
			} catch (ClassNotFoundException e) {
				if (firstClassException == null)
					firstClassException = e;
			}
		}
		throw firstClassException;
	}

	private static Class<Analyzer> findAnalyzer(FileClassCompilerLoader compilerLoader, String analyzer)
					throws ClassNotFoundException, InterruptedException, ScriptException, IOException {
		if (compilerLoader != null && analyzer.endsWith(".java"))
			return compilerLoader.loadClass(new File(analyzer));
		return findAnalyzerClass(analyzer);
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
