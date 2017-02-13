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
 **/
package com.qwazr.search.index;

import com.qwazr.search.field.FieldDefinition;
import com.qwazr.server.ServerException;
import com.qwazr.utils.HashUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

abstract class RecordsPoster {

	protected final Map<String, Field> fields;
	protected final FieldMap fieldMap;
	protected final IndexWriter indexWriter;
	protected final TaxonomyWriter taxonomyWriter;

	RecordsPoster(final Map<String, Field> fields, final FieldMap fieldMap, final IndexWriter indexWriter,
			final TaxonomyWriter taxonomyWriter) {
		this.fields = fields;
		this.fieldMap = fieldMap;
		this.indexWriter = indexWriter;
		this.taxonomyWriter = taxonomyWriter;
	}

	public abstract int writeIndex();

	private static abstract class Documents extends RecordsPoster {

		private final ConcurrentHashMap<Term, Document> documents;

		protected Documents(final Map<String, Field> fields, final FieldMap fieldMap, final IndexWriter indexWriter,
				final TaxonomyWriter taxonomyWriter) {
			super(fields, fieldMap, indexWriter, taxonomyWriter);
			this.documents = new ConcurrentHashMap<>();
		}

		protected void updateDocument(Object id, final FieldConsumer.ForDocument fields) {
			if (id == null)
				id = HashUtils.newTimeBasedUUID().toString();
			final Term termId = new Term(FieldDefinition.ID_FIELD, BytesRefUtils.fromAny(id));
			final FacetsConfig facetsConfig = fieldMap.getFacetsConfig(fields.fieldNameSet);
			try {
				final Document facetedDoc = facetsConfig.build(taxonomyWriter, fields.document);
				documents.put(termId, facetedDoc);
			} catch (IOException e) {
				throw new ServerException(e);
			}
		}

		@Override
		final public int writeIndex() {
			documents.forEach(100, (term, document) -> {
				try {
					indexWriter.updateDocument(term, document);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
			final int count = documents.size();
			documents.clear();
			return count;
		}

	}

	private static abstract class DocValues extends RecordsPoster {

		private final ConcurrentHashMap<Term, List<org.apache.lucene.document.Field>> docsValues;

		protected DocValues(final Map<String, Field> fields, final FieldMap fieldMap, final IndexWriter indexWriter,
				final TaxonomyWriter taxonomyWriter) {
			super(fields, fieldMap, indexWriter, taxonomyWriter);
			this.docsValues = new ConcurrentHashMap<>();
		}

		protected void updateDocValues(final Object id, final FieldConsumer.ForDocValues fields) {
			if (id == null)
				throw new ServerException(Response.Status.BAD_REQUEST,
						"The field " + FieldDefinition.ID_FIELD + " is missing");
			final Term termId = new Term(FieldDefinition.ID_FIELD, BytesRefUtils.fromAny(id));
			docsValues.put(termId, fields.fieldList);
		}

		@Override
		final public int writeIndex() {
			docsValues.forEach(100, (term, fieldList) -> {
				try {
					indexWriter.updateDocValues(term,
							fieldList.toArray(new org.apache.lucene.document.Field[fieldList.size()]));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
			final int count = docsValues.size();
			docsValues.clear();
			return count;
		}
	}

	final static class UpdateMapDocument extends Documents implements Consumer<Map<String, Object>> {

		UpdateMapDocument(final FieldMap fieldMap, final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) {
			super(null, fieldMap, indexWriter, taxonomyWriter);
		}

		@Override
		final public void accept(final Map<String, Object> document) {
			final FieldConsumer.ForDocument documentBuilder = new FieldConsumer.ForDocument();
			final RecordBuilder.ForMap recordBuilder = new RecordBuilder.ForMap(fieldMap, documentBuilder);
			document.forEach(recordBuilder);
			updateDocument(recordBuilder.id, documentBuilder);
		}

	}

	final static class UpdateObjectDocument extends Documents implements Consumer<Object> {

		UpdateObjectDocument(final Map<String, java.lang.reflect.Field> fields, final FieldMap fieldMap,
				final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) {
			super(fields, fieldMap, indexWriter, taxonomyWriter);
		}

		@Override
		final public void accept(final Object record) {
			final FieldConsumer.ForDocument documentBuilder = new FieldConsumer.ForDocument();
			final RecordBuilder.ForObject recordBuilder =
					new RecordBuilder.ForObject(fieldMap, documentBuilder, record);
			fields.forEach(recordBuilder);
			updateDocument(recordBuilder.id, documentBuilder);
		}
	}

	final static class UpdateMapDocValues extends DocValues implements Consumer<Map<String, Object>> {

		UpdateMapDocValues(final FieldMap fieldMap, final IndexWriter indexWriter,
				final TaxonomyWriter taxonomyWriter) {
			super(null, fieldMap, indexWriter, taxonomyWriter);
		}

		@Override
		final public void accept(final Map<String, Object> document) {
			final FieldConsumer.ForDocValues fieldsBuilder = new FieldConsumer.ForDocValues();
			final RecordBuilder.ForMap recordBuilder = new RecordBuilder.ForMap(fieldMap, fieldsBuilder);
			document.forEach(recordBuilder);
			updateDocValues(recordBuilder.id, fieldsBuilder);
		}
	}

	final static class UpdateObjectDocValues extends DocValues implements Consumer<Object> {

		UpdateObjectDocValues(final Map<String, Field> fields, final FieldMap fieldMap, final IndexWriter indexWriter,
				final TaxonomyWriter taxonomyWriter) {
			super(fields, fieldMap, indexWriter, taxonomyWriter);
		}

		@Override
		final public void accept(final Object record) {
			final FieldConsumer.ForDocValues fieldsBuilder = new FieldConsumer.ForDocValues();
			final RecordBuilder.ForObject recordBuilder = new RecordBuilder.ForObject(fieldMap, fieldsBuilder, record);
			fields.forEach(recordBuilder);
			updateDocValues(recordBuilder.id, fieldsBuilder);
		}
	}
}
