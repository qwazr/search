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
package com.qwazr.binder;

import java.lang.reflect.Field;

public class BinderException extends RuntimeException {

	public BinderException(String message, Field field, Object value, Exception cause) {
		super(getMessage(message, field, value), cause);
	}

	public BinderException(Field field, Object value, Exception cause) {
		super(getMessage(null, field, value), cause);
	}

	public BinderException(String message, Field field, Object value) {
		super(getMessage(message, field, value));
	}

	private static String getMessage(String message, Field field, Object value) {
		final StringBuilder sb = new StringBuilder(message == null ? "Binder error" : message);
		if (field != null) {
			sb.append("- Field: ");
			sb.append(field);
		}
		if (value != null) {
			sb.append(" - Value: ");
			sb.append(value);
		}
		return sb.toString();
	}
}
