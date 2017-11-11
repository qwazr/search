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

	public enum Strategy {
		full, incremental
	}

	public final Date start;
	public final Date end;
	public final long time;
	public final long bytes;
	public final String size;
	public final int ratio;
	public final Strategy strategy;

	ReplicationStatus(@JsonProperty("start") final Date start, @JsonProperty("end") final Date end,
			@JsonProperty("time") final long time, @JsonProperty("bytes") final long bytes,
			@JsonProperty("size") final String size, @JsonProperty("ratio") final int ratio,
			@JsonProperty("strategy") final Strategy strategy) {
		this.start = start;
		this.end = end;
		this.time = time;
		this.bytes = bytes;
		this.size = size;
		this.ratio = ratio;
		this.strategy = strategy;
	}

	static ReplicationStatus.Builder of(final Strategy strategy) {
		return new Builder(strategy);
	}

	static class Builder {

		final Strategy strategy;
		final Date start;
		long bytes;
		ReplicationSession session;

		Builder(Strategy strategy) {
			this.start = new Date();
			this.strategy = strategy;
		}

		Builder session(final ReplicationSession session) {
			this.session = session;
			return this;
		}

		void countSize(ReplicationProcess.Source source, String fileName) {
			final ReplicationSession.Item item = session.getItem(source, fileName);
			if (item != null && item.size != null)
				bytes += item.size;
		}

		ReplicationStatus build() {
			final Date end = new Date();
			final int ratio = bytes == 0 || session.size == 0 ? 0 : (int) ((bytes * 100) / session.size);
			return new ReplicationStatus(start, end, end.getTime() - start.getTime(), bytes,
					FileUtils.byteCountToDisplaySize(bytes), ratio, strategy);
		}

	}
}
