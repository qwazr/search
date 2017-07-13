/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qwazr.search.field;

import com.qwazr.search.index.BytesRefUtils;
import com.qwazr.search.index.FieldConsumer;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.utils.StringUtils;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.SortField;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;

class SmartFieldProviders {

	enum FieldPrefix {

		storedField("sto"), stringField("str"), facetField("fct"), docValues("dvl"), textField("txt"), fullField("ful");

		final String prefix;

		FieldPrefix(String prefix) {
			this.prefix = prefix;
		}
	}

	enum TypePrefix {

		textType("|TXT|"), longType("|LNG|"), intType("|INT|"), doubleType("|DBL|"), floatType("|FLT|");

		final String prefix;

		TypePrefix(String prefix) {
			this.prefix = prefix;
		}
	}

	private abstract static class FieldProviderByType {

		private final FieldPrefix fieldPrefix;

		FieldProviderByType(final FieldPrefix fieldPrefix) {
			this.fieldPrefix = fieldPrefix;
		}

		final String getTextName(String fieldName) {
			return fieldPrefix.prefix + TypePrefix.textType.prefix + fieldName;
		}

		final String getLongName(String fieldName) {
			return fieldPrefix.prefix + TypePrefix.longType.prefix + fieldName;
		}

		final String getIntegerName(String fieldName) {
			return fieldPrefix.prefix + TypePrefix.intType.prefix + fieldName;
		}

		final String getDoubleName(String fieldName) {
			return fieldPrefix.prefix + TypePrefix.doubleType.prefix + fieldName;
		}

		final String getFloatName(String fieldName) {
			return fieldPrefix.prefix + TypePrefix.floatType.prefix + fieldName;
		}

	}

	static final class StoreFieldProvider extends FieldProviderByType {

		final static StoreFieldProvider INSTANCE = new StoreFieldProvider();

		private StoreFieldProvider() {
			super(FieldPrefix.storedField);
		}

		void textField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new StoredField(getTextName(fieldName), value.toString()));
		}

		void longField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new StoredField(getLongName(fieldName), FieldUtils.getLongValue(value)));
		}

		void integerField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new StoredField(getIntegerName(fieldName), FieldUtils.getIntValue(value)));
		}

		void doubleField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new StoredField(getDoubleName(fieldName), FieldUtils.getDoubleValue(value)));
		}

		void floatField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new StoredField(getFloatName(fieldName), FieldUtils.getFloatValue(value)));
		}
	}

	static final class StringFieldProvider extends FieldProviderByType {

		final static StringFieldProvider INSTANCE = new StringFieldProvider();

		private StringFieldProvider() {
			super(FieldPrefix.stringField);
		}

		void textField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new StringField(getTextName(fieldName), value.toString(), Field.Store.NO));
		}

		Term textTerm(final String fieldName, final Object value) {
			return new Term(getTextName(fieldName), value.toString());
		}

		private BytesRef getLongValue(Object value) {
			return BytesRefUtils.fromLong(FieldUtils.getLongValue(value));
		}

		void longField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new StringField(getLongName(fieldName), getLongValue(value), Field.Store.NO));
		}

		Term longTerm(final String fieldName, final Object value) {
			return new Term(getLongName(fieldName), getLongValue(value));
		}

		private BytesRef getIntValue(Object value) {
			return BytesRefUtils.fromInteger(FieldUtils.getIntValue(value));
		}

		void integerField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new StringField(getIntegerName(fieldName), getIntValue(value), Field.Store.NO));
		}

		Term integerTerm(final String fieldName, final Object value) {
			return new Term(getIntegerName(fieldName), getIntValue(value));
		}

		private BytesRef getDoubleValue(Object value) {
			return BytesRefUtils.fromDouble(FieldUtils.getDoubleValue(value));
		}

		void doubleField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName,
					new StringField(getDoubleName(fieldName), getDoubleValue(value), Field.Store.NO));
		}

		Term doubleTerm(final String fieldName, final Object value) {
			return new Term(getDoubleName(fieldName), getDoubleValue(value));
		}

		private BytesRef getFloatValue(Object value) {
			return BytesRefUtils.fromFloat(FieldUtils.getFloatValue(value));
		}

		void floatField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new StringField(getFloatName(fieldName), getFloatValue(value), Field.Store.NO));
		}

		Term floatTerm(final String fieldName, final Object value) {
			return new Term(getFloatName(fieldName), getFloatValue(value));
		}
	}

	static final class SortedDocValuesFieldProvider extends FieldProviderByType {

		final static SortedDocValuesFieldProvider INSTANCE = new SortedDocValuesFieldProvider();

		private SortedDocValuesFieldProvider() {
			super(FieldPrefix.docValues);
		}

		void textField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName,
					new SortedDocValuesField(getTextName(fieldName), new BytesRef(value.toString())));
		}

		SortField sortTextField(final String fieldName, final QueryDefinition.SortEnum sortEnum) {
			return SortUtils.stringSortField(getTextName(fieldName), sortEnum);
		}

		void longField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName,
					new SortedNumericDocValuesField(getLongName(fieldName), FieldUtils.getLongValue(value)));
		}

		SortField sortLongField(final String fieldName, final QueryDefinition.SortEnum sortEnum) {
			return SortUtils.longSortField(getLongName(fieldName), sortEnum);
		}

		void integerField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName,
					new SortedNumericDocValuesField(getIntegerName(fieldName), FieldUtils.getIntValue(value)));
		}

		SortField sortIntegerField(final String fieldName, final QueryDefinition.SortEnum sortEnum) {
			return SortUtils.integerSortField(getIntegerName(fieldName), sortEnum);
		}

		void doubleField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new SortedNumericDocValuesField(getDoubleName(fieldName),
					NumericUtils.doubleToSortableLong(FieldUtils.getDoubleValue(value))));
		}

		SortField sortDoubleField(final String fieldName, final QueryDefinition.SortEnum sortEnum) {
			return SortUtils.doubleSortField(getDoubleName(fieldName), sortEnum);
		}

		void floatField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new SortedNumericDocValuesField(getFloatName(fieldName),
					NumericUtils.floatToSortableInt(FieldUtils.getFloatValue(value))));
		}

		SortField sortFloatField(final String fieldName, final QueryDefinition.SortEnum sortEnum) {
			return SortUtils.floatSortField(getFloatName(fieldName), sortEnum);
		}
	}

	static final class FacetFieldProvider extends FieldProviderByType {

		final static FacetFieldProvider INSTANCE = new FacetFieldProvider();

		private FacetFieldProvider() {
			super(FieldPrefix.facetField);
		}

		void textField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new SortedSetDocValuesFacetField(getTextName(fieldName), value.toString()));
		}
	}

	static final class TextFieldProvider extends FieldProviderByType {

		final static TextFieldProvider INSTANCE = new TextFieldProvider();

		private TextFieldProvider() {
			super(FieldPrefix.textField);
		}

		void textField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new TextField(getTextName(fieldName), value.toString(), Field.Store.NO));
		}
	}

	static final class FullFieldProvider extends FieldProviderByType {

		final static FullFieldProvider INSTANCE = new FullFieldProvider();

		private FullFieldProvider() {
			super(FieldPrefix.fullField);
		}

		void textField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new TextField(getTextName(StringUtils.EMPTY), value.toString(), Field.Store.NO));
		}
	}

}
