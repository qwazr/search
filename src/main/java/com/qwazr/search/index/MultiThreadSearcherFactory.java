/**
 * Copyright 2017 Emmanuel Keller / QWAZR
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
 **/
package com.qwazr.search.index;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

class MultiThreadSearcherFactory extends SearcherFactory {

	private final ExecutorService executorService;

	MultiThreadSearcherFactory(final ExecutorService executorService) {
		this.executorService = executorService;
	}

	public IndexSearcher newSearcher(IndexReader reader, IndexReader previousReader) throws IOException {
		return new IndexSearcher(reader, executorService);
	}
}
