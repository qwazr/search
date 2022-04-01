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
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class PIDFileTest {

	@Test
	public void testPidFile() throws IOException {
		final File pidFile = Files.createTempFile("test", ".pid").toFile();
		final PIDFile pid = new PIDFile(pidFile).savePidToFile();
		Assert.assertTrue(pid.isFileExists());
		Assert.assertTrue(Integer.parseInt(IOUtils.readFileAsString(pidFile)) >= 0);
	}

}
