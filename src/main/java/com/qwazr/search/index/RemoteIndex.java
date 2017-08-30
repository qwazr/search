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
import com.qwazr.server.RemoteService;
import com.qwazr.utils.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RemoteIndex extends RemoteService {

	final public String schema;
	final public String index;

	@JsonCreator
	RemoteIndex(@JsonProperty("scheme") String scheme, @JsonProperty("host") String host,
			@JsonProperty("port") Integer port, @JsonProperty("path") String path,
			@JsonProperty("timeout") Integer timeout, @JsonProperty("username") String username,
			@JsonProperty("password") String password, @JsonProperty("schema") String schema,
			@JsonProperty("index") String index) {
		super(scheme, host, port, path, timeout, username, password);
		this.schema = schema;
		this.index = index;
	}

	public RemoteIndex(final RemoteService.Builder builder, final String schema, final String index) {
		super(builder);
		this.schema = schema;
		this.index = index;
	}

	RemoteIndex(String schema, String index) {
		super(null, null, null, null, null, null, null);
		this.schema = schema;
		this.index = index;
	}

	/**
	 * Build a RemoteIndex using the given URL.
	 * The form of the URL should be:
	 * {protocol}://{username:password@}{host}:{port}/indexes/{schema}/{index}?timeout={timeout}
	 *
	 * @param remoteIndexPattern the URL of the remote index
	 * @return an array of RemoteIndex
	 * @throws URISyntaxException if the URI syntax is not correct
	 */

	public static RemoteIndex build(final String remoteIndexPattern) throws URISyntaxException {

		if (StringUtils.isEmpty(remoteIndexPattern))
			return null;

		if (remoteIndexPattern.contains("://") || remoteIndexPattern.startsWith("//")) {

			final URI uri = URI.create(remoteIndexPattern);

			final String[] paths = StringUtils.split(uri.getPath(), '/');
			if (paths.length != 3 || !IndexServiceInterface.PATH.equals(paths[0]))
				throw new URISyntaxException(remoteIndexPattern,
						"The URL form should be: http://hostname/" + IndexServiceInterface.PATH + "/{schema}/{index}?" +
								TIMEOUT_PARAMETER + "={timeout}");

			final RemoteService.Builder builder = RemoteService.of(uri);
			builder.setPath(null);
			return new RemoteIndex(builder, paths[1], paths[2]);
		} else {
			final String[] paths = StringUtils.split(remoteIndexPattern, '/');
			if (paths.length == 2)
				return new RemoteIndex(paths[0], paths[1]);
		}
		throw new IllegalArgumentException(
				"The index pattern should be in the local form {schema}/{index}, or using an URL");
	}

}
