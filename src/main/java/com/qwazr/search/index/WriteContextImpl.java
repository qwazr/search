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

import com.qwazr.search.analysis.UpdatableAnalyzer;
import com.qwazr.server.ServerException;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.IndexWriter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

final class WriteContextImpl extends IndexContextImpl implements WriteContext {

	final IndexWriter indexWriter;
	final TaxonomyWriter taxonomyWriter;

	WriteContextImpl(final IndexInstance.Provider indexProvider, final ResourceLoader resourceLoader,
			final ExecutorService executorService, final UpdatableAnalyzer indexAnalyzer,
			final UpdatableAnalyzer queryAnalyzer, final FieldMap fieldMap, final IndexWriter indexWriter,
			final TaxonomyWriter taxonomyWriter) {
		super(indexProvider, resourceLoader, executorService, indexAnalyzer, queryAnalyzer, fieldMap);
		this.indexWriter = indexWriter;
		this.taxonomyWriter = taxonomyWriter;
	}

	@Override
	public IndexWriter getIndexWriter() {
		return indexWriter;
	}

	@Override
	public TaxonomyWriter getTaxonomyWriter() {
		return taxonomyWriter;
	}

	private <T> int postObjectDoc(final RecordsPoster.ObjectDocument poster, final T document,
			final Map<String, String> commitUserData) throws IOException {
		poster.accept(document);
		if (commitUserData != null)
			setLiveCommitData(commitUserData, true);
		return poster.getCount();
	}

	private <T> int postObjectDocs(final RecordsPoster.ObjectDocument poster, final Collection<T> documents,
			final Map<String, String> commitUserData) throws IOException {
		for (final Object document : documents)
			poster.accept(document);
		if (commitUserData != null)
			setLiveCommitData(commitUserData, true);
		return poster.getCount();
	}

	public final void setLiveCommitData(Map<String, String> commitUserData, boolean doIncrementVersion) {
		indexWriter.setLiveCommitData(Objects.requireNonNull(commitUserData, "commitUserData is null").entrySet(),
				doIncrementVersion);
	}

	@Override
	public final <T> int postDocument(final Map<String, Field> fields, final T document,
			final Map<String, String> commitUserData, boolean update) throws IOException {
		if (document == null)
			return 0;
		final RecordsPoster.ObjectDocument poster =
				RecordsPoster.create(fields, fieldMap, indexWriter, taxonomyWriter, update);
		return postObjectDoc(poster, document, commitUserData);
	}

	@Override
	public final <T> int postDocuments(final Map<String, Field> fields, final Collection<T> documents,
			final Map<String, String> commitUserData, final boolean update) throws IOException {
		if (documents == null || documents.isEmpty())
			return 0;
		final RecordsPoster.ObjectDocument poster =
				RecordsPoster.create(fields, fieldMap, indexWriter, taxonomyWriter, update);
		return postObjectDocs(poster, documents, commitUserData);
	}

	@Override
	public int postMappedDoc(final RecordsPoster.MapDocument poster, final PostDefinition.Document post)
			throws IOException {
		poster.accept(post.document);
		if (post.commitUserData != null)
			setLiveCommitData(post.commitUserData, true);
		return poster.getCount();
	}

	@Override
	public int postMappedDocs(final RecordsPoster.MapDocument poster, final PostDefinition.Documents post)
			throws IOException {
		for (final Map<String, Object> doc : post.documents)
			poster.accept(doc);
		if (post.commitUserData != null)
			setLiveCommitData(post.commitUserData, true);
		return poster.getCount();
	}

	@Override
	public final int postMappedDocument(final PostDefinition.Document post) throws IOException {
		if (post == null || post.document == null || post.document.isEmpty())
			return 0;
		final RecordsPoster.MapDocument poster =
				RecordsPoster.create(fieldMap, indexWriter, taxonomyWriter, post.update == null ? true : post.update);
		return postMappedDoc(poster, post);
	}

	@Override
	public final int postMappedDocuments(final PostDefinition.Documents post) throws IOException {
		if (post == null || post.documents == null || post.documents.isEmpty())
			return 0;
		final RecordsPoster.MapDocument poster =
				RecordsPoster.create(fieldMap, indexWriter, taxonomyWriter, post.update == null ? true : post.update);
		return postMappedDocs(poster, post);
	}

	@Override
	public final <T> int updateDocValues(final Map<String, Field> fields, final T document,
			final Map<String, String> commitUserData) throws IOException {
		if (document == null)
			return 0;
		final RecordsPoster.UpdateObjectDocValues poster =
				new RecordsPoster.UpdateObjectDocValues(fields, fieldMap, indexWriter, taxonomyWriter);
		return postObjectDoc(poster, document, commitUserData);
	}

	@Override
	public final <T> int updateDocsValues(final Map<String, Field> fields, final Collection<T> documents,
			final Map<String, String> commitUserData) throws IOException {
		if (documents == null || documents.isEmpty())
			return 0;
		final RecordsPoster.UpdateObjectDocValues poster =
				new RecordsPoster.UpdateObjectDocValues(fields, fieldMap, indexWriter, taxonomyWriter);
		return postObjectDocs(poster, documents, commitUserData);
	}

	@Override
	public final int updateMappedDocValues(final PostDefinition.Document post) throws IOException {
		if (post == null || post.document == null || post.document.isEmpty())
			return 0;
		final RecordsPoster.MapDocument poster =
				new RecordsPoster.UpdateMapDocValues(fieldMap, indexWriter, taxonomyWriter);
		return postMappedDoc(poster, post);
	}

	@Override
	public final int updateMappedDocsValues(final PostDefinition.Documents post) throws IOException, ServerException {
		if (post == null || post.documents == null || post.documents.isEmpty())
			return 0;
		final RecordsPoster.MapDocument poster =
				new RecordsPoster.UpdateMapDocValues(fieldMap, indexWriter, taxonomyWriter);
		return postMappedDocs(poster, post);
	}
}
