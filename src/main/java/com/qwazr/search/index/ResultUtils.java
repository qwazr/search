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
 **/
package com.qwazr.search.index;

import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.field.FieldUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;

import java.io.IOException;
import java.util.*;

class ResultUtils {

	final static Map<String, Object> buildFields(final Document document) {
		Map<String, Object> fields = new LinkedHashMap<String, Object>();
		for (IndexableField field : document) {
			Object newValue = FieldUtils.getValue(field);
			if (newValue == null)
				continue;
			Object oldValue = fields.get(field.name());
			if (oldValue == null) {
				fields.put(field.name(), newValue);
				continue;
			}
			if (oldValue instanceof List<?>) {
				((List<Object>) oldValue).add(newValue);
				continue;
			}
			List<Object> list = new ArrayList<Object>(2);
			list.add(oldValue);
			list.add(newValue);
			fields.put(field.name(), list);
		}
		return fields;
	}

	final static void addDocValues(final int docId, Map<String, ValueUtils.DVConverter> sources,
					final Map<String, Object> dest) {
		if (sources == null)
			return;
		for (Map.Entry<String, ValueUtils.DVConverter> entry : sources.entrySet()) {
			Object o = entry.getValue().convert(docId);
			if (o != null)
				dest.put(entry.getKey(), o);
		}
	}

	final static Map<String, ValueUtils.DVConverter> extractDocValuesFields(final Map<String, FieldDefinition> fieldMap,
					final IndexReader indexReader, final Set<String> returned_fields) throws IOException {
		if (returned_fields == null)
			return null;
		//FieldInfos fieldInfos = MultiFields.getMergedFieldInfos(indexReader);
		LeafReader dvReader = SlowCompositeReaderWrapper.wrap(indexReader);
		Map<String, ValueUtils.DVConverter> map = new LinkedHashMap<String, ValueUtils.DVConverter>();
		for (String field : returned_fields) {
			FieldInfo fieldInfo = dvReader.getFieldInfos().fieldInfo(field);
			if (fieldInfo == null)
				continue;
			FieldDefinition fieldDef = fieldMap.get(field);
			if (fieldDef == null)
				continue;
			ValueUtils.DVConverter converter = ValueUtils.newConverter(fieldDef, dvReader, fieldInfo);
			if (converter == null)
				continue;
			map.put(field, converter);
		}
		return map;
	}

	final static List<ResultDefinition.Function> buildFunctions(
					final Collection<FunctionCollector> functionsCollector) {
		if (functionsCollector == null)
			return null;
		List<ResultDefinition.Function> functions = new ArrayList<ResultDefinition.Function>(functionsCollector.size());
		for (FunctionCollector functionCollector : functionsCollector)
			functions.add(new ResultDefinition.Function(functionCollector));
		return functions;
	}
}
