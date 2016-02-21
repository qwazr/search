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

import com.qwazr.search.field.FieldTypeInterface;
import com.qwazr.search.field.ValueConverter;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.SlowCompositeReaderWrapper;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;

class ResultUtils {

	final static Object getValue(IndexableField field) {
		if (field == null)
			return null;
		String s = field.stringValue();
		if (s != null)
			return s;
		Number n = field.numericValue();
		if (n != null)
			return n;
		return null;
	}

	final static Map<String, Object> buildFields(final Document document) {
		Map<String, Object> fields = new LinkedHashMap<String, Object>();
		for (IndexableField field : document) {
			Object newValue = getValue(field);
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

	final static void addDocValues(final int docId, Map<String, ValueConverter> sources,
			final Map<String, Object> dest) {
		if (sources == null)
			return;
		sources.forEach(new BiConsumer<String, ValueConverter>() {
			@Override
			public void accept(String fieldName, ValueConverter converter) {
				Object o = converter.convert(docId);
				if (o != null)
					dest.put(fieldName, o);
			}
		});
	}

	final static Map<String, ValueConverter> extractDocValuesFields(final Map<String, FieldTypeInterface> fieldTypes,
			final IndexReader indexReader, final Set<String> returned_fields) throws IOException {
		if (returned_fields == null)
			return null;
		//FieldInfos fieldInfos = MultiFields.getMergedFieldInfos(indexReader);
		LeafReader leafReader = SlowCompositeReaderWrapper.wrap(indexReader);
		Map<String, ValueConverter> map = new LinkedHashMap<String, ValueConverter>();
		for (String fieldName : returned_fields) {
			FieldTypeInterface fieldType = fieldTypes.get(fieldName);
			if (fieldType == null)
				continue;
			ValueConverter converter = fieldType.getConverter(leafReader);
			if (converter == null)
				continue;
			map.put(fieldName, converter);
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
