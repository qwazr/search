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

import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.field.FieldTypeInterface;
import com.qwazr.utils.WildcardMatcher;
import org.apache.lucene.facet.FacetsConfig;

import java.util.*;

public class FieldMap {

	private final LinkedHashMap<String, FieldDefinition> fieldDefinitionMap;
	private final HashMap<String, Item> nameDefMap;
	private final HashMap<WildcardMatcher, Item> wildcardMap;

	FieldMap(final LinkedHashMap<String, FieldDefinition> fieldDefinitionMap) {

		nameDefMap = new HashMap<>();
		wildcardMap = new HashMap<>();

		fieldDefinitionMap.forEach((name, definition) -> {
			if (name.indexOf('*') != -1 || name.indexOf('?') != -1) {
				final WildcardMatcher wildcardMatcher = new WildcardMatcher(name);
				wildcardMap.put(wildcardMatcher, new Item(name, definition, wildcardMatcher));
			} else
				nameDefMap.put(name, new Item(name, definition, null));
		});

		this.fieldDefinitionMap = fieldDefinitionMap;
	}

	final public Item find(final String fieldName) {
		if (fieldName == null || fieldName.isEmpty())
			throw new IllegalArgumentException("Empty fieldname is not allowed");
		final Item item = nameDefMap.get(fieldName);
		if (item != null)
			return item;
		for (Map.Entry<WildcardMatcher, Item> entry : wildcardMap.entrySet())
			if (entry.getKey().match(fieldName))
				return entry.getValue();
		throw new IllegalArgumentException("No field definition for the field: " + fieldName);
	}

	final public FieldTypeInterface getFieldType(final String fieldName) {
		return FieldTypeInterface.getInstance(find(fieldName));
	}

	final public LinkedHashMap<String, FieldDefinition> getFieldDefinitionMap() {
		return fieldDefinitionMap;
	}

	final public class Item {

		public final String name;

		public final FieldDefinition definition;

		public final WildcardMatcher matcher;

		private Item(final String name, final FieldDefinition definition, final WildcardMatcher matcher) {
			this.name = name;
			this.definition = definition;
			this.matcher = matcher;

		}

		final public boolean match(final String fieldName) {
			return matcher.match(fieldName);
		}
	}

	private void setFacetConfig(final String fieldName, final FacetsConfig facetsConfig) {
		FieldMap.Item fieldMapItem = find(fieldName);
		if (fieldMapItem.definition.template != null) {
			switch (fieldMapItem.definition.template) {
			case FacetField:
			case SortedSetDocValuesFacetField:
				facetsConfig.setMultiValued(fieldName, false);
				facetsConfig.setHierarchical(fieldName, false);
				break;
			case MultiFacetField:
			case SortedSetMultiDocValuesFacetField:
				facetsConfig.setMultiValued(fieldName, true);
				facetsConfig.setHierarchical(fieldName, false);
				break;
			}
		}
	}

	final public FacetsConfig getNewFacetsConfig(final Collection<String> concreteFieldNames) {
		final FacetsConfig facetsConfig = new FacetsConfig();
		concreteFieldNames.forEach((fieldName) -> setFacetConfig(fieldName, facetsConfig));
		return facetsConfig;
	}

	final public FacetsConfig getNewFacetsConfig(String fieldName) {
		final FacetsConfig facetsConfig = new FacetsConfig();
		setFacetConfig(fieldName, facetsConfig);
		return facetsConfig;
	}

	final public Set<String> getStaticFieldSet() {
		return nameDefMap.keySet();
	}
}
