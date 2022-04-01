/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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
package com.qwazr.utils.concurrent;

import com.qwazr.utils.ExceptionUtils;

import java.io.Closeable;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This class is an efficient and lazy ExecutorService singleton provider
 */
public class ExecutorSingleton implements Closeable {

    private final ExecutorService externalExecutorService;
    private final long closingTimeout;
    private final TimeUnit closingUnit;

    private volatile ExecutorService executorService;

    private ExecutorSingleton(final ExecutorService executorService, long closingTimeout, TimeUnit closingUnit) {
        this.externalExecutorService = this.executorService = executorService;
        this.closingTimeout = closingTimeout;
        this.closingUnit = Objects.requireNonNull(closingUnit, "The closing timeunit is missing");
    }

    public ExecutorSingleton(final ExecutorService executorService) {
        this(executorService, 1, TimeUnit.HOURS);
    }

    /**
     * @param closingTimeout the maximum time to wait
     * @param closingUnit    the time unit of the timeout argument
     */
    public ExecutorSingleton(long closingTimeout, TimeUnit closingUnit) {
        this(null, closingTimeout, Objects.requireNonNull(closingUnit, "The closingUnit is missing"));

    }

    /**
     * Ovveride this class to provide an alternative ExecutorService implementation.
     * By default a CachedThreadPool is provided.
     *
     * @return a new ExecutorService instance
     * @See ExecutorService.newCachedThreadPool()
     */
    protected ExecutorService newExecutorService() {
        return Executors.newCachedThreadPool();
    }

    /**
     * @return the ExecutorService singleton
     */
    public ExecutorService getExecutorService() {
        if (executorService == null) {
            synchronized (ExecutorService.class) {
                if (executorService == null)
                    executorService = newExecutorService();
            }
        }
        return executorService;
    }

    @Override
    public synchronized void close() {
        if (externalExecutorService == null && executorService != null) {
            if (!executorService.isShutdown())
                executorService.shutdown();
            ExceptionUtils.bypass(() -> ExecutorUtils.close(executorService, closingTimeout, closingUnit));
            executorService = null;
        }
    }

}
