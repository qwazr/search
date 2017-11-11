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
package com.qwazr.search.test.units;

import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.index.BackupStatus;
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.index.SchemaSettingsDefinition;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.SortedMap;

/**
 * Created by ekeller on 29/05/2017.
 */
public class BackupTest extends AbstractIndexTest.WithIndexRecord.WithTaxonomy {

	private final static String SCHEMA_NAME = "backup_schema";

	private static Path backupPath;

	private static IndexServiceInterface service;

	@BeforeClass
	public static void setup() throws IOException, InterruptedException, URISyntaxException {
		initIndexManager();
		service = indexManager.getService();
		backupPath = Files.createTempDirectory("backup");
		service.createUpdateSchema(SCHEMA_NAME,
				SchemaSettingsDefinition.of().backupDirectoryPath(backupPath.toAbsolutePath().toString()).build());
	}

	@Test
	public void test() throws URISyntaxException {

		// Backup one index without fields
		service.createUpdateIndex(SCHEMA_NAME, "test1");
		service.doBackup("*", "*", "backup");

		// Backup index with taxo
		AnnotatedIndexService<IndexRecord.WithTaxonomy> indexWithTaxo =
				new AnnotatedIndexService<>(service, IndexRecord.WithTaxonomy.class, SCHEMA_NAME, "indexWithTaxo",
						null);
		indexWithTaxo.createUpdateIndex();
		indexWithTaxo.createUpdateFields();

		// Backup index without taxo
		AnnotatedIndexService<IndexRecord.NoTaxonomy> indexNoTaxo =
				new AnnotatedIndexService<>(service, IndexRecord.NoTaxonomy.class, SCHEMA_NAME, "indexNoTaxo", null);
		indexNoTaxo.createUpdateIndex();
		indexNoTaxo.createUpdateFields();

		// Delete backup. There is only one
		Assert.assertEquals(1, service.deleteBackups("*", "*", "backup"), 0);

		// Backup two indexes
		checkBackup(indexNoTaxo.doBackup("backup"), "indexNoTaxo");
		checkBackup(indexWithTaxo.doBackup("backup"), "indexWithTaxo");

		// Delete backup. There is only two
		Assert.assertEquals(2, service.deleteBackups("*", "*", "backup"), 0);

		// Backup everything, 3 indexes
		service.doBackup("*", "*", "backup");

		// Backup deletion
		Assert.assertEquals(3, service.deleteBackups("*", "*", "backup"), 0);
		Assert.assertEquals(0, service.deleteBackups("*", "*", "backup"), 0);
	}

	private void checkBackup(SortedMap<String, SortedMap<String, BackupStatus>> backup, final String indexName) {
		Assert.assertNotNull(backup);
		final SortedMap<String, BackupStatus> statusMap = backup.get(SCHEMA_NAME);
		Assert.assertNotNull(statusMap);
		final BackupStatus backupStatus = statusMap.get(indexName);
		Assert.assertNotNull(backupStatus);
		Assert.assertNotNull(backupStatus.date);
		Assert.assertNotNull(backupStatus.bytes_size);
		Assert.assertTrue(backupStatus.bytes_size > 0);
		Assert.assertNotNull(backupStatus.files_count);
		Assert.assertTrue(backupStatus.files_count > 0);
	}
}
