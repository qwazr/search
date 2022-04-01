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

import com.qwazr.utils.LoggerUtils;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class PeriodicThread implements ThreadUtils.ExtendedRunnable {

	private final int monitoringPeriod;

	private volatile Long lastExecutionTime = null;

	private volatile boolean shutdown;

	private static final Logger LOGGER = LoggerUtils.getLogger(PeriodicThread.class);

	protected PeriodicThread(final int monitoringPeriodSeconds) {
		this.monitoringPeriod = monitoringPeriodSeconds * 1000;
		this.shutdown = false;
	}

	protected abstract void runner();

	@Override
	public void run() {
		try {
			while (!shutdown) {
				long start = System.currentTimeMillis();
				lastExecutionTime = start;

				runner();

				final long ms = monitoringPeriod - (System.currentTimeMillis() - start);
				if (ms > 0) {
					synchronized (this) {
						wait(ms);
					}
				}
			}
		} catch (InterruptedException e) {
			LOGGER.log(Level.INFO, e.getMessage(), e);
		}
	}

	public void shutdown() {
		shutdown = true;
		synchronized (this) {
			notifyAll();
		}
	}

	public Date getLastExecutionDate() {
		final Long time = lastExecutionTime;
		return time == null ? null : new Date(time);
	}
}
