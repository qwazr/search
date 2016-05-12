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
 **/
package com.qwazr.search.index;

import com.qwazr.search.field.Converters.ValueConverter;
import com.qwazr.search.field.FieldTypeInterface;
import org.apache.lucene.index.*;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;

abstract class ReturnedFields {

	abstract void toReturnedFields(final int docId, final Map<String, Object> dest);

	static class DocValuesReturnedFields extends ReturnedFields {

		private final Map<String, ValueConverter> valueConverterMap;

		DocValuesReturnedFields(final Map<String, FieldTypeInterface> fieldTypes, final LeafReader leafReader,
				final Set<String> returnedFields) throws IOException {
			if (returnedFields == null) {
				valueConverterMap = null;
				return;
			}
			valueConverterMap = new LinkedHashMap<>();
			for (String fieldName : returnedFields) {
				FieldTypeInterface fieldType = fieldTypes.get(fieldName);
				if (fieldType == null)
					continue;
				ValueConverter converter = fieldType.getConverter(leafReader);
				if (converter == null)
					continue;
				valueConverterMap.put(fieldName, converter);
			}
		}

		@Override
		final void toReturnedFields(final int docId, final Map<String, Object> dest) {
			if (valueConverterMap == null)
				return;
			valueConverterMap.forEach((fieldName, converter) -> {
				Object o = converter.convert(docId);
				if (o != null)
					dest.put(fieldName, o);
			});
		}

	}

}
