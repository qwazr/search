/**
 * Copyright 2015-2016 Emmanuel Keller / QWAZR
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.qwazr.server.ServerException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class BackupStatus {

	final public Long index_version;
	final public Long taxonomy_version;
	final public Date date;
	final public Long bytes_size;
	final public Integer files_count;

	public BackupStatus() {
		index_version = null;
		taxonomy_version = null;
		date = null;
		bytes_size = null;
		files_count = null;
	}

	private BackupStatus(Long index_version, Long taxonomy_version, FileTime date, Long bytes_size,
			Integer files_count) {
		this.index_version = index_version;
		this.taxonomy_version = taxonomy_version;
		this.date = new Date(date.toMillis());
		this.bytes_size = bytes_size;
		this.files_count = files_count;
	}

	static BackupStatus newBackupStatus(final Path backupDir) throws IOException {
		if (backupDir == null)
			return null;
		final Path dataPath = backupDir.resolve(IndexFileSet.INDEX_DATA);
		final Path taxoPath = backupDir.resolve(IndexFileSet.INDEX_TAXONOMY);
		try (final Directory indexDir = FSDirectory.open(dataPath);
				final Directory taxoDir = Files.exists(taxoPath) ? FSDirectory.open(taxoPath) : null) {
			try (final DirectoryReader indexReader = DirectoryReader.open(indexDir);
					final DirectoryReader taxoReader = taxoDir == null ? null : DirectoryReader.open(taxoDir)) {
				final AtomicLong size = new AtomicLong();
				final AtomicInteger count = new AtomicInteger();
				Files.walk(backupDir).forEach(path -> {
					try {
						size.addAndGet(Files.size(path));
					} catch (IOException e) {
						throw new ServerException(e);
					}
					count.incrementAndGet();
				});
				return new BackupStatus(indexReader.getVersion(), taxoReader == null ? null : taxoReader.getVersion(),
						Files.getLastModifiedTime(backupDir), size.get(), count.get());
			}
		}
	}

	public int hashCode() {
		assert false;
		return 42;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (!(o instanceof BackupStatus))
			return false;
		BackupStatus s = (BackupStatus) o;
		if (!Objects.equals(index_version, s.index_version))
			return false;
		if (!Objects.equals(taxonomy_version, s.taxonomy_version))
			return false;
		if (!Objects.equals(date, s.date))
			return false;
		if (!Objects.equals(bytes_size, s.bytes_size))
			return false;
		return Objects.equals(files_count, s.files_count);
	}

}
