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
import com.qwazr.search.field.FieldConsumer;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.utils.server.ServerException;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

abstract class DocumentPoster {

	protected final AnalyzerContext context;
	protected final IndexWriter indexWriter;
	protected final List<Object> ids = new ArrayList<>();

	DocumentPoster(AnalyzerContext context, IndexWriter indexWriter) {
		this.context = context;
		this.indexWriter = indexWriter;
	}

	static class DocumentMapPoster extends DocumentPoster implements Consumer<Map<String, Object>> {

		DocumentMapPoster(AnalyzerContext context, IndexWriter indexWriter) {
			super(context, indexWriter);
		}

		@Override
		final public void accept(Map<String, Object> document) {
			final FieldConsumer.FieldsDocument fields = new FieldConsumer.FieldsDocument(context.fieldTypes);
			document.forEach(fields);
			Document doc = fields.document;
			Object id = document.get(FieldDefinition.ID_FIELD);
			if (id == null)
				id = UUIDs.timeBased();
			final String id_string = id.toString();
			doc.add(new StringField(FieldDefinition.ID_FIELD, id_string, Field.Store.NO));
			final Term termId = new Term(FieldDefinition.ID_FIELD, id_string);
			try {
				final Document facetedDoc = context.facetsConfig.build(doc);
				if (termId == null)
					indexWriter.addDocument(facetedDoc);
				else
					indexWriter.updateDocument(termId, facetedDoc);
				ids.add(id);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	static class DocumentPojoPoster extends DocumentPoster implements Consumer<Object> {

		DocumentPojoPoster(AnalyzerContext context, IndexWriter indexWriter) {
			super(context, indexWriter);
		}

		@Override
		final public void accept(Object o) {
			//TODO Implement !
			throw new ServerException("Not yet implemented");
		}
	}

	static class DocValuesMapPoster extends DocumentPoster implements Consumer<Map<String, Object>> {

		DocValuesMapPoster(AnalyzerContext context, IndexWriter indexWriter) {
			super(context, indexWriter);
		}

		@Override
		final public void accept(Map<String, Object> document) {
			final Object id = document.get(FieldDefinition.ID_FIELD);
			if (id == null)
				throw new ServerException(Response.Status.BAD_REQUEST,
								"The field " + FieldDefinition.ID_FIELD + " is missing");
			final FieldConsumer.FieldsCollection fields = new FieldConsumer.FieldsCollection(context.fieldTypes);
			document.forEach(fields);
			final Term termId = new Term(FieldDefinition.ID_FIELD, id.toString());
			try {
				indexWriter.updateDocValues(termId, fields.toArray());
			} catch (IOException e) {
				throw new ServerException(e);
			}
		}
	}

	static class DocValuesPojoPoster extends DocumentPoster implements Consumer<Object> {

		DocValuesPojoPoster(AnalyzerContext context, IndexWriter indexWriter) {
			super(context, indexWriter);
		}

		@Override
		final public void accept(Object o) {
			//TODO Implement !
			throw new ServerException("Not yet implemented");
		}
	}
}
