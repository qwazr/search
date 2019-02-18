/*
 * Copyright 2016-2017 Emmanuel Keller / QWAZR
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
package com.qwazr.search.index;

import com.qwazr.utils.StringUtils;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.uhighlight.WholeBreakIterator;
import org.apache.lucene.search.uhighlight.DefaultPassageFormatter;
import org.apache.lucene.search.uhighlight.PassageFormatter;
import org.apache.lucene.search.uhighlight.UnifiedHighlighter;

import java.io.IOException;
import java.text.BreakIterator;
import java.util.List;
import java.util.Locale;

final class HighlighterImpl extends UnifiedHighlighter {

    private final HighlighterDefinition definition;

    private final Locale locale;

    private final String[] indexFields;

    private final String[] storedFields;

    HighlighterImpl(final String highlightName, final HighlighterDefinition definition,
            final QueryContextImpl queryContext) {
        super(queryContext.indexSearcher, queryContext.queryAnalyzers);
        if (definition.maxLength != null)
            setMaxLength(definition.maxLength);
        if (definition.highlightPhrasesStrictly != null)
            setHighlightPhrasesStrictly(definition.highlightPhrasesStrictly);
        if (definition.maxNoHighlightPassages != null)
            setMaxNoHighlightPassages(definition.maxNoHighlightPassages);
        this.definition = definition;
        final String field = definition.field == null ? highlightName : definition.field;
        final String storedField = definition.storedField == null ? field : definition.storedField;
        this.indexFields = new String[] { queryContext.fieldMap.resolveQueryFieldName(field, field) };
        this.storedFields = new String[] { queryContext.fieldMap.resolveStoredFieldName(storedField) };
        if (definition.breakIterator != null && definition.breakIterator.language != null)
            locale = Locale.forLanguageTag(definition.breakIterator.language);
        else
            locale = Locale.ROOT;
    }

    protected List<CharSequence[]> loadFieldValues(String[] fields, DocIdSetIterator docIter, int cacheCharsThreshold)
            throws IOException {
        return super.loadFieldValues(storedFields, docIter, cacheCharsThreshold);
    }

    @Override
    protected PassageFormatter getFormatter(String field) {
        return new DefaultPassageFormatter(definition.preTag == null ? "<b>" : definition.preTag,
                definition.postTag == null ? "</b>" : definition.postTag,
                definition.ellipsis == null ? "… " : definition.ellipsis,
                definition.escape == null ? false : definition.escape);
    }

    @Override
    protected BreakIterator getBreakIterator(String field) {
        if (definition.breakIterator == null)
            return new WholeBreakIterator();
        switch (definition.breakIterator.type) {
        case character:
            return BreakIterator.getCharacterInstance(locale);
        case word:
            return BreakIterator.getWordInstance(locale);
        case line:
            return BreakIterator.getLineInstance(locale);
        default:
        case sentence:
            return BreakIterator.getSentenceInstance(locale);
        }
    }

    final String[] highlights(final Query query, final TopDocs topDocs) throws IOException {
        final String[] highlights = highlightFields(indexFields, query, topDocs,
                definition.maxPassages == null ? new int[] { 1 } : new int[] { definition.maxPassages }).get(
                indexFields[0]);
        int i = 0;
        for (final String highlight : highlights) {
            final String[] parts = StringUtils.split(highlight, MULTIVAL_SEP_CHAR);
            highlights[i++] = StringUtils.join(parts, definition.multivaluedSeparator);
        }
        return highlights;
    }
}
