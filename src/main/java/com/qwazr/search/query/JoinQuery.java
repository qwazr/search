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

import com.qwazr.search.index.IndexManager;
import com.qwazr.search.index.QueryContext;
import com.qwazr.utils.ArrayUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.join.JoinUtil;
import org.apache.lucene.search.join.ScoreMode;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class JoinQuery extends AbstractQuery {

	final public String from_index;
	final public String from_field;
	final public String to_field;
	final public Boolean multiple_values_per_document;
	final public ScoreMode score_mode;
	final public AbstractQuery from_query;

	public JoinQuery() {
		from_index = null;
		from_field = null;
		to_field = null;
		multiple_values_per_document = null;
		score_mode = null;
		from_query = null;
	}

	public JoinQuery(final String fromIndex, final String fromField, final String toField,
			final Boolean multipleValuesPerDocument, final ScoreMode scoreMode, final AbstractQuery fromQuery) {
		this.from_index = fromIndex;
		this.from_field = fromField;
		this.to_field = toField;
		this.multiple_values_per_document = multipleValuesPerDocument;
		this.score_mode = scoreMode;
		this.from_query = fromQuery;
	}

	@Override
	final public Query getQuery(final QueryContext queryContext)
			throws IOException, ParseException, ReflectiveOperationException, QueryNodeException {
		return queryContext.schemaInstance.get(from_index, false).createJoinQuery(this);
	}

}
