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
package com.qwazr.utils.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class ThreadUtils {

	public static void sleep(final long duration, final TimeUnit unit) {
		try {
			Thread.sleep(unit.toMillis(duration));
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public interface ExtendedRunnable extends Runnable {

		default String getName(final long threadId) {
			return getClass().getName() + "-" + threadId;
		}

	}

	public static class ExtendedThreadFactory implements ThreadFactory {

		@Override
		public Thread newThread(final Runnable runnable) {
			final Thread thread = new Thread(runnable);
			if (runnable instanceof ExtendedRunnable) {
				final ExtendedRunnable runnableEx = (ExtendedRunnable) runnable;
				final String name = runnableEx.getName(thread.getId());
				if (name != null)
					thread.setName(name);
			}
			return thread;
		}
	}

}
