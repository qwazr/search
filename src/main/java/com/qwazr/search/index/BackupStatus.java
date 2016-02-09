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
import org.apache.commons.io.filefilter.FileFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class BackupStatus {

	final public Long generation;
	final public Date date;
	final public Long bytes_size;
	final public Integer files_count;

	public BackupStatus() {
		generation = null;
		date = null;
		bytes_size = null;
		files_count = null;
	}

	BackupStatus(Long generation, long date, Long bytes_size, Integer files_count) {
		this.generation = generation;
		this.date = new Date(date);
		this.bytes_size = bytes_size;
		this.files_count = files_count;
	}

	final static BackupStatus newBackupStatus(File backupDir) {
		if (backupDir == null)
			return null;
		try {
			long generation = Long.parseLong(backupDir.getName());
			File[] files = backupDir.listFiles((FileFilter) FileFileFilter.FILE);
			long bytes_size = 0;
			if (files == null)
				return null;
			for (File file : files)
				bytes_size += file.length();
			return new BackupStatus(generation, backupDir.lastModified(), bytes_size, files.length);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static final boolean equalsNull(Object o1, Object o2) {
		if (o1 == null)
			return o2 == null;
		if (o2 == null)
			return false;
		return o1.equals(o2);
	}

	public int hashCode() {
		assert false;
		return 42;
	}

	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (!(o instanceof BackupStatus))
			return false;
		BackupStatus s = (BackupStatus) o;
		if (!equalsNull(generation, s.generation))
			return false;
		if (!equalsNull(date, s.date))
			return false;
		if (!equalsNull(bytes_size, s.bytes_size))
			return false;
		return equalsNull(bytes_size, s.bytes_size);
	}

}
