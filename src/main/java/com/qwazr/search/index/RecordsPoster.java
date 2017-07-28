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

import com.qwazr.search.field.FieldDefinition;
import com.qwazr.server.ServerException;
import com.qwazr.utils.FunctionUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

interface RecordsPoster {

	int getCount();

	abstract class CommonPoster implements RecordsPoster {

		protected final Map<String, Field> fields;
		protected final FieldMap fieldMap;
		final IndexWriter indexWriter;
		final TaxonomyWriter taxonomyWriter;
		protected int count;

		CommonPoster(final Map<String, Field> fields, final FieldMap fieldMap, final IndexWriter indexWriter,
				final TaxonomyWriter taxonomyWriter) {
			this.fields = fields;
			this.fieldMap = fieldMap;
			this.indexWriter = indexWriter;
			this.taxonomyWriter = taxonomyWriter;
			this.count = 0;
		}

		@Override
		public final int getCount() {
			return count;
		}

	}

	abstract class Documents extends CommonPoster {

		final FieldConsumer.ForDocument documentBuilder;

		private Documents(final Map<String, Field> fields, final FieldMap fieldMap, final IndexWriter indexWriter,
				final TaxonomyWriter taxonomyWriter) {
			super(fields, fieldMap, indexWriter, taxonomyWriter);
			documentBuilder = new FieldConsumer.ForDocument();
		}

		private Document getFacetedDoc() throws IOException {
			final FacetsConfig facetsConfig = fieldMap.getFacetsConfig(documentBuilder.fieldNameSet);
			return facetsConfig.build(taxonomyWriter, documentBuilder.document);
		}

		void updateDocument(Term termId) throws IOException {
			if (termId == null)
				throw new ServerException(Response.Status.BAD_REQUEST,
						() -> "The field " + FieldDefinition.ID_FIELD + " is missing - Index: " +
								indexWriter.getDirectory());
			indexWriter.updateDocument(termId, getFacetedDoc());
			count++;
			documentBuilder.reset();
		}

		void addDocument() throws IOException {
			indexWriter.addDocument(getFacetedDoc());
			count++;
			documentBuilder.reset();
		}

	}

	abstract class DocValues extends CommonPoster {

		final FieldConsumer.ForDocValues documentBuilder;

		protected DocValues(final Map<String, Field> fields, final FieldMap fieldMap, final IndexWriter indexWriter,
				final TaxonomyWriter taxonomyWriter) {
			super(fields, fieldMap, indexWriter, taxonomyWriter);
			documentBuilder = new FieldConsumer.ForDocValues();
		}

		final void updateDocValues(final Term termId) throws IOException {
			if (termId == null)
				throw new ServerException(Response.Status.BAD_REQUEST,
						() -> "The field " + FieldDefinition.ID_FIELD + " is missing - Index: " + indexWriter);
			indexWriter.updateDocValues(termId, documentBuilder.fieldList.toArray(
					new org.apache.lucene.document.Field[documentBuilder.fieldList.size()]));
			count++;
			documentBuilder.reset();
		}

	}

	interface MapDocument extends RecordsPoster, FunctionUtils.ConsumerEx<Map<String, Object>, IOException> {
	}

	final class UpdateMapDocument extends Documents implements MapDocument {

		private UpdateMapDocument(final FieldMap fieldMap, final IndexWriter indexWriter,
				final TaxonomyWriter taxonomyWriter) {
			super(null, fieldMap, indexWriter, taxonomyWriter);
		}

		@Override
		final public void accept(final Map<String, Object> document) throws IOException {
			final RecordBuilder.ForMap recordBuilder = new RecordBuilder.ForMap(fieldMap, documentBuilder);
			document.forEach(recordBuilder);
			updateDocument(recordBuilder.termId);
		}

	}

	final class AddMapDocument extends Documents implements MapDocument {

		private AddMapDocument(final FieldMap fieldMap, final IndexWriter indexWriter,
				final TaxonomyWriter taxonomyWriter) {
			super(null, fieldMap, indexWriter, taxonomyWriter);
		}

		@Override
		final public void accept(final Map<String, Object> document) throws IOException {
			final RecordBuilder.ForMap recordBuilder = new RecordBuilder.ForMap(fieldMap, documentBuilder);
			document.forEach(recordBuilder);
			addDocument();
		}
	}

	static MapDocument create(final FieldMap fieldMap, final IndexWriter indexWriter,
			final TaxonomyWriter taxonomyWriter, final boolean update) throws IOException {
		return update ? new UpdateMapDocument(fieldMap, indexWriter, taxonomyWriter) : new AddMapDocument(fieldMap,
				indexWriter, taxonomyWriter);
	}

	interface ObjectDocument extends RecordsPoster, FunctionUtils.ConsumerEx<Object, IOException> {

	}

	final class UpdateObjectDocument extends Documents implements ObjectDocument {

		private UpdateObjectDocument(final Map<String, java.lang.reflect.Field> fields, final FieldMap fieldMap,
				final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) {
			super(fields, fieldMap, indexWriter, taxonomyWriter);
		}

		@Override
		final public void accept(final Object record) throws IOException {
			final RecordBuilder.ForObject recordBuilder = new RecordBuilder.ForObject(fieldMap, documentBuilder,
					record);
			fields.forEach(recordBuilder);
			updateDocument(recordBuilder.termId);
		}
	}

	final class AddObjectDocument extends Documents implements ObjectDocument {

		private AddObjectDocument(final Map<String, java.lang.reflect.Field> fields, final FieldMap fieldMap,
				final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) {
			super(fields, fieldMap, indexWriter, taxonomyWriter);
		}

		@Override
		final public void accept(final Object record) throws IOException {
			final RecordBuilder.ForObject recordBuilder = new RecordBuilder.ForObject(fieldMap, documentBuilder,
					record);
			fields.forEach(recordBuilder);
			addDocument();
		}
	}

	static ObjectDocument create(final Map<String, Field> fields, final FieldMap fieldMap,
			final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter, final boolean update)
			throws IOException {
		return update ? new UpdateObjectDocument(fields, fieldMap, indexWriter, taxonomyWriter) : new AddObjectDocument(
				fields, fieldMap, indexWriter, taxonomyWriter);
	}

	final class UpdateMapDocValues extends DocValues implements MapDocument {

		UpdateMapDocValues(final FieldMap fieldMap, final IndexWriter indexWriter,
				final TaxonomyWriter taxonomyWriter) {
			super(null, fieldMap, indexWriter, taxonomyWriter);
		}

		@Override
		final public void accept(final Map<String, Object> document) throws IOException {
			final RecordBuilder.ForMap recordBuilder = new RecordBuilder.ForMap(fieldMap, documentBuilder);
			document.forEach(recordBuilder);
			updateDocValues(recordBuilder.termId);
		}
	}

	final class UpdateObjectDocValues extends DocValues implements ObjectDocument {

		UpdateObjectDocValues(final Map<String, Field> fields, final FieldMap fieldMap, final IndexWriter indexWriter,
				final TaxonomyWriter taxonomyWriter) {
			super(fields, fieldMap, indexWriter, taxonomyWriter);
		}

		@Override
		final public void accept(final Object record) throws IOException {
			final RecordBuilder.ForObject recordBuilder = new RecordBuilder.ForObject(fieldMap, documentBuilder,
					record);
			fields.forEach(recordBuilder);
			updateDocValues(recordBuilder.termId);
		}
	}
}
