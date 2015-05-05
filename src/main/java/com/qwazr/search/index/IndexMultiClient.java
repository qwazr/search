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

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response;

import com.qwazr.utils.json.client.JsonMultiClientAbstract;

public class IndexMultiClient extends
		JsonMultiClientAbstract<IndexSingleClient> implements
		IndexServiceInterface {

	public IndexMultiClient(Collection<String> urls, int msTimeOut)
			throws URISyntaxException {
		super(null, new IndexSingleClient[urls.size()], urls, msTimeOut);
	}

	@Override
	protected IndexSingleClient newClient(String url, int msTimeOut)
			throws URISyntaxException {
		return new IndexSingleClient(url, msTimeOut);
	}

	@Override
	public Set<String> getIndexes(Boolean local) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IndexStatus createIndex(String index_name, Boolean local,
			Map<String, FieldDefinition> fields) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IndexStatus getIndex(String index_name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response deleteIndex(String index_name, Boolean local) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FieldDefinition createField(String index_name, String field_name,
			FieldDefinition field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, FieldDefinition> createFields(String index_name,
			Map<String, FieldDefinition> fields) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response deleteField(String index_name, String field_name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response postDocuments(String index_name,
			List<Map<String, FieldContent>> documents) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ResultDefinition> findDocuments(String index_name,
			List<QueryDefinition> queries, Boolean delete) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResultDefinition findDocuments(String index_name,
			QueryDefinition query, Boolean delete) {
		// TODO Auto-generated method stub
		return null;
	}

}
