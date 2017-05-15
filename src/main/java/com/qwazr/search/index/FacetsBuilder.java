/**
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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
import com.qwazr.search.query.AbstractQuery;
import com.qwazr.utils.FunctionUtils;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.TimeTracker;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.lucene.facet.DrillSideways;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetCounts;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.facet.taxonomy.TaxonomyFacetSumFloatAssociations;
import org.apache.lucene.facet.taxonomy.TaxonomyFacetSumIntAssociations;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

abstract class FacetsBuilder {

	protected final QueryContextImpl queryContext;
	protected final String sortedSetFacetField;
	private final LinkedHashMap<String, FacetDefinition> facetsDef;
	private final Query searchQuery;
	private final TimeTracker timeTracker;

	public final static int DEFAULT_TOP = 10;

	final LinkedHashMap<String, Map<String, Number>> results = new LinkedHashMap<>();

	private FacetsBuilder(final QueryContextImpl queryContext, final LinkedHashMap<String, FacetDefinition> facetsDef,
			final Query searchQuery, final TimeTracker timeTracker) {
		this.facetsDef = facetsDef;
		this.queryContext = queryContext;
		this.sortedSetFacetField = queryContext.fieldMap.getSortedSetFacetField();
		this.searchQuery = searchQuery;
		this.timeTracker = timeTracker;
	}

	final FacetsBuilder build() throws IOException, ReflectiveOperationException, ParseException, QueryNodeException {
		for (Map.Entry<String, FacetDefinition> entry : facetsDef.entrySet()) {
			final String dim = entry.getKey();
			final FacetDefinition facet = entry.getValue();
			final FacetBuilder facetBuilder = new FacetBuilder(facet);
			final boolean isQueries = MapUtils.isNotEmpty(facet.queries);
			final boolean isSpecificValues = CollectionUtils.isNotEmpty(facet.specific_values);
			final Integer top = facet.top != null ? facet.top : (isQueries || isSpecificValues) ? null : DEFAULT_TOP;
			if (isSpecificValues || top != null)
				buildFacetState(dim, top, facet.specific_values, facetBuilder);
			if (isQueries)
				buildFacetQueries(facet.queries, facetBuilder);
			results.put(dim, facetBuilder.build());
		}

		if (timeTracker != null)
			timeTracker.next("facet_count");
		return this;
	}

	protected abstract Facets getFacets(final String dim) throws IOException;

	private void buildFacetState(final String dim, final Integer top, final Set<String[]> specificValues,
			final FacetBuilder facetBuilder) throws IOException {
		final Facets facets = getFacets(dim);
		if (facets == null)
			return;
		if (top != null && top > 0) {
			final FacetResult facetResult = facets.getTopChildren(top, dim);
			if (facetResult != null && facetResult.labelValues != null)
				for (LabelAndValue lv : facetResult.labelValues)
					facetBuilder.put(lv);
		}
		if (specificValues != null) {
			for (String[] path : specificValues) {
				final Number count = facets.getSpecificValue(dim, path);
				facetBuilder.put(new LabelAndValue(StringUtils.join(path, '/'),
						count == null || count.longValue() <= 0 ? 0 : count));
			}
		}
	}

	private void buildFacetQueries(final LinkedHashMap<String, AbstractQuery> queries, final FacetBuilder facetBuilder)
			throws IOException, ParseException, QueryNodeException, ReflectiveOperationException {
		final FunctionUtils.BiConsumerEx4<String, AbstractQuery, IOException, ParseException, QueryNodeException, ReflectiveOperationException>
				consumer = (name, facetQuery) -> {
			final BooleanQuery.Builder builder = new BooleanQuery.Builder();
			builder.add(searchQuery, BooleanClause.Occur.FILTER);
			builder.add(facetQuery.getQuery(queryContext), BooleanClause.Occur.FILTER);
			facetBuilder.put(new LabelAndValue(name, queryContext.indexSearcher.count(builder.build())));
		};
		FunctionUtils.forEachEx4(queries, consumer);
	}

	static Set<String> getFields(LinkedHashMap<String, FacetDefinition> facets) {
		if (facets == null || facets.isEmpty())
			return null;
		final LinkedHashSet<String> fields = new LinkedHashSet<>();
		facets.forEach((field, facetDefinition) -> {
			if (facetDefinition.queries == null)
				fields.add(field);
		});
		return fields;
	}

	static class WithCollectors extends FacetsBuilder {

		private final SortedSetDocValuesFacetCounts sortedSetCounts;
		private final FastTaxonomyFacetCounts taxonomyCounts;
		private final TaxonomyFacetSumFloatAssociations floatTaxonomyCounts;
		private final TaxonomyFacetSumIntAssociations intTaxonomyCounts;
		private final FacetsConfig facetsConfig;

		WithCollectors(final QueryContextImpl queryContext, final FacetsConfig facetsConfig,
				final LinkedHashMap<String, FacetDefinition> facetsDef, final Query searchQuery,
				final TimeTracker timeTracker, final FacetsCollector facetsCollector)
				throws IOException, ParseException, ReflectiveOperationException, QueryNodeException {
			super(queryContext, facetsDef, searchQuery, timeTracker);
			this.facetsConfig = facetsConfig;
			int facetFlag = checkFacetTypeFlags(facetsConfig, facetsDef);
			this.sortedSetCounts = queryContext.docValueReaderState == null ?
					null :
					(facetFlag & FACET_IS_SORTED) == FACET_IS_SORTED ?
							new SortedSetDocValuesFacetCounts(queryContext.docValueReaderState, facetsCollector) :
							null;
			this.taxonomyCounts = (facetFlag & FACET_IS_TAXO) == FACET_IS_TAXO ?
					new FastTaxonomyFacetCounts(queryContext.taxonomyReader, facetsConfig, facetsCollector) :
					null;
			this.floatTaxonomyCounts = (facetFlag & FACET_IS_TAXO_FLOAT) == FACET_IS_TAXO_FLOAT ?
					new TaxonomyFacetSumFloatAssociations(FieldDefinition.TAXONOMY_FLOAT_ASSOC_FACET_FIELD,
							queryContext.taxonomyReader, facetsConfig, facetsCollector) :
					null;
			this.intTaxonomyCounts = (facetFlag & FACET_IS_TAXO_INT) == FACET_IS_TAXO_INT ?
					new TaxonomyFacetSumIntAssociations(FieldDefinition.TAXONOMY_INT_ASSOC_FACET_FIELD,
							queryContext.taxonomyReader, facetsConfig, facetsCollector) :
					null;
		}

		private static int FACET_IS_SORTED = 1;
		private static int FACET_IS_TAXO = 2;
		private static int FACET_IS_TAXO_INT = 4;
		private static int FACET_IS_TAXO_FLOAT = 8;

		private int checkFacetTypeFlags(final FacetsConfig facetsConfig,
				final LinkedHashMap<String, FacetDefinition> facetsDef) {
			int flag = 0;
			for (String dimName : facetsDef.keySet()) {
				final String indexField = facetsConfig.getDimConfig(dimName).indexFieldName;
				if (indexField == null)
					continue;
				if (indexField.equals(sortedSetFacetField)) {
					flag = flag | FACET_IS_SORTED;
				} else {
					switch (indexField) {
					case FieldDefinition.TAXONOMY_FACET_FIELD:
						flag = flag | FACET_IS_TAXO;
						break;
					case FieldDefinition.TAXONOMY_INT_ASSOC_FACET_FIELD:
						flag = flag | FACET_IS_TAXO_INT;
						break;
					case FieldDefinition.TAXONOMY_FLOAT_ASSOC_FACET_FIELD:
						flag = flag | FACET_IS_TAXO_FLOAT;
						break;
					}
				}
			}
			return flag;
		}

		@Override
		final protected Facets getFacets(final String dim) throws IOException {
			final String indexFieldName = facetsConfig.getDimConfig(dim).indexFieldName;
			if (indexFieldName == null)
				return null;
			if (indexFieldName.equals(sortedSetFacetField)) {
				if (queryContext.docValueReaderState != null)
					if (queryContext.docValueReaderState.getOrdRange(dim) != null)
						return sortedSetCounts;
			} else {
				switch (indexFieldName) {
				case FieldDefinition.TAXONOMY_FACET_FIELD:
					return taxonomyCounts;
				case FieldDefinition.TAXONOMY_INT_ASSOC_FACET_FIELD:
					return intTaxonomyCounts;
				case FieldDefinition.TAXONOMY_FLOAT_ASSOC_FACET_FIELD:
					return floatTaxonomyCounts;
				}
			}
			return null;
		}
	}

	static class WithSideways extends FacetsBuilder {

		final DrillSideways.DrillSidewaysResult results;
		private final FacetsConfig facetsConfig;

		WithSideways(final QueryContextImpl queryContext, final FacetsConfig facetsConfig,
				final LinkedHashMap<String, FacetDefinition> facetsDef, final Query searchQuery,
				final TimeTracker timeTracker, final DrillSideways.DrillSidewaysResult results)
				throws IOException, ParseException, ReflectiveOperationException, QueryNodeException {
			super(queryContext, facetsDef, searchQuery, timeTracker);
			this.facetsConfig = facetsConfig;
			this.results = results;
		}

		@Override
		final protected Facets getFacets(final String dim) throws IOException {
			if (sortedSetFacetField.equals(facetsConfig.getDimConfig(dim).indexFieldName)) {
				if (queryContext.docValueReaderState == null)
					return null;
				if (queryContext.docValueReaderState.getOrdRange(dim) == null)
					return null;
			}
			return results.facets;
		}
	}
}
