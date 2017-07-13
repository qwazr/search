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

import com.qwazr.search.index.FieldConsumer;
import com.qwazr.utils.WildcardMatcher;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.Term;

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
		return builder;
	}

	abstract static class FieldProviderByType {

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

		abstract void textField(final String fieldName, final Object value, final FieldConsumer consumer);

		abstract void longField(final String fieldName, final Object value, final FieldConsumer consumer);

		abstract void integerField(final String fieldName, final Object value, final FieldConsumer consumer);

		abstract void doubleField(final String fieldName, final Object value, final FieldConsumer consumer);

		abstract void floatField(final String fieldName, final Object value, final FieldConsumer consumer);

	}

	static final class StoreFieldProvider extends FieldProviderByType {

		static StoreFieldProvider INSTANCE = new StoreFieldProvider();

		void provider(Builder<SmartFieldDefinition> builder) {
			super.provider(builder);
			switch (builder.definition.type) {
			case TEXT:
				builder.storedFieldProvider(this::textStoredField);
				break;
			case LONG:
				builder.storedFieldProvider(this::longStoredField);
				break;
			case INTEGER:
				builder.storedFieldProvider(this::integerStoredField);
				break;
			case DOUBLE:
				builder.storedFieldProvider(this::doubleStoredField);
				break;
			case FLOAT:
				builder.storedFieldProvider(this::floatStoredField);
				break;
			default:
				break;
			}
		}

		String textStoredField(final String fieldName) {
			return "sto|str|" + fieldName;
		}

		@Override
		void textField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new StoredField(textStoredField(fieldName), value.toString()));
		}

		String longStoredField(final String fieldName) {
			return "sto|lng|" + fieldName;
		}

		@Override
		void longField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new StoredField(longStoredField(fieldName), FieldUtils.getLongValue(value)));
		}

		String integerStoredField(final String fieldName) {
			return "sto|int|" + fieldName;
		}

		@Override
		void integerField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new StoredField(integerStoredField(fieldName), FieldUtils.getIntValue(value)));
		}

		String doubleStoredField(final String fieldName) {
			return "sto|dbl|" + fieldName;
		}

		@Override
		void doubleField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new StoredField(doubleStoredField(fieldName), FieldUtils.getDoubleValue(value)));
		}

		String floatStoredField(final String fieldName) {
			return "sto|flt|" + fieldName;
		}

		@Override
		void floatField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new StoredField(floatStoredField(fieldName), FieldUtils.getFloatValue(value)));
		}
	}

	static final class IndexFieldProvider extends FieldProviderByType {

		static IndexFieldProvider INSTANCE = new IndexFieldProvider();

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
			consumer.accept(fieldName, new StringField("idx|str|" + fieldName, value.toString(), Field.Store.NO));
		}

		Term textTerm(final String fieldName, final Object value) {
			return new Term("idx|str|" + fieldName, value.toString());
		}

		private LongPoint getLongPoint(String fieldName, final Object value) {
			return new LongPoint("idx|lng|" + fieldName, FieldUtils.getLongValue(value));
		}

		@Override
		void longField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, getLongPoint(fieldName, value));
		}

		Term longTerm(final String fieldName, final Object value) {
			final LongPoint longPoint = getLongPoint(fieldName, value);
			return new Term(longPoint.name(), longPoint.binaryValue());
		}

		private IntPoint getIntPoint(String fieldName, final Object value) {
			return new IntPoint("idx|int|" + fieldName, FieldUtils.getIntValue(value));
		}

		@Override
		void integerField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, getIntPoint(fieldName, value));
		}

		Term integerTerm(final String fieldName, final Object value) {
			final IntPoint intPoint = getIntPoint(fieldName, value);
			return new Term(intPoint.name(), intPoint.binaryValue());
		}

		private DoublePoint getDoublePoint(String fieldName, final Object value) {
			return new DoublePoint("idx|dbl|" + fieldName, FieldUtils.getDoubleValue(value));
		}

		@Override
		void doubleField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, getDoublePoint(fieldName, value));
		}

		Term doubleTerm(final String fieldName, final Object value) {
			final DoublePoint doublePoint = getDoublePoint(fieldName, value);
			return new Term(doublePoint.name(), doublePoint.binaryValue());
		}

		private FloatPoint getFloatPoint(String fieldName, final Object value) {
			return new FloatPoint("idx|flt|" + fieldName, FieldUtils.getFloatValue(value));
		}

		@Override
		void floatField(final String fieldName, final Object value, final FieldConsumer consumer) {
			consumer.accept(fieldName, new FloatPoint("idx|flt|" + fieldName, FieldUtils.getFloatValue(value)));
		}

		Term floatTerm(final String fieldName, final Object value) {
			final FloatPoint floatPoint = getFloatPoint(fieldName, value);
			return new Term(floatPoint.name(), floatPoint.binaryValue());
		}

	}

}
