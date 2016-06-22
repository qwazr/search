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

import com.qwazr.classloader.ClassLoaderManager;
import com.qwazr.search.collector.BaseCollector;
import com.qwazr.search.field.FieldTypeInterface;
import com.qwazr.utils.FunctionUtils;
import com.qwazr.utils.server.ServerException;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.search.*;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;

class QueryCollectors {

	final List<Collector> collectors;

	final FacetsCollector facetsCollector;

	final Collection<BaseCollector> externalCollectors;

	final TotalHitCountCollector totalHitCountCollector;

	final TopDocsCollector topDocsCollector;

	final Collector finalCollector;

	QueryCollectors(boolean bNeedScore, Sort sort, int numHits, final LinkedHashMap<String, FacetDefinition> facets,
			final LinkedHashMap<String, QueryDefinition.CollectorDefinition> extCollectors)
			throws ReflectiveOperationException, IOException {
		collectors = new ArrayList<>();
		facetsCollector = buildFacetsCollector(facets);
		totalHitCountCollector = buildTotalHitsCollector(numHits);
		topDocsCollector = buildTopDocCollector(sort, numHits, bNeedScore);
		externalCollectors = buildExternalCollectors(extCollectors);
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

	private final FacetsCollector buildFacetsCollector(LinkedHashMap<String, FacetDefinition> facets) {
		if (facets == null || facets.isEmpty())
			return null;
		for (FacetDefinition facet : facets.values())
			if (facet.queries == null || facet.queries.isEmpty())
				return add(new FacetsCollector());
		return null;
	}

	final private Collection<BaseCollector> buildExternalCollectors(
			final Map<String, QueryDefinition.CollectorDefinition> collectors)
			throws ReflectiveOperationException {
		if (collectors == null || collectors.isEmpty())
			return null;
		final Collection<BaseCollector> externalCollectors = new ArrayList<>();
		FunctionUtils.forEach(collectors, (name, collector) -> {
			final Class<? extends Collector> collectorClass = ClassLoaderManager.findClass(collector.classname);
			Constructor<?>[] constructors = collectorClass.getConstructors();
			if (constructors.length == 0)
				throw new ReflectiveOperationException("No constructor for class: " + collectorClass);
			final BaseCollector baseCollector;
			if (collector.arguments == null || collector.arguments.length == 0)
				baseCollector = (BaseCollector) constructors[0].newInstance(name);
			else {
				Object[] arguments = new Object[collector.arguments.length + 1];
				arguments[0] = name;
				System.arraycopy(collector.arguments, 0, arguments, 1, collector.arguments.length);
				baseCollector = (BaseCollector) constructors[0].newInstance(arguments);
			}
			externalCollectors.add(add(baseCollector));
		});
		return externalCollectors;
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
