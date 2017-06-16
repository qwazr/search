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
package com.qwazr.search.test.units;

import com.qwazr.search.annotations.Copy;
import com.qwazr.search.annotations.Index;
import com.qwazr.search.annotations.IndexField;
import com.qwazr.search.field.FieldDefinition;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;

@Index(name = "IndexRecord", schema = "TestQueries", enableTaxonomyIndex = true)
public class IndexRecord {

	@IndexField(name = FieldDefinition.ID_FIELD, template = FieldDefinition.Template.StringField, stored = true)
	final public String id;

	@IndexField(template = FieldDefinition.Template.TextField, analyzerClass = StandardAnalyzer.class)
	public String textField;

	@IndexField(template = FieldDefinition.Template.TextField,
			queryAnalyzerClass = SynonymsResourceAnalyzer.class,
			analyzerClass = WhitespaceAnalyzer.class)
	public String textSynonymsField1;

	@IndexField(template = FieldDefinition.Template.TextField,
			queryAnalyzerClass = SynonymsResourceAnalyzer.class,
			analyzerClass = WhitespaceAnalyzer.class)
	public String textSynonymsField2;

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

	public IndexRecord textField(String textField) {
		this.textField = textField;
		return this;
	}

	public IndexRecord textSynonymsField1(String textSynonymsField1) {
		this.textSynonymsField1 = textSynonymsField1;
		return this;
	}

	public IndexRecord textSynonymsField2(String textSynonymsField2) {
		this.textSynonymsField2 = textSynonymsField2;
		return this;
	}

	public IndexRecord copyText1(String copyText) {
		this.copyText1 = copyText;
		return this;
	}

	public IndexRecord copyText2(String copyText) {
		this.copyText2 = copyText;
		return this;
	}

	public IndexRecord stringField(String stringField) {
		this.stringField = stringField;
		return this;
	}

	public IndexRecord sortedDocValue(String sortedDocValue) {
		this.sortedDocValue = sortedDocValue;
		return this;
	}

	public IndexRecord facetField(String facetField) {
		this.facetField = facetField;
		return this;
	}

	public IndexRecord storedField(String storedField) {
		this.storedField = storedField;
		return this;
	}

	public IndexRecord multivaluedIntegerStoredField(Integer... storedField) {
		if (multivaluedIntegerStoredField == null)
			multivaluedIntegerStoredField = new LinkedHashSet<>();
		Collections.addAll(multivaluedIntegerStoredField, storedField);
		return this;
	}

	public IndexRecord multivaluedStringStoredField(String... storedField) {
		if (multivaluedStringStoredField == null)
			multivaluedStringStoredField = new LinkedHashSet<>();
		Collections.addAll(multivaluedStringStoredField, storedField);
		return this;
	}

	public IndexRecord intDocValue(Integer intDocValue) {
		this.intDocValue = intDocValue;
		return this;
	}

	public IndexRecord longDocValue(long longDocValue) {
		this.longDocValue = longDocValue;
		return this;
	}

	public IndexRecord intPoint(Integer intPoint) {
		this.intPoint = intPoint;
		return this;
	}

	public IndexRecord floatDocValue(Float floatDocValue) {
		this.floatDocValue = floatDocValue;
		return this;
	}

	public IndexRecord doubleDocValue(Double doubleDocValue) {
		this.doubleDocValue = doubleDocValue;
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

	public IndexRecord sortedSetDocValuesFacetField(String sortedSetDocValuesFacetField) {
		this.sortedSetDocValuesFacetField = sortedSetDocValuesFacetField;
		return this;
	}

	public IndexRecord mlt(String... mlt) {
		if (this.mlt == null)
			this.mlt = new ArrayList<>();
		Collections.addAll(this.mlt, mlt);
		return this;
	}

}
