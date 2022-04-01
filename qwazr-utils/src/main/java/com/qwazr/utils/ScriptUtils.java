/**
 * Copyright 2014-2016 Emmanuel Keller / QWAZR
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

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.Reader;
import java.security.*;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ScriptUtils {

	public static class RestrictedAccessControlContext {
		public static final AccessControlContext INSTANCE;

		static {
			INSTANCE = new AccessControlContext(new ProtectionDomain[] { new ProtectionDomain(null, null) });
		}
	}

	public static void evalScript(final ScriptEngine scriptEngine, final AccessControlContext controlContext,
			final Reader reader, final Bindings bindings) throws ScriptException, PrivilegedActionException {
		AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> {
			scriptEngine.eval(reader, bindings);
			return null;
		}, controlContext);
	}

	public static <T> Map<String, T> toMap(ScriptObjectMirror som, Class<T> type) throws ScriptException {
		if (som == null)
			return null;
		if (som.isArray())
			throw new ScriptException("The JS object is an array");

		Map<String, T> map = new LinkedHashMap<>();
		if (som.isEmpty())
			return map;
		som.forEach((s, o) -> map.put(s, ((ScriptObjectMirror) o).to(type)));
		return map;
	}

	public static <T> T[] toArray(ScriptObjectMirror som, Class<T> type) throws ScriptException {
		if (som == null)
			return null;
		if (!som.isArray())
			throw new ScriptException("The JS object is not an array");
		T[] array = (T[]) new Object[som.size()];
		final AtomicInteger i = new AtomicInteger(0);
		som.values().forEach(o -> array[i.getAndIncrement()] = ((ScriptObjectMirror) o).to(type));
		return array;
	}

	public static void fillStringCollection(ScriptObjectMirror som, Collection<String> collection)
			throws ScriptException {
		if (som == null)
			return;
		if (!som.isArray())
			throw new ScriptException("The JS object is not an array");
		som.values().forEach(o -> collection.add(o.toString()));
	}

	public static void fillStringMap(ScriptObjectMirror som, Map<String, String> map) throws ScriptException {
		if (som == null)
			return;
		som.forEach((s, o) -> map.put(s, o.toString()));
	}
}
