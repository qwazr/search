/*
 * Copyright 2016-2017 Emmanuel Keller / QWAZR
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
package com.qwazr.cluster;

import com.qwazr.server.RemoteService;
import org.apache.commons.lang3.NotImplementedException;

import java.net.URISyntaxException;
import java.util.Collection;

public interface ServiceBuilderInterface<T> {

	T getService(String node) throws URISyntaxException;

	T getService(Collection<String> nodes) throws URISyntaxException;

	T getActive(String group) throws URISyntaxException;

	T getRandom(String group) throws URISyntaxException;

	T getLeader(String group) throws URISyntaxException;

	default T local() {
		throw new NotImplementedException("No local service");
	}

	default T remote(RemoteService remote) {
		throw new NotImplementedException("No single-node remote service");
	}

	default T remotes(RemoteService... remotes) {
		throw new NotImplementedException("No multi-node remote service");
	}
}
