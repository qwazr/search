/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.test.units;

import com.qwazr.search.analysis.SmartAnalyzerSet;
import com.qwazr.search.annotations.Copy;
import com.qwazr.search.annotations.Index;
import com.qwazr.search.annotations.IndexField;
import com.qwazr.search.field.FieldDefinition;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public abstract class IndexRecord<T extends IndexRecord> {

	@IndexField(name = FieldDefinition.ID_FIELD, template = FieldDefinition.Template.StringField, stored = true)
	final public String id;

	@IndexField(template = FieldDefinition.Template.TextField, analyzerClass = StandardAnalyzer.class)
	@Copy(to = { @Copy.To(order = 1, field = "textComplexAnalyzer") })
	public String textField;

	@IndexField(template = FieldDefinition.Template.TextField,
			queryAnalyzerClass = SynonymsResourceAnalyzer.class,
			analyzerClass = WhitespaceAnalyzer.class)
	@Copy(to = { @Copy.To(order = 2, field = "textComplexAnalyzer") })
	public String textSynonymsField1;

	@IndexField(template = FieldDefinition.Template.TextField,
			queryAnalyzerClass = SynonymsResourceAnalyzer.class,
			analyzerClass = WhitespaceAnalyzer.class)
	@Copy(to = { @Copy.To(order = 3, field = "textComplexAnalyzer") })
	public String textSynonymsField2;

	@IndexField(template = FieldDefinition.Template.TextField,
			queryAnalyzerClass = ComplexQueryAnalyzer.class,
			analyzerClass = SmartAnalyzerSet.EnglishIndex.class)
	public String textComplexAnalyzer;

	@IndexField(template = FieldDefinition.Template.StringField)
	public String stringField;

	@IndexField(template = FieldDefinition.Template.SortedDocValuesField)
	public String sortedDocValue;

	@IndexField(template = FieldDefinition.Template.IntDocValuesField)
	public Integer intDocValue;

	@IndexField(template = FieldDefinition.Template.LongDocValuesField)
	public Long longDocValue;

	@IndexField(template = FieldDefinition.Template.IntPoint)
	public Integer intPoint;

	@IndexField(template = FieldDefinition.Template.FloatDocValuesField)
	public Float floatDocValue;

	@IndexField(template = FieldDefinition.Template.DoubleDocValuesField)
	public Double doubleDocValue;

	@IndexField(template = FieldDefinition.Template.IntAssociatedField)
	public Object[] intAssociatedFacet;

	@IndexField(template = FieldDefinition.Template.FloatAssociatedField)
	public Object[] floatAssociatedFacet;

	@IndexField(template = FieldDefinition.Template.SortedSetDocValuesFacetField, facetMultivalued = false)
	public String sortedSetDocValuesFacetField;

	@IndexField(name = "dynamic_facets_*",
			template = FieldDefinition.Template.SortedSetDocValuesFacetField,
			facetMultivalued = true)
	public Map<String, List<String>> dynamicFacets;

	@IndexField(template = FieldDefinition.Template.FacetField)
	public String facetField;

	@IndexField(template = FieldDefinition.Template.StoredField)
	public String storedField;

	@IndexField(template = FieldDefinition.Template.StoredField)
	public LinkedHashSet<String> multivaluedStringStoredField;

	@IndexField(template = FieldDefinition.Template.StoredField)
	public LinkedHashSet<Integer> multivaluedIntegerStoredField;

	@Copy(to = { @Copy.To(order = 1, field = "textField") })
	public String copyText1;

	@Copy(to = { @Copy.To(order = 2, field = "textField") })
	public String copyText2;

	@IndexField(template = FieldDefinition.Template.TextField,
			analyzerClass = StandardAnalyzer.class,
			stored = true,
			omitNorms = true)
	public ArrayList<String> mlt;

	public IndexRecord() {
		id = null;
	}

	public IndexRecord(String id) {
		this.id = id;
	}

	public T textField(String textField) {
		this.textField = textField;
		return (T) this;
	}

	public T textSynonymsField1(String textSynonymsField1) {
		this.textSynonymsField1 = textSynonymsField1;
		return (T) this;
	}

	public T textSynonymsField2(String textSynonymsField2) {
		this.textSynonymsField2 = textSynonymsField2;
		return (T) this;
	}

	public T copyText1(String copyText) {
		this.copyText1 = copyText;
		return (T) this;
	}

	public T copyText2(String copyText) {
		this.copyText2 = copyText;
		return (T) this;
	}

	public T stringField(String stringField) {
		this.stringField = stringField;
		return (T) this;
	}

	public T sortedDocValue(String sortedDocValue) {
		this.sortedDocValue = sortedDocValue;
		return (T) this;
	}

	public T facetField(String facetField) {
		this.facetField = facetField;
		return (T) this;
	}

	public T storedField(String storedField) {
		this.storedField = storedField;
		return (T) this;
	}

	public T multivaluedIntegerStoredField(Integer... storedField) {
		if (multivaluedIntegerStoredField == null)
			multivaluedIntegerStoredField = new LinkedHashSet<>();
		Collections.addAll(multivaluedIntegerStoredField, storedField);
		return (T) this;
	}

	public T multivaluedStringStoredField(String... storedField) {
		if (multivaluedStringStoredField == null)
			multivaluedStringStoredField = new LinkedHashSet<>();
		Collections.addAll(multivaluedStringStoredField, storedField);
		return (T) this;
	}

	public T intDocValue(Integer intDocValue) {
		this.intDocValue = intDocValue;
		return (T) this;
	}

	public T longDocValue(long longDocValue) {
		this.longDocValue = longDocValue;
		return (T) this;
	}

	public T intPoint(Integer intPoint) {
		this.intPoint = intPoint;
		return (T) this;
	}

	public T floatDocValue(Float floatDocValue) {
		this.floatDocValue = floatDocValue;
		return (T) this;
	}

	public T doubleDocValue(Double doubleDocValue) {
		this.doubleDocValue = doubleDocValue;
		return (T) this;
	}

	public T intAssociatedFacet(Integer assoc, String... path) {
		final Object[] array = new Object[path.length + 1];
		array[0] = assoc;
		System.arraycopy(path, 0, array, 1, path.length);
		this.intAssociatedFacet = array;
		return (T) this;
	}

	public T floatAssociatedFacet(Float assoc, String... path) {
		final Object[] array = new Object[path.length + 1];
		array[0] = assoc;
		System.arraycopy(path, 0, array, 1, path.length);
		this.floatAssociatedFacet = array;
		return (T) this;
	}

	public T sortedSetDocValuesFacetField(String sortedSetDocValuesFacetField) {
		this.sortedSetDocValuesFacetField = sortedSetDocValuesFacetField;
		return (T) this;
	}

	public T dynamicFacets(String fieldName, String value) {
		if (dynamicFacets == null)
			dynamicFacets = new LinkedHashMap<>();
		dynamicFacets.computeIfAbsent(fieldName, f -> new ArrayList<>()).add(value);
		return (T) this;
	}

	public T mlt(String... mlt) {
		if (this.mlt == null)
			this.mlt = new ArrayList<>();
		Collections.addAll(this.mlt, mlt);
		return (T) this;
	}

	@Index(name = "IndexRecord", schema = "TestQueries", enableTaxonomyIndex = true)
	public static class WithTaxonomy extends IndexRecord<WithTaxonomy> {

		public WithTaxonomy() {
		}

		public WithTaxonomy(String id) {
			super(id);
		}

	}

	@Index(name = "IndexRecord", schema = "TestQueries", enableTaxonomyIndex = false)
	public static class NoTaxonomy extends IndexRecord<NoTaxonomy> {

		public NoTaxonomy() {
		}

		public NoTaxonomy(String id) {
			super(id);
		}
	}
}
