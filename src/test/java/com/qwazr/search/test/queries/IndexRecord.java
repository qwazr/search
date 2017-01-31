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
 */
package com.qwazr.search.test.queries;

import com.qwazr.search.annotations.Index;
import com.qwazr.search.annotations.IndexField;
import com.qwazr.search.field.FieldDefinition;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

@Index(name = "IndexRecord", schema = "TestQueries")
public class IndexRecord {

	@IndexField(name = FieldDefinition.ID_FIELD, template = FieldDefinition.Template.StringField)
	final public String id;

	@IndexField(template = FieldDefinition.Template.TextField, analyzerClass = StandardAnalyzer.class)
	public String label;

	@IndexField(template = FieldDefinition.Template.IntDocValuesField)
	public Integer intDocValue;

	@IndexField(template = FieldDefinition.Template.IntAssociatedField)
	public Object[] intAssociatedFacet;

	@IndexField(template = FieldDefinition.Template.FloatAssociatedField)
	public Object[] floatAssociatedFacet;

	public IndexRecord() {
		id = null;
	}

	public IndexRecord(String id) {
		this.id = id;
	}

	public IndexRecord label(String label) {
		this.label = label;
		return this;
	}

	public IndexRecord intDocValue(Integer intDocValue) {
		this.intDocValue = intDocValue;
		return this;
	}

	public IndexRecord intAssociatedFacet(Integer assoc, String... path) {
		final Object[] array = new Object[path.length + 1];
		array[0] = assoc;
		System.arraycopy(path, 0, array, 1, path.length);
		this.intAssociatedFacet = array;
		return this;
	}

	public IndexRecord floatAssociatedFacet(Float assoc, String... path) {
		final Object[] array = new Object[path.length + 1];
		array[0] = assoc;
		System.arraycopy(path, 0, array, 1, path.length);
		this.floatAssociatedFacet = array;
		return this;
	}
}
