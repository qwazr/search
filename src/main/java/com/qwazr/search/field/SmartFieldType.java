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
 */
package com.qwazr.search.field;

import com.qwazr.search.index.BytesRefUtils;
import com.qwazr.search.index.FieldConsumer;
import com.qwazr.utils.WildcardMatcher;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetField;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;

final class SmartFieldType extends FieldTypeAbstract<SmartFieldDefinition> {

	SmartFieldType(final WildcardMatcher wildcardMatcher, final FieldDefinition definition) {
		super(of(wildcardMatcher, (SmartFieldDefinition) definition));
	}

	@Override
	Builder<SmartFieldDefinition> setup(Builder<SmartFieldDefinition> builder) {
		if (builder.definition.stored)
			StoreFieldProvider.INSTANCE.provider(builder);
		if (builder.definition.index)
			IndexFieldProvider.INSTANCE.provider(builder);
		if (builder.definition.sort)
			SortedFieldProvider.INSTANCE.provider(builder);
		if (builder.definition.facet)
			FacetFieldProvider.INSTANCE.provider(builder);
		if (builder.definition.snippet)
			SnippetFieldProvider.INSTANCE.provider(builder);
		if (builder.definition.fulltext)
			FullTextFieldProvider.INSTANCE.provider(builder);
		if (builder.definition.autocomplete)
			AutoCompleteFieldProvider.INSTANCE.provider(builder);
		return builder;
	}

	enum TypePrefix {
		sto /*Stored*/,
		idx /*Indexed*/,
		sdv /*sorteddocvalue*/,
		fct /*facet*/,
		ful /*fulltext*/,
		aut /*autocomplete*/,
		sni /*snippet*/
	}

	abstract static class FieldProviderByType {

		private final String textPrefix;
		private final String longPrefix;
		private final String intPrefix;
		private final String doublePrefix;
		private final String floatPrefix;

		FieldProviderByType(TypePrefix typePrefix) {
			this.textPrefix = typePrefix.name().concat("|TXT|");
			this.longPrefix = typePrefix.name().concat("|LNG|");
			this.intPrefix = typePrefix.name().concat("|INT|");
			this.doublePrefix = typePrefix.name().concat("|DBL|");
			this.floatPrefix = typePrefix.name().concat("|FLT|");
		}

		void provider(Builder<SmartFieldDefinition> builder) {
			switch (builder.definition.type) {
			case TEXT:
				builder.fieldProvider(this::textField);
				break;
			case LONG:
				builder.fieldProvider(this::longField);
				break;
			case INTEGER:
				builder.fieldProvider(this::integerField);
				break;
			case DOUBLE:
				builder.fieldProvider(this::doubleField);
				break;
			case FLOAT:
				builder.fieldProvider(this::floatField);
				break;
			default:
				break;
			}
		}

		final String getTextName(final String fieldName) {
			return textPrefix.concat(fieldName);
		}

		abstract void textField(final String fieldName, final Object value, final FieldConsumer consumer);

		final String getLongName(final String fieldName) {
			return longPrefix.concat(fieldName);
		}

		abstract void longField(final String fieldName, final Object value, final FieldConsumer consumer);

		final String getIntegerName(final String fieldName) {
			return intPrefix.concat(fieldName);
		}

		abstract void integerField(final String fieldName, final Object value, final FieldConsumer consumer);

		final String getDoubleName(final String fieldName) {
			return doublePrefix.concat(fieldName);
		}

		abstract void doubleField(final String fieldName, final Object value, final FieldConsumer consumer);

		final String getFloatName(final String fieldName) {
			return floatPrefix.concat(fieldName);
		}

		abstract void floatField(final String fieldName, final Object value, final FieldConsumer consumer);

	}

	static final class StoreFieldProvider extends FieldProviderByType {

		final static StoreFieldProvider INSTANCE = new StoreFieldProvider();

		StoreFieldProvider() {
			super(TypePrefix.sto);
		}

		void provider(Builder<SmartFieldDefinition> builder) {
			super.provider(builder);
			switch (builder.definition.type) {
			case TEXT:
				builder.storedFieldProvider(this::getTextName);
				break;
			case LONG:
				builder.storedFieldProvider(this::getLongName);
				break;
			case INTEGER:
				builder.storedFieldProvider(this::getIntegerName);
				break;
			case DOUBLE:
				builder.storedFieldProvider(this::getDoubleName);
				break;
			case FLOAT:
				builder.storedFieldProvider(this::getFloatName);
				break;
			default:
				break;
			}
		}

		@Override
		void textField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new StoredField(getTextName(fieldName), value.toString()));
		}

		@Override
		void longField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new StoredField(getLongName(fieldName), FieldUtils.getLongValue(value)));
		}

		@Override
		void integerField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new StoredField(getIntegerName(fieldName), FieldUtils.getIntValue(value)));
		}

		@Override
		void doubleField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new StoredField(getDoubleName(fieldName), FieldUtils.getDoubleValue(value)));
		}

		@Override
		void floatField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new StoredField(getFloatName(fieldName), FieldUtils.getFloatValue(value)));
		}
	}

	static final class IndexFieldProvider extends FieldProviderByType {

		final static IndexFieldProvider INSTANCE = new IndexFieldProvider();

		IndexFieldProvider() {
			super(TypePrefix.idx);
		}

		void provider(Builder<SmartFieldDefinition> builder) {
			super.provider(builder);
			switch (builder.definition.type) {
			case TEXT:
				builder.termProvider(this::textTerm);
				break;
			case LONG:
				builder.termProvider(this::longTerm);
				break;
			case INTEGER:
				builder.termProvider(this::integerTerm);
				break;
			case DOUBLE:
				builder.termProvider(this::doubleTerm);
				break;
			case FLOAT:
				builder.termProvider(this::floatTerm);
				break;
			default:
				break;
			}
		}

		@Override
		void textField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new StringField(getTextName(fieldName), value.toString(), Field.Store.NO));
		}

		Term textTerm(final String fieldName, final Object value) {
			return new Term(getTextName(fieldName), value.toString());
		}

		private BytesRef getLongValue(Object value) {
			return BytesRefUtils.fromLong(FieldUtils.getLongValue(value));
		}

		@Override
		void longField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new StringField(getLongName(fieldName), getLongValue(value), Field.Store.NO));
		}

		Term longTerm(final String fieldName, final Object value) {
			return new Term(getLongName(fieldName), getLongValue(value));
		}

		private BytesRef getIntValue(Object value) {
			return BytesRefUtils.fromInteger(FieldUtils.getIntValue(value));
		}

		@Override
		void integerField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new StringField(getIntegerName(fieldName), getIntValue(value), Field.Store.NO));
		}

		Term integerTerm(final String fieldName, final Object value) {
			return new Term(getIntegerName(fieldName), getIntValue(value));
		}

		private BytesRef getDoubleValue(Object value) {
			return BytesRefUtils.fromDouble(FieldUtils.getDoubleValue(value));
		}

		@Override
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

		@Override
		void floatField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new StringField(getFloatName(fieldName), getFloatValue(value), Field.Store.NO));
		}

		Term floatTerm(final String fieldName, final Object value) {
			return new Term(getFloatName(fieldName), getFloatValue(value));
		}

	}

	static final class SortedFieldProvider extends FieldProviderByType {

		final static SortedFieldProvider INSTANCE = new SortedFieldProvider();

		SortedFieldProvider() {
			super(TypePrefix.sdv);
		}

		@Override
		void textField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName,
					new SortedDocValuesField(getTextName(fieldName), new BytesRef(value.toString())));
		}

		@Override
		void longField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName,
					new SortedNumericDocValuesField(getLongName(fieldName), FieldUtils.getLongValue(value)));
		}

		@Override
		void integerField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName,
					new SortedNumericDocValuesField(getIntegerName(fieldName), FieldUtils.getIntValue(value)));
		}

		@Override
		void doubleField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new SortedNumericDocValuesField(getDoubleName(fieldName),
					NumericUtils.doubleToSortableLong(FieldUtils.getDoubleValue(value))));
		}

		@Override
		void floatField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new SortedNumericDocValuesField(getFloatName(fieldName),
					NumericUtils.floatToSortableInt(FieldUtils.getFloatValue(value))));
		}
	}

	static abstract class CommonTextFieldProvider extends FieldProviderByType {

		CommonTextFieldProvider(final TypePrefix typePrefix) {
			super(typePrefix);
		}

		@Override
		final void longField(final String fieldName, final Object value, final FieldConsumer consumer) {
			textField(fieldName, value, consumer);
		}

		@Override
		final void integerField(final String fieldName, final Object value, final FieldConsumer consumer) {
			textField(fieldName, value, consumer);
		}

		@Override
		final void doubleField(final String fieldName, final Object value, final FieldConsumer consumer) {
			textField(fieldName, value, consumer);
		}

		@Override
		final void floatField(final String fieldName, final Object value, final FieldConsumer consumer) {
			textField(fieldName, value, consumer);
		}
	}

	static final class FacetFieldProvider extends CommonTextFieldProvider {

		final static FacetFieldProvider INSTANCE = new FacetFieldProvider();

		FacetFieldProvider() {
			super(TypePrefix.fct);
		}

		@Override
		void textField(String fieldName, Object value, FieldConsumer consumer) {
			consumer.accept(fieldName, new SortedSetDocValuesFacetField(getTextName(fieldName), value.toString()));
		}

	}

	static final class FullTextFieldProvider extends CommonTextFieldProvider {

		final static FullTextFieldProvider INSTANCE = new FullTextFieldProvider();

		FullTextFieldProvider() {
			super(TypePrefix.ful);
		}

		@Override
		void textField(String fieldName, Object value, FieldConsumer consumer) {
			consumer.accept(fieldName, new TextField(getTextName(fieldName), value.toString(), Field.Store.NO));
		}
	}

	static final class AutoCompleteFieldProvider extends CommonTextFieldProvider {

		final static AutoCompleteFieldProvider INSTANCE = new AutoCompleteFieldProvider();

		AutoCompleteFieldProvider() {
			super(TypePrefix.aut);
		}

		@Override
		void textField(String fieldName, Object value, FieldConsumer consumer) {
			consumer.accept(fieldName, new StringField(getTextName(fieldName), value.toString(), Field.Store.YES));
		}
	}

	static final class SnippetFieldProvider extends CommonTextFieldProvider {

		final static SnippetFieldProvider INSTANCE = new SnippetFieldProvider();

		SnippetFieldProvider() {
			super(TypePrefix.sni);
		}

		@Override
		void textField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new TextField(getTextName(fieldName), value.toString(), Field.Store.YES));
		}
	}
}
