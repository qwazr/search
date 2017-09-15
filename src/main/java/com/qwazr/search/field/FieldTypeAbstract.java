/*
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
package com.qwazr.search.field;

import com.qwazr.search.field.Converters.MultiReader;
import com.qwazr.search.field.Converters.ValueConverter;
import com.qwazr.search.index.BytesRefUtils;
import com.qwazr.search.index.FieldConsumer;
import com.qwazr.search.index.FieldMap;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.server.ServerException;
import com.qwazr.utils.WildcardMatcher;
import jdk.nashorn.api.scripting.JSObject;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.SortField;
import org.apache.lucene.util.BytesRef;

import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

abstract class FieldTypeAbstract<T extends FieldDefinition> implements FieldTypeInterface {

	final protected String genericFieldName;
	final private WildcardMatcher wildcardMatcher;
	final protected T definition;
	final protected BytesRefUtils.Converter bytesRefConverter;
	final private FieldTypeInterface.Facet[] facetConfig;
	final private FieldTypeInterface.FieldProvider[] fieldProviders;
	final private TermProvider termProvider;
	final private FieldNameProvider storedFieldNameProvider;
	final private FieldNameProvider queryFieldNameProvider;
	final private SortFieldProvider sortFieldProvider;
	final private Map<FieldTypeInterface, String> copyToFields;

	protected FieldTypeAbstract(final Builder<T> builder) {
		this.genericFieldName = builder.genericFieldName;
		this.wildcardMatcher = builder.wildcardMatcher;
		this.definition = builder.definition;
		setup(builder);
		this.bytesRefConverter = builder.bytesRefConverter;
		this.facetConfig = builder.facetConfig == null || builder.facetConfig.isEmpty() ?
				null :
				builder.facetConfig.toArray(new FieldTypeInterface.Facet[builder.facetConfig.size()]);
		this.fieldProviders = builder.fieldProviders == null || builder.fieldProviders.isEmpty() ?
				null :
				builder.fieldProviders.toArray(new FieldTypeInterface.FieldProvider[builder.fieldProviders.size()]);
		this.termProvider = builder.termProvider;
		this.storedFieldNameProvider = builder.storedFieldNameProvider;
		this.queryFieldNameProvider = builder.queryFieldNameProvider;
		this.sortFieldProvider = builder.sortFieldProvider;
		this.copyToFields = new LinkedHashMap<>();
	}

	abstract Builder<T> setup(Builder<T> builder);

	public final void setFacetsConfig(final String fieldName, final FieldMap fieldMap,
			final FacetsConfig facetsConfig) {
		if (facetConfig != null)
			for (FieldTypeInterface.Facet config : facetConfig)
				config.config(fieldName, fieldMap, facetsConfig);
	}

	public final Term term(String fieldName, Object value) {
		if (termProvider != null)
			return termProvider.term(fieldName, value);
		else
			throw new ServerException("Term not supported by the field: " + fieldName);
	}

	@Override
	public ValueConverter getConverter(String fieldName, MultiReader reader) {
		return null;
	}

	@Override
	public final String getStoredFieldName(final String fieldName) {
		if (storedFieldNameProvider != null)
			return storedFieldNameProvider.fieldName(fieldName);
		else
			return null;
	}

	@Override
	public final String getQueryFieldName(final String fieldName) {
		if (queryFieldNameProvider != null)
			return queryFieldNameProvider.fieldName(fieldName);
		else
			return null;
	}

	@Override
	public final SortField getSortField(final String fieldName, final QueryDefinition.SortEnum sortEnum) {
		if (sortFieldProvider != null)
			return sortFieldProvider.sortField(fieldName, sortEnum);
		else
			return null;
	}

	@Override
	final public void copyTo(final String fieldName, final FieldTypeInterface fieldType) {
		copyToFields.put(fieldType, fieldName);
	}

	@Override
	final public FieldDefinition getDefinition() {
		return definition;
	}

	protected void fillArray(final String fieldName, final int[] values, final FieldConsumer consumer) {
		for (int value : values)
			fill(fieldName, value, consumer);
	}

	protected void fillArray(final String fieldName, final long[] values, final FieldConsumer consumer) {
		for (long value : values)
			fill(fieldName, value, consumer);
	}

	protected void fillArray(final String fieldName, final double[] values, final FieldConsumer consumer) {
		for (double value : values)
			fill(fieldName, value, consumer);
	}

	protected void fillArray(final String fieldName, final float[] values, final FieldConsumer consumer) {
		for (float value : values)
			fill(fieldName, value, consumer);
	}

	protected void fillArray(final String fieldName, final Object[] values, final FieldConsumer consumer) {
		for (Object value : values)
			fill(fieldName, value, consumer);
	}

	protected void fillArray(final String fieldName, final String[] values, final FieldConsumer consumer) {
		for (String value : values)
			fill(fieldName, value, consumer);
	}

	protected void fillCollection(final String fieldName, final Collection<Object> values,
			final FieldConsumer consumer) {
		values.forEach(value -> {
			if (value != null)
				fill(fieldName, value, consumer);
		});
	}

	protected void fillMap(final String fieldName, final Map<Object, Object> values, final FieldConsumer consumer) {
		throw new ServerException(Response.Status.NOT_ACCEPTABLE,
				"Map is not asupported type for the field: " + fieldName);
	}

	protected void fillJSObject(final String fieldName, final JSObject values, final FieldConsumer consumer) {
		fillCollection(fieldName, values.values(), consumer);
	}

	protected void fillWildcardMatcher(final String wildcardName, final Object value,
			final FieldConsumer fieldConsumer) {
		if (value instanceof Map) {
			((Map<String, Object>) value).forEach((fieldName, valueObject) -> {
				if (!wildcardMatcher.match(fieldName))
					throw new ServerException(Response.Status.NOT_ACCEPTABLE,
							"The field name does not match the field pattern: " + wildcardName);
				fill(fieldName, valueObject, fieldConsumer);
			});
		} else
			fill(wildcardName, value, fieldConsumer);
	}

	protected void fill(final String fieldName, final Object value, final FieldConsumer fieldConsumer) {
		if (value == null)
			return;
		if (value instanceof String[])
			fillArray(fieldName, (String[]) value, fieldConsumer);
		else if (value instanceof int[])
			fillArray(fieldName, (int[]) value, fieldConsumer);
		else if (value instanceof long[])
			fillArray(fieldName, (long[]) value, fieldConsumer);
		else if (value instanceof double[])
			fillArray(fieldName, (double[]) value, fieldConsumer);
		else if (value instanceof float[])
			fillArray(fieldName, (float[]) value, fieldConsumer);
		else if (value instanceof Object[])
			fillArray(fieldName, (Object[]) value, fieldConsumer);
		else if (value instanceof Collection)
			fillCollection(fieldName, (Collection) value, fieldConsumer);
		else if (value instanceof JSObject)
			fillJSObject(fieldName, (JSObject) value, fieldConsumer);
		else if (value instanceof Map)
			fillMap(fieldName, (Map) value, fieldConsumer);
		else
			fillValue(fieldName, value, fieldConsumer);
	}

	final protected void fillValue(final String fieldName, final Object value, final FieldConsumer fieldConsumer) {
		if (fieldProviders != null) {
			for (FieldProvider fieldProvider : fieldProviders)
				fieldProvider.fillValue(fieldName, value, fieldConsumer);
		} else
			throw new ServerException("Unsupported value type for field \"" + fieldName + "\" : " + value.getClass());
	}

	@Override
	final public void dispatch(final String fieldName, final Object value, final FieldConsumer fieldConsumer) {
		if (value == null)
			return;
		if (wildcardMatcher != null)
			fillWildcardMatcher(fieldName, value, fieldConsumer);
		else {
			fill(fieldName, value, fieldConsumer);
			if (!copyToFields.isEmpty())
				copyToFields.forEach(
						(fieldType, copyFieldName) -> fieldType.dispatch(copyFieldName, value, fieldConsumer));
		}
	}

	@Override
	final public Object toTerm(final BytesRef bytesRef) {
		return bytesRef == null ? null : bytesRefConverter == null ? null : bytesRefConverter.to(bytesRef);
	}

	static <T extends FieldDefinition> Builder<T> of(String genericFieldName, WildcardMatcher wildcardMatcher,
			T definition) {
		return new Builder<>(genericFieldName, wildcardMatcher, definition);
	}

	static class Builder<T extends FieldDefinition> {

		final T definition;
		private final String genericFieldName;
		private final WildcardMatcher wildcardMatcher;
		private BytesRefUtils.Converter bytesRefConverter;
		private LinkedHashSet<Facet> facetConfig;
		private LinkedHashSet<FieldProvider> fieldProviders;
		private TermProvider termProvider;
		private FieldNameProvider storedFieldNameProvider;
		private FieldNameProvider queryFieldNameProvider;
		private SortFieldProvider sortFieldProvider;

		Builder(String genericFieldName, WildcardMatcher wildcardMatcher, T definition) {
			this.genericFieldName = genericFieldName;
			this.wildcardMatcher = wildcardMatcher;
			this.definition = definition;
		}

		Builder<T> bytesRefConverter(BytesRefUtils.Converter bytesRefConverter) {
			this.bytesRefConverter = bytesRefConverter;
			return this;
		}

		Builder<T> facetConfig(FieldTypeInterface.Facet facetConfig) {
			if (this.facetConfig == null)
				this.facetConfig = new LinkedHashSet<>();
			this.facetConfig.add(facetConfig);
			return this;
		}

		Builder<T> fieldProvider(FieldTypeInterface.FieldProvider fieldProvider) {
			if (this.fieldProviders == null)
				this.fieldProviders = new LinkedHashSet<>();
			this.fieldProviders.add(fieldProvider);
			return this;
		}

		Builder<T> termProvider(FieldTypeInterface.TermProvider termProvider) {
			this.termProvider = termProvider;
			return this;
		}

		Builder<T> storedFieldNameProvider(FieldTypeInterface.FieldNameProvider fieldNameProvider) {
			this.storedFieldNameProvider = fieldNameProvider;
			return this;
		}

		Builder<T> queryFieldNameProvider(FieldTypeInterface.FieldNameProvider fieldNameProvider) {
			this.queryFieldNameProvider = fieldNameProvider;
			return this;
		}

		Builder<T> sortFieldProvider(FieldTypeInterface.SortFieldProvider sortFieldProvider) {
			this.sortFieldProvider = sortFieldProvider;
			return this;
		}

	}
}
