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

import com.qwazr.utils.StringUtils;
import com.qwazr.utils.WildcardMatcher;

final class SmartFieldType extends FieldTypeAbstract<SmartFieldDefinition> {

	SmartFieldType(final WildcardMatcher wildcardMatcher, final FieldDefinition definition) {
		super(of(wildcardMatcher, (SmartFieldDefinition) definition));
	}

	@Override
	Builder<SmartFieldDefinition> setup(Builder<SmartFieldDefinition> builder) {
		if (builder.definition.stored != null && builder.definition.stored)
			storeProvider(builder);
		if (builder.definition.index != null && builder.definition.index) {
			if (StringUtils.isEmpty(builder.definition.analyzer) && StringUtils.isEmpty(
					builder.definition.queryAnalyzer))
				indexProvider(builder);
			else
				fullTextProvider(builder);
		}
		if (builder.definition.sort != null && builder.definition.sort)
			sortProvider(builder);
		if (builder.definition.facet != null && builder.definition.facet)
			facetProvider(builder);
		return builder;
	}

	private static SmartFieldDefinition.Type getType(final Builder<SmartFieldDefinition> builder) {
		return builder.definition.type == null ? SmartFieldDefinition.Type.TEXT : builder.definition.type;
	}

	static void storeProvider(final Builder<SmartFieldDefinition> builder) {
		switch (getType(builder)) {
		case TEXT:
			builder.fieldProvider(SmartFieldProviders.StoreFieldProvider.INSTANCE::textField);
			builder.storedFieldNameProvider(SmartFieldProviders.StoreFieldProvider.INSTANCE::getTextName);
			break;
		case LONG:
			builder.fieldProvider(SmartFieldProviders.StoreFieldProvider.INSTANCE::longField);
			builder.storedFieldNameProvider(SmartFieldProviders.StoreFieldProvider.INSTANCE::getLongName);
			break;
		case INTEGER:
			builder.fieldProvider(SmartFieldProviders.StoreFieldProvider.INSTANCE::integerField);
			builder.storedFieldNameProvider(SmartFieldProviders.StoreFieldProvider.INSTANCE::getIntegerName);
			break;
		case DOUBLE:
			builder.fieldProvider(SmartFieldProviders.StoreFieldProvider.INSTANCE::doubleField);
			builder.storedFieldNameProvider(SmartFieldProviders.StoreFieldProvider.INSTANCE::getDoubleName);
			break;
		case FLOAT:
			builder.fieldProvider(SmartFieldProviders.StoreFieldProvider.INSTANCE::floatField);
			builder.storedFieldNameProvider(SmartFieldProviders.StoreFieldProvider.INSTANCE::getFloatName);
			break;
		default:
			break;
		}
	}

	static void indexProvider(final FieldTypeAbstract.Builder<SmartFieldDefinition> builder) {
		switch (getType(builder)) {
		case TEXT:
			builder.fieldProvider(SmartFieldProviders.StringFieldProvider.INSTANCE::textField);
			builder.termProvider(SmartFieldProviders.StringFieldProvider.INSTANCE::textTerm);
			builder.queryFieldNameProvider(SmartFieldProviders.StringFieldProvider.INSTANCE::getTextName);
			break;
		case LONG:
			builder.fieldProvider(SmartFieldProviders.StringFieldProvider.INSTANCE::longField);
			builder.queryFieldNameProvider(SmartFieldProviders.StringFieldProvider.INSTANCE::getLongName);
			builder.termProvider(SmartFieldProviders.StringFieldProvider.INSTANCE::longTerm);
			break;
		case INTEGER:
			builder.fieldProvider(SmartFieldProviders.StringFieldProvider.INSTANCE::integerField);
			builder.termProvider(SmartFieldProviders.StringFieldProvider.INSTANCE::integerTerm);
			builder.queryFieldNameProvider(SmartFieldProviders.StringFieldProvider.INSTANCE::getIntegerName);
			break;
		case DOUBLE:
			builder.fieldProvider(SmartFieldProviders.StringFieldProvider.INSTANCE::doubleField);
			builder.termProvider(SmartFieldProviders.StringFieldProvider.INSTANCE::doubleTerm);
			builder.queryFieldNameProvider(SmartFieldProviders.StringFieldProvider.INSTANCE::getDoubleName);
			break;
		case FLOAT:
			builder.fieldProvider(SmartFieldProviders.StringFieldProvider.INSTANCE::floatField);
			builder.termProvider(SmartFieldProviders.StringFieldProvider.INSTANCE::floatTerm);
			builder.queryFieldNameProvider(SmartFieldProviders.StringFieldProvider.INSTANCE::getFloatName);
			break;
		default:
			break;
		}
	}

	static void sortProvider(final FieldTypeAbstract.Builder<SmartFieldDefinition> builder) {
		switch (getType(builder)) {
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
		builder.facetConfig(((fieldName, fieldMap, facetsConfig) -> {
			final String resolvedFieldName = SmartFieldProviders.FacetFieldProvider.INSTANCE.getTextName(fieldName);
			facetsConfig.setMultiValued(resolvedFieldName, true);
			facetsConfig.setIndexFieldName(resolvedFieldName, fieldMap.sortedSetFacetField);
		}));
		builder.fieldProvider(SmartFieldProviders.FacetFieldProvider.INSTANCE::textField);
		builder.queryFieldNameProvider(SmartFieldProviders.FacetFieldProvider.INSTANCE::getTextName);
	}

	static void fullTextProvider(final FieldTypeAbstract.Builder<SmartFieldDefinition> builder) {
		builder.fieldProvider(SmartFieldProviders.TextFieldProvider.INSTANCE::textField);
		builder.queryFieldNameProvider(SmartFieldProviders.TextFieldProvider.INSTANCE::getTextName);
	}

	public static FieldTypeInterface build(final WildcardMatcher wildcardMatcher,
			final SmartFieldDefinition definition) {
		return new SmartFieldType(wildcardMatcher, definition);
	}
}
