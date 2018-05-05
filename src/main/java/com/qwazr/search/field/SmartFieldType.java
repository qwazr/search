/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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

	SmartFieldType(final String genericFieldName, final WildcardMatcher wildcardMatcher,
			final FieldDefinition definition) {
		super(of(genericFieldName, wildcardMatcher, (SmartFieldDefinition) definition));
	}

	@Override
	Builder<SmartFieldDefinition> setup(Builder<SmartFieldDefinition> builder) {
		if (builder.definition.stored != null && builder.definition.stored)
			storeProvider(genericFieldName, builder);
		if (builder.definition.index != null && builder.definition.index) {
			if (StringUtils.isEmpty(builder.definition.analyzer) &&
					StringUtils.isEmpty(builder.definition.queryAnalyzer))
				indexProvider(genericFieldName, builder);
			else
				fullTextProvider(genericFieldName, builder);
		}
		if (builder.definition.sort != null && builder.definition.sort)
			sortProvider(genericFieldName, builder);
		if (builder.definition.facet != null && builder.definition.facet)
			facetProvider(genericFieldName, builder);
		return builder;
	}

	private static SmartFieldDefinition.Type getType(final Builder<SmartFieldDefinition> builder) {
		return builder.definition.type == null ? SmartFieldDefinition.Type.TEXT : builder.definition.type;
	}

	static void storeProvider(final String genericFieldName, final Builder<SmartFieldDefinition> builder) {
		final SmartFieldProviders.StoreFieldProvider provider =
				new SmartFieldProviders.StoreFieldProvider(genericFieldName);
		switch (getType(builder)) {
		case TEXT:
			builder.fieldProvider(provider::textField);
			builder.storedFieldNameProvider(provider::getTextName);
			break;
		case LONG:
			builder.fieldProvider(provider::longField);
			builder.storedFieldNameProvider(provider::getLongName);
			break;
		case INTEGER:
			builder.fieldProvider(provider::integerField);
			builder.storedFieldNameProvider(provider::getIntegerName);
			break;
		case DOUBLE:
			builder.fieldProvider(provider::doubleField);
			builder.storedFieldNameProvider(provider::getDoubleName);
			break;
		case FLOAT:
			builder.fieldProvider(provider::floatField);
			builder.storedFieldNameProvider(provider::getFloatName);
			break;
		default:
			break;
		}
	}

	static void indexProvider(final String genericFieldName,
			final FieldTypeAbstract.Builder<SmartFieldDefinition> builder) {
		final SmartFieldProviders.StringFieldProvider provider =
				new SmartFieldProviders.StringFieldProvider(genericFieldName);
		switch (getType(builder)) {
		case TEXT:
			builder.fieldProvider(provider::textField);
			builder.termProvider(provider::textTerm);
			builder.queryFieldNameProvider(provider::getTextName);
			break;
		case LONG:
			builder.fieldProvider(provider::longField);
			builder.queryFieldNameProvider(provider::getLongName);
			builder.termProvider(provider::longTerm);
			break;
		case INTEGER:
			builder.fieldProvider(provider::integerField);
			builder.termProvider(provider::integerTerm);
			builder.queryFieldNameProvider(provider::getIntegerName);
			break;
		case DOUBLE:
			builder.fieldProvider(provider::doubleField);
			builder.termProvider(provider::doubleTerm);
			builder.queryFieldNameProvider(provider::getDoubleName);
			break;
		case FLOAT:
			builder.fieldProvider(provider::floatField);
			builder.termProvider(provider::floatTerm);
			builder.queryFieldNameProvider(provider::getFloatName);
			break;
		default:
			break;
		}
	}

	static void sortProvider(final String genericFieldName,
			final FieldTypeAbstract.Builder<SmartFieldDefinition> builder) {
		final SmartFieldProviders.SortedDocValuesFieldProvider provider =
				new SmartFieldProviders.SortedDocValuesFieldProvider(genericFieldName);
		switch (getType(builder)) {
		case TEXT:
			builder.fieldProvider(provider::textField);
			builder.sortFieldProvider(provider::sortTextField);
			break;
		case LONG:
			builder.fieldProvider(provider::longField);
			builder.sortFieldProvider(provider::sortLongField);
			break;
		case INTEGER:
			builder.fieldProvider(provider::integerField);
			builder.sortFieldProvider(provider::sortIntegerField);
			break;
		case DOUBLE:
			builder.fieldProvider(provider::doubleField);
			builder.sortFieldProvider(provider::sortDoubleField);
			break;
		case FLOAT:
			builder.fieldProvider(provider::floatField);
			builder.sortFieldProvider(provider::sortFloatField);
			break;
		default:
			break;
		}
	}

	static void facetProvider(final String genericFieldName,
			final FieldTypeAbstract.Builder<SmartFieldDefinition> builder) {
		final SmartFieldProviders.FacetFieldProvider provider =
				new SmartFieldProviders.FacetFieldProvider(genericFieldName);
		builder.facetConfig(((fieldName, fieldMap, facetsConfig) -> {
			final String resolvedFieldName = provider.getTextName(fieldName);
			facetsConfig.setMultiValued(resolvedFieldName, true);
			facetsConfig.setIndexFieldName(resolvedFieldName, fieldMap.sortedSetFacetField);
		}));
		builder.fieldProvider(provider::textField);
		builder.queryFieldNameProvider(provider::getTextName);
	}

	static void fullTextProvider(final String genericFieldName,
			final FieldTypeAbstract.Builder<SmartFieldDefinition> builder) {
		final SmartFieldProviders.TextFieldProvider provider =
				new SmartFieldProviders.TextFieldProvider(genericFieldName);
		builder.fieldProvider(provider::textField);
		builder.queryFieldNameProvider(provider::getTextName);
	}

}
