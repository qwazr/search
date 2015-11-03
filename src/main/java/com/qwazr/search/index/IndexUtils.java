/**
 * Copyright 2015 Emmanuel Keller / QWAZR
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
import com.qwazr.utils.FileClassCompilerLoader;
import com.qwazr.utils.server.ServerException;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.similarities.Similarity;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

class IndexUtils {

	final static String FIELD_ID = "$id$";

	private final static void fieldCollection(final AnalyzerContext context, final Map<String, Object> document,
			final Consumer<Field> consumer) throws IOException {
		for (Map.Entry<String, Object> field : document.entrySet()) {
			final String fieldName = field.getKey();
			if (FIELD_ID.equals(fieldName))
				continue;
			Object fieldValue = field.getValue();
			if (fieldValue instanceof Map<?, ?>)
				fieldValue = ((Map<?, Object>) fieldValue).values();
			if (fieldValue instanceof Collection<?>) {
				for (Object val : ((Collection<Object>) fieldValue))
					consumer.accept(context.getNewLuceneField(fieldName, val));
			} else
				consumer.accept(context.getNewLuceneField(fieldName, fieldValue));
		}
	}

	private final static Document newLuceneDocument(final AnalyzerContext context, final Map<String, Object> document)
			throws IOException {
		final Document doc = new Document();
		fieldCollection(context, document, new Consumer<Field>() {
			@Override
			public void accept(Field field) {
				doc.add(field);
			}
		});
		return doc;
	}

	static final Object addNewLuceneDocument(final AnalyzerContext context, final Map<String, Object> document,
			IndexWriter indexWriter) throws IOException {
		final Document doc = newLuceneDocument(context, document);
		Object id = document.get(IndexUtils.FIELD_ID);
		if (id == null)
			id = UUIDs.timeBased();
		final String id_string = id.toString();
		doc.add(new StringField(IndexUtils.FIELD_ID, id_string, Field.Store.NO));
		final Term termId = new Term(IndexUtils.FIELD_ID, id_string);

		final Document facetedDoc = context.facetsConfig.build(doc);
		if (termId == null)
			indexWriter.addDocument(facetedDoc);
		else
			indexWriter.updateDocument(termId, facetedDoc);
		return id;
	}

	private static final Field[] newFieldList(final AnalyzerContext context, final Map<String, Object> document)
			throws IOException {
		final Field[] fields = new Field[document.size() - 1];
		final AtomicInteger i = new AtomicInteger();
		fieldCollection(context, document, new Consumer<Field>() {
			@Override
			public void accept(Field field) {
				fields[i.getAndIncrement()] = field;
			}
		});
		return fields;
	}

	static final void updateDocValues(final AnalyzerContext context, final Map<String, Object> document,
			IndexWriter indexWriter) throws ServerException, IOException {
		Object id = document.get(IndexUtils.FIELD_ID);
		if (id == null)
			throw new ServerException(Response.Status.BAD_REQUEST, "The field " + IndexUtils.FIELD_ID + " is missing");
		final Term termId = new Term(IndexUtils.FIELD_ID, id.toString());
		indexWriter.updateDocValues(termId, newFieldList(context, document));
	}

	static final Similarity createSimilarity(FileClassCompilerLoader compilerLoader, String similarity_class)
			throws InterruptedException, ReflectiveOperationException, IOException {
		if (similarity_class == null)
			return null;
		final Class<?> clazz;
		if (compilerLoader != null && similarity_class.endsWith(".java"))
			clazz = compilerLoader.loadClass(new File(similarity_class));
		else
			clazz = Class.forName(similarity_class);
		return (Similarity) clazz.newInstance();
	}
}
