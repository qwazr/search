package com.qwazr.search.test.units;

import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.index.SchemaSettingsDefinition;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by ekeller on 29/05/2017.
 */
public class BackupTest extends AbstractIndexTest {

	private final static String SCHEMA_NAME = "backup_schema";

	private static IndexServiceInterface service;

	@BeforeClass
	public static void setup() throws IOException, InterruptedException, URISyntaxException {
		initIndexManager();
		service = indexManager.getService();
		Path backupPath = Files.createTempDirectory("backup");
		service.createUpdateSchema(SCHEMA_NAME,
				SchemaSettingsDefinition.of().backupDirectoryPath(backupPath.toFile().getAbsolutePath()).build());
	}

	@Test
	public void test() {
		service.createUpdateIndex(SCHEMA_NAME, "test1");
		service.doBackup("*", "*", "backup");
		service.createUpdateIndex(SCHEMA_NAME, "test2");
		Assert.assertEquals(1, service.deleteBackups("*", "*", "backup"), 0);
		service.doBackup("*", "*", "backup");
		Assert.assertEquals(2, service.deleteBackups("*", "*", "backup"), 0);
		Assert.assertEquals(0, service.deleteBackups("*", "*", "backup"), 0);
	}
}
