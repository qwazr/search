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
 **/
package com.qwazr.search.index;

import com.qwazr.search.field.Converters.ValueConverter;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.StoredFieldVisitor;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

abstract class ResultDocumentBuilder<T extends ResultDocumentAbstract> {

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

	abstract void setStoredFieldString(final String fieldName, final List<String> values);

	abstract void setStoredFieldBytes(final String fieldName, final List<byte[]> values);

	abstract void setStoredFieldInteger(final String fieldName, final int[] values);

	abstract void setStoredFieldLong(final String fieldName, final long[] values);

	abstract void setStoredFieldFloat(final String fieldName, final float[] values);

	abstract void setStoredFieldDouble(final String fieldName, final double[] values);

	final void extractDocValuesReturnedFields(@NotNull final Map<String, ValueConverter> returnedFields)
			throws IOException {
		returnedFields.forEach(this::setDocValuesField);
	}

	final void extractStoredReturnedFields(@NotNull final IndexSearcher searcher,
			@NotNull final Map<String, String> storedFields) throws IOException {
		final Visitor visitor = new Visitor(storedFields);
		searcher.doc(scoreDoc.doc, visitor);
		visitor.extract();
	}

	private class Visitor extends StoredFieldVisitor {

		private final Map<String, String> storedFields;
		private Map<String, List<String>> stringFields;
		private Map<String, List<byte[]>> bytesFields;
		private Map<String, long[]> longFields;
		private Map<String, int[]> intFields;
		private Map<String, float[]> floatFields;
		private Map<String, double[]> doubleFields;

		private Visitor(Map<String, String> storedFields) {
			this.storedFields = storedFields;
		}

		@Override
		public Status needsField(FieldInfo fieldInfo) throws IOException {
			return storedFields.containsKey(fieldInfo.name) ? Status.YES : Status.NO;
		}

		private String getReturnedField(String fieldInfoName) {
			return storedFields.get(fieldInfoName);
		}

		@Override
		public void binaryField(FieldInfo fieldInfo, byte[] value) throws IOException {
			if (bytesFields == null)
				bytesFields = new HashMap<>();
			bytesFields.computeIfAbsent(getReturnedField(fieldInfo.name), name -> new ArrayList<>()).add(value);
		}

		@Override
		public void stringField(FieldInfo fieldInfo, byte[] value) throws IOException {
			if (stringFields == null)
				stringFields = new HashMap<>();
			stringFields.computeIfAbsent(getReturnedField(fieldInfo.name), name -> new ArrayList<>()).add(
					new String(value, StandardCharsets.UTF_8));
		}

		@Override
		public void intField(FieldInfo fieldInfo, int value) throws IOException {
			final String fieldName = getReturnedField(fieldInfo.name);
			if (intFields == null)
				intFields = new HashMap<>();
			int[] array = intFields.get(fieldName);
			if (array == null) {
				array = new int[1];
			} else {
				final int[] newArray = new int[array.length + 1];
				System.arraycopy(array, 0, newArray, 0, array.length);
				array = newArray;
			}
			array[array.length - 1] = value;
			intFields.put(fieldName, array);
		}

		@Override
		public void longField(FieldInfo fieldInfo, long value) throws IOException {
			final String fieldName = getReturnedField(fieldInfo.name);
			if (longFields == null)
				longFields = new HashMap<>();
			long[] array = longFields.get(fieldName);
			if (array == null) {
				array = new long[1];
			} else {
				final long[] newArray = new long[array.length + 1];
				System.arraycopy(array, 0, newArray, 0, array.length);
				array = newArray;
			}
			array[array.length - 1] = value;
			longFields.put(fieldName, array);
		}

		@Override
		public void floatField(FieldInfo fieldInfo, float value) throws IOException {
			final String fieldName = getReturnedField(fieldInfo.name);
			if (floatFields == null)
				floatFields = new HashMap<>();
			float[] array = floatFields.get(fieldName);
			if (array == null) {
				array = new float[1];
			} else {
				final float[] newArray = new float[array.length + 1];
				System.arraycopy(array, 0, newArray, 0, array.length);
				array = newArray;
			}
			array[array.length - 1] = value;
			floatFields.put(fieldName, array);
		}

		@Override
		public void doubleField(FieldInfo fieldInfo, double value) throws IOException {
			final String fieldName = getReturnedField(fieldInfo.name);
			if (doubleFields == null)
				doubleFields = new HashMap<>();
			double[] array = doubleFields.get(fieldName);
			if (array == null) {
				array = new double[1];
			} else {
				final double[] newArray = new double[array.length + 1];
				System.arraycopy(array, 0, newArray, 0, array.length);
				array = newArray;
			}
			array[array.length - 1] = value;
			doubleFields.put(fieldName, array);
		}

		void extract() {
			if (stringFields != null)
				stringFields.forEach(ResultDocumentBuilder.this::setStoredFieldString);
			if (bytesFields != null)
				bytesFields.forEach(ResultDocumentBuilder.this::setStoredFieldBytes);
			if (longFields != null)
				longFields.forEach(ResultDocumentBuilder.this::setStoredFieldLong);
			if (intFields != null)
				intFields.forEach(ResultDocumentBuilder.this::setStoredFieldInteger);
			if (floatFields != null)
				floatFields.forEach(ResultDocumentBuilder.this::setStoredFieldFloat);
			if (doubleFields != null)
				doubleFields.forEach(ResultDocumentBuilder.this::setStoredFieldDouble);
		}
	}

}
