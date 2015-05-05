/**
 * Copyright 2014-2015 OpenSearchServer Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.index.osse.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.jaeksoft.searchlib.index.osse.api.OsseJNILibrary;
import com.qwazr.search.index.QueryDefinition.AbstractQuery;
import com.qwazr.search.index.QueryDefinition.GroupQuery;
import com.qwazr.search.index.osse.OsseCursor;
import com.qwazr.search.index.osse.OsseErrorHandler;
import com.qwazr.search.index.osse.OsseException;
import com.qwazr.search.index.osse.OsseIndex;
import com.qwazr.search.index.osse.OsseIndex.FieldInfo;
import com.qwazr.utils.IOUtils;

public class OsseGroupQuery extends OsseAbstractQuery {

	private class OsseBooleanClause {

		private final OsseAbstractQuery query;

		private OsseBooleanClause(AbstractQuery queryDef) throws OsseException {
			query = OsseAbstractQuery.create(queryDef);
		}

		public OsseCursor execute(OsseIndex index,
				Map<String, FieldInfo> fieldMap, OsseErrorHandler error)
				throws OsseException {
			query.execute(index, fieldMap, error);
			return query.cursor;
		}

	}

	private final List<OsseBooleanClause> clauses;
	private final int operator;

	OsseGroupQuery(GroupQuery groupQuery) throws OsseException {
		if (groupQuery.operator != null) {
			switch (groupQuery.operator) {
			case or:
				operator = OsseJNILibrary.OSSCLIB_QCURSOR_UI32BOP_OR;
				break;
			default:
			case and:
				operator = OsseJNILibrary.OSSCLIB_QCURSOR_UI32BOP_AND;
				break;
			}
		} else
			operator = OsseJNILibrary.OSSCLIB_QCURSOR_UI32BOP_AND;
		clauses = new ArrayList<OsseBooleanClause>(groupQuery.queries.size());
		for (AbstractQuery query : groupQuery.queries)
			clauses.add(new OsseBooleanClause(query));
	}

	@Override
	public void execute(OsseIndex index, Map<String, FieldInfo> fieldMap,
			OsseErrorHandler error) throws OsseException {
		List<OsseCursor> cursors = new ArrayList<OsseCursor>(clauses.size());
		for (OsseBooleanClause clause : clauses) {
			OsseCursor curs = clause.execute(index, fieldMap, error);
			cursors.add(curs);
		}
		if (cursors.size() == 0)
			return;
		cursor = new OsseCursor(index, error, operator, cursors);
	}

	@Override
	public void close() {
		super.close();
		for (OsseBooleanClause clause : clauses)
			IOUtils.close(clause.query);
	}
}