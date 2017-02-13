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
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.Objects;

public class FacetPathQuery extends AbstractQuery {

	final public String dimension;
	final public String[] path;

	public FacetPathQuery() {
		dimension = null;
		path = null;
	}

	public FacetPathQuery(final String dimension, final String... path) {
		this.dimension = dimension;
		this.path = path;
	}

	@Override
	final public Query getQuery(final QueryContext queryContext) throws IOException {
		Objects.requireNonNull(dimension, "The dimension is missing");
		final String indexFieldName =
				queryContext.fieldMap.getFacetsConfig(dimension).getDimConfig(dimension).indexFieldName;
		final Term term = new Term(indexFieldName, FacetsConfig.pathToString(dimension, path));
		return new org.apache.lucene.search.TermQuery(term);
	}
}
