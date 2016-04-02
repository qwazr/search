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

import com.datastax.driver.core.utils.UUIDs;
import com.qwazr.search.analysis.AnalyzerContext;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.utils.server.ServerException;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

abstract class RecordsPoster {

	protected final Map<String, Field> fields;
	protected final AnalyzerContext context;
	protected final IndexWriter indexWriter;
	protected final Collection<Object> ids;

	RecordsPoster(Map<String, Field> fields, AnalyzerContext context, IndexWriter indexWriter, Collection<Object> ids) {
		this.fields = fields;
		this.context = context;
		this.indexWriter = indexWriter;
		this.ids = ids;
	}

	final protected void updateDocument(final Object id, final FieldConsumer.ForDocument fields) {
		final String id_string = id == null ? UUIDs.timeBased().toString() : id.toString();
		fields.document.add(new StringField(FieldDefinition.ID_FIELD, id_string, StringField.Store.NO));
		final Term termId = new Term(FieldDefinition.ID_FIELD, id_string);
		try {
			final Document facetedDoc = context.facetsConfig.build(fields.document);
			indexWriter.updateDocument(termId, facetedDoc);
		} catch (IOException e) {
			throw new ServerException(e);
		}
		ids.add(id);
	}

	final protected void updateDocValues(final Object id, final FieldConsumer.ForDocValues fields) {
		if (id == null)
			throw new ServerException(Response.Status.BAD_REQUEST,
					"The field " + FieldDefinition.ID_FIELD + " is missing");
		final Term termId = new Term(FieldDefinition.ID_FIELD, id.toString());
		try {
			indexWriter.updateDocValues(termId, fields.toArray());
		} catch (IOException e) {
			throw new ServerException(e);
		}
	}

	final static class UpdateMapDocument extends RecordsPoster implements Consumer<Map<String, Object>> {

		UpdateMapDocument(final AnalyzerContext context, final IndexWriter indexWriter) {
			super(null, context, indexWriter, new ArrayList<>());
		}

		@Override
		final public void accept(final Map<String, Object> document) {
			final FieldConsumer.ForDocument documentBuilder = new FieldConsumer.ForDocument();
			final RecordBuilder.ForMap recordBuilder = new RecordBuilder.ForMap(context.fieldTypes, documentBuilder);
			document.forEach(recordBuilder);
			updateDocument(recordBuilder.id, documentBuilder);
		}
	}

	final static class UpdateObjectDocument extends RecordsPoster implements Consumer<Object> {

		UpdateObjectDocument(final Map<String, java.lang.reflect.Field> fields, final AnalyzerContext context,
				final IndexWriter indexWriter) {
			super(fields, context, indexWriter, new ArrayList<>());
		}

		@Override
		final public void accept(final Object record) {
			final FieldConsumer.ForDocument documentBuilder = new FieldConsumer.ForDocument();
			final RecordBuilder.ForObject recordBuilder = new RecordBuilder.ForObject(context.fieldTypes,
					documentBuilder, record);
			fields.forEach(recordBuilder);
			updateDocument(recordBuilder.id, documentBuilder);
		}
	}

	final static class UpdateMapDocValues extends RecordsPoster implements Consumer<Map<String, Object>> {

		UpdateMapDocValues(final AnalyzerContext context, final IndexWriter indexWriter) {
			super(null, context, indexWriter, null);
		}

		@Override
		final public void accept(final Map<String, Object> document) {
			final FieldConsumer.ForDocValues fieldsBuilder = new FieldConsumer.ForDocValues();
			final RecordBuilder.ForMap recordBuilder = new RecordBuilder.ForMap(context.fieldTypes, fieldsBuilder);
			document.forEach(recordBuilder);
			updateDocValues(recordBuilder.id, fieldsBuilder);
		}
	}

	final static class UpdateObjectDocValues extends RecordsPoster implements Consumer<Object> {

		UpdateObjectDocValues(Map<String, Field> fields, AnalyzerContext context, IndexWriter indexWriter) {
			super(fields, context, indexWriter, null);
		}

		@Override
		final public void accept(Object record) {
			final FieldConsumer.ForDocValues fieldsBuilder = new FieldConsumer.ForDocValues();
			final RecordBuilder.ForObject recordBuilder = new RecordBuilder.ForObject(context.fieldTypes, fieldsBuilder,
					record);
			fields.forEach(recordBuilder);
			updateDocValues(recordBuilder.id, fieldsBuilder);
		}
	}
}
