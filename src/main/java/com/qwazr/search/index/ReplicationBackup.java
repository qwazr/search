/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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

import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.replication.ReplicationProcess;
import com.qwazr.search.replication.ReplicationSession;
import com.qwazr.utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.UUID;

class ReplicationBackup extends ReplicationClient.Base {

	private final IndexInstance indexInstance;
	private final Path backupIndexDirectory;

	ReplicationBackup(final IndexInstance indexInstance, final Path backupIndexDirectory) {
		super(null);
		this.indexInstance = indexInstance;
		this.backupIndexDirectory = backupIndexDirectory;
	}

	/**
	 * Backup the index UUID and the optional Master UUID
	 *
	 * @param indexUuid the index UUID
	 * @throws IOException
	 */
	private void backupIndexAndMasterUuid(final IndexFileSet indexFileSet, UUID indexUuid) throws IOException {

		// Copy the UUID
		IOUtils.writeStringAsFile(indexUuid.toString(), backupIndexDirectory.resolve(IndexFileSet.UUID_FILE).toFile());

		// Copy the option UUID Master File
		if (indexFileSet.uuidMasterFile != null && indexFileSet.uuidMasterFile.exists() &&
				indexFileSet.uuidMasterFile.isFile())
			Files.copy(indexFileSet.uuidMasterFile.toPath(),
					backupIndexDirectory.resolve(IndexFileSet.UUID_MASTER_FILE), StandardCopyOption.REPLACE_EXISTING);
	}

	/**
	 * Backup the settings, field definitions analyzer definitions
	 *
	 * @param settings              the index settings
	 * @param fieldMap              the field map
	 * @param analyzerDefinitionMap the analyzer definition
	 */
	private void backupSettings(final IndexSettingsDefinition settings, final FieldMap fieldMap,
			final LinkedHashMap<String, AnalyzerDefinition> analyzerDefinitionMap) throws IOException {
		IndexSettingsDefinition.save(settings, backupIndexDirectory.resolve(IndexFileSet.SETTINGS_FILE).toFile());
		FieldDefinition.saveMap(fieldMap.getFieldDefinitionMap(),
				backupIndexDirectory.resolve(IndexFileSet.FIELDS_FILE).toFile());
		AnalyzerDefinition.saveMap(analyzerDefinitionMap,
				backupIndexDirectory.resolve(IndexFileSet.ANALYZERS_FILE).toFile());
	}

	private void backupResources() {

	}

	/**
	 * Backup the index files
	 */
	private void backupIndexFiles(final ReplicationMaster replicationMaster, boolean withTaxo) throws IOException {

		final ReplicationSession session = replicationMaster.newReplicationSession();
		try {
			//TODO implements
		} finally {
			replicationMaster.releaseSession(session.sessionUuid);
		}
	}

	/**
	 * Execute the entire backup process
	 *
	 * @param indexFileSet          the directory list of the index instance
	 * @param indexUUID             the index UUID
	 * @param settings              the index settings
	 * @param fieldMap              the field map of the index
	 * @param analyzerDefinitionMap the analyzers of the index
	 * @param replicationMaster     the replicationMaster of the index
	 * @return the result of the backup
	 * @throws IOException
	 */
	BackupStatus doBackup(final IndexFileSet indexFileSet, final UUID indexUUID, final IndexSettingsDefinition settings,
			final FieldMap fieldMap, final LinkedHashMap<String, AnalyzerDefinition> analyzerDefinitionMap,
			final ReplicationMaster replicationMaster) throws IOException {

		backupIndexAndMasterUuid(indexFileSet, indexUUID);
		backupSettings(settings, fieldMap, analyzerDefinitionMap);
		return BackupStatus.newBackupStatus(backupIndexDirectory, false);
	}

	@Override
	public LinkedHashMap<String, AnalyzerDefinition> getMasterAnalyzers() {
		return null;
	}

	@Override
	public void setClientAnalyzers(LinkedHashMap<String, AnalyzerDefinition> analyzers) {

	}

	@Override
	public LinkedHashMap<String, FieldDefinition> getMasterFields() {
		return null;
	}

	@Override
	public void setClientFields(LinkedHashMap<String, FieldDefinition> fields) {

	}

	@Override
	public UUID getClientMasterUuid() throws IOException {
		return null;
	}

	@Override
	public void setClientMasterUuid(UUID masterUuid) {

	}

	@Override
	public void switchRefresh(ReplicationStatus.Strategy strategy) {

	}

	@Override
	public InputStream getIndexFile(String sessionId, ReplicationProcess.Source source, String file) {
		return null;
	}

	@Override
	public ReplicationStatus getLastStatus() {
		return null;
	}
}
