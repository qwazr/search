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
package com.qwazr.search.index;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.qwazr.search.analysis.TermConsumer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.FlagsAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TermDefinition {

	public final String char_term;
	public final Integer start_offset;
	public final Integer end_offset;
	public final Integer position_increment;
	public final Integer position_length;
	public final String type;
	public final Integer flags;
	public final Boolean is_keyword;

	public TermDefinition() {
		char_term = null;
		start_offset = null;
		end_offset = null;
		position_increment = null;
		position_length = null;
		type = null;
		flags = null;
		is_keyword = null;
	}

	TermDefinition(final CharTermAttribute charTermAttr, final FlagsAttribute flagsAttr,
			final OffsetAttribute offsetAttr, final PositionIncrementAttribute posIncAttr,
			final PositionLengthAttribute posLengthAttr, final TypeAttribute typeAttr,
			final KeywordAttribute keywordAttr) {
		char_term = charTermAttr == null ? null : charTermAttr.toString();
		if (offsetAttr != null) {
			start_offset = offsetAttr.startOffset();
			end_offset = offsetAttr.endOffset();
		} else {
			start_offset = null;
			end_offset = null;
		}
		flags = flagsAttr == null ? null : flagsAttr.getFlags();
		position_increment = posIncAttr == null ? null : posIncAttr.getPositionIncrement();
		position_length = posLengthAttr == null ? null : posLengthAttr.getPositionLength();
		type = typeAttr == null ? null : typeAttr.type();
		is_keyword = keywordAttr == null ? null : keywordAttr.isKeyword();
	}

	final static List<TermDefinition> buildTermList(final Analyzer analyzer, final String field, final String text)
			throws IOException {
		Objects.requireNonNull(analyzer, "The analyzer cannot be null");
		Objects.requireNonNull(field, "The field cannot be null");
		Objects.requireNonNull(text, "The text cannot be null");
		final List<TermDefinition> termList = new ArrayList<>();
		try (final TokenStream tokenStream = analyzer.tokenStream(field, text)) {
			final TermConsumer.AllAttributes consumer = new TermConsumer.AllAttributes(tokenStream) {
				@Override
				public boolean token() {
					termList.add(
							new TermDefinition(charTermAttr, flagsAttr, offsetAttr, posIncAttr, posLengthAttr, typeAttr,
									keywordAttr));
					return true;
				}
			};
			consumer.forEachToken();
		}
		return termList;
	}

	private final static String[] DOT_PREFIX = { "digraph G {",
			"rankdir = LR;",
			"label = \"\";",
			"center = 1;",
			"ranksep = \"0.4\";",
			"nodesep = \"0.25\";" };

	private final static String[] DOT_SUFFIX = { "}" };

	private void writeDot(final PrintWriter pw) {
		pw.print(start_offset);
		pw.print(" -> ");
		pw.print(end_offset + 1);
		pw.print(" [label = \"");
		pw.print(char_term);
		pw.println("\"];");
	}

	static String toDot(final List<TermDefinition> terms) throws IOException {
		try (final StringWriter sw = new StringWriter()) {
			try (final PrintWriter pw = new PrintWriter(sw)) {
				for (String t : DOT_PREFIX)
					pw.println(t);

				// Build graph
				terms.forEach(term -> term.writeDot(pw));

				for (String t : DOT_SUFFIX)
					pw.println(t);
				pw.close();
				sw.close();
				return sw.toString();
			}
		}
	}
}
