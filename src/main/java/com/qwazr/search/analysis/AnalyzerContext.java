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
package com.qwazr.search.analysis;

import com.qwazr.classloader.ClassLoaderManager;
import com.qwazr.search.field.AbstractFieldType;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.utils.ClassLoaderUtils;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.server.ServerException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.facet.FacetsConfig;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AnalyzerContext {

	public final Map<String, AbstractFieldType> fieldTypes;
	public final FacetsConfig facetsConfig;
	public final Map<String, Analyzer> indexAnalyzerMap;
	public final Map<String, Analyzer> queryAnalyzerMap;

	public AnalyzerContext(Map<String, AnalyzerDefinition> analyzerMap, Map<String, FieldDefinition> fields)
			throws ServerException {
		this.fieldTypes = new HashMap<String, AbstractFieldType>();
		this.facetsConfig = new FacetsConfig();
		if (fields == null || fields.size() == 0) {
			this.indexAnalyzerMap = Collections.<String, Analyzer>emptyMap();
			this.queryAnalyzerMap = Collections.<String, Analyzer>emptyMap();
			return;
		}
		this.indexAnalyzerMap = new HashMap<String, Analyzer>();
		this.queryAnalyzerMap = new HashMap<String, Analyzer>();

		for (Map.Entry<String, FieldDefinition> field : fields.entrySet()) {
			String fieldName = field.getKey();
			FieldDefinition fieldDef = field.getValue();
			fieldTypes.put(fieldName, AbstractFieldType.getInstance(fieldDef));
			if (fieldDef.template != null) {
				switch (fieldDef.template) {
				case FacetField:
				case SortedSetDocValuesFacetField:
					facetsConfig.setMultiValued(fieldName, false);
					facetsConfig.setHierarchical(fieldName, false);
					break;
				case MultiFacetField:
				case SortedSetMultiDocValuesFacetField:
					facetsConfig.setMultiValued(fieldName, true);
					facetsConfig.setHierarchical(fieldName, false);
					break;
				case HierarchicalFacetField:
					facetsConfig.setMultiValued(fieldName, false);
					facetsConfig.setHierarchical(fieldName, true);
					break;
				case HierarchicalMultiFacetField:
					facetsConfig.setMultiValued(fieldName, true);
					facetsConfig.setHierarchical(fieldName, true);
					break;
				}
			}
			try {

				final Analyzer indexAnalyzer = StringUtils.isEmpty(fieldDef.analyzer) ?
						null :
						findAnalyzer(analyzerMap, fieldDef.analyzer);
				if (indexAnalyzer != null)
					indexAnalyzerMap.put(fieldName, indexAnalyzer);

				final Analyzer queryAnalyzer = StringUtils.isEmpty(fieldDef.query_analyzer) ?
						indexAnalyzer :
						findAnalyzer(analyzerMap, fieldDef.query_analyzer);
				if (queryAnalyzer != null)
					queryAnalyzerMap.put(fieldName, queryAnalyzer);

			} catch (ReflectiveOperationException | InterruptedException | IOException e) {
				throw new ServerException(Response.Status.NOT_ACCEPTABLE,
						"Class " + fieldDef.analyzer + " not known for the field " + fieldName, e);
			}
		}
	}

	final static String[] analyzerClassPrefixes = { "", "org.apache.lucene.analysis." };

	static private Analyzer findAnalyzer(Map<String, AnalyzerDefinition> analyzerMap, String analyzer)
			throws InterruptedException, ReflectiveOperationException, IOException {
		if (analyzerMap != null) {
			AnalyzerDefinition analyzerDef = analyzerMap.get(analyzer);
			if (analyzerDef != null)
				return new CustomAnalyzer(analyzerDef);
		}

		return (Analyzer) ClassLoaderUtils.findClass(ClassLoaderManager.classLoader, analyzer, analyzerClassPrefixes)
				.newInstance();
	}

}
