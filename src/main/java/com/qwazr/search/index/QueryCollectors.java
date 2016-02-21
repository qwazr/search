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
import com.qwazr.search.field.FieldTypeInterface;
import com.qwazr.utils.server.ServerException;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.search.*;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

class QueryCollectors {

	final List<Collector> collectors;

	final FacetsCollector facetsCollector;

	final Collection<FunctionCollector> functionsCollectors;

	final TotalHitCountCollector totalHitCountCollector;

	final TopDocsCollector topDocsCollector;

	final Collector finalCollector;

	QueryCollectors(boolean bNeedScore, Sort sort, int numHits, final LinkedHashMap<String, QueryDefinition.Facet> facets,
			Collection<QueryDefinition.Function> functions, final Map<String, FieldTypeInterface> fields)
			throws ServerException, IOException {
		collectors = new ArrayList<Collector>();
		facetsCollector = buildFacetsCollector(facets);
		functionsCollectors = buildFunctionsCollectors(fields, functions);
		totalHitCountCollector = buildTotalHitsCollector(numHits);
		topDocsCollector = buildTopDocCollector(sort, numHits, bNeedScore);
		finalCollector = getFinalCollector();
	}

	private final <T extends Collector> T add(T collector) {
		collectors.add(collector);
		return collector;
	}

	private final Collector getFinalCollector() {
		switch (collectors.size()) {
		case 0:
			return null;
		case 1:
			return collectors.get(0);
		default:
			return MultiCollector.wrap(collectors);
		}
	}

	private final FacetsCollector buildFacetsCollector(LinkedHashMap<String, QueryDefinition.Facet> facets) {
		if (facets == null || facets.isEmpty())
			return null;
		return add(new FacetsCollector());
	}

	private final Collection<FunctionCollector> buildFunctionsCollectors(Map<String, FieldTypeInterface> fields,
			Collection<QueryDefinition.Function> functions) throws ServerException {
		if (functions == null || functions.isEmpty())
			return null;
		Collection<FunctionCollector> functionsCollectors = new ArrayList<FunctionCollector>();
		for (QueryDefinition.Function function : functions) {
			FieldDefinition fieldDef = fields.get(function.field);
			if (fieldDef == null)
				throw new ServerException(Response.Status.NOT_ACCEPTABLE,
						"Cannot compute the function " + function.function + " because the field is unknown: "
								+ function.field);
			functionsCollectors.add(new FunctionCollector(function, fieldDef));
		}
		collectors.addAll(functionsCollectors);
		return functionsCollectors;
	}

	private final TopDocsCollector buildTopDocCollector(Sort sort, int numHits, boolean bNeedScore) throws IOException {
		if (numHits == 0)
			return null;
		final TopDocsCollector topDocsCollector;
		if (sort != null)
			topDocsCollector = TopFieldCollector.create(sort, numHits, true, bNeedScore, bNeedScore);
		else
			topDocsCollector = TopScoreDocCollector.create(numHits);
		return add(topDocsCollector);
	}

	private final TotalHitCountCollector buildTotalHitsCollector(int numHits) {
		if (numHits > 0)
			return null;
		return add(new TotalHitCountCollector());
	}

	final Integer getTotalHits() {
		if (totalHitCountCollector != null)
			return totalHitCountCollector.getTotalHits();
		if (topDocsCollector != null)
			return topDocsCollector.getTotalHits();
		return null;
	}

	final TopDocs getTopDocs() {
		return topDocsCollector == null ? null : topDocsCollector.topDocs();
	}

}
