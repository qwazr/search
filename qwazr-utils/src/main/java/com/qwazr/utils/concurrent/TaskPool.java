/*
 * Copyright 2016-2019 Emmanuel Keller / QWAZR
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

import com.qwazr.utils.LoggerUtils;

import java.io.Closeable;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface TaskPool extends Closeable {

    Logger DEFAULT_LOGGER = LoggerUtils.getLogger(TaskPool.class);

    int DEFAULT_MAX_CONCURRENT_TASKS = Runtime.getRuntime().availableProcessors() + 1;

    default CompletableFuture<?> submit(final Runnable task) {
        return submit((Supplier<?>) () -> {
            task.run();
            return null;
        });
    }

    <RESULT> CompletableFuture<RESULT> submit(final Supplier<RESULT> task);

    int getConcurrentTasks();

    TaskPool shutdown();

    boolean isShutdown();

    TaskPool awaitCompletion();

    @Override
    void close();

    static TaskPool of(final Logger logger) {
        return new WithExecutor(logger);
    }

    static TaskPool of() {
        return of(DEFAULT_LOGGER);
    }

    static TaskPool of(final Logger logger, final int maxConcurrentTasks) {
        return new WithExecutor(logger, maxConcurrentTasks);
    }

    static TaskPool of(final int maxConcurrentTasks) {
        return of(DEFAULT_LOGGER, maxConcurrentTasks);
    }

    static TaskPool of(final Logger logger, final ExecutorService executorService) {
        return new Base(logger, executorService);
    }

    static TaskPool of(final ExecutorService executorService) {
        return of(DEFAULT_LOGGER, executorService);
    }

    static TaskPool of(final Logger logger, final ExecutorService executorService, final int maxConcurrentTasks) {
        return new Base(logger, executorService, maxConcurrentTasks);
    }

    static TaskPool of(final ExecutorService executorService, final int maxConcurrentTasks) {
        return of(DEFAULT_LOGGER, executorService, maxConcurrentTasks);
    }

    class Base implements TaskPool {

        private final AtomicBoolean shutdown;
        private final Semaphore tasksSemaphore;
        private final Logger logger;
        private final ExecutorService executorService;
        private final Set<CompletableFuture> futures;

        protected Base(final Logger logger, final ExecutorService executorService, final int maxConcurrentTasks) {
            this.shutdown = new AtomicBoolean(false);
            this.tasksSemaphore = new Semaphore(maxConcurrentTasks, true);
            this.logger = logger;
            this.executorService = executorService;
            this.futures = ConcurrentHashMap.newKeySet(maxConcurrentTasks);
        }

        protected Base(final Logger logger, final ExecutorService executorService) {
            this(logger, executorService, DEFAULT_MAX_CONCURRENT_TASKS);
        }

        protected ExecutorService getExecutorService() {
            return executorService;
        }

        public int getConcurrentTasks() {
            futures.removeIf(Future::isDone);
            return futures.size();
        }

        @Override
        public <RESULT> CompletableFuture<RESULT> submit(final Supplier<RESULT> task) {
            synchronized (shutdown) {
                if (shutdown.get())
                    throw new IllegalStateException("The task pool is shutdown");
                try {
                    tasksSemaphore.acquire();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                try {
                    final CompletableFuture<RESULT> future = CompletableFuture.supplyAsync(task, executorService);
                    futures.removeIf(Future::isDone);
                    futures.add(future);
                    return future.whenComplete((result, throwable) -> tasksSemaphore.release());
                } catch (RuntimeException e) {
                    tasksSemaphore.release();
                    throw e;
                }
            }
        }

        @Override
        public TaskPool shutdown() {
            shutdown.set(true);
            return this;
        }

        @Override
        public boolean isShutdown() {
            return shutdown.get();
        }

        @Override
        public TaskPool awaitCompletion() {
            synchronized (shutdown) {
                if (!isShutdown())
                    throw new IllegalStateException("The pool must be shutdown first");
                futures.forEach(future -> {
                    try {
                        future.join();
                    } catch (final CancellationException e) {
                        logger.log(Level.WARNING, e, () -> "Job cancelled");
                    } catch (final CompletionException e) {
                        logger.log(Level.WARNING, e, () -> "Job completion exception");
                    }
                });
                futures.clear();
            }
            return this;
        }

        @Override
        public void close() {
            synchronized (shutdown()) {
                shutdown().awaitCompletion();
            }
        }
    }

    class WithExecutor extends Base {

        private final Logger logger;

        protected WithExecutor(final Logger logger, final int maxConcurrentTasks) {
            super(logger, Executors.newFixedThreadPool(maxConcurrentTasks), maxConcurrentTasks);
            this.logger = logger;
        }

        protected WithExecutor(final Logger logger) {
            super(logger, Executors.newCachedThreadPool());
            this.logger = logger;
        }

        @Override
        public void close() {
            super.close();
            try {
                ExecutorUtils.close(getExecutorService(), 1, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, e, () -> "Task pool closing interrupted");
            }
        }
    }
}
