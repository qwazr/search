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
package com.qwazr.search.index;

import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.utils.json.AbstractStreamingOutput;
import com.qwazr.utils.json.client.JsonMultiClientAbstract;
import com.qwazr.utils.server.RemoteService;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.*;

public class IndexMultiClient extends JsonMultiClientAbstract<IndexSingleClient> implements IndexServiceInterface {

	public IndexMultiClient(RemoteService... remotes) {
		super(new IndexSingleClient[remotes.length], remotes);
	}

	@Override
	protected IndexSingleClient newClient(RemoteService remote) {
		return new IndexSingleClient(remote);
	}

	@Override
	public SchemaSettingsDefinition createUpdateSchema(String schema_name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SchemaSettingsDefinition createUpdateSchema(String schema_name, SchemaSettingsDefinition settings) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response deleteSchema(String schema_name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getSchemas() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getIndexes(String schema_name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IndexStatus createUpdateIndex(String schema_name, String index_name) {
		return null;
	}

	@Override
	public IndexStatus createUpdateIndex(String schema_name, String index_name, IndexSettingsDefinition settings) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedHashMap<String, FieldDefinition> getFields(String schema_name, String index_name) {
		return null;
	}

	@Override
	public LinkedHashMap<String, FieldDefinition> setFields(String schema_name, String index_name,
			LinkedHashMap<String, FieldDefinition> fields) {
		return null;
	}

	@Override
	public List<TermDefinition> doAnalyzeQuery(String schema_name, String index_name, String field_name, String text) {
		return null;
	}

	@Override
	public List<TermDefinition> doAnalyzeIndex(String schema_name, String index_name, String field_name, String text) {
		return null;
	}

	@Override
	public List<TermEnumDefinition> doExtractTerms(String schema_name, String index_name, String field_name,
			Integer start, Integer rows) {
		return null;
	}

	@Override
	public List<TermEnumDefinition> doExtractTerms(String schema_name, String index_name, String field_name,
			String prefix, Integer start, Integer rows) {
		return null;
	}

	@Override
	public FieldDefinition getField(String schema_name, String index_name, String field_name) {
		return null;
	}

	@Override
	public FieldDefinition setField(String schema_name, String index_name, String field_name, FieldDefinition fields) {
		return null;
	}

	@Override
	public Response deleteField(String schema_name, String index_name, String field_name) {
		return null;
	}

	@Override
	public LinkedHashMap<String, AnalyzerDefinition> getAnalyzers(String schema_name, String index_name) {
		return null;
	}

	@Override
	public AnalyzerDefinition getAnalyzer(String schema_name, String index_name, String analyzer_name) {
		return null;
	}

	@Override
	public AnalyzerDefinition setAnalyzer(String schema_name, String index_name, String analyzer_name,
			AnalyzerDefinition analyzer) {
		return null;
	}

	@Override
	public LinkedHashMap<String, AnalyzerDefinition> setAnalyzers(String schema_name, String index_name,
			LinkedHashMap<String, AnalyzerDefinition> analyzers) {
		return null;
	}

	@Override
	public Response deleteAnalyzer(String schema_name, String index_name, String analyzer_name) {
		return null;
	}

	@Override
	public List<TermDefinition> testAnalyzer(String schema_name, String index_name, String analyzer_name, String text) {
		return null;
	}

	@Override
	public String testAnalyzerDot(String schema_name, String index_name, String analyzer_name, String text) {
		return null;
	}

	@Override
	public IndexStatus getIndex(String schema_name, String index_name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response deleteIndex(String schema_name, String index_name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BackupStatus doBackup(String schema_name, String index_name, Integer keep_last_count) {
		return null;
	}

	@Override
	public List<BackupStatus> getBackups(String schema_name, String index_name) {
		return null;
	}

	@Override
	public AbstractStreamingOutput replicationObtain(String schema_name, String index_name, String sessionID,
			String source, String fileName) {
		return null;
	}

	@Override
	public Response replicationRelease(String schema_name, String index_name, String sessionID) {
		return null;
	}

	@Override
	public AbstractStreamingOutput replicationUpdate(String schema_name, String index_name, String current_version) {
		return null;
	}

	@Override
	public Response replicationCheck(String schema_name, String index_name) {
		return null;
	}

	@Override
	public LinkedHashMap<String, IndexInstance.ResourceInfo> getResources(String schema_name, String index_name) {
		return null;
	}

	@Override
	public AbstractStreamingOutput getResource(String schema_name, String index_name, String resourceName) {
		return null;
	}

	@Override
	public Response postResource(String schema_name, String index_name, String resourceName, long lastModified,
			InputStream input) {
		return null;
	}

	@Override
	public Response deleteResource(String schema_name, String index_name, String resourceName) {
		return null;
	}

	@Override
	public Integer postMappedDocument(String schema_name, String index_name, Map<String, Object> document) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer postMappedDocuments(String schema_name, String index_name,
			Collection<Map<String, Object>> documents) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer updateMappedDocValues(String schema_name, String index_name, Map<String, Object> document) {
		return null;
	}

	@Override
	public Integer updateMappedDocsValues(String schema_name, String index_name,
			Collection<Map<String, Object>> documents) {
		return null;
	}

	@Override
	public Response deleteAll(String schema_name, String index_name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<LinkedHashMap<String, Object>> getDocuments(String schema_name, String index_name, Integer start,
			Integer rows) {
		return null;
	}

	@Override
	public LinkedHashMap<String, Object> getDocument(String schema_name, String index_name, String doc_id) {
		return null;
	}

	@Override
	public ResultDefinition.WithMap searchQuery(String schema_name, String index_name, QueryDefinition query,
			Boolean delete) {
		// TODO Auto-generated method stub
		return null;
	}

}
