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

import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.postingshighlight.WholeBreakIterator;
import org.apache.lucene.search.uhighlight.DefaultPassageFormatter;
import org.apache.lucene.search.uhighlight.PassageFormatter;
import org.apache.lucene.search.uhighlight.UnifiedHighlighter;

import java.io.IOException;
import java.text.BreakIterator;
import java.util.List;
import java.util.Locale;

class HighlighterImpl extends UnifiedHighlighter {

	private final HighlighterDefinition definition;

	private final Locale locale;

	private final String[] indexFields;

	private final String[] storedFields;

	HighlighterImpl(HighlighterDefinition definition, QueryContextImpl queryContext) {
		super(queryContext.indexSearcher, queryContext.queryAnalyzer);
		if (definition.max_length != null)
			setMaxLength(definition.max_length);
		if (definition.highlight_phrases_strictly != null)
			setHighlightPhrasesStrictly(definition.highlight_phrases_strictly);
		if (definition.max_no_highlight_passages != null)
			setMaxNoHighlightPassages(definition.max_no_highlight_passages);
		this.definition = definition;
		this.indexFields = new String[] { queryContext.fieldMap.resolveQueryFieldName(definition.field) };
		this.storedFields = new String[] { queryContext.fieldMap.resolveStoredFieldName(definition.field) };
		if (definition.break_iterator != null && definition.break_iterator.language != null)
			locale = Locale.forLanguageTag(definition.break_iterator.language);
		else
			locale = Locale.ROOT;
	}

	protected List<CharSequence[]> loadFieldValues(String[] fields, DocIdSetIterator docIter, int cacheCharsThreshold)
			throws IOException {
		return super.loadFieldValues(storedFields, docIter, cacheCharsThreshold);
	}

	@Override
	protected PassageFormatter getFormatter(String field) {
		return new DefaultPassageFormatter(definition.pre_tag == null ? "<b>" : definition.pre_tag,
				definition.post_tag == null ? "</b>" : definition.post_tag,
				definition.ellipsis == null ? "… " : definition.ellipsis,
				definition.escape == null ? false : definition.escape);
	}

	@Override
	protected BreakIterator getBreakIterator(String field) {
		if (definition.break_iterator == null)
			return new WholeBreakIterator();
		switch (definition.break_iterator.type) {
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

	String[] highlights(final Query query, final TopDocs topDocs) throws IOException {
		return highlightFields(indexFields, query, topDocs,
				definition.max_passages == null ? new int[] { 1 } : new int[] { definition.max_passages }).get(
				indexFields[0]);

	}
}
