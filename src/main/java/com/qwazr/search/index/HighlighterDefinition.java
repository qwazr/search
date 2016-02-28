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

public class HighlighterDefinition {

	final public String field;

	final public Integer max_passages;

	final public Integer max_length;

	final public String multivalued_separator;

	final public String pre_tag;

	final public String post_tag;

	final public String ellipsis;

	final public Boolean escape;

	public HighlighterDefinition() {
		field = null;
		max_length = null;
		max_passages = null;
		multivalued_separator = null;
		pre_tag = null;
		post_tag = null;
		ellipsis = null;
		escape = null;
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
	}

	public static class Builder {

		private String field;

		private Integer maxPassages;

		private Integer maxLength;

		private char multivaluedSeparator = ' ';

		private String preTag;

		private String postTag;

		private String ellipsis;

		private Boolean escape;

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
	}
}
