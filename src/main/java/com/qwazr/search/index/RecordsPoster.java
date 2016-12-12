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
import com.qwazr.utils.HashUtils;
import com.qwazr.utils.server.ServerException;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Consumer;

abstract class RecordsPoster {

	protected final Map<String, Field> fields;
	protected final FieldMap fieldMap;
	protected final IndexWriter indexWriter;
	protected int counter;

	RecordsPoster(final Map<String, Field> fields, final FieldMap fieldMap, final IndexWriter indexWriter) {
		this.fields = fields;
		this.fieldMap = fieldMap;
		this.indexWriter = indexWriter;
		this.counter = 0;
	}

	final protected void updateDocument(Object id, final FieldConsumer.ForDocument fields) {
		if (id == null)
			id = HashUtils.newTimeBasedUUID().toString();
		final Term termId = new Term(FieldDefinition.ID_FIELD, BytesRefUtils.fromAny(id));
		final FacetsConfig facetsConfig = fieldMap.getNewFacetsConfig(fields.fieldNameSet);
		try {
			final Document facetedDoc = facetsConfig.build(fields.document);
			indexWriter.updateDocument(termId, facetedDoc);
		} catch (IOException e) {
			throw new ServerException(e);
		}
		counter++;
	}

	final protected void updateDocValues(final Object id, final FieldConsumer.ForDocValues fields) {
		if (id == null)
			throw new ServerException(Response.Status.BAD_REQUEST,
					"The field " + FieldDefinition.ID_FIELD + " is missing");
		final Term termId = new Term(FieldDefinition.ID_FIELD, BytesRefUtils.fromAny(id));
		try {
			indexWriter.updateDocValues(termId, fields.toArray());
		} catch (IOException e) {
			throw new ServerException(e);
		}
		counter++;
	}

	final static class UpdateMapDocument extends RecordsPoster implements Consumer<Map<String, Object>> {

		UpdateMapDocument(final FieldMap fieldMap, final IndexWriter indexWriter) {
			super(null, fieldMap, indexWriter);
		}

		@Override
		final public void accept(final Map<String, Object> document) {
			final FieldConsumer.ForDocument documentBuilder = new FieldConsumer.ForDocument();
			final RecordBuilder.ForMap recordBuilder = new RecordBuilder.ForMap(fieldMap, documentBuilder);
			document.forEach(recordBuilder);
			updateDocument(recordBuilder.id, documentBuilder);
		}
	}

	final static class UpdateObjectDocument extends RecordsPoster implements Consumer<Object> {

		UpdateObjectDocument(final Map<String, java.lang.reflect.Field> fields, final FieldMap fieldMap,
				final IndexWriter indexWriter) {
			super(fields, fieldMap, indexWriter);
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

	final static class UpdateMapDocValues extends RecordsPoster implements Consumer<Map<String, Object>> {

		UpdateMapDocValues(final FieldMap fieldMap, final IndexWriter indexWriter) {
			super(null, fieldMap, indexWriter);
		}

		@Override
		final public void accept(final Map<String, Object> document) {
			final FieldConsumer.ForDocValues fieldsBuilder = new FieldConsumer.ForDocValues();
			final RecordBuilder.ForMap recordBuilder = new RecordBuilder.ForMap(fieldMap, fieldsBuilder);
			document.forEach(recordBuilder);
			updateDocValues(recordBuilder.id, fieldsBuilder);
		}
	}

	final static class UpdateObjectDocValues extends RecordsPoster implements Consumer<Object> {

		UpdateObjectDocValues(Map<String, Field> fields, FieldMap fieldMap, IndexWriter indexWriter) {
			super(fields, fieldMap, indexWriter);
		}

		@Override
		final public void accept(Object record) {
			final FieldConsumer.ForDocValues fieldsBuilder = new FieldConsumer.ForDocValues();
			final RecordBuilder.ForObject recordBuilder = new RecordBuilder.ForObject(fieldMap, fieldsBuilder, record);
			fields.forEach(recordBuilder);
			updateDocValues(recordBuilder.id, fieldsBuilder);
		}
	}
}
