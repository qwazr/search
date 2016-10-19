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
import com.qwazr.search.annotations.Index;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.json.JsonMapper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IndexSettingsDefinition {

	final public String similarity_class;
	final public RemoteIndex master;

	public IndexSettingsDefinition() {
		similarity_class = null;
		master = null;
	}

	public IndexSettingsDefinition(final String similaritySlass, final RemoteIndex master) {
		this.similarity_class = similaritySlass;
		this.master = master;
	}

	public IndexSettingsDefinition(final String similaritySlass, final String master)
			throws URISyntaxException {
		this.similarity_class = similaritySlass;
		this.master = RemoteIndex.build(master);
	}

	public IndexSettingsDefinition(final Index annotatedIndex) throws URISyntaxException {
		this(annotatedIndex.similarityClass(), annotatedIndex.replicationMaster());
	}

	final static IndexSettingsDefinition EMPTY = new IndexSettingsDefinition();

	public final static IndexSettingsDefinition newSettings(String jsonString) throws IOException {
		if (StringUtils.isEmpty(jsonString))
			return null;
		return JsonMapper.MAPPER.readValue(jsonString, IndexSettingsDefinition.class);
	}

	@Override
	public final boolean equals(final Object o) {
		if (o == null || !(o instanceof IndexSettingsDefinition))
			return false;
		final IndexSettingsDefinition s = (IndexSettingsDefinition) o;
		if (!Objects.equals(similarity_class, s.similarity_class))
			return false;
		if (!Objects.deepEquals(master, s.master))
			return false;
		return true;
	}
}
