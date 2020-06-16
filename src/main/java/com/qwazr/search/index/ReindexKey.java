/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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
import com.qwazr.utils.Equalizer;
import java.util.Map;
import java.util.Objects;

/**
 * This class is the key used to keep track on any change in the schema that would require a reindexing
 */
class FieldKey extends Equalizer.Immutable<FieldKey> {

    private final String primaryKey;
    private final Map<String, FieldDefinition<?>> fields;

    FieldKey(final String primaryKey, final Map<String, FieldDefinition<?>> fields) {
        super(FieldKey.class);
        this.primaryKey = primaryKey;
        this.fields = fields;
    }

    @Override
    protected boolean isEqual(final FieldKey query) {
        return Objects.equals(query.primaryKey, primaryKey)
            && Objects.equals(query.fields, fields);
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(primaryKey, fields);
    }
}
