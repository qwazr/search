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

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

abstract public class ConcurrentQueue<T> implements Consumer<T>, Closeable {

	private final ArrayBlockingQueue<T> queue;
	private final List<Future> futures;
	private final T breakItem;

	protected ConcurrentQueue(final ExecutorService executor, final int threadNumber, final T breakItem) {
		this.futures = new ArrayList<>();
		this.breakItem = breakItem;
		this.queue = new ArrayBlockingQueue<>(threadNumber);
		for (int i = 0; i < threadNumber; i++)
			futures.add(executor.submit(new ItemConsumer()));
	}

	protected abstract Consumer<T> getNewConsumer();

	@Override
	public void close() {
		try {
			for (int i = 0; i < futures.size(); i++)
				queue.put(breakItem);
			for (Future future : futures)
				future.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
		futures.clear();
	}

	@Override
	final public void accept(final T entry) {
		try {
			queue.put(entry);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private class ItemConsumer implements Runnable {

		private final Consumer<T> consumer = getNewConsumer();

		@Override
		final public void run() {
			try {
				for (; ; ) {
					final T item = queue.take();
					if (item == breakItem)
						return;
					consumer.accept(item);
				}
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
