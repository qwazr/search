/**
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
 **/
package com.qwazr.search.index;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.qwazr.utils.TimeTracker;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class ResultDefinition<T extends ResultDocumentAbstract> {

	final public TimeTracker.Status timer;
	final public Long total_hits;
	final public Float max_score;
	final public List<T> documents;
	final public Map<String, Map<String, Number>> facets;
	final public String query;
	final public Map<String, Object> collectors;

	public ResultDefinition() {
		this.timer = null;
		this.total_hits = null;
		this.documents = null;
		this.facets = null;
		this.collectors = null;
		this.max_score = null;
		this.query = null;
	}

	protected ResultDefinition(final ResultDocumentsBuilder builder, @NotNull final List<T> documents) {
		this.query = builder.queryDebug;
		this.timer = builder.timeTrackerStatus;
		this.total_hits = builder.totalHits;
		this.max_score = builder.maxScore;
		this.documents = documents;
		this.facets = builder.facets;
		this.collectors = builder.collectors;
	}

	protected ResultDefinition(final ResultDefinition<?> src, @NotNull final List<T> documents) {
		this.query = src.query;
		this.timer = src.timer;
		this.total_hits = src.total_hits;
		this.max_score = src.max_score;
		this.documents = documents;
		this.facets = src.facets;
		this.collectors = src.collectors;
	}

	ResultDefinition(TimeTracker timeTracker) {
		query = null;
		total_hits = 0L;
		documents = Collections.emptyList();
		facets = null;
		collectors = null;
		max_score = null;
		this.timer = timeTracker != null ? timeTracker.getStatus() : null;
	}

	protected ResultDefinition(long total_hits) {
		query = null;
		this.total_hits = total_hits;
		documents = Collections.emptyList();
		facets = null;
		collectors = null;
		max_score = null;
		this.timer = null;
	}

	@JsonIgnore
	public Long getTotalHits() {
		return total_hits;
	}

	@Deprecated
	public Long getTotal_hits() {
		return total_hits;
	}

	@JsonIgnore
	public Float getMaxScore() {
		return max_score;
	}

	@Deprecated
	public Float getMax_score() {
		return max_score;
	}

	public List<T> getDocuments() {
		return documents == null ? Collections.emptyList() : documents;
	}

	public Map<String, Map<String, Number>> getFacets() {
		return facets == null ? Collections.emptyMap() : facets;
	}

	@JsonIgnore
	public Map<String, Number> getFacet(String facetName) {
		return facets == null ? Collections.emptyMap() : facets.get(facetName);
	}

	public TimeTracker.Status getTimer() {
		return timer;
	}

	public String getQuery() {
		return query;
	}

	public <O> O getCollector(String name) {
		return collectors == null ? null : (O) collectors.get(name);
	}

	final public void forEach(final Consumer<T> consumer) {
		if (documents != null)
			for (T document : documents)
				consumer.accept(document);
	}

	@JsonInclude(Include.NON_NULL)
	public static class WithMap extends ResultDefinition<ResultDocumentMap> {

		public WithMap() {
		}

		WithMap(ResultDocumentsBuilder builder, List<ResultDocumentMap> documents) {
			super(builder, documents);
		}

		public WithMap(int docs) {
			super(docs);
		}
	}

	public static class WithObject<T> extends ResultDefinition<ResultDocumentObject<T>> {

		WithObject(ResultDocumentsBuilder builder, List<ResultDocumentObject<T>> documents) {
			super(builder, documents);
		}

		public WithObject(final ResultDefinition<?> result, final List<ResultDocumentObject<T>> documents) {
			super(result, documents);
		}

		public WithObject(long totalHits) {
			super(totalHits);
		}

	}

	public static class Empty extends ResultDefinition {

		Empty(final ResultDocumentsBuilder builder) {
			super(builder, null);
		}

	}

	interface Builder<T extends ResultDocumentAbstract> extends Function<ResultDocumentsBuilder, ResultDefinition<T>> {
		ResultDocumentsInterface getResultDocuments();
	}

}
