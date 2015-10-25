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

import com.qwazr.utils.json.client.JsonMultiClientAbstract;

import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class IndexMultiClient extends JsonMultiClientAbstract<String, IndexSingleClient>
				implements IndexServiceInterface {

	public IndexMultiClient(ExecutorService executor, String[] urls, Integer msTimeOut) throws URISyntaxException {
		super(executor, new IndexSingleClient[urls.length], urls, msTimeOut);
	}

	@Override
	protected IndexSingleClient newClient(String url, Integer msTimeOut) throws URISyntaxException {
		return new IndexSingleClient(url, msTimeOut);
	}

	@Override
	public Response createUpdateSchema(String schema_name, Boolean local) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response deleteSchema(String schema_name, Boolean local) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getSchemas(Boolean local) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResultDefinition searchQuery(String schema_name, QueryDefinition query, Boolean local) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getIndexes(String schema_name, Boolean local) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IndexStatus createUpdateIndex(String schema_name, String index_name, Boolean local,
					Map<String, FieldDefinition> fields) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IndexStatus getIndex(String schema_name, String index_name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response deleteIndex(String schema_name, String index_name, Boolean local) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SettingsDefinition getSettings(String index_name) {
		return null;
	}

	@Override
	public SettingsDefinition setSettings(String index_name, SettingsDefinition settings) {
		return null;
	}

	@Override
	public Response postDocument(String schema_name, String index_name, Map<String, Object> document) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BackupStatus doBackup(String schema_name, String index_name) {
		return null;
	}

	@Override
	public List<BackupStatus> getBackups(String schema_name, String index_name) {
		return null;
	}

	@Override
	public Response purgeBackups(String schema_name, String index_name, Integer keep_last_count) {
		return null;
	}

	@Override
	public Response postDocuments(String schema_name, String index_name, List<Map<String, Object>> documents) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response deleteAll(String schema_name, String index_name, Boolean local) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResultDefinition searchQuery(String schema_name, String index_name, QueryDefinition query, Boolean delete) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResultDefinition mltQuery(String schema_name, String index_name, MltQueryDefinition mltQuery) {
		// TODO Auto-generated method stub
		return null;
	}

}
