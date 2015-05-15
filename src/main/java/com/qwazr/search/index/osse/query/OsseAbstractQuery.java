/**
 * Copyright 2014-2015 Emmanuel Keller / QWAZR
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

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import com.qwazr.search.index.QueryDefinition.AbstractQuery;
import com.qwazr.search.index.QueryDefinition.GroupQuery;
import com.qwazr.search.index.QueryDefinition.TermQuery;
import com.qwazr.search.index.osse.OsseCursor;
import com.qwazr.search.index.osse.OsseErrorHandler;
import com.qwazr.search.index.osse.OsseException;
import com.qwazr.search.index.osse.OsseIndex;
import com.qwazr.search.index.osse.OsseIndex.FieldInfo;
import com.qwazr.search.index.osse.OsseTransaction;
import com.qwazr.utils.IOUtils;

public abstract class OsseAbstractQuery implements Closeable {

	protected OsseCursor cursor = null;

	final public static OsseAbstractQuery create(AbstractQuery query)
			throws OsseException {
		if (query instanceof TermQuery)
			return new OsseTermQuery((TermQuery) query);
		else if (query instanceof GroupQuery)
			return new OsseGroupQuery((GroupQuery) query);
		throw new OsseException("Unsupported query type: "
				+ query.getClass().getName(), null);
	}

	public abstract void execute(OsseIndex index,
			Map<String, FieldInfo> fieldMap, OsseErrorHandler error)
			throws OsseException;

	/**
	 * Collect a list of document id(s)
	 * 
	 * @param index
	 *            the index instance
	 * @param collector
	 *            a collection to fill
	 * @throws IOException
	 *             if any I/O error occurs
	 * @throws OsseException
	 *             if any error occurs
	 */
	public void collect(OsseIndex index, Collection<Long> collector)
			throws IOException, OsseException {
		if (cursor == null)
			return;
		cursor.collect(index.getMemoryBuffer(), collector);
	}

	/**
	 * Delete the documents matching this query
	 * 
	 * @param transaction
	 *            the current transaction
	 * @return the number of document deleted
	 * @throws OsseException
	 *             if any error occurs
	 */
	public long deleteDocuments(OsseTransaction transaction)
			throws OsseException {
		if (cursor == null)
			return 0;
		long nDocs = cursor.getNumberOfDocs();
		if (nDocs == 0)
			return 0;
		transaction.deleteDocumentsByCursor(cursor);
		return nDocs;
	}

	@Override
	public void close() {
		if (cursor != null)
			IOUtils.close(cursor);
	}

}