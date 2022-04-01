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
package com.qwazr.utils.concurrent;

import com.qwazr.utils.concurrent.readwritelock.ReadWriteLockImpl;
import com.qwazr.utils.concurrent.readwritelock.StamptedReadWriteLockImpl;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public interface ReadWriteLock {

    <T> T read(final Callable<T> call);

    <V, E extends Throwable> V readEx(final ExceptionCallable<V, E> call) throws E;

    void read(final Runnable run);

    <E extends Throwable> void readEx(final ExceptionRunnable<E> run) throws E;

    <T> T write(final Callable<T> call);

    <V, E extends Throwable> V writeEx(final ExceptionCallable<V, E> call) throws E;

    <E extends Throwable> void writeEx(final ExceptionRunnable<E> run) throws E;

    void write(final Runnable run);

    default <V> V readOrWrite(final Callable<V> read, final Callable<V> write) {
        final V result = read(read);
        return result != null ? result : write(write);
    }

    default <V, E extends Exception> V readOrWriteEx(final ExceptionCallable<V, E> read, final ExceptionCallable<V, E> write)
            throws Exception {
        final V result = readEx(read);
        return result != null ? result : writeEx(write);

    }

    interface ExceptionRunnable<E extends Throwable> {
        void run() throws E;
    }

    interface ExceptionCallable<V, E extends Throwable> {
        V call() throws E;
    }

    class InsideLockException extends RuntimeException {

        public final Exception exception;

        public InsideLockException(Exception cause) {
            super(cause);
            this.exception = cause;
        }
    }

    static ReadWriteLock of(java.util.concurrent.locks.ReadWriteLock rwl) {
        return new ReadWriteLockImpl(rwl);
    }

    static ReadWriteLock reentrant(boolean fair) {
        return of(new ReentrantReadWriteLock(fair));
    }

    static ReadWriteLock stamped() {
        return new StamptedReadWriteLockImpl();
    }
}
