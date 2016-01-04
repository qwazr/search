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
package com.qwazr.search.index;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.qwazr.search.analysis.AnalyzerUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.tokenattributes.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

	TermDefinition(CharTermAttribute charTermAttr, FlagsAttribute flagsAttr, OffsetAttribute offsetAttr,
					PositionIncrementAttribute posIncAttr, PositionLengthAttribute posLengthAttr,
					TypeAttribute typeAttr, KeywordAttribute keywordAttr) {
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

	final static List<TermDefinition> buildTermList(Analyzer analyzer, String field, String text) throws IOException {
		final List<TermDefinition> termList = new ArrayList<TermDefinition>();
		AnalyzerUtils.forEachTerm(analyzer, field, text, new AnalyzerUtils.TermConsumer() {
			@Override
			public boolean apply(CharTermAttribute charTermAttr, FlagsAttribute flagsAttr, OffsetAttribute offsetAttr,
							PositionIncrementAttribute posIncAttr, PositionLengthAttribute posLengthAttr,
							TypeAttribute typeAttr, KeywordAttribute keywordAttr) {
				termList.add(new TermDefinition(charTermAttr, flagsAttr, offsetAttr, posIncAttr, posLengthAttr,
								typeAttr, keywordAttr));
				return true;
			}
		});
		return termList;
	}

	public final static TypeReference<List<TermDefinition>> MapListTermDefinitionRef = new TypeReference<List<TermDefinition>>() {
	};
}
