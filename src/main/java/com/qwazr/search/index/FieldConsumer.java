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

import com.qwazr.search.field.FieldDefinition;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

abstract public class FieldConsumer implements Consumer<Field> {

	static class ForDocument extends FieldConsumer {

		Document document = new Document();

		@Override
		final public void accept(Field field) {
			document.add(field);
		}
	}

	static class ForDocValues extends FieldConsumer {

		private List<Field> fieldList = new ArrayList<>();

		@Override
		final public void accept(Field field) {
			// We will not update the internal ID of the document
			if (FieldDefinition.ID_FIELD.equals(field.name()))
				return;
			fieldList.add(field);
		}

		final Field[] toArray() {
			return fieldList.toArray(new Field[fieldList.size()]);
		}
	}
}
