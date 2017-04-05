/**
 * Copyright 2017 Emmanuel Keller / QWAZR
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

import org.apache.lucene.search.ScoreDoc;

import java.util.List;
import java.util.Set;

final class ResultDocumentsMap extends ResultDocumentsAbstract<ResultDocumentMap> {

	ResultDocumentsMap(final QueryContext context, final QueryDefinition queryDefinition,
			final Set<String> returnedFields) {
		super((QueryContextImpl) context, queryDefinition, returnedFields);
	}

	@Override
	ResultDocumentBuilder<ResultDocumentMap> newResultDocumentBuilder(int absolutePos, ScoreDoc scoreDoc) {
		return new ResultDocumentMap.Builder(absolutePos, scoreDoc);
	}

	@Override
	ResultDefinition<ResultDocumentMap> newResultDefinition(ResultDocumentsBuilder resultDocumentsBuilder,
			List<ResultDocumentMap> documents) {
		return new ResultDefinition.WithMap(resultDocumentsBuilder, documents);
	}

}
