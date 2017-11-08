/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qwazr.search.index;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.replication.ReplicationProcess;
import com.qwazr.search.replication.ReplicationSession;
import org.apache.commons.io.FileUtils;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ReplicationStatus {

	public final Date start;
	public final Date end;
	public final long time;
	public final long bytes;
	public final String size;

	ReplicationStatus(@JsonProperty("start") final Date start, @JsonProperty("end") final Date end,
			@JsonProperty("time") final long time, @JsonProperty("bytes") final long bytes,
			@JsonProperty("size") final String size) {
		this.start = start;
		this.end = end;
		this.time = time;
		this.bytes = bytes;
		this.size = size;
	}

	static ReplicationStatus.Builder of() {
		return new Builder();
	}

	static class Builder {

		final Date start;
		long bytes;
		ReplicationSession session;

		Builder() {
			this.start = new Date();
		}

		void session(final ReplicationSession session) {
			this.session = session;
		}

		void countSize(ReplicationProcess.Source source, String fileName) {
			final Long size = session.getFileLength(source, fileName);
			if (size != null)
				bytes += size;
		}

		ReplicationStatus build() {
			final Date end = new Date();
			return new ReplicationStatus(start, end, end.getTime() - start.getTime(), bytes,
					FileUtils.byteCountToDisplaySize(bytes));
		}

	}
}
