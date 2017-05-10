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

import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.IndexWriter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

public interface WriteContext extends IndexContext {

	IndexWriter getIndexWriter();

	TaxonomyWriter getTaxonomyWriter();

	void setLiveCommitData(Map<String, String> commitUserData, boolean doIncrementVersion);

	<T> int postDocument(final Map<String, Field> fields, final T document, final Map<String, String> commitUserData,
			boolean update) throws IOException;

	<T> int postDocuments(final Map<String, Field> fields, final Collection<T> documents,
			final Map<String, String> commitUserData, final boolean update) throws IOException;

	int postMappedDoc(final RecordsPoster.MapDocument poster, final PostDefinition.Document post) throws IOException;

	int postMappedDocs(final RecordsPoster.MapDocument poster, final PostDefinition.Documents post) throws IOException;

	int postMappedDocument(final PostDefinition.Document post) throws IOException;

	int postMappedDocuments(final PostDefinition.Documents post) throws IOException;

	<T> int updateDocValues(final Map<String, Field> fields, final T document, final Map<String, String> commitUserData)
			throws IOException;

	<T> int updateDocsValues(final Map<String, Field> fields, final Collection<T> documents,
			final Map<String, String> commitUserData) throws IOException;

	int updateMappedDocValues(final PostDefinition.Document post) throws IOException;

	int updateMappedDocsValues(final PostDefinition.Documents post) throws IOException;

}
