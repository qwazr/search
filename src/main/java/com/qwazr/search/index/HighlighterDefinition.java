/**
 * Copyright 2016 Emmanuel Keller / QWAZR
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

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class HighlighterDefinition {

	final public String field;

	final public Integer max_passages;

	final public Integer max_length;

	final public String multivalued_separator;

	final public String pre_tag;

	final public String post_tag;

	final public String ellipsis;

	final public Boolean escape;

	public static class BreakIteratorDefinition {

		public enum Type {
			character, line, sentence, word
		}

		final public Type type;

		final public String language;

		public BreakIteratorDefinition() {
			type = null;
			language = null;
		}

		public BreakIteratorDefinition(Type type, String language) {
			this.type = type == null ? Type.sentence : type;
			this.language = language;
		}
	}

	final public BreakIteratorDefinition break_iterator;

	public HighlighterDefinition() {
		field = null;
		max_length = null;
		max_passages = null;
		multivalued_separator = null;
		pre_tag = null;
		post_tag = null;
		ellipsis = null;
		escape = null;
		break_iterator = null;
	}

	private HighlighterDefinition(Builder builder) {
		this.field = builder.field;
		this.max_length = builder.maxLength;
		this.max_passages = builder.maxPassages;
		this.multivalued_separator = Character.toString(builder.multivaluedSeparator);
		this.pre_tag = builder.preTag;
		this.post_tag = builder.postTag;
		this.ellipsis = builder.ellipsis;
		this.escape = builder.escape;
		this.break_iterator = builder.breakIterator;
	}

	public static class Builder {

		private String field = null;

		private Integer maxPassages = null;

		private Integer maxLength = null;

		private char multivaluedSeparator = ' ';

		private String preTag = null;

		private String postTag = null;

		private String ellipsis = null;

		private Boolean escape = null;

		private BreakIteratorDefinition breakIterator = null;

		public Builder() {
		}

		public HighlighterDefinition build() {
			return new HighlighterDefinition(this);
		}

		/**
		 * @param field field name to highlight. Must have a stored string value and also be indexed with offsets
		 * @return the current builder
		 */
		public Builder setField(String field) {
			this.field = field;
			return this;
		}

		/**
		 * @param maxPassages The maximum number of top-N ranked passages used to form the highlighted snippets
		 * @return the current builder
		 */
		public Builder setMaxPassages(int maxPassages) {
			this.maxPassages = maxPassages;
			return this;
		}

		/**
		 * @param maxLength maximum content size to process
		 * @return the current builder
		 */
		public Builder setMaxLength(int maxLength) {
			this.maxLength = maxLength;
			return this;
		}

		/**
		 * @param multivaluedSeparator the logical separator between values for multi-valued fields
		 * @return the current builder
		 */
		public Builder setMultivaluedSeparator(char multivaluedSeparator) {
			this.multivaluedSeparator = multivaluedSeparator;
			return this;
		}

		/**
		 * @param preTag text that will appear before highlighted terms
		 * @return the current builder
		 */
		public Builder setPreTag(String preTag) {
			this.preTag = preTag;
			return this;
		}

		/**
		 * @param postTag text that will appear after highlighted terms
		 * @return the current builder
		 */
		public Builder setPostTag(String postTag) {
			this.postTag = postTag;
			return this;
		}

		/**
		 * @param ellipsis text that will appear between two unconnected passages
		 * @return the current builder
		 */
		public Builder setEllipsis(String ellipsis) {
			this.ellipsis = ellipsis;
			return this;
		}

		/**
		 * @param escape true if we should escape for html
		 * @return the current builder
		 */
		public Builder setEscape(boolean escape) {
			this.escape = escape;
			return this;
		}

		/**
		 * @param breakIterator the break iterator parameters
		 * @return the current builder
		 */
		public Builder setBreak(BreakIteratorDefinition breakIterator) {
			this.breakIterator = breakIterator;
			return this;
		}

		/**
		 * @param type     the break iterator type
		 * @param language the language tag of the text to break
		 * @return the current builder
		 */
		public Builder setBreak(BreakIteratorDefinition.Type type, String language) {
			this.breakIterator = new BreakIteratorDefinition(type, language);
			return this;
		}
	}
}
