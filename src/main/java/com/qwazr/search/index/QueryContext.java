/**
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

import com.qwazr.utils.FieldMapWrapper;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;

import java.io.IOException;

public interface QueryContext extends IndexContext {

	default IndexReader getIndexReader() {
		return null;
	}

	default IndexSearcher getIndexSearcher() {
		return null;
	}

	default ResultDefinition.WithMap searchMap(QueryDefinition queryDefinition) throws IOException {
		throw new NotImplementedException("Not available");
	}

	default <T> ResultDefinition.WithObject<T> searchObject(QueryDefinition queryDefinition,
			final FieldMapWrapper<T> wrapper) throws IOException {
		throw new NotImplementedException("Not available");
	}

	default <T> ResultDefinition.WithObject<T> searchObject(QueryDefinition queryDefinition, Class<T> objectClass)
			throws IOException {
		throw new NotImplementedException("Not available");
	}

	default ResultDefinition.Empty searchInterface(final QueryDefinition queryDefinition,
			final ResultDocumentsInterface resultDocuments) throws IOException {
		throw new NotImplementedException("Not available");
	}

	QueryContext DEFAULT = new QueryContext() {
	};

}
