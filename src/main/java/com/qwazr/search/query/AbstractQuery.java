/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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

package com.qwazr.search.query;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.field.FieldTypeInterface;
import com.qwazr.search.field.SmartFieldDefinition;
import com.qwazr.search.index.BytesRefUtils;
import com.qwazr.search.index.FieldMap;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.QueryContext;
import com.qwazr.utils.Equalizer;
import com.qwazr.utils.StringUtils;
import javax.ws.rs.NotAcceptableException;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = BlendedTermQuery.class),
    @JsonSubTypes.Type(value = BooleanQuery.class),
    @JsonSubTypes.Type(value = BoostQuery.class),
    @JsonSubTypes.Type(value = CommonTermsQuery.class),
    @JsonSubTypes.Type(value = ConstantScoreQuery.class),
    @JsonSubTypes.Type(value = DisjunctionMaxQuery.class),
    @JsonSubTypes.Type(value = DocValuesFieldExistsQuery.class),
    @JsonSubTypes.Type(value = DoubleDocValuesExactQuery.class),
    @JsonSubTypes.Type(value = DoubleDocValuesRangeQuery.class),
    @JsonSubTypes.Type(value = DoubleExactQuery.class),
    @JsonSubTypes.Type(value = DoubleMultiRangeQuery.class),
    @JsonSubTypes.Type(value = DoubleRangeQuery.class),
    @JsonSubTypes.Type(value = DoubleSetQuery.class),
    @JsonSubTypes.Type(value = DrillDownQuery.class),
    @JsonSubTypes.Type(value = FacetPathQuery.class),
    @JsonSubTypes.Type(value = FloatDocValuesExactQuery.class),
    @JsonSubTypes.Type(value = FloatDocValuesRangeQuery.class),
    @JsonSubTypes.Type(value = FloatExactQuery.class),
    @JsonSubTypes.Type(value = FloatMultiRangeQuery.class),
    @JsonSubTypes.Type(value = FloatRangeQuery.class),
    @JsonSubTypes.Type(value = FloatSetQuery.class),
    @JsonSubTypes.Type(value = FunctionQuery.class),
    @JsonSubTypes.Type(value = FunctionScoreQuery.class),
    @JsonSubTypes.Type(value = FuzzyQuery.class),
    @JsonSubTypes.Type(value = Geo3DDistanceQuery.class),
    @JsonSubTypes.Type(value = Geo3DBoxQuery.class),
    @JsonSubTypes.Type(value = IntDocValuesExactQuery.class),
    @JsonSubTypes.Type(value = IntDocValuesRangeQuery.class),
    @JsonSubTypes.Type(value = IntExactQuery.class),
    @JsonSubTypes.Type(value = IntMultiRangeQuery.class),
    @JsonSubTypes.Type(value = IntRangeQuery.class),
    @JsonSubTypes.Type(value = IntSetQuery.class),
    @JsonSubTypes.Type(value = JoinQuery.class),
    @JsonSubTypes.Type(value = LatLonPointBBoxQuery.class),
    @JsonSubTypes.Type(value = LatLonPointDistanceQuery.class),
    @JsonSubTypes.Type(value = LatLonPointPolygonQuery.class),
    @JsonSubTypes.Type(value = LongDocValuesExactQuery.class),
    @JsonSubTypes.Type(value = LongDocValuesRangeQuery.class),
    @JsonSubTypes.Type(value = LongExactQuery.class),
    @JsonSubTypes.Type(value = LongMultiRangeQuery.class),
    @JsonSubTypes.Type(value = LongRangeQuery.class),
    @JsonSubTypes.Type(value = LongSetQuery.class),
    @JsonSubTypes.Type(value = MatchAllDocsQuery.class),
    @JsonSubTypes.Type(value = MatchNoDocsQuery.class),
    @JsonSubTypes.Type(value = MultiPhraseQuery.class),
    @JsonSubTypes.Type(value = MoreLikeThisQuery.class),
    @JsonSubTypes.Type(value = MultiFieldQuery.class),
    @JsonSubTypes.Type(value = MultiFieldQueryParser.class),
    @JsonSubTypes.Type(value = NGramPhraseQuery.class),
    @JsonSubTypes.Type(value = PayloadScoreQuery.class),
    @JsonSubTypes.Type(value = PhraseQuery.class),
    @JsonSubTypes.Type(value = PrefixQuery.class),
    @JsonSubTypes.Type(value = QueryParser.class),
    @JsonSubTypes.Type(value = RegexpQuery.class),
    @JsonSubTypes.Type(value = SimpleQueryParser.class),
    @JsonSubTypes.Type(value = SortedDocValuesExactQuery.class),
    @JsonSubTypes.Type(value = SortedDocValuesRangeQuery.class),
    @JsonSubTypes.Type(value = SortedDoubleDocValuesExactQuery.class),
    @JsonSubTypes.Type(value = SortedDoubleDocValuesRangeQuery.class),
    @JsonSubTypes.Type(value = SortedFloatDocValuesExactQuery.class),
    @JsonSubTypes.Type(value = SortedFloatDocValuesRangeQuery.class),
    @JsonSubTypes.Type(value = SortedIntDocValuesExactQuery.class),
    @JsonSubTypes.Type(value = SortedIntDocValuesRangeQuery.class),
    @JsonSubTypes.Type(value = SortedLongDocValuesExactQuery.class),
    @JsonSubTypes.Type(value = SortedLongDocValuesRangeQuery.class),
    @JsonSubTypes.Type(value = SortedSetDocValuesExactQuery.class),
    @JsonSubTypes.Type(value = SortedSetDocValuesRangeQuery.class),
    @JsonSubTypes.Type(value = SpanContainingQuery.class),
    @JsonSubTypes.Type(value = SpanFirstQuery.class),
    @JsonSubTypes.Type(value = SpanNearQuery.class),
    @JsonSubTypes.Type(value = SpanNotQuery.class),
    @JsonSubTypes.Type(value = SpanOrQuery.class),
    @JsonSubTypes.Type(value = SpanPositionsQuery.class),
    @JsonSubTypes.Type(value = SpanQueryWrapper.class),
    @JsonSubTypes.Type(value = SpanTermQuery.class),
    @JsonSubTypes.Type(value = SpanWithinQuery.class),
    @JsonSubTypes.Type(value = SynonymQuery.class),
    @JsonSubTypes.Type(value = TermQuery.class),
    @JsonSubTypes.Type(value = TermRangeQuery.class),
    @JsonSubTypes.Type(value = TermsQuery.class),
    @JsonSubTypes.Type(value = WildcardQuery.class)})
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    creatorVisibility = JsonAutoDetect.Visibility.NONE,
    fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
public abstract class AbstractQuery<T extends AbstractQuery<T>> extends Equalizer<T> {

    protected final static URI CORE_BASE_DOC_URI = URI.create("https://lucene.apache.org/core/8_5_2/");

    @JsonProperty("doc")
    public final URI docUri;

    protected AbstractQuery(final Class<T> queryClass) {
        super(queryClass);
        docUri = null;
    }

    protected AbstractQuery(final Class<T> queryClass, final URI docUri) {
        super(queryClass);
        this.docUri = CORE_BASE_DOC_URI.resolve(docUri);
    }

    @JsonIgnore
    public abstract Query getQuery(final QueryContext queryContext)
        throws IOException, ParseException, QueryNodeException, ReflectiveOperationException;

    static public final SortedMap<String, Class<AbstractQuery<?>>> TYPES;

    static {
        final JsonSubTypes types = AbstractQuery.class.getAnnotation(JsonSubTypes.class);
        final SortedMap<String, Class<AbstractQuery<?>>> typeMap = new TreeMap<>();
        for (final JsonSubTypes.Type type : types.value()) {
            final Class<AbstractQuery<?>> typeClass = (Class<AbstractQuery<?>>) type.value();
            typeMap.put(type.value().getSimpleName(), typeClass);
        }
        TYPES = Collections.unmodifiableSortedMap(typeMap);
    }

    static public boolean hasFullTextAnalyzer(final FieldDefinition fieldDefinition) {
        if (StringUtils.isAllEmpty(fieldDefinition.analyzer, fieldDefinition.queryAnalyzer, fieldDefinition.indexAnalyzer))
            return false;
        final String alzr = fieldDefinition.analyzer != null ? fieldDefinition.analyzer
            : fieldDefinition.queryAnalyzer != null ? fieldDefinition.queryAnalyzer
            : fieldDefinition.indexAnalyzer;
        return !(alzr.endsWith(KeywordAnalyzer.class.getSimpleName()));
    }


    static private boolean fieldCheck(final FieldDefinition field,
                                      final Function<FieldDefinition, Boolean> fieldConsumer,
                                      final Function<SmartFieldDefinition, Boolean> smartFieldConsumer) {

        if (field instanceof SmartFieldDefinition)
            if (smartFieldConsumer.apply((SmartFieldDefinition) field))
                return true;
        return fieldConsumer.apply(field);
    }

    static private String forEachField(final Map<String, FieldDefinition> fields,
                                       final Function<FieldDefinition, Boolean> fieldConsumer,
                                       final Function<SmartFieldDefinition, Boolean> smartFieldConsumer,
                                       final Supplier<String> defaultField) {
        if (fields != null)
            for (final Map.Entry<String, FieldDefinition> entry : fields.entrySet())
                if (fieldCheck(entry.getValue(), fieldConsumer, smartFieldConsumer))
                    return entry.getKey();
        return defaultField.get();
    }

    static protected String getTextField(Map<String, FieldDefinition> fields,
                                         final Supplier<String> defaultField) {
        return forEachField(fields,
            f -> Boolean.FALSE,
            s -> s.type == SmartFieldDefinition.Type.TEXT,
            defaultField);
    }

    static protected String getFullTextField(final Map<String, FieldDefinition> fields,
                                             final Supplier<String> defaultField) {
        return forEachField(fields,
            AbstractQuery::hasFullTextAnalyzer,
            s -> Boolean.FALSE,
            defaultField);
    }

    static protected String getDoubleField(final Map<String, FieldDefinition> fields,
                                           final Supplier<String> defaultField) {
        return forEachField(fields,
            f -> Boolean.FALSE,
            s -> s.type == SmartFieldDefinition.Type.DOUBLE,
            defaultField);
    }

    static protected String getLongField(final Map<String, FieldDefinition> fields,
                                         final Supplier<String> defaultField) {
        return forEachField(fields,
            f -> Boolean.FALSE,
            s -> s.type == SmartFieldDefinition.Type.LONG,
            defaultField);
    }

    static protected String getFloatField(final Map<String, FieldDefinition> fields,
                                          final Supplier<String> defaultField) {
        return forEachField(fields,
            f -> Boolean.FALSE,
            s -> s.type == SmartFieldDefinition.Type.FLOAT,
            defaultField);
    }

    static protected String getIntField(final Map<String, FieldDefinition> fields,
                                        final Supplier<String> defaultField) {
        return forEachField(fields,
            f -> Boolean.FALSE,
            s -> s.type == SmartFieldDefinition.Type.INTEGER,
            defaultField);
    }

    public static AbstractQuery<?> getSample(final Class<AbstractQuery<?>> queryClass,
                                             final IndexSettingsDefinition settings,
                                             final Map<String, AnalyzerDefinition> analyzers,
                                             final Map<String, FieldDefinition> fields) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        final Constructor<AbstractQuery<?>> samplerConstructor = queryClass.getConstructor(IndexSettingsDefinition.class, Map.class, Map.class);
        return samplerConstructor.newInstance(settings, analyzers, fields);
    }

    static FieldTypeInterface getFieldType(final FieldMap fieldMap,
                                           final String genericFieldName,
                                           final String concreteFieldName,
                                           final Object value,
                                           final FieldTypeInterface.ValueType expectedValue) {
        final FieldTypeInterface fieldTypeInterface = fieldMap.getFieldType(genericFieldName, concreteFieldName, value);
        if (expectedValue != null && fieldTypeInterface.getValueType() != expectedValue)
            throw new NotAcceptableException("The field " + (genericFieldName == null ? concreteFieldName : genericFieldName)
                + " has a wrong value type: " + fieldTypeInterface.getValueType() + " -  The expected value type is " + expectedValue + ".");
        return fieldTypeInterface;
    }

    static String resolveFieldName(final FieldMap fieldMap,
                                   final String genericFieldName,
                                   final String concreteFieldName,
                                   final Object value,
                                   final FieldTypeInterface.ValueType expectedValueType,
                                   final FieldTypeInterface.FieldType... fieldTypes) {
        final FieldTypeInterface fieldTypeInterface = getFieldType(fieldMap, genericFieldName, concreteFieldName, value, expectedValueType);
        final FieldTypeInterface.FieldType fieldType = fieldTypeInterface.findFirstOf(fieldTypes);
        if (fieldType == null)
            throw new NotAcceptableException("The field "
                + (genericFieldName == null ? concreteFieldName : genericFieldName) + " is not indexed.");
        return fieldTypeInterface.resolveFieldName(concreteFieldName, fieldType, FieldTypeInterface.ValueType.textType);
    }

    static String resolveDocValueField(final FieldMap fieldMap,
                                       final String genericFieldName,
                                       final String concreteFieldName,
                                       final Object value,
                                       final FieldTypeInterface.ValueType valueType) {
        return resolveFieldName(fieldMap, genericFieldName, concreteFieldName, value,
            valueType, FieldTypeInterface.FieldType.docValues);
    }

    static String resolvePointField(final FieldMap fieldMap,
                                    final String genericFieldName,
                                    final String concreteFieldName,
                                    final Object value,
                                    final FieldTypeInterface.ValueType valueType) {
        return resolveFieldName(fieldMap, genericFieldName, concreteFieldName, value,
            valueType, FieldTypeInterface.FieldType.pointField);
    }

    static String resolveIndexTextField(final FieldMap fieldMap,
                                        final String genericFieldName,
                                        final String concreteFieldName,
                                        final Object value) {
        return resolveFieldName(fieldMap, genericFieldName, concreteFieldName, value, null,
            FieldTypeInterface.FieldType.stringField, FieldTypeInterface.FieldType.textField);
    }

    static String resolveFullTextField(final FieldMap fieldMap,
                                       final String genericFieldName,
                                       final String concreteFieldName,
                                       final Object value) {
        return resolveFieldName(fieldMap, genericFieldName, concreteFieldName, value, null,
            FieldTypeInterface.FieldType.textField, FieldTypeInterface.FieldType.stringField);
    }

    static Term resolveIndexTextTerm(final FieldMap fieldMap,
                                     final String genericFieldName,
                                     final String concreteFieldName,
                                     final Object value) {
        final String fieldName = resolveIndexTextField(fieldMap, genericFieldName, concreteFieldName, value);
        return new Term(fieldName, BytesRefUtils.fromAny(value));
    }

    static Term resolveFullTextTerm(final FieldMap fieldMap,
                                    final String genericFieldName,
                                    final String concreteFieldName,
                                    final Object value) {
        final String fieldName = resolveFullTextField(fieldMap, genericFieldName, concreteFieldName, value);
        return new Term(fieldName, BytesRefUtils.fromAny(value));
    }
}

