/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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
package com.qwazr.search.backups;

import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.index.BackupStatus;
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.index.SchemaSettingsDefinition;
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.search.test.units.IndexRecord;
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
    public static void setup() throws IOException {
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

        // Check backup API without having made any backup
        Assert.assertTrue(service.getBackups("*", "*", "*", true).isEmpty());

        //Do a backup
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
        final BackupStatus status1 = checkBackupSchema(indexNoTaxo.doBackup("backup"), "indexNoTaxo", null);
        checkBackups(indexNoTaxo.getBackups("backup", false), "indexNoTaxo", status1);
        final BackupStatus status2 = checkBackupSchema(indexWithTaxo.doBackup("backup"), "indexWithTaxo", null);
        checkBackups(indexWithTaxo.getBackups("backup", false), "indexWithTaxo", status2);

        // Delete backup. There is only two
        Assert.assertEquals(2, service.deleteBackups("*", "*", "backup"), 0);

        // Backup everything, 3 indexes
        service.doBackup("*", "*", "backup");

        // Backup deletion
        Assert.assertEquals(3, service.deleteBackups("*", "*", "backup"), 0);
        Assert.assertEquals(0, service.deleteBackups("*", "*", "backup"), 0);

        // Check backup status after deleting all backups
        Assert.assertTrue(service.getBackups("*", "*", "*", true).isEmpty());

    }

    private BackupStatus checkBackup(SortedMap<String, BackupStatus> backupIndexMap, final String indexName,
                                     final BackupStatus status) {
        Assert.assertNotNull(backupIndexMap);
        final BackupStatus backupStatus = backupIndexMap.get(indexName);
        Assert.assertNotNull(backupStatus);
        Assert.assertNotNull(backupStatus.date);
        Assert.assertNotNull(backupStatus.humanDate);
        Assert.assertNotNull(backupStatus.bytesSize);
        Assert.assertTrue(backupStatus.bytesSize > 0);
        Assert.assertNotNull(backupStatus.filesCount);
        Assert.assertTrue(backupStatus.filesCount > 0);
        if (status != null)
            Assert.assertEquals(backupStatus, status);
        return backupStatus;
    }

    private BackupStatus checkBackupSchema(SortedMap<String, SortedMap<String, BackupStatus>> backup,
                                           final String indexName, final BackupStatus status) {
        Assert.assertNotNull(backup);
        return checkBackup(backup.get(SCHEMA_NAME), indexName, status);
    }

    private BackupStatus checkBackups(SortedMap<String, SortedMap<String, SortedMap<String, BackupStatus>>> backups,
                                      final String indexName, final BackupStatus status) {
        Assert.assertNotNull(backups);
        final SortedMap<String, SortedMap<String, BackupStatus>> schemaMap = backups.get(SCHEMA_NAME);
        Assert.assertNotNull(schemaMap);
        final SortedMap<String, BackupStatus> backupMap = schemaMap.get("backup");
        Assert.assertNotNull(backupMap);
        return checkBackup(backupMap, indexName, status);
    }
}
