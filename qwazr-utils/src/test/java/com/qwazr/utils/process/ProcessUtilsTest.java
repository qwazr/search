/*
 * Copyright 2017 Emmanuel Keller / QWAZR
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
package com.qwazr.utils.process;

import com.qwazr.utils.IOUtils;
import com.qwazr.utils.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ProcessUtilsTest {

	private final static String EXIT_VALUE = "exit_value";

	@Test
	public void processTest() throws IOException, InterruptedException {

		final int exitValue = RandomUtils.nextInt(0, 100);
		final File pidFile = Files.createTempFile("test", ".pid").toFile();

		final Map<String, String> env = new HashMap<>();
		env.put(EXIT_VALUE, Integer.toString(exitValue));
		env.put(PIDFile.PID_PATH_ENV_VAR, pidFile.getAbsolutePath());

		final Process process = ProcessUtils.java(ProcessUtilsTest.class, env);
		try {
			Assert.assertNotNull(process);
			process.waitFor(1, TimeUnit.MINUTES);
			Assert.assertEquals(exitValue, process.exitValue());
		} finally {
			process.destroy();
		}
		Assert.assertTrue(Integer.parseInt(IOUtils.readFileAsString(pidFile)) >= 0);
	}

	public static void main(String[] args) throws InterruptedException, IOException {
		final int exitValue = Integer.parseInt(System.getenv(EXIT_VALUE));
		new PIDFile().savePidToFile();
		System.exit(exitValue);
	}
}
