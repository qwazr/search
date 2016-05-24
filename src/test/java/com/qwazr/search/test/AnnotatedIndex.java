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
package com.qwazr.search.test;

import com.qwazr.search.annotations.Index;
import com.qwazr.search.annotations.IndexField;
import com.qwazr.search.field.FieldDefinition;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.SortedSetDocValues;

import java.util.LinkedHashSet;
import java.util.Objects;

import static com.qwazr.search.field.FieldDefinition.Template.SortedSetDocValuesField;
import static com.qwazr.search.field.FieldDefinition.Template.StoredField;
import static com.qwazr.search.field.FieldDefinition.Template.StringField;

@Index(name = AnnotatedIndex.INDEX_NAME_MASTER, schema = AnnotatedIndex.SCHEMA_NAME)
public class AnnotatedIndex {

	public final static String SCHEMA_NAME = "testSchema";
	public final static String INDEX_NAME_MASTER = "testIndexMaster";
	public final static String INDEX_NAME_SLAVE = "testIndexSlave";

	@IndexField(name = FieldDefinition.ID_FIELD, template = StringField, stored = true)
	final public String id;

	@IndexField(
			analyzer = "en.EnglishAnalyzer",
			tokenized = true,
			stored = true,
			indexOptions = IndexOptions.DOCS_AND_FREQS_AND_POSITIONS)
	final public String title;

	@IndexField(
			analyzerClass = EnglishAnalyzer.class,
			queryAnalyzerClass = EnglishAnalyzer.class,
			tokenized = true,
			stored = true,
			indexOptions = IndexOptions.DOCS_AND_FREQS_AND_POSITIONS)
	final public String content;

	@IndexField(template = FieldDefinition.Template.SortedSetMultiDocValuesFacetField)
	final public String[] category;

	@IndexField(template = FieldDefinition.Template.DoubleDocValuesField)
	final public Double price;

	final static String QUANTITY_FIELD = "quantity";
	@IndexField(name = QUANTITY_FIELD, template = FieldDefinition.Template.LongField)
	final public Long quantity;

	@IndexField(template = StringField, stored = true)
	final public LinkedHashSet<String> storedCategory;

	@IndexField(template = SortedSetDocValuesField)
	final public LinkedHashSet<String> docValuesCategory;

	public AnnotatedIndex() {
		this(null, null, null, null, null);
	}

	public AnnotatedIndex(Integer id, String title, String content, Double price, Long quantity, String... categories) {
		this.id = id == null ? null : id.toString();
		this.title = title;
		this.content = content;
		this.category = categories;
		this.price = price;
		this.quantity = quantity;
		if (categories == null) {
			this.storedCategory = null;
			this.docValuesCategory = null;
		} else {
			this.storedCategory = new LinkedHashSet<>();
			this.docValuesCategory = this.storedCategory;
			for (String category : categories)
				this.storedCategory.add(category);
		}
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof AnnotatedIndex))
			return false;
		AnnotatedIndex record = (AnnotatedIndex) object;
		if (!Objects.equals(title, record.title))
			return false;
		if (!Objects.equals(content, record.content))
			return false;
		if (!Objects.equals(price, record.price))
			return false;
		return true;
	}

}
