/**
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
 **/
package com.qwazr.search.index;

import com.qwazr.search.field.Converters.ValueConverter;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.BytesRef;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

abstract class ResultDocumentBuilder<T extends ResultDocumentAbstract> implements Consumer<IndexableField> {

	final int pos;
	final ScoreDoc scoreDoc;

	Map<String, String> highlights;

	ResultDocumentBuilder(final int pos, final ScoreDoc scoreDoc) {
		this.pos = pos;
		this.scoreDoc = scoreDoc;
	}

	final void setHighlight(final String name, final String snippet) {
		if (name == null || snippet == null)
			return;
		if (highlights == null)
			highlights = new LinkedHashMap<>();
		highlights.put(name, snippet);
	}

	abstract T build();

	abstract void setDocValuesField(final String fieldName, final ValueConverter converter);

	abstract void setStoredField(final String fieldName, final Object value);

	@Override
	public final void accept(final IndexableField field) {
		final Object value = getValue(field);
		if (value != null)
			setStoredField(field.name(), value);
	}

	private static Object getValue(final IndexableField field) {
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

	final void extractStoredReturnedFields(@NotNull final IndexSearcher searcher,
			@NotNull final Set<String> returnedFields) throws IOException {
		final Document doc = searcher.doc(scoreDoc.doc, returnedFields);
		doc.forEach(this);
	}

	final void extractDocValuesReturnedFields(@NotNull final Map<String, ValueConverter> returnedFields)
			throws IOException {
		returnedFields.forEach(this::setDocValuesField);
	}

}
