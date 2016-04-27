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

import com.qwazr.utils.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class RemoteIndex {

	final public String uri;
	final public String schema;
	final public String index;
	final public Integer timeout;

	public RemoteIndex() {
		uri = null;
		schema = null;
		index = null;
		timeout = null;
	}

	private RemoteIndex(String uri) throws URISyntaxException {

		URI u = new URI(uri);
		this.uri = new URI(u.getScheme(), null, u.getHost(), u.getPort(), null, null, null).toASCIIString();

		String[] pathSegments = StringUtils.split(u.getPath(), '/');
		if (pathSegments == null || pathSegments.length != 3)
			throw new IllegalArgumentException("The URL is not formatted as expected: " + uri);
		this.schema = pathSegments[1];
		this.index = pathSegments[2];

		String q = u.getQuery();
		Integer timeout = null;
		if (q != null && !q.isEmpty()) {
			String[] params = StringUtils.split(q, "&");
			for (String param : params) {
				String[] keyvalue = StringUtils.split(q, "=");
				if (keyvalue == null || keyvalue.length != 2)
					continue;
				if ("timeout".equals(keyvalue[0]))
					timeout = Integer.parseInt(keyvalue[1]);
			}
		}
		this.timeout = timeout;
	}

	public RemoteIndex(final String uri, final String schema, final String index, final Integer timeout) {
		this.uri = uri;
		this.schema = schema;
		this.index = index;
		this.timeout = timeout;
	}

	/**
	 * Build an array of RemoteIndex using an array of URL.
	 * The form of the URL should be: {protocol}://{host}:{port}/indexes/{schema}/{index}?timeout={timeout}
	 *
	 * @param remoteIndexUrl
	 * @return
	 */
	public static RemoteIndex[] build(String... remoteIndexUrl) throws URISyntaxException {
		if (remoteIndexUrl == null || remoteIndexUrl.length == 0)
			return null;
		final List<RemoteIndex> remoteIndexList = new ArrayList<>();
		for (String url : remoteIndexUrl)
			if (url != null && !url.isEmpty())
				remoteIndexList.add(new RemoteIndex(url));
		return remoteIndexList.isEmpty() ? null : remoteIndexList.toArray(new RemoteIndex[remoteIndexList.size()]);
	}

}
