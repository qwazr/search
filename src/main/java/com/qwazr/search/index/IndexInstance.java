/**
 * Copyright 2015 OpenSearchServer Inc.
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
package com.qwazr.search.index;

import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qwazr.search.SearchServer;
import com.qwazr.search.index.osse.OsseDocCursor;
import com.qwazr.search.index.osse.OsseErrorHandler;
import com.qwazr.search.index.osse.OsseException;
import com.qwazr.search.index.osse.OsseIndex;
import com.qwazr.search.index.osse.OsseIndex.FieldInfo;
import com.qwazr.search.index.osse.OsseTransaction;
import com.qwazr.search.index.osse.query.OsseAbstractQuery;
import com.qwazr.search.memory.MemoryBuffer;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.server.ServerException;

public class IndexInstance implements Closeable {

	private static final Logger logger = LoggerFactory
			.getLogger(IndexInstance.class);

	private final static String INDEX_DATA = "data";

	private final MemoryBuffer memoryBuffer;
	private final File indexDirectory;
	private final File dataDirectory;
	private final OsseIndex indexEngine;
	private Map<String, FieldInfo> fieldMap = new TreeMap<String, FieldInfo>();

	/**
	 * Create an index directory
	 * 
	 * @param indexDirectory
	 *            the root location of the directory
	 * @throws IOException
	 * @throws OsseException
	 */
	IndexInstance(File indexDirectory, MemoryBuffer memoryBuffer)
			throws IOException, OsseException {
		this.memoryBuffer = memoryBuffer;
		this.indexDirectory = indexDirectory;
		this.dataDirectory = new File(indexDirectory, INDEX_DATA);
		checkDirectories();
		File[] fileList = dataDirectory.listFiles((FileFilter) FileFilterUtils
				.fileFileFilter());
		boolean bCreate = fileList == null || fileList.length == 0;
		OsseErrorHandler errorHandler = new OsseErrorHandler();
		try {
			indexEngine = new OsseIndex(memoryBuffer, dataDirectory,
					errorHandler, bCreate);
			reloadFieldMap(errorHandler);
		} finally {
			errorHandler.close();
		}
	}

	private void reloadFieldMap(OsseErrorHandler errorHandler)
			throws OsseException {
		OsseErrorHandler localErrorHandler = null;
		if (errorHandler == null)
			localErrorHandler = errorHandler = new OsseErrorHandler();
		try {
			fieldMap = indexEngine.getFieldMap(errorHandler);
		} finally {
			if (localErrorHandler != null)
				IOUtils.closeQuietly(localErrorHandler);
			if (fieldMap == null)
				fieldMap = Collections.emptyMap();
		}
	}

	private void checkDirectories() throws IOException {
		SearchServer.checkDirectoryExists(indexDirectory);
		SearchServer.checkDirectoryExists(dataDirectory);
	}

	@Override
	public void close() {
		OsseErrorHandler errorHandler = null;
		try {
			errorHandler = new OsseErrorHandler();
			if (indexEngine != null) {
				try {
					indexEngine.close(errorHandler);
				} catch (OsseException e) {
					logger.warn(e.getMessage(), e);
				}
			}
		} catch (OsseException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (errorHandler != null)
				errorHandler.close();
		}
	}

	/**
	 * Delete the index. The directory is deleted from the local file system.
	 */
	void delete() {
		if (indexDirectory.exists())
			FileUtils.deleteQuietly(indexDirectory);
	}

	void createFields(Map<String, FieldDefinition> fields) throws OsseException {
		OsseTransaction transaction = new OsseTransaction(indexEngine,
				memoryBuffer, null, 0);
		try {
			for (Map.Entry<String, FieldDefinition> entry : fields.entrySet())
				transaction.createField(entry.getKey(), entry.getValue()
						.getFlags());
			transaction.commit();
			reloadFieldMap(null);
		} finally {
			IOUtils.closeQuietly(transaction);
		}
	}

	void createField(String fieldName, FieldDefinition fieldDefinition)
			throws OsseException, ServerException {
		if (fieldMap.containsKey(fieldName))
			throw new ServerException(Status.CONFLICT,
					"The field already exists : " + fieldName);
		OsseTransaction transaction = new OsseTransaction(indexEngine,
				memoryBuffer, null, 0);
		transaction.createField(fieldName, fieldDefinition.getFlags());
		transaction.commit();
		reloadFieldMap(null);
		try {
		} finally {
			IOUtils.closeQuietly(transaction);
		}
	}

	FieldInfo getField(String fieldName) throws ServerException {
		FieldInfo fieldInfo = fieldMap.get(fieldName);
		if (fieldInfo == null)
			throw new ServerException(Status.NOT_FOUND, "Field not found : "
					+ fieldName);
		return fieldInfo;
	}

	void deleteField(String fieldName) throws OsseException, ServerException {
		getField(fieldName);
		OsseTransaction transaction = new OsseTransaction(indexEngine,
				memoryBuffer, fieldMap, 1);
		try {
			transaction.deleteField(fieldName);
			transaction.commit();
			reloadFieldMap(null);
		} finally {
			IOUtils.closeQuietly(transaction);
		}
	}

	IndexStatus getStatus() throws OsseException {
		OsseErrorHandler errorHandler = new OsseErrorHandler();
		try {
			long numberOfDocs = indexEngine.getNumberOfDocs(errorHandler);
			TreeMap<String, FieldDefinition> map = new TreeMap<String, FieldDefinition>();
			for (Map.Entry<String, FieldInfo> entry : fieldMap.entrySet())
				map.put(entry.getKey(), entry.getValue().fieldDefinition);
			return new IndexStatus(numberOfDocs, map);
		} finally {
			IOUtils.closeQuietly(errorHandler);
		}
	}

	void postDocuments(List<Map<String, FieldContent>> documents)
			throws OsseException, IOException {
		OsseTransaction transaction = new OsseTransaction(indexEngine,
				memoryBuffer, fieldMap, documents.size());
		try {
			for (Map<String, FieldContent> document : documents) {
				int docId = transaction.newDocumentId();
				for (Map.Entry<String, FieldContent> entry : document
						.entrySet())
					transaction.addTerms(docId, entry.getKey(),
							entry.getValue());
			}
			transaction.commit();
		} finally {
			IOUtils.closeQuietly(transaction);
		}
	}

	private static class FacetCount {
		private long value = 1;
	}

	private static class QueryField {

		private final int id;
		private final String name;
		private final boolean returned;
		private Map<String, FacetCount> facets;

		private QueryField(int id, String name, boolean returned) {
			this.id = id;
			this.name = name;
			this.returned = returned;
			this.facets = null;
		}

		private void activeFacets() {
			facets = new HashMap<String, FacetCount>();
		}

		private void addTermFacet(List<String> terms) {
			for (String term : terms) {
				FacetCount facetCount = facets.get(term);
				if (facetCount == null)
					facets.put(term, new FacetCount());
				else
					facetCount.value++;
			}
		}
	}

	private Map<String, Map<String, Long>> getFacetsMap(QueryField[] queryFields) {
		if (queryFields == null)
			return null;
		Map<String, Map<String, Long>> facetsMap = new TreeMap<String, Map<String, Long>>();
		for (QueryField queryField : queryFields) {
			if (queryField.facets == null)
				continue;
			Map<String, Long> facetMap = new TreeMap<String, Long>();
			for (Map.Entry<String, FacetCount> entry : queryField.facets
					.entrySet())
				facetMap.put(entry.getKey(), entry.getValue().value);
			facetsMap.put(queryField.name, facetMap);
		}
		return facetsMap;
	}

	private QueryField[] getQueryFields(QueryDefinition query)
			throws OsseException {

		if (query == null
				|| (query.facet_fields == null || query.facet_fields.isEmpty())
				&& (query.returned_fields == null || query.returned_fields
						.isEmpty()))
			return null;
		Map<String, QueryField> map = new LinkedHashMap<String, QueryField>();

		// Check returned fields
		if (query.returned_fields != null) {
			for (String field : query.returned_fields) {
				FieldInfo fieldInfo = fieldMap.get(field);
				if (fieldInfo == null)
					throw new OsseException("Unknown returned field: " + field,
							null);
				map.put(field, new QueryField(fieldInfo.id, field, true));
			}
		}

		// Check facet fields
		if (query.facet_fields != null) {
			for (String field : query.facet_fields) {
				FieldInfo fieldInfo = fieldMap.get(field);
				if (fieldInfo == null)
					throw new OsseException("Unknown facet field: " + field,
							null);
				QueryField queryField = map.get(field);
				if (queryField == null) {
					queryField = new QueryField(fieldInfo.id, field, false);
					map.put(field, queryField);
				}
				queryField.activeFacets();
			}
		}
		return map.values().toArray(new QueryField[map.size()]);
	}

	long deleteQuery(OsseTransaction transaction, QueryDefinition query)
			throws OsseException {
		OsseErrorHandler errorHandler = new OsseErrorHandler();
		try {

			// Build the query
			OsseAbstractQuery osseQuery = OsseAbstractQuery.create(query.query);
			try {
				osseQuery.execute(indexEngine, fieldMap, errorHandler);
				errorHandler.checkNoError();
				return osseQuery.deleteDocuments(transaction);
			} finally {
				IOUtils.closeQuietly(osseQuery);
			}
		} finally {
			IOUtils.closeQuietly(errorHandler);
		}
	}

	ResultDefinition findDocuments(QueryDefinition query) throws OsseException,
			IOException {

		// Get the prepared fields for returned field and facets
		QueryField[] queryFields = getQueryFields(query);

		OsseErrorHandler errorHandler = new OsseErrorHandler();
		try {

			// Build the query
			OsseAbstractQuery osseQuery = OsseAbstractQuery.create(query.query);

			try {

				// Execute the query and collect the doc ids
				osseQuery.execute(indexEngine, fieldMap, errorHandler);
				errorHandler.checkNoError();
				Collection<Long> docIds = new ArrayList<Long>();
				osseQuery.collect(indexEngine, docIds);

				List<Map<String, List<String>>> documents = null;
				if (query.returned_fields != null
						&& !query.returned_fields.isEmpty())
					documents = new ArrayList<Map<String, List<String>>>();
				for (Long docId : docIds) {
					Map<String, List<String>> returnedFieldMap = null;
					if (queryFields != null) {
						if (documents != null)
							returnedFieldMap = new LinkedHashMap<String, List<String>>();
						for (QueryField queryField : queryFields) {
							OsseDocCursor docCursor = new OsseDocCursor(
									indexEngine, errorHandler);
							try {
								List<String> terms = new ArrayList<String>();
								docCursor
										.fillTerms(queryField.id, docId, terms);
								if (queryField.returned)
									returnedFieldMap
											.put(queryField.name, terms);
								if (queryField.facets != null)
									queryField.addTermFacet(terms);
							} finally {
								IOUtils.closeQuietly(docCursor);
							}
						}
					}
					if (documents != null && returnedFieldMap != null)
						documents.add(returnedFieldMap);
				}
				return new ResultDefinition(docIds.size(), documents,
						getFacetsMap(queryFields));
			} finally {
				IOUtils.closeQuietly(osseQuery);
			}
		} finally {
			IOUtils.closeQuietly(errorHandler);
		}
	}

	List<ResultDefinition> findDocuments(List<QueryDefinition> queries)
			throws OsseException, IOException {
		List<ResultDefinition> list = new ArrayList<ResultDefinition>(
				queries.size());
		for (QueryDefinition query : queries)
			list.add(findDocuments(query));
		return list;
	}

	ResultDefinition deleteDocuments(QueryDefinition query)
			throws OsseException {
		OsseTransaction transaction = new OsseTransaction(indexEngine,
				memoryBuffer, null, 0);
		try {
			long numDeleted = deleteQuery(transaction, query);
			if (numDeleted > 0)
				transaction.commit();
			return new ResultDefinition(numDeleted, null, null);
		} finally {
			IOUtils.closeQuietly(transaction);
		}
	}

	List<ResultDefinition> deleteDocuments(List<QueryDefinition> queries)
			throws OsseException {
		OsseTransaction transaction = new OsseTransaction(indexEngine,
				memoryBuffer, null, 0);
		try {
			List<ResultDefinition> list = new ArrayList<ResultDefinition>(
					queries.size());
			long totalDeleted = 0;
			for (QueryDefinition query : queries) {
				long numDeleted = deleteQuery(transaction, query);
				list.add(new ResultDefinition(numDeleted, null, null));
			}
			if (totalDeleted > 0)
				transaction.commit();
			return list;
		} finally {
			IOUtils.closeQuietly(transaction);
		}
	}
}
