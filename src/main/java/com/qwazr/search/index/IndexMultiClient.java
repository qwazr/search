/**
 * Copyright 2015 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.index;

import com.qwazr.utils.json.client.JsonMultiClientAbstract;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IndexMultiClient extends
		JsonMultiClientAbstract<String, IndexSingleClient> implements
		IndexServiceInterface {

	public IndexMultiClient(String[] urls, Integer msTimeOut)
			throws URISyntaxException {
		super(null, new IndexSingleClient[urls.length], urls, msTimeOut);
	}

	@Override
	protected IndexSingleClient newClient(String url, Integer msTimeOut)
			throws URISyntaxException {
		return new IndexSingleClient(url, msTimeOut);
	}

	@Override
	public Set<String> getIndexes(Boolean local) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IndexStatus createUpdateIndex(String index_name, Boolean local, Map<String, FieldDefinition> fields) {
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
	public Response postDocument(String index_name, Map<String, Object> document) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response postDocuments(String index_name, List<Map<String, Object>> documents) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResultDefinition searchQuery(@PathParam("index_name") String index_name, QueryDefinition query) {
		// TODO Auto-generated method stub
		return null;
	}


}
