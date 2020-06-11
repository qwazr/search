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
package com.qwazr.search.field;

import com.qwazr.search.analysis.SmartAnalyzerSet;
import com.qwazr.search.index.DocumentBuilder;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.WildcardMatcher;
import java.util.Objects;
import java.util.function.Function;
import javax.validation.constraints.NotNull;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.SortField;

final class SmartFieldType extends FieldTypeAbstract<SmartFieldDefinition> {

    private final Function<String, SmartFieldProvider> storeProvider;
    private final Function<String, SmartFieldProvider> indexProvider;
    private final Function<String, SmartFieldProvider> fullTextProvider;
    private final Function<String, SmartFieldProvider> sortProvider;

    SmartFieldType(final String genericFieldName,
                   final WildcardMatcher wildcardMatcher,
                   final SmartFieldDefinition definition) {
        super(of(genericFieldName, wildcardMatcher, definition));
        final boolean stored = definition.stored != null && definition.stored;
        final SmartFieldDefinition.Type type = getType(definition);
        if (stored) {
            switch (type) {
                case TEXT:
                    storeProvider = SmartFieldProvider.StoredText::new;
                    break;
                case LONG:
                    storeProvider = SmartFieldProvider.StoredLong::new;
                    break;
                case DOUBLE:
                    storeProvider = SmartFieldProvider.StoredDouble::new;
                    break;
                case INTEGER:
                    storeProvider = SmartFieldProvider.StoredInteger::new;
                    break;
                case FLOAT:
                    storeProvider = SmartFieldProvider.StoredFloat::new;
                    break;
                default:
                    storeProvider = null;
                    break;
            }
        } else {
            storeProvider = null;
        }
        final boolean index = definition.index != null && definition.index;
        if (index) {
            final int maxKeywordLength = getMaxKeywordLength(definition);
            switch (type) {
                case TEXT:
                    indexProvider = f -> new SmartFieldProvider.StringText(f, maxKeywordLength);
                    break;
                case LONG:
                    indexProvider = SmartFieldProvider.StringLong::new;
                    break;
                case INTEGER:
                    indexProvider = SmartFieldProvider.StringInteger::new;
                    break;
                case DOUBLE:
                    indexProvider = SmartFieldProvider.StringDouble::new;
                    break;
                case FLOAT:
                    indexProvider = SmartFieldProvider.StringFloat::new;
                    break;
                default:
                    indexProvider = null;
                    break;
            }
            if (isFullTextIndexAnalyzer(definition)) {
                fullTextProvider = SmartFieldProvider.FullText::new;
            } else
                fullTextProvider = null;
        } else {
            indexProvider = null;
            fullTextProvider = null;
        }
        final boolean sorted = definition.sort != null && definition.sort;
        if (sorted) {
            switch (type) {
                case TEXT:
                    sortProvider = SmartFieldProvider.SortedDocValuesText::new;
                    break;
                case LONG:
                    sortProvider = SmartFieldProvider.SortedDocValuesLong::new;
                    break;
                case INTEGER:
                    sortProvider = SmartFieldProvider.SortedDocValuesInteger::new;
                    break;
                case DOUBLE:
                    sortProvider = SmartFieldProvider.SortedDocValuesDouble::new;
                    break;
                case FLOAT:
                    sortProvider = SmartFieldProvider.SortedDocValuesFloat::new;
                    break;
                default:
                    sortProvider = null;
                    break;
            }
        } else {
            sortProvider = null;
        }
    }

    private static boolean isFullTextIndexAnalyzer(final SmartFieldDefinition definition) {
        final String keywordName = SmartAnalyzerSet.keyword.name();
        if (StringUtils.isNotEmpty(definition.indexAnalyzer)
            && !Objects.equals(definition.indexAnalyzer, keywordName))
            return true;
        if (StringUtils.isNotEmpty(definition.analyzer)
            && !Objects.equals(definition.analyzer, keywordName))
            return true;
        return false;
    }

    @Override
    protected void prepareFacet(Builder<SmartFieldDefinition> builder) {

    }

    @Override
    final protected void newField(final String fieldName, final Object value, final DocumentBuilder builder) {
        if (storeProvider != null)
            storeProvider.apply(fieldName).apply(value, builder);
        if (indexProvider != null)
            indexProvider.apply(fieldName).apply(value, builder);
        if (fullTextProvider != null)
            fullTextProvider.apply(fieldName).apply(value, builder);
    }

    private static SmartFieldDefinition.Type getType(final SmartFieldDefinition definition) {
        return definition.type == null ? SmartFieldDefinition.Type.TEXT : definition.type;
    }

    private static int getMaxKeywordLength(final SmartFieldDefinition definition) {
        return definition.maxKeywordLength == null
            ? SmartFieldDefinition.DEFAULT_MAX_KEYWORD_LENGTH : definition.maxKeywordLength;
    }

    /*
    static Function<String, SmartFieldProvider> facetProvider(final String genericFieldName,
                              final FieldTypeAbstract.Builder<SmartFieldDefinition> builder) {
        final SmartFieldProviders.FacetFieldProvider provider =
            new SmartFieldProviders.FacetFieldProvider(genericFieldName);
        builder.facetConfig(((fieldName, fieldMap, facetsConfig) -> {
            final String resolvedFieldName = provider.getTextName(fieldName);
            facetsConfig.setMultiValued(resolvedFieldName, true);
            facetsConfig.setIndexFieldName(resolvedFieldName, fieldMap.sortedSetFacetField);
        }));
        builder.fieldProvider(provider::textField);
        builder.queryFieldNameProvider(LuceneFieldType.facet, provider::getTextName);
    }
     */

    @Override
    public SortField getSortField(final String fieldName, final QueryDefinition.SortEnum sortEnum) {
        return sortProvider == null ? null : sortProvider.apply(fieldName).getSort(sortEnum);
    }

    @Override
    public String getQueryFieldName(@NotNull LuceneFieldType luceneFieldType, @NotNull String fieldName) {
        return null;
    }

    @Override
    public String getStoredFieldName(String fieldName) {
        return null;
    }

    @Override
    public Term term(final String fieldName, final Object value) {
        return indexProvider == null ? null : indexProvider.apply(fieldName).getTerm(value);
    }
}
