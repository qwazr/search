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
 **/
package com.qwazr.search.index;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;

public abstract class ResultDocumentAbstract {

	final public Float score;
	final public Float percent_score;
	final private int doc;
	final private int shard_index;
	final public Map<String, String> highlights;

	public ResultDocumentAbstract() {
		score = null;
		percent_score = null;
		highlights = null;
		doc = -1;
		shard_index = -1;
	}

	protected ResultDocumentAbstract(final ResultDocumentBuilder builder) {
		this.score = builder.scoreDoc.score;
		this.percent_score = builder.percentScore;
		highlights = builder.highlights;
		this.doc = builder.scoreDoc.doc;
		this.shard_index = builder.scoreDoc.shardIndex;
	}

	protected ResultDocumentAbstract(final ResultDocumentAbstract src) {
		this.score = src.score;
		this.percent_score = src.percent_score;
		highlights = src.highlights;
		this.doc = src.doc;
		this.shard_index = src.shard_index;
	}

	final public Float getScore() {
		return score;
	}

	@JsonIgnore
	final public Float getPercentScore() {
		return percent_score;
	}

	final public Float getPercent_score() {
		return percent_score;
	}

	final public int getDoc() {
		return doc;
	}

	final public int getShard_index() {
		return shard_index;
	}

	@JsonIgnore
	final public int getShardIndex() {
		return shard_index;
	}

	final public Map<String, String> getHighlights() {
		return highlights;
	}

}
