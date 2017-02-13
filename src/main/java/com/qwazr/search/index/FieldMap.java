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
 */
package com.qwazr.search.index;

import com.qwazr.search.field.CopyToFieldType;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.field.FieldTypeInterface;
import com.qwazr.utils.WildcardMatcher;
import org.apache.lucene.facet.FacetsConfig;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class FieldMap {

	private final LinkedHashMap<String, FieldDefinition> fieldDefinitionMap;
	private final HashMap<String, FieldTypeInterface> nameDefMap;
	private final HashMap<WildcardMatcher, FieldTypeInterface> wildcardMap;
	private final FacetsConfig facetsConfig;

	FieldMap(final LinkedHashMap<String, FieldDefinition> fieldDefinitionMap) {

		nameDefMap = new HashMap<>();
		wildcardMap = new HashMap<>();

		fieldDefinitionMap.forEach((name, definition) -> {
			if (name.indexOf('*') != -1 || name.indexOf('?') != -1) {
				final WildcardMatcher wildcardMatcher = new WildcardMatcher(name);
				wildcardMap.put(wildcardMatcher, FieldTypeInterface.getInstance(wildcardMatcher, definition));
			} else {
				final FieldTypeInterface fieldType = FieldTypeInterface.getInstance(null, definition);
				nameDefMap.put(name, fieldType);
			}
		});

		// Handle copy-to
		final HashMap<String, FieldTypeInterface> newFields = new HashMap<>();
		nameDefMap.forEach((name, fieldType) -> {
			final FieldDefinition definition = fieldType.getDefinition();
			if (definition.copy_from == null)
				return;
			for (FieldDefinition.CopyFrom copyFrom : definition.copy_from) {
				final FieldTypeInterface fieldDest;
				if (nameDefMap.containsKey(copyFrom.field))
					fieldDest = nameDefMap.get(copyFrom.field);
				else
					fieldDest = newFields.computeIfAbsent(copyFrom.field, n -> new CopyToFieldType());
				fieldDest.copyTo(name, fieldType, copyFrom.boost);
			}
		});
		nameDefMap.putAll(newFields);

		this.fieldDefinitionMap = fieldDefinitionMap;

		facetsConfig = new FacetsConfig();

	}

	final public FieldTypeInterface getFieldType(final String fieldName) {
		if (fieldName == null || fieldName.isEmpty())
			throw new IllegalArgumentException("Empty fieldname is not allowed");
		final FieldTypeInterface fieldType = nameDefMap.get(fieldName);
		if (fieldType != null)
			return fieldType;
		for (Map.Entry<WildcardMatcher, FieldTypeInterface> entry : wildcardMap.entrySet())
			if (entry.getKey().match(fieldName))
				return entry.getValue();
		throw new IllegalArgumentException("No field definition for the field: " + fieldName);
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
		final FieldDefinition definition = fieldType.getDefinition();
		if (definition == null || definition.template == null)
			return;
		switch (definition.template) {
		case SortedSetDocValuesFacetField:
			facetsConfig.setIndexFieldName(fieldName, FieldDefinition.SORTEDSET_FACET_FIELD);
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
		case StringAssociatedField:
			facetsConfig.setIndexFieldName(fieldName, FieldDefinition.TAXONOMY_STRING_ASSOC_FACET_FIELD);
			break;
		}
		if (definition.facet_multivalued != null)
			facetsConfig.setMultiValued(fieldName, definition.facet_multivalued);
		if (definition.facet_hierarchical != null)
			facetsConfig.setHierarchical(fieldName, definition.facet_hierarchical);
		if (definition.facet_require_dim_count != null)
			facetsConfig.setRequireDimCount(fieldName, definition.facet_require_dim_count);
	}

	final public FacetsConfig getFacetsConfig(final Collection<String> concreteFieldNames) {
		concreteFieldNames.forEach(this::checkFacetConfig);
		return facetsConfig;
	}

	final public FacetsConfig getFacetsConfig(final String fieldName) {
		checkFacetConfig(fieldName);
		return facetsConfig;
	}

	final Set<String> getStaticFieldSet() {
		return nameDefMap.keySet();
	}
}
