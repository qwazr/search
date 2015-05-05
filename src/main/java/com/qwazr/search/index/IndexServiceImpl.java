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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qwazr.search.index.osse.OsseException;
import com.qwazr.search.index.osse.OsseIndex;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.server.ServerException;

public class IndexServiceImpl implements IndexServiceInterface {

	private static final Logger logger = LoggerFactory
			.getLogger(IndexServiceImpl.class);

	@Override
	public Set<String> getIndexes(Boolean local) {
		return IndexManager.INSTANCE.nameSet();
		// TODO Local
	}

	@Override
	public IndexStatus createIndex(String index_name, Boolean local,
			Map<String, FieldDefinition> fields) {
		try {
			return IndexManager.INSTANCE.create(index_name, fields);
		} catch (ServerException | IOException | OsseException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
		// TODO Local
	}

	@Override
	public IndexStatus getIndex(String index_name) {
		try {
			return IndexManager.INSTANCE.get(index_name).getStatus();
		} catch (ServerException | OsseException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public Response deleteIndex(String index_name, Boolean local) {
		try {
			IndexManager.INSTANCE.delete(index_name);
			return Response.ok().build();
		} catch (ServerException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
		// TODO Local
	}

	@Override
	public FieldDefinition createField(String index_name, String field_name,
			FieldDefinition field) {
		try {
			if (StringUtils.isEmpty(field_name))
				throw new ServerException(Status.NOT_ACCEPTABLE,
						"No field name");
			if (field == null)
				throw new ServerException(Status.NOT_ACCEPTABLE,
						"No field definition");
			IndexInstance index = IndexManager.INSTANCE.get(index_name);
			index.createField(field_name, field);
			return index.getField(field_name).fieldDefinition;
		} catch (OsseException | ServerException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public Map<String, FieldDefinition> createFields(String index_name,
			Map<String, FieldDefinition> fields) {
		try {
			if (fields == null || fields.isEmpty())
				throw new ServerException(Status.NOT_ACCEPTABLE, "No fields");
			IndexInstance index = IndexManager.INSTANCE.get(index_name);
			index.createFields(fields);
			return index.getStatus().fields;
		} catch (OsseException | ServerException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public Response deleteField(String index_name, String field_name) {
		try {
			if (StringUtils.isEmpty(field_name))
				throw new ServerException(Status.NOT_ACCEPTABLE,
						"No field name");
			IndexInstance index = IndexManager.INSTANCE.get(index_name);
			index.deleteField(field_name);
			return Response.ok().build();
		} catch (OsseException | ServerException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getTextException(e);
		}
	}

	@Override
	public Response postDocuments(String index_name,
			List<Map<String, FieldContent>> documents) {
		try {
			if (documents == null || documents.isEmpty())
				return Response.notModified().build();
			IndexManager.INSTANCE.get(index_name).postDocuments(documents);
			return Response.ok().build();
		} catch (OsseException | ServerException | IOException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getTextException(e);
		}
	}

	@Override
	public List<ResultDefinition> findDocuments(String index_name,
			List<QueryDefinition> queries, Boolean delete) {
		try {
			IndexInstance indexInstance = IndexManager.INSTANCE.get(index_name);
			if (delete != null && delete)
				return indexInstance.deleteDocuments(queries);
			else
				return indexInstance.findDocuments(queries);
		} catch (ServerException | OsseException | IOException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public ResultDefinition findDocuments(String index_name,
			QueryDefinition query, Boolean delete) {
		try {
			IndexInstance indexInstance = IndexManager.INSTANCE.get(index_name);
			if (delete != null && delete)
				return indexInstance.deleteDocuments(query);
			else
				return indexInstance.findDocuments(query);
		} catch (ServerException | OsseException | IOException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public String getVersion() {
		return OsseIndex.LIB.OSSCLib_GetVersionInfoText();
	}

}
