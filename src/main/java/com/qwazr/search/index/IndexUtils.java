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
import com.qwazr.classloader.ClassLoaderManager;
import com.qwazr.search.analysis.AnalyzerContext;
import com.qwazr.search.field.FieldConsumer;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.utils.ClassLoaderUtils;
import com.qwazr.utils.server.ServerException;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.facet.sortedset.DefaultSortedSetDocValuesReaderState;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.index.*;
import org.apache.lucene.search.similarities.Similarity;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;

public class IndexUtils {

	static final Object addNewLuceneDocument(final AnalyzerContext context, final Map<String, Object> document,
			IndexWriter indexWriter) throws IOException {
		final FieldConsumer.FieldsDocument fields = new FieldConsumer.FieldsDocument(context.fieldTypes);
		document.forEach(fields);
		final Document doc = fields.document;
		Object id = document.get(FieldDefinition.ID_FIELD);
		if (id == null)
			id = UUIDs.timeBased();
		final String id_string = id.toString();
		doc.add(new StringField(FieldDefinition.ID_FIELD, id_string, Field.Store.NO));
		final Term termId = new Term(FieldDefinition.ID_FIELD, id_string);
		final Document facetedDoc = context.facetsConfig.build(doc);
		if (termId == null)
			indexWriter.addDocument(facetedDoc);
		else
			indexWriter.updateDocument(termId, facetedDoc);
		return id;
	}

	static final void updateDocValues(final AnalyzerContext context, final Map<String, Object> document,
			IndexWriter indexWriter) throws ServerException, IOException {
		Object id = document.get(FieldDefinition.ID_FIELD);
		if (id == null)
			throw new ServerException(Response.Status.BAD_REQUEST,
					"The field " + FieldDefinition.ID_FIELD + " is missing");
		final Term termId = new Term(FieldDefinition.ID_FIELD, id.toString());
		final FieldConsumer.FieldsCollection fields = new FieldConsumer.FieldsCollection(context.fieldTypes);
		document.forEach(fields);
		indexWriter.updateDocValues(termId, fields.toArray());
	}

	final static String[] similarityClassPrefixes = { "", "com.qwazr.search.similarity.",
			"org.apache.lucene.search.similarities." };

	final static Similarity findSimilarity(String similarity)
			throws InterruptedException, ReflectiveOperationException, IOException {
		return (Similarity) ClassLoaderUtils
				.findClass(ClassLoaderManager.classLoader, similarity, similarityClassPrefixes).newInstance();
	}

	final static SortedSetDocValuesReaderState getNewFacetsState(IndexReader indexReader) throws IOException {
		LeafReader topReader = SlowCompositeReaderWrapper.wrap(indexReader);
		if (topReader == null)
			return null;
		SortedSetDocValues dv = topReader.getSortedSetDocValues(FieldDefinition.FACET_FIELD);
		if (dv == null)
			return null;
		return new DefaultSortedSetDocValuesReaderState(indexReader, FieldDefinition.FACET_FIELD);
	}

}
