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

import org.apache.lucene.search.Collector;
import org.apache.lucene.search.CollectorManager;
import org.apache.lucene.search.Sort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

class QueryCollectorManager implements CollectorManager<Collector, QueryCollectors.Result> {

	final boolean bNeedScore;
	final Sort sort;
	final int numHits;
	final LinkedHashMap<String, FacetDefinition> facets;
	final boolean useDrillSideways;
	final LinkedHashMap<String, QueryDefinition.CollectorDefinition> extCollectors;

	private final Collection<QueryCollectors> queryCollectorsList;
	private QueryCollectors.Result result;

	QueryCollectorManager(final boolean bNeedScore, final Sort sort, final int numHits,
			final LinkedHashMap<String, FacetDefinition> facets, final boolean useDrillSideways,
			final LinkedHashMap<String, QueryDefinition.CollectorDefinition> extCollectors) {
		this.bNeedScore = bNeedScore;
		this.sort = sort;
		this.numHits = numHits;
		this.facets = facets;
		this.useDrillSideways = useDrillSideways;
		this.extCollectors = extCollectors;
		this.queryCollectorsList = new ArrayList<>();
	}

	@Override
	final public Collector newCollector() throws IOException {
		final QueryCollectors queryCollectors;
		try {
			queryCollectors = new QueryCollectors(this);
		} catch (ReflectiveOperationException e) {
			throw new IOException(e);
		}
		queryCollectorsList.add(queryCollectors);
		return queryCollectors.finalCollector;
	}

	@Override
	final public QueryCollectors.Result reduce(final Collection collectors) throws IOException {
		result = new QueryCollectors.Result(this, queryCollectorsList);
		return result;
	}

	final QueryCollectors.Result getResult() {
		return result;
	}

}
