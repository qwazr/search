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
package com.qwazr.search.query;

import com.qwazr.search.index.QueryContext;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.*;

public class DrillDownQuery extends AbstractQuery {

	final public AbstractQuery baseQuery;
	final public List<LinkedHashMap<String, String[]>> dimPath;
	final public Boolean useDrillSideways;

	public DrillDownQuery() {
		baseQuery = null;
		useDrillSideways = null;
		dimPath = null;
	}

	public DrillDownQuery(final AbstractQuery baseQuery, final boolean useDrillSideways,
			final List<LinkedHashMap<String, String[]>> dimPath) {
		this.baseQuery = baseQuery;
		this.useDrillSideways = useDrillSideways;
		this.dimPath = dimPath;
	}

	private DrillDownQuery getDrillSideways(final String excludedDimension) {
		if (dimPath == null)
			return null;
		final List<LinkedHashMap<String, String[]>> newDimPath = new ArrayList<>();
		dimPath.forEach(map -> {
			if (!map.containsKey(excludedDimension)) newDimPath.add(map);
		});
		return newDimPath.isEmpty() ? null : new DrillDownQuery(baseQuery, false, newDimPath);
	}

	public final Collection<DrillDownQuery> getDrillSidewaysQueries(final Collection<String> dimensions) {
		if (dimPath == null || dimPath.isEmpty() || dimensions == null || dimensions.isEmpty())
			return null;
		final List<DrillDownQuery> queries = new ArrayList<>(dimensions.size());
		for (String dimension : dimensions)
			queries.add(getDrillSideways(dimension));
		return queries;
	}

	public void add(final String dim, final String... path) {
		final LinkedHashMap<String, String[]> map = new LinkedHashMap<>();
		map.put(dim, path);
		dimPath.add(map);
	}

	@Override
	final public org.apache.lucene.facet.DrillDownQuery getQuery(final QueryContext queryContext)
			throws IOException, ParseException, ReflectiveOperationException, QueryNodeException {
		final org.apache.lucene.facet.DrillDownQuery drillDownQuery;
		final Set<String> fieldSet = new HashSet<>();
		dimPath.forEach(map -> fieldSet.addAll(map.keySet()));
		final FacetsConfig facetsConfig = queryContext.fieldMap.getNewFacetsConfig(fieldSet);
		Objects.requireNonNull(facetsConfig, "FacetsConfig is null");
		if (baseQuery == null)
			drillDownQuery = new org.apache.lucene.facet.DrillDownQuery(facetsConfig);
		else
			drillDownQuery = new org.apache.lucene.facet.DrillDownQuery(facetsConfig, baseQuery.getQuery(queryContext));
		if (dimPath != null)
			dimPath.forEach(dimPath -> dimPath.forEach((dim, path) -> drillDownQuery.add(dim, path)));
		return drillDownQuery;
	}

	final static Term facetTerm(final String indexedField, final String dim, final String... path) {
		return new Term(indexedField, FacetsConfig.pathToString(dim, path));
	}

	final static List<Term> facetTerms(final String indexedField, final String dim, final Collection<String> terms) {
		final List<Term> termList = new ArrayList<>(terms.size());
		for (String term : terms)
			termList.add(facetTerm(indexedField, dim, term));
		return termList;
	}

	final static Query facetTermQuery(final FacetsConfig facetsConfig, final String dim,
			final Set<String> filter_terms) {
		final String indexedField = facetsConfig.getDimConfig(dim).indexFieldName;
		if (filter_terms.size() == 1)
			return new org.apache.lucene.search.TermQuery(facetTerm(indexedField, dim, filter_terms.iterator().next()));
		return new org.apache.lucene.queries.TermsQuery(facetTerms(indexedField, dim, filter_terms));
	}
}
