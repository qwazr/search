/**
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
 **/
package com.qwazr.search.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.postingshighlight.DefaultPassageFormatter;
import org.apache.lucene.search.postingshighlight.PassageFormatter;
import org.apache.lucene.search.postingshighlight.PostingsHighlighter;
import org.apache.lucene.search.postingshighlight.WholeBreakIterator;

import java.io.IOException;
import java.text.BreakIterator;
import java.util.Locale;

class HighlighterImpl extends PostingsHighlighter {

	private final HighlighterDefinition definition;

	private final char separator;

	private final Locale locale;

	private final Analyzer analyzer;

	HighlighterImpl(HighlighterDefinition definition, Analyzer analyzer) {
		super(definition.max_length == null ? PostingsHighlighter.DEFAULT_MAX_LENGTH : definition.max_length);
		this.definition = definition;
		this.analyzer = analyzer;

		separator = definition.multivalued_separator == null || definition.multivalued_separator.isEmpty() ?
				' ' :
				definition.multivalued_separator.charAt(0);

		if (definition.break_iterator != null && definition.break_iterator.language != null)
			locale = Locale.forLanguageTag(definition.break_iterator.language);
		else
			locale = Locale.ROOT;
	}

	@Override
	protected PassageFormatter getFormatter(String field) {
		return new DefaultPassageFormatter(definition.pre_tag == null ? "<b>" : definition.pre_tag,
				definition.post_tag == null ? "</b>" : definition.post_tag,
				definition.ellipsis == null ? "... " : definition.ellipsis,
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

	@Override
	protected Analyzer getIndexAnalyzer(String field) {
		return analyzer;
	}

	@Override
	protected char getMultiValuedSeparator(String field) {
		return separator;
	}

	String[] highlights(final Query query, final IndexSearcher indexSearcher, final int[] docs) throws IOException {
		return highlightFields(new String[] { definition.field }, query, indexSearcher, docs,
				definition.max_passages == null ? new int[] { 1 } : new int[] { definition.max_passages }).get(
				definition.field);

	}
}
