/**
 * Copyright 2015 Emmanuel Keller / QWAZR
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.index;

import com.qwazr.utils.server.ServerException;
import org.apache.lucene.queryparser.classic.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IndexServiceImpl implements IndexServiceInterface {

    private static final Logger logger = LoggerFactory.getLogger(IndexServiceImpl.class);

    @Override
    public Set<String> getIndexes(Boolean local) {
	return IndexManager.INSTANCE.nameSet();
	// TODO Local
    }

    @Override
    public IndexStatus createUpdateIndex(String index_name, Boolean local, Map<String, FieldDefinition> fields) {
	try {
	    return IndexManager.INSTANCE.createUpdate(index_name, fields);
	} catch (ServerException | IOException | InterruptedException e) {
	    logger.warn(e.getMessage(), e);
	    throw ServerException.getJsonException(e);
	}
	// TODO Local
    }

    @Override
    public IndexStatus getIndex(String index_name) {
	try {
	    return IndexManager.INSTANCE.get(index_name).getStatus();
	} catch (ServerException | IOException | InterruptedException e) {
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
    public SettingsDefinition getSettings(@PathParam("index_name") String index_name) {
	try {
	    return IndexManager.INSTANCE.get(index_name).getSettings();
	} catch (ServerException e) {
	    logger.warn(e.getMessage(), e);
	    throw ServerException.getJsonException(e);
	}
    }

    @Override
    public SettingsDefinition setSettings(@PathParam("index_name") String index_name, SettingsDefinition settings) {
	try {
	    IndexManager.INSTANCE.get(index_name).setSettings(settings);
	    return settings;
	} catch (ServerException | IOException e) {
	    logger.warn(e.getMessage(), e);
	    throw ServerException.getJsonException(e);
	}
    }

    @Override
    public Response deleteSettings(@PathParam("index_name") String index_name) {
	try {
	    IndexManager.INSTANCE.get(index_name).setSettings(null);
	    return Response.ok().build();
	} catch (ServerException | IOException e) {
	    logger.warn(e.getMessage(), e);
	    throw ServerException.getJsonException(e);
	}
    }

    @Override
    public Response postDocument(String index_name, Map<String, Object> document) {
	try {
	    if (document == null || document.isEmpty())
		return Response.notModified().build();
	    IndexManager.INSTANCE.get(index_name).postDocument(document);
	    return Response.ok().build();
	} catch (ServerException | IOException | IllegalArgumentException | InterruptedException e) {
	    logger.warn(e.getMessage(), e);
	    throw ServerException.getJsonException(e);
	}
    }

    @Override
    public Response postDocuments(String index_name, List<Map<String, Object>> documents) {
	try {
	    if (documents == null || documents.isEmpty())
		return Response.notModified().build();
	    IndexManager.INSTANCE.get(index_name).postDocuments(documents);
	    return Response.ok().build();
	} catch (ServerException | IOException | IllegalArgumentException | InterruptedException e) {
	    logger.warn(e.getMessage(), e);
	    throw ServerException.getJsonException(e);
	}
    }

    @Override
    public Response deleteAll(String index_name, Boolean local) {
	try {
	    IndexManager.INSTANCE.get(index_name).deleteAll();
	    return Response.ok().build();
	} catch (ServerException | IOException | InterruptedException e) {
	    logger.warn(e.getMessage(), e);
	    throw ServerException.getJsonException(e);
	}
    }

    @Override
    public Response deleteByQuery(String index_name, QueryDefinition query) {
	try {
	    IndexManager.INSTANCE.get(index_name).deleteByQuery(query);
	    return Response.ok().build();
	} catch (ServerException | IOException | ParseException | InterruptedException e) {
	    logger.warn(e.getMessage(), e);
	    throw ServerException.getJsonException(e);
	}
    }

    @Override
    public ResultDefinition searchQuery(String index_name, QueryDefinition query) {
	try {
	    return IndexManager.INSTANCE.get(index_name).search(query);
	} catch (ServerException | IOException | ParseException | InterruptedException e) {
	    logger.warn(e.getMessage(), e);
	    throw ServerException.getJsonException(e);
	}
    }
}
