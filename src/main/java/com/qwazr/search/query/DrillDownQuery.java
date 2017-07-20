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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DrillDownQuery extends AbstractQuery {

	final public AbstractQuery baseQuery;
	final public List<LinkedHashMap<String, String[]>> dimPath;
	final public Boolean useDrillSideways;

	@JsonCreator
	public DrillDownQuery(@JsonProperty("baseQuery") final AbstractQuery baseQuery,
			@JsonProperty("useDrillSideways") final boolean useDrillSideways,
			@JsonProperty("dimPath") final List<LinkedHashMap<String, String[]>> dimPath) {
		this.baseQuery = baseQuery;
		this.useDrillSideways = useDrillSideways;
		this.dimPath = dimPath == null ? new ArrayList<>() : dimPath;
	}

	public DrillDownQuery(final AbstractQuery baseQuery, final boolean useDrillSideways) {
		this(baseQuery, useDrillSideways, null);
	}

	public DrillDownQuery add(final String dim, final String... path) {
		final LinkedHashMap<String, String[]> map = new LinkedHashMap<>();
		map.put(dim, path);
		dimPath.add(map);
		return this;
	}

	@Override
	final public org.apache.lucene.facet.DrillDownQuery getQuery(final QueryContext queryContext)
			throws IOException, ParseException, ReflectiveOperationException, QueryNodeException {
		final org.apache.lucene.facet.DrillDownQuery drillDownQuery;
		final Set<String> fieldSet = new HashSet<>();
		dimPath.forEach(map -> fieldSet.addAll(map.keySet()));
		final FacetsConfig facetsConfig = queryContext.getFacetsConfig(fieldSet);
		Objects.requireNonNull(facetsConfig, "FacetsConfig is null");
		if (baseQuery == null)
			drillDownQuery = new org.apache.lucene.facet.DrillDownQuery(facetsConfig);
		else
			drillDownQuery = new org.apache.lucene.facet.DrillDownQuery(facetsConfig, baseQuery.getQuery(queryContext));
		final FieldMap fieldMap = queryContext.getFieldMap();
		dimPath.forEach(dimPath -> dimPath.forEach(
				(dim, path) -> drillDownQuery.add(fieldMap == null ? dim : fieldMap.resolveQueryFieldName(dim), path)));
		return drillDownQuery;
	}

}
