/*
 * Copyright 2020 Emmanuel Keller / QWAZR
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

import com.qwazr.search.analysis.SmartAnalyzerSet;
import com.qwazr.utils.StringUtils;

public class SmartDynamicTypes {

    final static SmartFieldType doNothingType = new SmartFieldType(null, null,
        SmartFieldDefinition.of().build());

    final static SmartFieldDefinition defaultNumericDefinition =
        SmartFieldDefinition.of()
            .type(SmartFieldDefinition.Type.DOUBLE)
            .index(true)
            .sort(true)
            .build();

    final static SmartFieldDefinition defaultTextDefinition =
        SmartFieldDefinition.of()
            .type(SmartFieldDefinition.Type.TEXT)
            .index(true)
            .analyzer(SmartAnalyzerSet.ascii.name())
            .build();

    final static SmartFieldDefinition defaultBooleanDefinition =
        SmartFieldDefinition.of()
            .type(SmartFieldDefinition.Type.TEXT)
            .index(true)
            .build();

    final private SmartFieldType primaryKeyType;

    public SmartDynamicTypes(final String primaryKey) {
        primaryKeyType = StringUtils.isEmpty(primaryKey) ? null
            : new SmartFieldType(primaryKey, null,
            SmartFieldDefinition.of()
                .type(SmartFieldDefinition.Type.TEXT)
                .index(true)
                .build());
    }

    public FieldTypeInterface getDoNothingType() {
        return doNothingType;
    }

    public FieldTypeInterface getPrimaryKeyType() {
        return primaryKeyType;
    }

    public SmartFieldType getTypeFromValue(final String fieldName, final Object value) {
        if (value == null)
            return doNothingType;
        else if (value instanceof Number)
            return new SmartFieldType(fieldName, null, defaultNumericDefinition);
        else if (value instanceof String)
            return new SmartFieldType(fieldName, null, defaultTextDefinition);
        else if (value instanceof Boolean)
            return new SmartFieldType(fieldName, null, defaultBooleanDefinition);
        else
            return doNothingType;
    }


}
