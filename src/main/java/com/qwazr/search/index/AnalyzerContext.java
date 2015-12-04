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
import org.apache.lucene.document.Field;
import org.apache.lucene.facet.FacetsConfig;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class AnalyzerContext {

	final Map<String, FieldDefinition> fields;
	final FacetsConfig facetsConfig;
	final Map<String, Analyzer> indexAnalyzerMap;
	final Map<String, Analyzer> queryAnalyzerMap;

	AnalyzerContext(FileClassCompilerLoader compilerLoader, Map<String, FieldDefinition> fields)
			throws ServerException {
		this.fields = fields;
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
			if (fieldDef.template == FieldDefinition.Template.SortedSetMultiDocValuesFacetField)
				facetsConfig.setMultiValued(fieldName, true);
			else if (fieldDef.template == FieldDefinition.Template.SortedSetDocValuesFacetField)
				facetsConfig.setMultiValued(fieldName, false);
			try {

				final Analyzer indexAnalyzer = StringUtils.isEmpty(fieldDef.analyzer) ?
						null :
						IndexUtils.findAnalyzer(compilerLoader, fieldDef.analyzer);
				if (indexAnalyzer != null)
					indexAnalyzerMap.put(fieldName, indexAnalyzer);

				final Analyzer queryAnalyzer = StringUtils.isEmpty(fieldDef.query_analyzer) ?
						indexAnalyzer :
						IndexUtils.findAnalyzer(compilerLoader, fieldDef.query_analyzer);
				if (queryAnalyzer != null)
					queryAnalyzerMap.put(fieldName, queryAnalyzer);

			} catch (ReflectiveOperationException | InterruptedException | IOException e) {
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