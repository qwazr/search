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
package com.qwazr.server.logs;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface AccessLogger extends Consumer<LogContext> {

	void log(Object[] params);

	abstract class Common implements AccessLogger {

		final LogParam[] logParams;

		protected Common(final LogParam... logParams) {
			this.logParams = logParams;
		}

		@Override
		final public void accept(final LogContext context) {
			log(LogParam.translate(context, logParams));
		}

	}

	final class Jul extends Common {

		private final String logMessage;
		private final Logger logger;
		private final Level level;

		public Jul(Logger logger, Level level, String logMessage, LogParam... logParams) {
			super(logParams);
			this.logger = logger;
			this.logMessage = logMessage;
			this.level = level;
		}

		@Override
		public void log(final Object[] params) {
			if (logger.isLoggable(level))
				logger.log(level, logMessage, params);
		}
	}

}