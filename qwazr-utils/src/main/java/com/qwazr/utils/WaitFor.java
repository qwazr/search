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
package com.qwazr.utils;

import com.qwazr.utils.concurrent.CallableEx;

import java.util.concurrent.TimeUnit;

public class WaitFor {

	private final TimeUnit timeOutUnit;
	private final long timeOutDuration;
	private final TimeUnit pauseTimeUnit;
	private final long pauseTimeDuration;

	private WaitFor(final Builder builder) {
		this.timeOutUnit = builder.timeOutUnit == null ? TimeUnit.MILLISECONDS : builder.timeOutUnit;
		this.timeOutDuration = builder.timeOutDuration;
		this.pauseTimeUnit = builder.pauseTimeUnit == null ? TimeUnit.MILLISECONDS : builder.pauseTimeUnit;
		this.pauseTimeDuration = builder.pauseTimeDuration;
	}

	public interface UntilCondition extends CallableEx<Boolean, InterruptedException> {
	}

	public <T extends UntilCondition> T until(final T condition) throws InterruptedException {
		final long timeOut = timeOutUnit.toMillis(timeOutDuration) + System.currentTimeMillis();
		final long pauseTime = pauseTimeUnit.toMillis(pauseTimeDuration);
		while (System.currentTimeMillis() < timeOut) {
			if (condition.call())
				return condition;
			Thread.sleep(pauseTime);
		}
		throw new InterruptedException("Time-out reached");
	}

	public static Builder of() {
		return new Builder();
	}

	public static class Builder {

		private TimeUnit timeOutUnit = TimeUnit.MILLISECONDS;
		private long timeOutDuration = 1000;
		private TimeUnit pauseTimeUnit = TimeUnit.MILLISECONDS;
		private long pauseTimeDuration = 200;

		public Builder timeOut(TimeUnit unit, long duration) {
			timeOutUnit = unit;
			timeOutDuration = duration;
			return this;
		}

		public Builder pauseTime(TimeUnit unit, long duration) {
			pauseTimeUnit = unit;
			pauseTimeDuration = duration;
			return this;
		}

		public WaitFor build() {
			return new WaitFor(this);
		}

		public <T extends UntilCondition> T until(T condition) throws InterruptedException {
			return build().until(condition);
		}
	}
}
