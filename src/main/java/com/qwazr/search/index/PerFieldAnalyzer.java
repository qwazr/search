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

import com.qwazr.utils.StringUtils;
import com.qwazr.utils.server.ServerException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.DelegatingAnalyzerWrapper;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.facet.FacetsConfig;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PerFieldAnalyzer extends DelegatingAnalyzerWrapper {

	private final Analyzer defaultAnalyzer = new KeywordAnalyzer();
	private volatile Map<String, Analyzer> analyzerMap;

	PerFieldAnalyzer(FacetsConfig facetsConfig, Map<String, FieldDefinition> fields) throws ServerException {
		super(PER_FIELD_REUSE_STRATEGY);
		update(facetsConfig, fields);
	}

	private static Class<?> findAnalyzer(String analyzer) throws ClassNotFoundException {
		try {
			return Class.forName(analyzer);
		} catch (ClassNotFoundException e1) {
			try {
				return Class.forName("com.qwazr.search.analysis." + analyzer);
			} catch (ClassNotFoundException e2) {
				try {
					return Class.forName("org.apache.lucene.analysis." + analyzer);
				} catch (ClassNotFoundException e3) {
					throw e1;
				}
			}
		}
	}

	synchronized void update(FacetsConfig facetsConfig, Map<String, FieldDefinition> fields) throws ServerException {
		this.analyzerMap = newMap(facetsConfig, fields);
	}

	private static Map<String, Analyzer> newMap(FacetsConfig facetsConfig, Map<String, FieldDefinition> fields)
					throws ServerException {
		if (fields == null || fields.size() == 0)
			return Collections.<String, Analyzer>emptyMap();
		Map<String, Analyzer> analyzerMap = new HashMap<String, Analyzer>();
		for (Map.Entry<String, FieldDefinition> field : fields.entrySet()) {
			String fieldName = field.getKey();
			FieldDefinition fieldDef = field.getValue();
			if (fieldDef.template == FieldDefinition.Template.SortedSetMultiDocValuesFacetField)
				facetsConfig.setMultiValued(fieldName, true);
			else if (fieldDef.template == FieldDefinition.Template.SortedSetDocValuesFacetField)
				facetsConfig.setMultiValued(fieldName, false);
			try {
				if (!StringUtils.isEmpty(fieldDef.analyzer))
					analyzerMap.put(field.getKey(), (Analyzer) findAnalyzer(fieldDef.analyzer).newInstance());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				throw new ServerException(Response.Status.NOT_ACCEPTABLE,
								"Class " + fieldDef.analyzer + " not known for the field " + fieldName);
			}
		}
		return analyzerMap;
	}

	@Override
	final protected Analyzer getWrappedAnalyzer(String fieldName) {
		Analyzer analyzer = analyzerMap.get(fieldName);
		return analyzer == null ? defaultAnalyzer : analyzer;
	}
}
