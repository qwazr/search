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
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NoLockFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class BackupStatus {

	final private static Logger LOGGER = LoggerUtils.getLogger(BackupStatus.class);

	@JsonProperty("index_version")
	final public Long indexVersion;
	@JsonProperty("taxonomy_version")
	final public Long taxonomyVersion;
	@JsonProperty("human_date")
	final public String humanDate;
	final public Long date;
	@JsonProperty("bytes_size")
	final public Long bytesSize;
	@JsonProperty("files_count")
	final public Integer filesCount;

	private final int hashCode;

	@JsonCreator
	BackupStatus(@JsonProperty("index_version") Long indexVersion,
			@JsonProperty("taxonomy_version") Long taxonomyVersion, @JsonProperty("date") Long date,
			@JsonProperty("human_date") String humanDate, @JsonProperty("bytes_size") Long bytesSize,
			@JsonProperty("files_count") Integer filesCount) {

		this.indexVersion = indexVersion;
		this.taxonomyVersion = taxonomyVersion;
		this.humanDate = humanDate;
		this.date = date;
		this.bytesSize = bytesSize;
		this.filesCount = filesCount;

		this.hashCode = new HashCodeBuilder().append(date)
				.append(bytesSize)
				.append(indexVersion)
				.append(taxonomyVersion)
				.build();
	}

	static BackupStatus newBackupStatus(final Path backupDir, final boolean extractVersion) throws IOException {
		if (backupDir == null)
			return null;

		final Path dataPath = backupDir.resolve(IndexFileSet.INDEX_DATA);
		final Path taxoPath = backupDir.resolve(IndexFileSet.INDEX_TAXONOMY);

		final AtomicLong size = new AtomicLong();
		final AtomicInteger count = new AtomicInteger();

		final AtomicReference<FileTime> lastModified = new AtomicReference<>();

		Files.walk(backupDir).filter(p -> Files.isRegularFile(p)).forEach(path -> {
			try {
				if (Files.isHidden(path))
					return;
				size.addAndGet(Files.size(path));
				// Get the most recent modified file
				lastModified.updateAndGet(ft -> {
					try {
						final FileTime nft = Files.getLastModifiedTime(path);
						return ft != null && ft.compareTo(nft) >= 0 ? ft : nft;
					} catch (IOException e) {
						throw ServerException.of(e);
					}
				});
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

		final FileTime lastFileTime = lastModified.get();
		final Long date;
		final String humanDate;
		if (lastFileTime != null) {
			date = lastFileTime.toMillis();
			humanDate = lastFileTime.toInstant().toString();
		} else {
			date = null;
			humanDate = null;
		}
		return new BackupStatus(indexVersion, taxonomyVersion, date, humanDate, size.get(), count.get());
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
		return Objects.equals(indexVersion, s.indexVersion) && Objects.equals(taxonomyVersion, s.taxonomyVersion) &&
				Objects.equals(date, s.date) && Objects.equals(bytesSize, s.bytesSize) &&
				Objects.equals(filesCount, s.filesCount);
	}

	@Override
	public String toString() {
		return "Index version: " + indexVersion + " - Taxo version: " + taxonomyVersion + " - Date: " + date +
				" - Size: " + bytesSize + " - Count:" + filesCount + " - Hash: " + hashCode;
	}

}
