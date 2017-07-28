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
package com.qwazr.search.index;

import com.qwazr.search.field.CopyToFieldType;
import com.qwazr.search.field.CustomFieldDefinition;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.field.FieldTypeInterface;
import com.qwazr.utils.WildcardMatcher;
import org.apache.lucene.facet.FacetsConfig;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class FieldMap {

	private final LinkedHashMap<String, FieldDefinition> fieldDefinitionMap;
	private final HashMap<String, FieldTypeInterface> nameDefMap;
	private final HashMap<WildcardMatcher, FieldTypeInterface> wildcardMap;
	private final FacetsConfig facetsConfig;
	public final String sortedSetFacetField;

	FieldMap(final LinkedHashMap<String, FieldDefinition> fieldDefinitionMap, final String sortedSetFacetField) {

		this.sortedSetFacetField =
				sortedSetFacetField == null ? FieldDefinition.DEFAULT_SORTEDSET_FACET_FIELD : sortedSetFacetField;

		nameDefMap = new HashMap<>();
		wildcardMap = new HashMap<>();

		fieldDefinitionMap.forEach((name, definition) -> {
			if (name.indexOf('*') != -1 || name.indexOf('?') != -1) {
				final WildcardMatcher wildcardMatcher = new WildcardMatcher(name);
				wildcardMap.put(wildcardMatcher, FieldTypeInterface.build(wildcardMatcher, definition));
			} else {
				final FieldTypeInterface fieldType = FieldTypeInterface.build(null, definition);
				nameDefMap.put(name, fieldType);
			}
		});

		// Handle copy-to
		final HashMap<String, FieldTypeInterface> newFields = new HashMap<>();
		nameDefMap.forEach((name, fieldType) -> {
			final FieldDefinition definition = fieldType.getDefinition();
			if (definition.copyFrom == null)
				return;
			for (String copyFrom : definition.copyFrom) {
				final FieldTypeInterface fieldDest;
				if (nameDefMap.containsKey(copyFrom))
					fieldDest = nameDefMap.get(copyFrom);
				else
					fieldDest = newFields.computeIfAbsent(copyFrom, n -> new CopyToFieldType());
				fieldDest.copyTo(name, fieldType);
			}
		});
		nameDefMap.putAll(newFields);

		this.fieldDefinitionMap = fieldDefinitionMap;

		facetsConfig = new FacetsConfig();

	}

	public final boolean isEmpty() {
		return fieldDefinitionMap.isEmpty();
	}

	public final void forEach(final BiConsumer<String, FieldTypeInterface> consumer) {
		nameDefMap.forEach(consumer);
	}

	final public FieldTypeInterface getFieldType(final String fieldName) {
		if (fieldName == null || fieldName.isEmpty())
			return null;
		final FieldTypeInterface fieldType = nameDefMap.get(fieldName);
		if (fieldType != null)
			return fieldType;
		for (Map.Entry<WildcardMatcher, FieldTypeInterface> entry : wildcardMap.entrySet())
			if (entry.getKey().match(fieldName))
				return entry.getValue();
		return null;
	}

	final LinkedHashMap<String, FieldDefinition> getFieldDefinitionMap() {
		return fieldDefinitionMap;
	}

	private void checkFacetConfig(final String fieldName) {
		if (facetsConfig.getDimConfigs().containsKey(fieldName))
			return;
		final FieldTypeInterface fieldType = getFieldType(fieldName);
		if (fieldType == null)
			return;
		fieldType.setFacetsConfig(fieldName, this, facetsConfig);
		final FieldDefinition definition = fieldType.getDefinition();
		if (definition == null)
			return;
		if (definition instanceof CustomFieldDefinition) {
			final CustomFieldDefinition customDef = (CustomFieldDefinition) definition;
			final FieldDefinition.Template template =
					customDef.template == null ? FieldDefinition.Template.NONE : customDef.template;
			switch (template) {
			case SortedSetDocValuesFacetField:
				facetsConfig.setIndexFieldName(fieldName, sortedSetFacetField);
				break;
			case FacetField:
				facetsConfig.setIndexFieldName(fieldName, FieldDefinition.TAXONOMY_FACET_FIELD);
				break;
			case IntAssociatedField:
				facetsConfig.setIndexFieldName(fieldName, FieldDefinition.TAXONOMY_INT_ASSOC_FACET_FIELD);
				break;
			case FloatAssociatedField:
				facetsConfig.setIndexFieldName(fieldName, FieldDefinition.TAXONOMY_FLOAT_ASSOC_FACET_FIELD);
				break;
			}
		}
	}

	final public String resolveStoredFieldName(final String fieldName) {
		final FieldTypeInterface fieldType = getFieldType(fieldName);
		return fieldType == null ? null : fieldType.getStoredFieldName(fieldName);
	}

	final public String resolveQueryFieldName(final String fieldName) {
		final FieldTypeInterface fieldType = getFieldType(fieldName);
		return fieldType == null ? null : fieldType.getQueryFieldName(fieldName);
	}

	final public String[] resolveFieldNames(final String[] fields, final Function<String, String> resolver) {
		final String[] resolvedFields = new String[fields.length];
		int i = 0;
		for (String f : fields)
			resolvedFields[i++] = resolver.apply(f);
		return resolvedFields;
	}

	final public <T> Map<String, T> resolveFieldNames(final Map<String, T> fields, final Map<String, T> resolvedFields,
			final Function<String, String> resolver) {
		fields.forEach((f, t) -> resolvedFields.put(resolver.apply(f), t));
		return resolvedFields;
	}

	public HashMap<String, String> resolveFieldNames(final Set<String> fields,
			final Function<String, String> resolver) {
		final HashMap<String, String> resolvedFieldNames = new HashMap<>();
		fields.forEach((name) -> resolvedFieldNames.put(name, resolver.apply(name)));
		return resolvedFieldNames;
	}

	final public FacetsConfig getFacetsConfig(final Collection<String> concreteFieldNames) {
		concreteFieldNames.forEach(this::checkFacetConfig);
		return facetsConfig;
	}

	final public FacetsConfig getFacetsConfig(final String fieldName) {
		checkFacetConfig(fieldName);
		return facetsConfig;
	}

	final String getSortedSetFacetField() {
		return sortedSetFacetField;
	}

	final Set<String> getStaticFieldSet() {
		return nameDefMap.keySet();
	}

}
