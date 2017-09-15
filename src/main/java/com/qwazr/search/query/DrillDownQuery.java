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
 */
package com.qwazr.search.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.index.FieldMap;
import com.qwazr.search.index.QueryContext;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DrillDownQuery extends AbstractQuery {

	final public AbstractQuery baseQuery;
	final public List<LinkedHashMap<String, String[]>> dimPath;
	final public Map<String, String> genericFieldNames;
	final public Boolean useDrillSideways;

	@JsonCreator
	public DrillDownQuery(@JsonProperty("baseQuery") final AbstractQuery baseQuery,
			@JsonProperty("useDrillSideways") final boolean useDrillSideways,
			@JsonProperty("dimPath") final List<LinkedHashMap<String, String[]>> dimPath,
			@JsonProperty("genericFieldNames") final Map<String, String> genericFieldNames) {
		this.baseQuery = baseQuery;
		this.useDrillSideways = useDrillSideways;
		this.dimPath = dimPath == null ? new ArrayList<>() : dimPath;
		this.genericFieldNames = genericFieldNames == null ? new LinkedHashMap<>() : genericFieldNames;
	}

	public DrillDownQuery(final AbstractQuery baseQuery, final boolean useDrillSideways) {
		this(baseQuery, useDrillSideways, null, null);
	}

	public DrillDownQuery dynamicFilter(final String genericFieldName, final String dim, final String... path) {
		final LinkedHashMap<String, String[]> map = new LinkedHashMap<>();
		map.put(dim, path);
		dimPath.add(map);
		if (genericFieldName != null)
			genericFieldNames.put(dim, genericFieldName);
		return this;
	}

	public DrillDownQuery filter(final String dim, final String... path) {
		return dynamicFilter(null, dim, path);
	}

	@Override
	final public org.apache.lucene.facet.DrillDownQuery getQuery(final QueryContext queryContext)
			throws IOException, ParseException, ReflectiveOperationException, QueryNodeException {

		final org.apache.lucene.facet.DrillDownQuery drillDownQuery;
		final FieldMap fieldMap = queryContext.getFieldMap();

		final Map<String, String> dimensions = new HashMap<>();
		final Map<String, String> resolvedDimensions = new HashMap<>();
		dimPath.forEach(map -> map.keySet().forEach(concreteField -> {
			final String genericField = genericFieldNames.getOrDefault(concreteField, concreteField);
			dimensions.put(concreteField, genericField);
			if (fieldMap != null)
				resolvedDimensions.put(concreteField, fieldMap.resolveQueryFieldName(genericField, concreteField));
		}));

		final FacetsConfig facetsConfig = queryContext.getFacetsConfig(resolvedDimensions);
		Objects.requireNonNull(facetsConfig, "FacetsConfig is null");
		if (baseQuery == null)
			drillDownQuery = new org.apache.lucene.facet.DrillDownQuery(facetsConfig);
		else
			drillDownQuery = new org.apache.lucene.facet.DrillDownQuery(facetsConfig, baseQuery.getQuery(queryContext));

		dimPath.forEach(dimPath -> dimPath.forEach(
				(dim, path) -> drillDownQuery.add(resolvedDimensions.getOrDefault(dim, dim), path)));

		return drillDownQuery;
	}

}
