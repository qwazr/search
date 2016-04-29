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

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IndexSettingsDefinition {

	final public String similarity_class;
	final public RemoteIndex[] master;

	public IndexSettingsDefinition() {
		similarity_class = null;
		master = null;
	}

	public IndexSettingsDefinition(final String similarity_class, final RemoteIndex... masters) {
		this.similarity_class = similarity_class;
		this.master = masters;
	}

	public IndexSettingsDefinition(final String similarity_class, final String... masters) throws URISyntaxException {
		this.similarity_class = similarity_class;
		this.master = RemoteIndex.build(masters);
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

}
