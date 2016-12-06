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
import com.qwazr.utils.FunctionUtils;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;

class QueryCollectors {

	private final List<Collector> collectors;

	private final FacetsCollector facetsCollector;

	private final List<BaseCollector> externalCollectors;

	private final TotalHitCountCollector totalHitCountCollector;

	private final TopDocsCollector topDocsCollector;

	final Collector finalCollector;

	QueryCollectors(final QueryCollectorManager manager)
			throws IOException, ReflectiveOperationException {
		collectors = new ArrayList<>();
		facetsCollector = manager.useDrillSideways ? null : buildFacetsCollector(manager.facets);
		totalHitCountCollector = buildTotalHitsCollector(manager.numHits);
		topDocsCollector = buildTopDocCollector(manager.sort, manager.numHits, manager.bNeedScore);
		externalCollectors = buildExternalCollectors(manager.extCollectors);
		finalCollector = getFinalCollector();
	}

	private <T extends Collector> T add(final T collector) {
		collectors.add(collector);
		return collector;
	}

	private Collector getFinalCollector() {
		switch (collectors.size()) {
			case 0:
				return null;
			case 1:
				return collectors.get(0);
			default:
				return MultiCollector.wrap(collectors);
		}
	}

	private FacetsCollector buildFacetsCollector(final LinkedHashMap<String, FacetDefinition> facets) {
		if (facets == null || facets.isEmpty())
			return null;
		for (FacetDefinition facet : facets.values())
			if (facet.queries == null || facet.queries.isEmpty())
				return add(new FacetsCollector());
		return null;
	}

	private List<BaseCollector> buildExternalCollectors(
			final Map<String, QueryDefinition.CollectorDefinition> collectors)
			throws ReflectiveOperationException {
		if (collectors == null || collectors.isEmpty())
			return null;
		final List<BaseCollector> externalCollectors = new ArrayList<>();
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

	private TopDocsCollector buildTopDocCollector(final Sort sort, final int numHits, final boolean bNeedScore)
			throws IOException {
		if (numHits == 0)
			return null;
		final TopDocsCollector topDocsCollector;
		if (sort != null)
			topDocsCollector = TopFieldCollector.create(sort, numHits, true, bNeedScore, bNeedScore);
		else
			topDocsCollector = TopScoreDocCollector.create(numHits);
		return add(topDocsCollector);
	}

	private TotalHitCountCollector buildTotalHitsCollector(final int numHits) {
		if (numHits > 0)
			return null;
		return add(new TotalHitCountCollector());
	}

	static class Result {

		private final QueryCollectorManager manager;
		private final Collection<QueryCollectors> list;

		Result(final QueryCollectorManager manager, final Collection<QueryCollectors> list) {
			this.manager = manager;
			this.list = list;
		}

		final Integer getTotalHits() {
			if (list == null || list.isEmpty())
				return 0;
			int totalHits = 0;
			for (QueryCollectors queryCollectors : list) {
				if (queryCollectors.totalHitCountCollector != null)
					totalHits += queryCollectors.totalHitCountCollector.getTotalHits();
				else if (queryCollectors.topDocsCollector != null)
					totalHits += queryCollectors.topDocsCollector.getTotalHits();
			}
			return totalHits;
		}

		final TopDocs getTopDocs() throws IOException {
			if (list == null || list.isEmpty())
				return null;
			if (manager.sort != null)
				return getTopFieldDocs();
			final List<TopDocs> topDocsList = new ArrayList<>(list.size());
			for (QueryCollectors queryCollectors : list)
				if (queryCollectors.topDocsCollector != null)
					topDocsList.add(queryCollectors.topDocsCollector.topDocs());
			return TopDocs.merge(manager.numHits, topDocsList.toArray(new TopDocs[topDocsList.size()]));
		}

		private TopDocs getTopFieldDocs() throws IOException {
			final List<TopFieldDocs> topFieldDocsList = new ArrayList<>(list.size());
			for (QueryCollectors queryCollectors : list)
				if (queryCollectors.topDocsCollector == null)
					topFieldDocsList.add(((TopFieldCollector) queryCollectors.topDocsCollector).topDocs());
			return TopFieldDocs.merge(manager.sort, manager.numHits,
					topFieldDocsList.toArray(new TopFieldDocs[topFieldDocsList.size()]));
		}

		private final static FacetsCollector EMPTY = new FacetsCollector();

		final FacetsCollector getFacetsCollector() {
			if (manager.facets == null || manager.facets.isEmpty())
				return null;
			if (list == null || list.isEmpty())
				return EMPTY;
			for (QueryCollectors queryCollectors : list)
				if (queryCollectors.facetsCollector != null)
					return queryCollectors.facetsCollector;
			return null; //EK TOTO Reduce FacetCollectors
		}

		final Map<String, Object> getExternalResults() {
			if (list == null || list.isEmpty())
				return null;
			if (manager.extCollectors == null)
				return null;
			final Map<String, Object> results = new HashMap<>();
			int i = 0;
			for (String name : manager.extCollectors.keySet()) {
				final List<BaseCollector> externalCollectors = new ArrayList<>();
				for (QueryCollectors queryCollectors : list)
					if (queryCollectors.externalCollectors != null)
						externalCollectors.add(queryCollectors.externalCollectors.get(i));
				if (!externalCollectors.isEmpty())
					results.put(name, externalCollectors.get(0).getReducedResult(externalCollectors));
				i++;
			}
			return results;
		}
	}
}
