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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.qwazr.search.index.QueryContext;
import org.apache.lucene.facet.DrillDownQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.DocValuesRewriteMethod;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;

import java.io.IOException;

public abstract class AbstractMultiTermQuery extends AbstractQuery {

	private final boolean rewriteDocValue;

	protected AbstractMultiTermQuery(Float boost, boolean rewriteDocValue) {
		super(boost);
		this.rewriteDocValue = rewriteDocValue;
	}

	protected  Query getQuery(QueryContext queryContext)
					throws IOException, ParseException, QueryNodeException, ReflectiveOperationException {
		MultiTermQuery query = super.getQuery(queryContext);
		if (rewriteDocValue)
			query.setRewriteMethod(new DocValuesRewriteMethod());
		return query;
	}

}
