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

import java.util.concurrent.Callable;
import java.util.concurrent.locks.StampedLock;

public class StamptedReadWriteLockImpl extends AbstractReadWriteLockImpl {

	final private StampedLock stampedLock;

	public StamptedReadWriteLockImpl() {
		stampedLock = new StampedLock();
	}

	@Override
	final public <T> T read(final Callable<T> call) {
		final long stamp = stampedLock.readLock();
		try {
			return call(call);
		} finally {
			stampedLock.unlockRead(stamp);
		}
	}

	@Override
	final public <V, E extends Throwable> V readEx(final ExceptionCallable<V, E> call) throws E {
		final long stamp = stampedLock.readLock();
		try {
			return call.call();
		} finally {
			stampedLock.unlockRead(stamp);
		}
	}

	@Override
	final public void read(final Runnable run) {
		final long stamp = stampedLock.readLock();
		try {
			run(run);
		} finally {
			stampedLock.unlockRead(stamp);
		}
	}

	@Override
	final public <E extends Throwable> void readEx(final ExceptionRunnable<E> run) throws E {
		final long stamp = stampedLock.readLock();
		try {
			run.run();
		} finally {
			stampedLock.unlockRead(stamp);
		}
	}

	@Override
	final public <T> T write(final Callable<T> call) {
		final long stamp = stampedLock.writeLock();
		try {
			return call(call);
		} finally {
			stampedLock.unlockWrite(stamp);
		}
	}

	@Override
	final public <V, E extends Throwable> V writeEx(final ExceptionCallable<V, E> call) throws E {
		final long stamp = stampedLock.writeLock();
		try {
			return call.call();
		} finally {
			stampedLock.unlockWrite(stamp);
		}
	}

	@Override
	final public <E extends Throwable> void writeEx(final ExceptionRunnable<E> run) throws E {
		final long stamp = stampedLock.writeLock();
		try {
			run.run();
		} finally {
			stampedLock.unlockWrite(stamp);
		}
	}

	@Override
	final public void write(final Runnable run) {
		final long stamp = stampedLock.writeLock();
		try {
			run.run();
		} finally {
			stampedLock.unlockWrite(stamp);
		}
	}

	@Override
	final public <V> V readOrWrite(final Callable<V> read, final Callable<V> write) {
		V result = read(read);
		if (result != null)
			return result;
		final long stamp = stampedLock.writeLock();
		try {
			result = read.call();
			if (result != null)
				return result;
			return write.call();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new InsideLockException(e);
		} finally {
			stampedLock.unlockWrite(stamp);
		}
	}

	@Override
	final public <V, E extends Exception> V readOrWriteEx(final ExceptionCallable<V, E> read,
			final ExceptionCallable<V, E> write) throws Exception {
		V result = readEx(read);
		if (result != null)
			return result;
		final long stamp = stampedLock.writeLock();
		try {
			result = read.call();
			if (result != null)
				return result;
			return write.call();
		} finally {
			stampedLock.unlockWrite(stamp);
		}
	}
}
