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

import com.qwazr.utils.WildcardMatcher;

final class SmartFieldType extends FieldTypeAbstract<SmartFieldDefinition> {

	SmartFieldType(final WildcardMatcher wildcardMatcher, final FieldDefinition definition) {
		super(of(wildcardMatcher, (SmartFieldDefinition) definition));
	}

	@Override
	Builder<SmartFieldDefinition> setup(Builder<SmartFieldDefinition> builder) {
		if (builder.definition.stored)
			storeProvider(builder);
		if (builder.definition.index)
			indexProvider(builder);
		if (builder.definition.sort)
			sortProvider(builder);
		if (builder.definition.facet)
			facetProvider(builder);
		if (builder.definition.snippet)
			snippetProvider(builder);
		if (builder.definition.fulltext)
			fullTextProvider(builder);
		if (builder.definition.autocomplete)
			autocompleteProvider(builder);
		return builder;
	}

	static void storeProvider(final Builder<SmartFieldDefinition> builder) {
		switch (builder.definition.type) {
		case TEXT:
			builder.fieldProvider(SmartFieldProviders.StoreFieldProvider.INSTANCE::textField);
			builder.storedFieldProvider(SmartFieldProviders.StoreFieldProvider.INSTANCE::getTextName);
			break;
		case LONG:
			builder.fieldProvider(SmartFieldProviders.StoreFieldProvider.INSTANCE::longField);
			builder.storedFieldProvider(SmartFieldProviders.StoreFieldProvider.INSTANCE::getLongName);
			break;
		case INTEGER:
			builder.fieldProvider(SmartFieldProviders.StoreFieldProvider.INSTANCE::integerField);
			builder.storedFieldProvider(SmartFieldProviders.StoreFieldProvider.INSTANCE::getIntegerName);
			break;
		case DOUBLE:
			builder.fieldProvider(SmartFieldProviders.StoreFieldProvider.INSTANCE::doubleField);
			builder.storedFieldProvider(SmartFieldProviders.StoreFieldProvider.INSTANCE::getDoubleName);
			break;
		case FLOAT:
			builder.fieldProvider(SmartFieldProviders.StoreFieldProvider.INSTANCE::floatField);
			builder.storedFieldProvider(SmartFieldProviders.StoreFieldProvider.INSTANCE::getFloatName);
			break;
		default:
			break;
		}
	}

	static void indexProvider(final FieldTypeAbstract.Builder<SmartFieldDefinition> builder) {
		switch (builder.definition.type) {
		case TEXT:
			builder.fieldProvider(SmartFieldProviders.StringFieldProvider.INSTANCE::textField);
			builder.termProvider(SmartFieldProviders.StringFieldProvider.INSTANCE::textTerm);
			break;
		case LONG:
			builder.fieldProvider(SmartFieldProviders.StringFieldProvider.INSTANCE::longField);
			builder.termProvider(SmartFieldProviders.StringFieldProvider.INSTANCE::longTerm);
			break;
		case INTEGER:
			builder.fieldProvider(SmartFieldProviders.StringFieldProvider.INSTANCE::integerField);
			builder.termProvider(SmartFieldProviders.StringFieldProvider.INSTANCE::integerTerm);
			break;
		case DOUBLE:
			builder.fieldProvider(SmartFieldProviders.StringFieldProvider.INSTANCE::doubleField);
			builder.termProvider(SmartFieldProviders.StringFieldProvider.INSTANCE::doubleTerm);
			break;
		case FLOAT:
			builder.fieldProvider(SmartFieldProviders.StringFieldProvider.INSTANCE::floatField);
			builder.termProvider(SmartFieldProviders.StringFieldProvider.INSTANCE::floatTerm);
			break;
		default:
			break;
		}
	}

	static void sortProvider(final FieldTypeAbstract.Builder<SmartFieldDefinition> builder) {
		switch (builder.definition.type) {
		case TEXT:
			builder.fieldProvider(SmartFieldProviders.SortedDocValuesFieldProvider.INSTANCE::textField);
			builder.sortFieldProvider(SmartFieldProviders.SortedDocValuesFieldProvider.INSTANCE::sortTextField);
			break;
		case LONG:
			builder.fieldProvider(SmartFieldProviders.SortedDocValuesFieldProvider.INSTANCE::longField);
			builder.sortFieldProvider(SmartFieldProviders.SortedDocValuesFieldProvider.INSTANCE::sortLongField);
			break;
		case INTEGER:
			builder.fieldProvider(SmartFieldProviders.SortedDocValuesFieldProvider.INSTANCE::integerField);
			builder.sortFieldProvider(SmartFieldProviders.SortedDocValuesFieldProvider.INSTANCE::sortIntegerField);
			break;
		case DOUBLE:
			builder.fieldProvider(SmartFieldProviders.SortedDocValuesFieldProvider.INSTANCE::doubleField);
			builder.sortFieldProvider(SmartFieldProviders.SortedDocValuesFieldProvider.INSTANCE::sortDoubleField);
			break;
		case FLOAT:
			builder.fieldProvider(SmartFieldProviders.SortedDocValuesFieldProvider.INSTANCE::floatField);
			builder.sortFieldProvider(SmartFieldProviders.SortedDocValuesFieldProvider.INSTANCE::sortFloatField);
			break;
		default:
			break;
		}
	}

	static void facetProvider(final FieldTypeAbstract.Builder<SmartFieldDefinition> builder) {
		builder.fieldProvider(SmartFieldProviders.FacetFieldProvider.INSTANCE::textField);
	}

	static void fullTextProvider(final FieldTypeAbstract.Builder<SmartFieldDefinition> builder) {
		builder.fieldProvider(SmartFieldProviders.TextFieldProvider.INSTANCE::textField);
		builder.fieldProvider(SmartFieldProviders.FullFieldProvider.INSTANCE::textField);
	}

	static void autocompleteProvider(final FieldTypeAbstract.Builder<SmartFieldDefinition> builder) {
		builder.fieldProvider(SmartFieldProviders.StringFieldProvider.INSTANCE::textField);
		builder.fieldProvider(SmartFieldProviders.StoreFieldProvider.INSTANCE::textField);
	}

	static void snippetProvider(final FieldTypeAbstract.Builder<SmartFieldDefinition> builder) {
		builder.fieldProvider(SmartFieldProviders.TextFieldProvider.INSTANCE::textField);
		builder.fieldProvider(SmartFieldProviders.StoreFieldProvider.INSTANCE::textField);
	}

}
