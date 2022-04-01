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
package com.qwazr.search.index;

import com.qwazr.cluster.ClusterManager;
import com.qwazr.cluster.ServiceBuilderAbstract;
import com.qwazr.server.RemoteService;

public class IndexServiceBuilder extends ServiceBuilderAbstract<IndexServiceInterface> {

    public IndexServiceBuilder(final ClusterManager clusterManager, final IndexManager indexManager) {
		super(clusterManager, IndexServiceInterface.SERVICE_NAME,
				indexManager == null ? null : indexManager.getService());
	}

	@Override
	public IndexServiceInterface remote(RemoteService remote) {
		return new IndexSingleClient(remote);
	}
}
