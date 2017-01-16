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
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexOptions;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Objects;

import static com.qwazr.search.field.FieldDefinition.Template.FacetField;
import static com.qwazr.search.field.FieldDefinition.Template.MultiFacetField;
import static com.qwazr.search.field.FieldDefinition.Template.SortedDocValuesField;
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

	@IndexField(analyzer = "en.EnglishAnalyzer",
			tokenized = true,
			stored = true,
			indexOptions = IndexOptions.DOCS_AND_FREQS_AND_POSITIONS)
	final public String title;

	@IndexField(analyzerClass = StandardAnalyzer.class,
			tokenized = true,
			indexOptions = IndexOptions.DOCS_AND_FREQS_AND_POSITIONS)
	final public String titleStd;

	@IndexField(template = SortedDocValuesField)
	final public String titleSort;

	@IndexField(analyzerClass = EnglishAnalyzer.class,
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

	final static String DV_QUANTITY_FIELD = "dvQty";
	@IndexField(name = DV_QUANTITY_FIELD, template = FieldDefinition.Template.LongDocValuesField)
	final public Long dvQty;

	@IndexField(template = StringField, stored = true)
	final public Collection<String> storedCategory;

	@IndexField(template = SortedSetDocValuesField)
	final public Collection<String> docValuesCategory;

	@IndexField(name = "dynamic_simple_facet_*", template = FacetField)
	final public LinkedHashMap<String, Object> simpleFacets;

	@IndexField(name = "dynamic_multi_facet_*", template = MultiFacetField, stored = true)
	final public LinkedHashMap<String, Object> multiFacets;

	@IndexField(template = StoredField)
	final public ExternalTest externalValue;

	@IndexField(template = StoredField)
	final public SerialTest serialValue;

	public AnnotatedIndex() {
		id = null;
		title = null;
		titleStd = null;
		titleSort = null;
		content = null;
		category = null;
		price = null;
		quantity = null;
		dvQty = null;
		storedCategory = docValuesCategory = new LinkedHashSet<>();
		simpleFacets = null;
		multiFacets = null;
		externalValue = null;
		serialValue = null;
	}

	public AnnotatedIndex(Integer id, String title, String content, Double price, Long quantity, boolean withFacets,
			boolean updateDVOnly, String... categories) {
		this.id = id.toString();
		this.title = title;
		this.titleStd = title;
		this.titleSort = title;
		this.content = content;
		this.category = categories;
		this.price = price;
		this.quantity = quantity;
		this.dvQty = quantity;
		if (categories == null) {
			this.storedCategory = null;
			this.docValuesCategory = null;
		} else {
			this.storedCategory = new LinkedHashSet<>();
			this.docValuesCategory = this.storedCategory;
			for (String category : categories)
				this.storedCategory.add(category);
		}
		this.simpleFacets = withFacets ? new LinkedHashMap<>() : null;
		this.multiFacets = withFacets ? new LinkedHashMap<>() : null;
		this.externalValue = updateDVOnly ? null : new ExternalTest(id, title);
		this.serialValue = updateDVOnly ? null : new SerialTest(price, content);
	}

	public AnnotatedIndex simpleFacet(String field, String value) {
		simpleFacets.put("dynamic_simple_facet_" + field, value);
		return this;
	}

	public AnnotatedIndex multiFacet(String field, String... values) {
		multiFacets.put("dynamic_multi_facet_" + field, values);
		return this;
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

	public static class ExternalTest implements Serializable {

		private static final long serialVersionUID = 7475266508025830265L;

		public int id;
		public String title;

		public ExternalTest() {
		}

		ExternalTest(int id, String title) {
			this.id = id;
			this.title = title;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof ExternalTest))
				return false;
			ExternalTest v = (ExternalTest) o;
			if (!Objects.equals(id, v.id))
				return false;
			return Objects.equals(title, v.title);
		}

	}

	public static class SerialTest implements Serializable {

		private static final long serialVersionUID = 2670913031496416189L;

		final public Double price;
		final public String content;

		SerialTest(Double price, String content) {
			this.price = price;
			this.content = content;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof SerialTest))
				return false;
			SerialTest v = (SerialTest) o;
			if (!Objects.equals(price, v.price))
				return false;
			return Objects.equals(content, v.content);
		}
	}

}
