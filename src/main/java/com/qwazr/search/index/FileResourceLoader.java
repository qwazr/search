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
package com.qwazr.search.index;

import com.qwazr.server.ServerException;
import com.qwazr.utils.ClassLoaderUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.analysis.util.ResourceLoader;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

class FileResourceLoader implements ResourceLoader {

	private final ResourceLoader delegate;
	private final File directory;

	FileResourceLoader(final ResourceLoader delegate, final File directory) {
		this.delegate = delegate;
		this.directory = directory;
	}

	final File checkResourceName(final String resourceName) {
		Objects.requireNonNull(resourceName, "The resource name is missing");
		final String expectedResourceName = FilenameUtils.getName(FilenameUtils.normalize(resourceName));
		if (!resourceName.equals(expectedResourceName))
			throw new ServerException(Response.Status.NOT_ACCEPTABLE,
					"The resource name is not valid. Expected: " + expectedResourceName);
		return new File(directory, expectedResourceName);
	}

	@Override
	public InputStream openResource(final String resourceName) throws IOException {
		if (directory.exists()) {
			final File resourceFile = checkResourceName(resourceName);
			if (resourceFile.exists())
				return new FileInputStream(resourceFile);
		}
		if (delegate != null)
			return delegate.openResource(resourceName);
		throw new ServerException(Response.Status.NOT_FOUND, "Resource not found : " + resourceName);
	}

	@Override
	public <T> Class<? extends T> findClass(final String cname, final Class<T> expectedType) {
		try {
			return ClassLoaderUtils.findClass(cname);
		} catch (ClassNotFoundException e) {
			throw ServerException.of("Cannot find class " + cname, e);
		}
	}

	@Override
	public <T> T newInstance(final String className, final Class<T> expectedType) {
		try {
			return findClass(className, expectedType).newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw ServerException.of("Cannot create an instance of class " + className, e);
		}
	}
}
