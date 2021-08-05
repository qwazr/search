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
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import javax.validation.constraints.NotNull;

/**
 * This class is the key used to keep track on any change in the schema that would require a reindexing
 */
public class FieldsContext extends Equalizer.Immutable<FieldsContext> {

    public final String primaryKey;
    public final String sortedSetFacetField;
    public final String recordField;
    public final Map<String, FieldDefinition> fields;

    public FieldsContext(@NotNull final IndexSettingsDefinition indexSettings,
                         final Map<String, FieldDefinition> fields) {
        super(FieldsContext.class);
        this.primaryKey = indexSettings.primaryKey == null
            ? FieldDefinition.ID_FIELD : indexSettings.primaryKey;
        this.sortedSetFacetField = indexSettings.sortedSetFacetField == null
            ? FieldDefinition.DEFAULT_SORTEDSET_FACET_FIELD : indexSettings.sortedSetFacetField;
        this.recordField = indexSettings.recordField;
        this.fields = fields == null ? Collections.emptyMap() : Collections.unmodifiableMap(fields);
    }

    @Override
    protected boolean isEqual(final FieldsContext key) {
        return Objects.equals(key.primaryKey, primaryKey)
            && Objects.equals(key.sortedSetFacetField, sortedSetFacetField)
            && Objects.equals(key.recordField, recordField)
            && Objects.equals(key.fields, fields);
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(primaryKey, sortedSetFacetField, recordField, fields);
    }

}
