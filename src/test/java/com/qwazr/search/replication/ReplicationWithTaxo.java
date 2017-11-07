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

package com.qwazr.search.replication;

import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.test.AnnotatedRecord;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class ReplicationWithTaxo extends ReplicationTestManager {

	private final static String SCHEMA = "FilesReplicationNoTaxo";
	private final static String MASTER = "master";

	@Override
	public AnnotatedIndexService<AnnotatedRecord> getMaster() throws URISyntaxException {
		return new AnnotatedIndexService<>(service, AnnotatedRecord.class, SCHEMA, MASTER, IndexSettingsDefinition.of()
				.mergeScheduler(IndexSettingsDefinition.MergeScheduler.CONCURRENT)
				.enableTaxonomyIndex(true)
				.build());
	}

	@Override
	public List<AnnotatedIndexService<AnnotatedRecord>> getSlaves() throws URISyntaxException {
		return Arrays.asList(new AnnotatedIndexService<>(service, AnnotatedRecord.class, SCHEMA, "slave",
				IndexSettingsDefinition.of().master(SCHEMA, MASTER).enableTaxonomyIndex(true).build()));

	}

	@Test
	public void test() throws IOException, InterruptedException {
		super.test();
	}
}
