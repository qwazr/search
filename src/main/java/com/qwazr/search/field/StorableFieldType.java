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

abstract class StorableFieldType extends CustomFieldTypeAbstract {

	final boolean store;

	StorableFieldType(final Builder<CustomFieldDefinition> builder) {
		super(builder);
		store = getStore(builder.definition);
	}

	private static boolean getStore(CustomFieldDefinition definition) {
		return definition != null && definition.stored != null ? definition.stored : false;
	}

	@Override
	final void setupFields(Builder<CustomFieldDefinition> builder) {
		if (getStore(builder.definition)) {
			builder.fieldProvider(this::newFieldWithStore);
			builder.storedFieldProvider(FieldUtils::storedField);
		} else
			builder.fieldProvider(this::newFieldNoStore);
	}

	abstract void newFieldWithStore(final String fieldName, final Object value, final FieldConsumer consumer);

	abstract void newFieldNoStore(final String fieldName, final Object value, final FieldConsumer consumer);

}
