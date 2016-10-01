/**
 * Copyright 2016 Emmanuel Keller / QWAZR
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
package com.qwazr.search.utils;

import com.qwazr.classloader.ClassLoaderManager;
import com.qwazr.library.LibraryManager;
import com.qwazr.utils.ClassLoaderUtils;

import java.io.IOException;

public class ClassUtils {

	final static boolean LIBRARY_AVAILABLE;

	static {
		boolean f;
		try {
			Class.forName(LibraryManager.class.getName());
			f = true;
		} catch (ClassNotFoundException e) {
			f = false;
		}
		LIBRARY_AVAILABLE = f;
	}

	final public static <T> T newInstance(final String className, final String[] classPrefixes)
			throws IOException, ReflectiveOperationException {
		final T instance =
				(T) ClassLoaderUtils.findClass(ClassLoaderManager.classLoader, className, classPrefixes).newInstance();
		if (LIBRARY_AVAILABLE)
			LibraryManager.inject(instance);
		return instance;
	}

	final public static <T> T newInstance(final String className) throws IOException, ReflectiveOperationException {
		final T instance = (T) ClassLoaderUtils.findClass(ClassLoaderManager.classLoader, className).newInstance();
		if (LIBRARY_AVAILABLE)
			LibraryManager.inject(instance);
		return instance;
	}
}
