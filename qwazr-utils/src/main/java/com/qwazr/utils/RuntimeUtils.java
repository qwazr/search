/*
 * Copyright 2014-2018 Emmanuel Keller / QWAZR
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
package com.qwazr.utils;

import com.sun.management.UnixOperatingSystemMXBean;
import org.apache.commons.io.FileUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public class RuntimeUtils {

    public static long getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    public static String getMemoryUsagePretty() {
        return FileUtils.byteCountToDisplaySize(getMemoryUsage());
    }

    public static Long getOpenFileCount() {
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        if (os instanceof UnixOperatingSystemMXBean)
            return ((UnixOperatingSystemMXBean) os).getOpenFileDescriptorCount();
        return null;
    }

    final public static Thread mainThread = Thread.currentThread();

    public static Integer getActiveThreadCount() {
        ThreadGroup threadGroup = mainThread.getThreadGroup();
        if (threadGroup == null)
            threadGroup = Thread.currentThread().getThreadGroup();
        return threadGroup == null ? null : threadGroup.activeCount();
    }


}
