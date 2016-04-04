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

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.ScoreDoc;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

abstract class ResultDocumentBuilder<T extends ResultDocumentAbstract> implements Consumer<IndexableField> {

	final int pos;
	final ScoreDoc scoreDoc;
	final Float percent_score;

	Map<String, String> highlights;

	protected ResultDocumentBuilder(final int pos, final ScoreDoc scoreDoc, final float maxScore) {
		this.pos = pos;
		this.scoreDoc = scoreDoc;
		if (maxScore > 0)
			this.percent_score = scoreDoc.score == 0 ? 0 : scoreDoc.score / maxScore;
		else
			this.percent_score = null;
	}

	final void setHighlight(final String name, final String snippet) {
		if (name == null || snippet == null)
			return;
		if (highlights == null)
			highlights = new LinkedHashMap<>();
		highlights.put(name, snippet);
	}

	abstract T build();

	abstract void setReturnedField(final String fieldName, final Object fieldValue);

	@Override
	public final void accept(final IndexableField field) {
		Object value = getValue(field);
		if (value == null)
			return;
		setReturnedField(field.name(), value);
	}

	final void setStoredFields(final Document document) {
		document.forEach(this);
	}

	private final static Object getValue(final IndexableField field) {
		if (field == null)
			return null;
		Number n = field.numericValue();
		if (n != null)
			return n;
		String s = field.stringValue();
		if (s != null)
			return s;
		return null;
	}

	abstract static class BuilderFactory<T extends ResultDocumentAbstract> {

		abstract ResultDocumentBuilder<T> createBuilder(int pos, ScoreDoc scoreDoc, float maxScore);

		abstract ResultDocumentBuilder<T>[] createArray(int size);

		abstract ResultDefinition<T> build(ResultDefinitionBuilder<T> resultBuilder);
	}

	static class ObjectBuilderFactory<T> extends BuilderFactory<ResultDocumentObject<T>> {

		private final Class<T> objectClass;
		private final Map<String, Field> fieldMap;

		private ObjectBuilderFactory(final Class<T> objectClass, final Map<String, Field> fieldMap) {
			this.objectClass = objectClass;
			this.fieldMap = fieldMap;
		}

		@Override
		final ResultDocumentBuilder<ResultDocumentObject<T>> createBuilder(final int pos, final ScoreDoc scoreDoc,
				final float maxScore) {
			return new ResultDocumentObject.Builder<>(pos, scoreDoc, maxScore, objectClass, fieldMap);
		}

		@Override
		final ResultDocumentBuilder<ResultDocumentObject<T>>[] createArray(final int size) {
			return new ResultDocumentBuilder[size];
		}

		@Override
		final ResultDefinition<ResultDocumentObject<T>> build(
				final ResultDefinitionBuilder<ResultDocumentObject<T>> resultBuilder) {
			return new ResultDefinition.WithObject(resultBuilder);
		}

		final static <T> ObjectBuilderFactory<T> createFactory(final Map<String, Field> fieldMap,
				final Class<T> objectClass) {
			return new ObjectBuilderFactory(objectClass, fieldMap);
		}

	}

	static class MapBuilderFactory extends BuilderFactory<ResultDocumentMap> {

		static final MapBuilderFactory INSTANCE = new MapBuilderFactory();

		@Override
		final ResultDocumentBuilder<ResultDocumentMap> createBuilder(final int pos, final ScoreDoc scoreDoc,
				final float maxScore) {
			return new ResultDocumentMap.Builder(pos, scoreDoc, maxScore);
		}

		@Override
		final ResultDocumentBuilder<ResultDocumentMap>[] createArray(final int size) {
			return new ResultDocumentBuilder[size];
		}

		@Override
		final ResultDefinition<ResultDocumentMap> build(
				final ResultDefinitionBuilder<ResultDocumentMap> resultBuilder) {
			return new ResultDefinition.WithMap(resultBuilder);
		}
	}

}
