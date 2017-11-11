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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.server.ServerException;
import com.qwazr.utils.LoggerUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NoLockFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class BackupStatus {

	final private static Logger LOGGER = LoggerUtils.getLogger(BackupStatus.class);

	final public Long index_version;
	final public Long taxonomy_version;
	final public Date date;
	final public Long bytes_size;
	final public Integer files_count;

	private final int hashCode;

	@JsonCreator
	BackupStatus(@JsonProperty("index_version") Long index_version,
			@JsonProperty("taxonomy_version") Long taxonomy_version, @JsonProperty("date") Date date,
			@JsonProperty("bytes_size") Long bytes_size, @JsonProperty("files_count") Integer files_count) {
		this.index_version = index_version;
		this.taxonomy_version = taxonomy_version;
		this.date = date;
		this.bytes_size = bytes_size;
		this.files_count = files_count;

		this.hashCode = Objects.hash(date, bytes_size, files_count, index_version, taxonomy_version);
	}

	static BackupStatus newBackupStatus(final Path backupDir, final boolean extractVersion) throws IOException {
		if (backupDir == null)
			return null;

		final Path dataPath = backupDir.resolve(IndexFileSet.INDEX_DATA);
		final Path taxoPath = backupDir.resolve(IndexFileSet.INDEX_TAXONOMY);

		final AtomicLong size = new AtomicLong();
		final AtomicInteger count = new AtomicInteger();

		Files.walk(backupDir).forEach(path -> {
			try {
				size.addAndGet(Files.size(path));
			} catch (IOException e) {
				throw ServerException.of(e);
			}
			count.incrementAndGet();
		});

		final Long indexVersion;
		final Long taxonomyVersion;
		if (extractVersion) {
			indexVersion = getIndexVersion(dataPath);
			taxonomyVersion = getIndexVersion(taxoPath);
		} else {
			indexVersion = null;
			taxonomyVersion = null;
		}
		return new BackupStatus(indexVersion, taxonomyVersion,
				new Date(Files.getLastModifiedTime(backupDir).toMillis()), size.get(), count.get());
	}

	private static Long getIndexVersion(final Path indexPath) throws IOException {
		if (indexPath == null || !Files.exists(indexPath) || !Files.isDirectory(indexPath))
			return null;
		if (!Files.list(indexPath).findAny().isPresent())
			return null;
		try (final Directory indexDir = FSDirectory.open(indexPath, NoLockFactory.INSTANCE)) {
			if (!DirectoryReader.indexExists(indexDir))
				return null;
			try (final DirectoryReader indexReader = DirectoryReader.open(indexDir)) {
				return indexReader.getVersion();
			} catch (IndexNotFoundException e) {
				LOGGER.log(Level.WARNING, e, e::getMessage);
				return null;
			}
		}
	}

	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (!(o instanceof BackupStatus))
			return false;
		if (o == this)
			return true;
		final BackupStatus s = (BackupStatus) o;
		return Objects.equals(index_version, s.index_version) && Objects.equals(taxonomy_version, s.taxonomy_version) &&
				Objects.equals(date, s.date) && Objects.equals(bytes_size, s.bytes_size) &&
				Objects.equals(files_count, s.files_count);
	}

	@Override
	public String toString() {
		return "Index version: " + index_version + " - Taxo version: " + taxonomy_version + " - Date: " + date +
				" - Size: " + bytes_size + " - Count:" + files_count + " - Hash: " + hashCode;
	}

}
