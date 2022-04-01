/*
 * Copyright 2016-2017 Emmanuel Keller / QWAZR
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

import com.qwazr.utils.LoggerUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

public class ProcessUtils {

    public final static String NOT_SUPPORTED_ERROR = "Process command unsupported on this operating system";

    final static Logger LOGGER = LoggerUtils.getLogger(ProcessUtils.class);

    public static Integer kill(Number pid) throws IOException, InterruptedException {
        if (pid == null)
            return null;
        final String commandLine;
        if (SystemUtils.IS_OS_UNIX)
            commandLine = "kill  " + pid;
        else if (SystemUtils.IS_OS_WINDOWS)
            commandLine = "taskkill /PID " + pid;
        else
            throw new IOException(NOT_SUPPORTED_ERROR);
        return run(commandLine);
    }

    public static Integer forceKill(Number pid) throws IOException, InterruptedException {
        if (pid == null)
            return null;
        final String commandLine;
        if (SystemUtils.IS_OS_UNIX)
            commandLine = "kill -9 " + pid;
        else if (SystemUtils.IS_OS_WINDOWS)
            commandLine = "taskkill /F /PID " + pid;
        else
            throw new IOException(NOT_SUPPORTED_ERROR);
        return run(commandLine);
    }

    public static boolean isRunning(final Number pid) throws IOException, InterruptedException {
        Objects.requireNonNull(pid, "The PID is null");
        final String commandLine;
        if (SystemUtils.IS_OS_UNIX)
            commandLine = "kill -0 " + pid;
        else
            throw new IOException(NOT_SUPPORTED_ERROR);
        return run(commandLine) == 0;
    }

    public static Integer run(final String commandLine) throws InterruptedException, IOException {
        final Process process = Runtime.getRuntime().exec(commandLine);
        try {
            return process.waitFor();
        }
        finally {
            process.destroy();
            if (process.isAlive())
                process.destroyForcibly();
        }
    }

    public static Process run(final File workingDirectory, final Map<String, String> env, final String... commandLine)
            throws IOException {
        final ProcessBuilder builder = new ProcessBuilder(commandLine);
        if (workingDirectory != null)
            builder.directory(workingDirectory);
        if (env != null)
            builder.environment().putAll(env);
        builder.inheritIO();
        return builder.start();
    }

    public static Process java(final Class<?> javaClass, final Map<String, String> env) throws IOException {
        final String javaHome = System.getProperty("java.home");
        final String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        final String classpath = System.getProperty("java.class.path");
        final String[] commandLine = new String[]{javaBin, "-cp", classpath, javaClass.getCanonicalName()};
        return run(null, env, commandLine);
    }

}
