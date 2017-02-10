/**
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
 **/
package com.qwazr.search.index;

import com.qwazr.search.field.FieldDefinition;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public interface FieldConsumer {

	void accept(final String fieldName, final Field field, final Float boost);

	final class ForDocument implements FieldConsumer {

		HashSet<String> fieldNameSet = new HashSet<>();
		Document document = new Document();

		@Override
		final public void accept(final String fieldName, final Field field, final Float boost) {
			if (boost != null)
				field.setBoost(boost);
			document.add(field);
			fieldNameSet.add(fieldName);
		}
	}

	final class ForDocValues implements FieldConsumer {

		private List<Field> fieldList = new ArrayList<>();

		@Override
		final public void accept(final String fieldName, final Field field, final Float boost) {
			// We will not update the internal ID of the document
			if (FieldDefinition.ID_FIELD.equals(field.name()))
				return;
			if (boost != null)
				field.setBoost(boost);
			fieldList.add(field);
		}

		final Field[] toArray() {
			return fieldList.toArray(new Field[fieldList.size()]);
		}
	}
}
