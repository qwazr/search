/*
 * Copyright 2017 Emmanuel Keller / QWAZR
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

import com.qwazr.search.field.Converters.MultiReader;
import com.qwazr.search.field.Converters.ValueConverter;
import com.qwazr.search.field.FieldTypeInterface;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

abstract class ResultDocumentsList<T extends ResultDocumentAbstract>
		implements ResultDocuments<T>, ResultDocumentsInterface {

	private final IndexReader indexReader;
	private final FieldMap fieldMap;
	private final List<ResultDocumentBuilder<T>> documentsBuilder;
	private final Set<String> returnedFields;
	private final Map<String, String> storedFields;
	private final Map<String, ValueConverter> returnedFieldsConverter;
	protected final int start;

	ResultDocumentsList(final QueryContextImpl context, final QueryDefinition queryDefinition,
			final Set<String> returnedFields) {
		this.indexReader = context.indexReader;
		this.fieldMap = context.fieldMap;
		this.start = queryDefinition.getStartValue();
		this.returnedFields = returnedFields != null ?
				returnedFields :
				queryDefinition.returned_fields != null && !queryDefinition.returned_fields.isEmpty() ?
						queryDefinition.returned_fields :
						null;
		if (this.returnedFields != null && !this.returnedFields.isEmpty()) {
			this.storedFields = new HashMap<>();
			this.returnedFieldsConverter = new LinkedHashMap<>();
			final MultiReader multiReader = new MultiReader(indexReader);
			for (final String fieldName : this.returnedFields) {
				final FieldTypeInterface fieldType = fieldMap.getFieldType(null, fieldName);
				if (fieldType == null)
					continue;
				final String storedFieldName = fieldType.getStoredFieldName(fieldName);
				if (storedFieldName != null)
					storedFields.put(storedFieldName, fieldName);
				final ValueConverter converter = fieldType.getConverter(fieldName, multiReader);
				if (converter != null)
					returnedFieldsConverter.put(fieldName, converter);
			}
		} else {
			this.storedFields = null;
			this.returnedFieldsConverter = null;
		}
		this.documentsBuilder = new ArrayList<>();
	}

	protected abstract ResultDocumentBuilder<T> newResultDocumentBuilder(int absolutePos, ScoreDoc scoreDoc)
			throws IOException;

	protected abstract ResultDefinition<T> newResultDefinition(ResultDocumentsBuilder resultDocumentsBuilder,
			List<T> documents);

	@Override
	final public void doc(IndexSearcher searcher, int pos, ScoreDoc scoreDoc) throws IOException {
		final ResultDocumentBuilder<T> builder = newResultDocumentBuilder(start + pos, scoreDoc);
		if (builder == null)
			return;
		if (storedFields != null && !storedFields.isEmpty())
			builder.extractStoredReturnedFields(searcher, storedFields);
		if (returnedFieldsConverter != null && !returnedFieldsConverter.isEmpty())
			builder.extractDocValuesReturnedFields(returnedFieldsConverter);
		documentsBuilder.add(builder);
	}

	@Override
	final public void highlight(int pos, String name, String snippet) {
		documentsBuilder.get(pos).setHighlight(name, snippet);
	}

	@Override
	final public ResultDefinition<T> apply(ResultDocumentsBuilder resultDocumentsBuilder) {
		final List<T> documents = new ArrayList<>(documentsBuilder.size());
		documentsBuilder.forEach(builder -> documents.add(builder.build()));
		return newResultDefinition(resultDocumentsBuilder, documents);
	}

	@Override
	final public ResultDocumentsInterface getResultDocuments() {
		return this;
	}

}
