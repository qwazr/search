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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * This class keep a persistent version of your instance in the file system
 *
 * @param <T> the cached type
 */
public class FileInstanceCache<T> {

	private volatile T cachedInstance;
	private volatile long lastModifiedFile;

	final Path path;
	final File file;
	final Reader<T> reader;
	final Writer<T> writer;

	private FileInstanceCache(Builder<T> builder) {
		path = builder.path;
		file = path.toFile();
		reader = builder.reader;
		writer = builder.writer;
	}

	/**
	 * Return the instance. The last modified time if the file is checked.
	 * If the file has changed, the instance is read again from the file.
	 * If the file does not exist, the method returns null.
	 *
	 * @return the instance
	 * @throws IOException if any I/O error occurs
	 */
	public T get() throws IOException {
		if (reader == null)
			throw new IOException("This file cache instance is write only");
		synchronized (file) {
			if (!Files.exists(path))
				return null;
			final long newLastUserModified = file.lastModified();
			if (cachedInstance != null && lastModifiedFile == newLastUserModified)
				return cachedInstance;
			cachedInstance = reader.read(file);
			lastModifiedFile = newLastUserModified;
			return cachedInstance;
		}
	}

	/**
	 * Write the instance in the file.
	 * If the new instance eguals the cached instance, the file is not writed again.
	 *
	 * @param instance the instance to write
	 * @return the cached instance
	 * @throws IOException if any I/O error occurs
	 */
	public FileInstanceCache<T> set(T instance) throws IOException {
		if (writer == null)
			throw new IOException("This file cache instance is read only");
		synchronized (file) {
			if (!Objects.equals(instance, cachedInstance) || !Files.exists(path))
				writer.write(instance, file);
			cachedInstance = instance;
		}
		return this;
	}

	@FunctionalInterface
	public interface Reader<T> {

		T read(File file) throws IOException;
	}

	@FunctionalInterface
	public interface Writer<T> {

		void write(T instance, File file) throws IOException;
	}

	public static <T> Builder<T> of(final Path path) {
		return new Builder<T>().path(path);
	}

	static public class Builder<T> {

		private Path path;
		private Reader<T> reader;
		private Writer<T> writer;

		public Builder<T> path(Path path) {
			this.path = path;
			return this;
		}

		public Builder<T> reader(Reader<T> reader) {
			this.reader = reader;
			return this;
		}

		public Builder<T> writer(Writer<T> writer) {
			this.writer = writer;
			return this;
		}

		public FileInstanceCache<T> build() throws IOException {
			return new FileInstanceCache<>(this);
		}
	}
}
