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
 **/
package com.qwazr.search.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.index.*;

import java.io.IOException;
import java.util.*;

class ResultUtils {

	final static Map<String, Object> buildFields(final Document document) {
		Map<String, Object> fields = new LinkedHashMap<String, Object>();
		for (IndexableField field : document) {
			Object newValue = FieldDefinition.getValue(field);
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

	final static void addDocValues(final int docId, Map<String, DocValueUtils.DVConverter> sources,
					final Map<String, Object> dest) {
		if (sources == null)
			return;
		for (Map.Entry<String, DocValueUtils.DVConverter> entry : sources.entrySet()) {
			Object o = entry.getValue().convert(docId);
			if (o != null)
				dest.put(entry.getKey(), o);
		}
	}

	final static Map<String, Map<String, Number>> buildFacets(final Map<String, QueryDefinition.Facet> facetsDef,
					final Facets facets) throws IOException {
		Map<String, Map<String, Number>> facetResults = new LinkedHashMap<String, Map<String, Number>>();
		for (Map.Entry<String, QueryDefinition.Facet> entry : facetsDef.entrySet()) {
			String dim = entry.getKey();
			Map<String, Number> facetMap = buildFacet(dim, entry.getValue(), facets);
			if (facetMap != null)
				facetResults.put(dim, facetMap);
		}
		return facetResults;
	}

	final static Map<String, Number> buildFacet(String dim, QueryDefinition.Facet facet, Facets facets)
					throws IOException {
		int top = facet.top == null ? 10 : facet.top;
		LinkedHashMap<String, Number> facetMap = new LinkedHashMap<String, Number>();
		FacetResult facetResult = facets.getTopChildren(top, dim);
		if (facetResult == null || facetResult.labelValues == null)
			return null;
		for (LabelAndValue lv : facetResult.labelValues)
			facetMap.put(lv.label, lv.value);
		return facetMap;
	}

	final static Map<String, DocValueUtils.DVConverter> extractDocValuesFields(
					final Map<String, FieldDefinition> fieldMap, final IndexReader indexReader,
					final Set<String> returned_fields) throws IOException {
		if (returned_fields == null)
			return null;
		FieldInfos fieldInfos = MultiFields.getMergedFieldInfos(indexReader);
		LeafReader dvReader = SlowCompositeReaderWrapper.wrap(indexReader);
		Map<String, DocValueUtils.DVConverter> map = new LinkedHashMap<String, DocValueUtils.DVConverter>();
		for (String field : returned_fields) {
			FieldInfo fieldInfo = dvReader.getFieldInfos().fieldInfo(field);
			if (fieldInfo == null)
				continue;
			FieldDefinition fieldDef = fieldMap.get(field);
			if (fieldDef == null)
				continue;
			DocValueUtils.DVConverter converter = DocValueUtils.newConverter(fieldDef, dvReader, fieldInfo);
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
