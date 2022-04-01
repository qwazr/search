/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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
import com.qwazr.utils.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;

public class PIDFile {

	public final static String PID_PATH_PROPERTY = "com.qwazr.pid.path";
	public final static String PID_PATH_ENV_VAR = "QWAZR_PID_PATH";
	public final static String PID_PROPERTY = "com.qwazr.pid";
	public final static String PID_ENV_VAR = "QWAZR_PID";
	public final static String DEFAULT_PID_PATH = "qwazr.pid";

	private final Integer pid;
	private final File pidFile;

	public PIDFile(File pidFile) {
		pid = PIDFile.getPid();
		this.pidFile = pidFile;
	}

	public PIDFile() {
		this(PIDFile.getPidFile());
	}

	/**
	 * @return true if a pid file exists
	 */
	public boolean isFileExists() {
		if (pidFile == null)
			return false;
		return pidFile.exists();
	}

	/**
	 * Save the PID number in the PID file
	 *
	 * @return this instance
	 * @throws IOException if any I/O error occurs
	 */
	public PIDFile savePidToFile() throws IOException {
		try (final FileOutputStream fos = new FileOutputStream(pidFile)) {
			IOUtils.write(pid.toString(), fos, Charset.defaultCharset());
			return this;
		}
	}

	/**
	 * Delete the PID file on exit
	 *
	 * @return this instance
	 */
	public PIDFile deletePidFileOnExit() {
		if (pidFile != null)
			pidFile.deleteOnExit();
		return this;
	}

	/**
	 * Define the location of the PID File using:
	 * - First the JAVA property: "com.qwazr.pid.path"
	 * - Second the Environment Variable QWAZR_PID_PATH
	 *
	 * @return a file instance where to store the PID
	 */
	public static File getPidFile() {
		String pidPath = System.getProperty(PID_PATH_PROPERTY);
		if (pidPath == null)
			pidPath = System.getenv(PID_PATH_ENV_VAR);
		if (pidPath == null)
			pidPath = DEFAULT_PID_PATH;
		return new File(pidPath);
	}

	/**
	 * Try to locate the PID of the process:
	 * - First by checking the JAVA property "com.qwazr.pid".
	 * - Second by checking the Environment Variable QWAZR_PID.
	 * - Finally by using the RuntimeMXBean method.
	 *
	 * @return the PID of the process
	 */
	public static Integer getPid() {
		String pid = System.getProperty(PID_PROPERTY);
		if (!StringUtils.isEmpty(pid))
			pid = System.getenv(PID_ENV_VAR);
		if (!StringUtils.isEmpty(pid))
			return Integer.parseInt(pid);
		return getPidFromMxBean();
	}

	/**
	 * @return the PID using the RuntimeMXBean method
	 */
	public static Integer getPidFromMxBean() {
		final String name = ManagementFactory.getRuntimeMXBean().getName();
		if (name == null)
			return null;
		int i = name.indexOf('@');
		if (i == -1)
			return null;
		return Integer.parseInt(name.substring(0, i));
	}

	@Override
	public String toString() {
		return "PID: " + pid + " - File: " + pidFile;
	}
}
