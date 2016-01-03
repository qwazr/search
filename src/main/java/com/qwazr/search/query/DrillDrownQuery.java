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
package com.qwazr.search.query;

import com.qwazr.search.index.QueryContext;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.*;

public class DrillDrownQuery extends AbstractQuery {

	final public AbstractQuery baseQuery;
	final public List<LinkedHashMap<String, String[]>> dimPath;

	public DrillDrownQuery() {
		super(null);
		baseQuery = null;
		dimPath = null;
	}

	DrillDrownQuery(Float boost, AbstractQuery baseQuery, List<LinkedHashMap<String, String[]>> dimPath) {
		super(boost);
		this.baseQuery = baseQuery;
		this.dimPath = dimPath;
	}

	@Override
	protected Query getQuery(QueryContext queryContext)
			throws IOException, ParseException, ReflectiveOperationException, QueryNodeException {
		final org.apache.lucene.facet.DrillDownQuery drillDownQuery;
		final FacetsConfig facetsConfig = queryContext.analyzer.getContext().facetsConfig;
		Objects.requireNonNull(facetsConfig, "FacetsConfig is null");
		if (baseQuery == null)
			drillDownQuery = new org.apache.lucene.facet.DrillDownQuery(facetsConfig);
		else
			drillDownQuery = new org.apache.lucene.facet.DrillDownQuery(facetsConfig, baseQuery.getQuery(queryContext));
		if (dimPath != null)
			dimPath.forEach(dimPath -> dimPath.forEach((dim, path) -> drillDownQuery.add(dim, path)));
		return drillDownQuery;
	}

	final static Term facetTerm(String indexedField, String dim, String... path) {
		return new Term(indexedField, FacetsConfig.pathToString(dim, path));
	}

	final static List<Term> facetTerms(String indexedField, String dim, Collection<String> terms) {
		List<Term> termList = new ArrayList<>(terms.size());
		for (String term : terms)
			termList.add(facetTerm(indexedField, dim, term));
		return termList;
	}

	final static Query facetTermQuery(FacetsConfig facetsConfig, String dim, Set<String> filter_terms) {
		String indexedField = facetsConfig.getDimConfig(dim).indexFieldName;
		if (filter_terms.size() == 1)
			return new org.apache.lucene.search.TermQuery(facetTerm(indexedField, dim, filter_terms.iterator().next()));
		return new org.apache.lucene.queries.TermsQuery(facetTerms(indexedField, dim, filter_terms));
	}
}
