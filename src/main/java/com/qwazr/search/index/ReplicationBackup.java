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

import com.qwazr.search.replication.ReplicationProcess;
import com.qwazr.search.replication.ReplicationSession;
import com.qwazr.search.replication.SlaveNode;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

class ReplicationBackup extends ReplicationClient {

	private final IndexInstance indexInstance;
	private final Path backupIndexDirectory;

	ReplicationBackup(final IndexInstance indexInstance, final Path backupIndexDirectory, final boolean withTaxonomy)
			throws IOException {
		super(getSlaveNode(backupIndexDirectory, withTaxonomy));
		this.indexInstance = indexInstance;
		this.backupIndexDirectory = backupIndexDirectory;
	}

	@Override
	InputStream getItem(final String sessionUuid, final ReplicationProcess.Source source, final String itemName)
			throws FileNotFoundException {
		return indexInstance.replicationObtain(sessionUuid, source, itemName);
	}

	/**
	 * Execute the entire backup process
	 *
	 * @return the final status of the backup
	 * @throws IOException
	 */
	BackupStatus backup() throws IOException {
		final ReplicationSession session = indexInstance.replicationUpdate(null);
		try {
			replicate(session, null, (strategy, remoteMasterUuid) -> {
			});
			return BackupStatus.newBackupStatus(backupIndexDirectory, false);
		} finally {
			indexInstance.replicationRelease(session.sessionUuid);
		}
	}

	static SlaveNode getSlaveNode(final Path backupIndexDirectory, final boolean withTaxonomy) throws IOException {
		final Path resourcesPath = backupIndexDirectory.resolve(IndexFileSet.RESOURCES_DIR);
		final Path dataIndexPath = backupIndexDirectory.resolve(IndexFileSet.INDEX_DATA);
		final Path replWorkDirectory = backupIndexDirectory.resolve(IndexFileSet.REPL_WORK);

		if (!withTaxonomy)
			return new SlaveNode.WithIndex(resourcesPath, null, dataIndexPath, replWorkDirectory, backupIndexDirectory,
					IndexFileSet.FIELDS_FILE, IndexFileSet.ANALYZERS_FILE, IndexFileSet.SETTINGS_FILE,
					IndexFileSet.UUID_FILE, IndexFileSet.UUID_MASTER_FILE);

		final Path taxoIndexPath = backupIndexDirectory.resolve(IndexFileSet.INDEX_TAXONOMY);

		return new SlaveNode.WithIndexAndTaxo(resourcesPath, null, dataIndexPath, null, taxoIndexPath,
				replWorkDirectory, backupIndexDirectory, IndexFileSet.FIELDS_FILE, IndexFileSet.ANALYZERS_FILE,
				IndexFileSet.SETTINGS_FILE, IndexFileSet.UUID_FILE, IndexFileSet.UUID_MASTER_FILE);
	}

}
