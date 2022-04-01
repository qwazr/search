/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.NotImplementedException;

public class BlockingExecutorService implements ExecutorService {

    public static class BlockingInterruptionException extends RuntimeException {

        private BlockingInterruptionException(InterruptedException interruptedException) {
            super(interruptedException);
        }
    }

    private final Semaphore semaphore;
    private final ExecutorService executorService;

    protected BlockingExecutorService(@Nonnull final ExecutorService executorService, final int poolSize) {
        this.executorService = Objects.requireNonNull(executorService, "The executorService is null");
        this.semaphore = new Semaphore(poolSize);
    }

    public BlockingExecutorService(final int poolSize) {
        this(Executors.newFixedThreadPool(poolSize), poolSize);
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return executorService.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return executorService.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return executorService.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executorService.awaitTermination(timeout, unit);
    }

    public int availablePermits() {
        return semaphore.availablePermits();
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        Objects.requireNonNull(task);
        try {
            semaphore.acquire();
            try {
                return executorService.submit(() -> {
                    try {
                        return task.call();
                    } finally {
                        semaphore.release();
                    }
                });
            } catch (RejectedExecutionException | NullPointerException e) {
                semaphore.release();
                throw e;
            }
        } catch (InterruptedException e) {
            throw new BlockingInterruptionException(e);
        }
    }

    @Override
    public <T> Future<T> submit(@NotNull Runnable task, T result) {
        Objects.requireNonNull(task);
        try {
            semaphore.acquire();
            try {
                return executorService.submit(() -> {
                    try {
                        task.run();
                    } finally {
                        semaphore.release();
                    }
                }, result);
            } catch (RejectedExecutionException | NullPointerException e) {
                semaphore.release();
                throw e;
            }
        } catch (InterruptedException e) {
            throw new BlockingInterruptionException(e);
        }
    }

    @NotNull
    @Override
    public Future<?> submit(@NotNull Runnable task) {
        Objects.requireNonNull(task);
        try {
            semaphore.acquire();
            try {
                return executorService.submit(() -> {
                    try {
                        task.run();
                    } finally {
                        semaphore.release();
                    }
                });
            } catch (RejectedExecutionException | NullPointerException e) {
                semaphore.release();
                throw e;
            }
        } catch (InterruptedException e) {
            throw new BlockingInterruptionException(e);
        }
    }

    @NotNull
    @Override
    public <T> List<Future<T>> invokeAll(@NotNull Collection<? extends Callable<T>> tasks) {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public <T> List<Future<T>> invokeAll(@NotNull Collection<? extends Callable<T>> tasks, long timeout, @NotNull TimeUnit unit) {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public <T> T invokeAny(@NotNull Collection<? extends Callable<T>> tasks) {
        throw new NotImplementedException();
    }

    @Override
    public <T> T invokeAny(@NotNull Collection<? extends Callable<T>> tasks, long timeout, @NotNull TimeUnit unit) {
        throw new NotImplementedException();
    }

    @Override
    public void execute(@NotNull Runnable command) {
        Objects.requireNonNull(command);
        try {
            semaphore.acquire();
            try {
                executorService.execute(() -> {
                    try {
                        command.run();
                    } finally {
                        semaphore.release();
                    }
                });
            } catch (RejectedExecutionException | NullPointerException e) {
                semaphore.release();
                throw e;
            }
        } catch (InterruptedException e) {
            throw new BlockingInterruptionException(e);
        }
    }
}
