/**
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
 **/
package com.qwazr.utils;

import java.io.InputStream;

public class ClassLoaderUtils {

	public static <T> Class<T> findClass(final ClassLoader classLoader, final String className)
			throws ClassNotFoundException {
		return (Class<T>) classLoader.loadClass(className);
	}

	public static <T> Class<T> findClass(final String className) throws ClassNotFoundException {
		return findClass(Thread.currentThread().getContextClassLoader(), className);
	}

	public static <T> Class<T> findClass(final ClassLoader classLoader, final String classSuffix,
			final String... classPrefixes) throws ClassNotFoundException {
		if (classPrefixes == null || classPrefixes.length == 0)
			return findClass(classLoader, classSuffix);
		ClassNotFoundException firstClassException = null;
		for (String prefix : classPrefixes) {
			try {
				return (Class<T>) findClass(classLoader, prefix + classSuffix);
			} catch (ClassNotFoundException e) {
				if (firstClassException == null)
					firstClassException = e;
			}
		}
		throw firstClassException;
	}

	public static <T> Class<T> findClass(final String classSuffix, final String... classPrefixes)
			throws ClassNotFoundException {
		return findClass(Thread.currentThread().getContextClassLoader(), classSuffix, classPrefixes);
	}

	public static InputStream getResourceAsStream(final ClassLoader classLoader, final String name) {
		return classLoader.getResourceAsStream(name);
	}

	public static InputStream getResourceAsStream(final String name) {
		return getResourceAsStream(Thread.currentThread().getContextClassLoader(), name);
	}

}
