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

import com.qwazr.search.field.Converters.ValueConverter;
import com.qwazr.utils.FieldMapWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.BytesRef;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

abstract class ResultDocumentBuilder<T extends ResultDocumentAbstract> implements Consumer<IndexableField> {

	final int pos;
	final ScoreDoc scoreDoc;
	final Float percentScore;

	Map<String, String> highlights;

	protected ResultDocumentBuilder(final int pos, final ScoreDoc scoreDoc, final float maxScore) {
		this.pos = pos;
		this.scoreDoc = scoreDoc;
		if (maxScore > 0)
			this.percentScore = scoreDoc.score == 0 ? 0 : scoreDoc.score / maxScore;
		else
			this.percentScore = null;
	}

	final void setHighlight(final String name, final String snippet) {
		if (name == null || snippet == null)
			return;
		if (highlights == null)
			highlights = new LinkedHashMap<>();
		highlights.put(name, snippet);
	}

	abstract T build();

	abstract void setDocValuesField(final String fieldName, final ValueConverter converter, final int docId);

	abstract void setStoredField(final String fieldName, final Object value);

	@Override
	public final void accept(final IndexableField field) {
		Object value = getValue(field);
		if (value == null)
			return;
		setStoredField(field.name(), value);
	}

	final void setStoredFields(final Document document) {
		document.forEach(this);
	}

	private final static Object getValue(final IndexableField field) {
		if (field == null)
			return null;
		final Number n = field.numericValue();
		if (n != null)
			return n;
		final String s = field.stringValue();
		if (s != null)
			return s;
		final BytesRef b = field.binaryValue();
		if (b != null)
			return b;
		return null;
	}

	abstract static class BuilderFactory<T extends ResultDocumentAbstract> {

		abstract ResultDocumentBuilder<T> createBuilder(int pos, ScoreDoc scoreDoc, float maxScore);

		abstract ResultDocumentBuilder<T>[] createArray(int size);

		abstract ResultDefinition<T> build(ResultDefinitionBuilder<T> resultBuilder);
	}

	static class ObjectBuilderFactory<T> extends BuilderFactory<ResultDocumentObject<T>> {

		private final FieldMapWrapper<T> wrapper;

		private ObjectBuilderFactory(final FieldMapWrapper<T> wrapper) {
			this.wrapper = wrapper;
		}

		@Override
		final ResultDocumentBuilder<ResultDocumentObject<T>> createBuilder(final int pos, final ScoreDoc scoreDoc,
				final float maxScore) {
			return new ResultDocumentObject.Builder<T>(pos, scoreDoc, maxScore, wrapper);
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

		final static <T> ObjectBuilderFactory<T> createFactory(final FieldMapWrapper<?> wrapper) {
			return new ObjectBuilderFactory(wrapper);
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
