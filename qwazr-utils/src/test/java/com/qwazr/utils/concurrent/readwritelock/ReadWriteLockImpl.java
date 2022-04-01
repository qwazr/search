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
package com.qwazr.utils.concurrent.readwritelock;

import com.qwazr.utils.concurrent.ReadWriteLock;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;

public class ReadWriteLockImpl implements ReadWriteLock {

    final private Lock r;
    final private Lock w;

    public ReadWriteLockImpl(java.util.concurrent.locks.ReadWriteLock rwl) {
        this.r = rwl.readLock();
        this.w = rwl.writeLock();
    }

    private <T> T call(final Callable<T> call) {
        try {
            return call.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InsideLockException(e);
        }
    }

    @Override
    final public <T> T read(final Callable<T> call) {
        r.lock();
        try {
            return call(call);
        } finally {
            r.unlock();
        }
    }

    @Override
    final public <V, E extends Throwable> V readEx(final ExceptionCallable<V, E> call) throws E {
        r.lock();
        try {
            return call.call();
        } finally {
            r.unlock();
        }
    }

    @Override
    final public void read(final Runnable run) {
        r.lock();
        try {
            run.run();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InsideLockException(e);
        } finally {
            r.unlock();
        }
    }

    @Override
    final public <E extends Throwable> void readEx(final ExceptionRunnable<E> run) throws E {
        r.lock();
        try {
            run.run();
        } finally {
            r.unlock();
        }
    }

    @Override
    final public <T> T write(final Callable<T> call) {
        w.lock();
        try {
            return call(call);
        } finally {
            w.unlock();
        }
    }

    @Override
    final public <V, E extends Throwable> V writeEx(final ExceptionCallable<V, E> call) throws E {
        w.lock();
        try {
            return call.call();
        } finally {
            w.unlock();
        }
    }

    @Override
    final public <E extends Throwable> void writeEx(final ExceptionRunnable<E> run) throws E {
        w.lock();
        try {
            run.run();
        } finally {
            w.unlock();
        }
    }

    @Override
    final public void write(final Runnable run) {
        w.lock();
        try {
            run.run();
        } finally {
            w.unlock();
        }
    }

    @Override
    final public <V, E extends Exception> V readOrWriteEx(final ExceptionCallable<V, E> read,
                                                          final ExceptionCallable<V, E> write) throws Exception {
        V result = readEx(read);
        if (result != null)
            return result;
        w.lock();
        try {
            result = read.call();
            if (result != null)
                return result;
            return write.call();
        } finally {
            w.unlock();
        }
    }
}
