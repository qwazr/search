/*
 * Copyright 2017-2020 Emmanuel Keller / QWAZR
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

import javax.validation.constraints.NotNull;
import org.apache.lucene.index.Term;

final public class CopyToFieldType extends FieldTypeAbstract<FieldDefinition> {

    public CopyToFieldType() {
        super(null, null, null, null, null, null, null);
    }

    @Override
    public String getQueryFieldName(@NotNull LuceneFieldType luceneFieldType, @NotNull String fieldName) {
        return null;
    }

    @Override
    public String getStoredFieldName(String fieldName) {
        return null;
    }

    @Override
    public Term term(String fieldName, Object value) {
        return null;
    }
}
