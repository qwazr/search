/*
 * Copyright 2017-2020 Emmanuel Keller / QWAZR
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

import javax.validation.constraints.NotNull;
import javax.ws.rs.NotSupportedException;
import java.util.List;

abstract class ResultDocumentsMap extends ResultDocumentsList<ResultDocumentMap> {

    private ResultDocumentsMap(@NotNull final QueryDefinition queryDefinition,
                               @NotNull final ReturnedFieldStrategy returnedFieldStrategy) {
        super(queryDefinition.getStartValue(), returnedFieldStrategy);
    }

    static ResultDocumentsMap of(@NotNull final QueryDefinition queryDefinition,
                                 @NotNull final ReturnedFieldStrategy returnedFieldStrategy) {
        switch (returnedFieldStrategy.type()) {
            case FIELDS:
                return new ForFields(queryDefinition, returnedFieldStrategy);
            case RECORD:
                return new ForRecord(queryDefinition, returnedFieldStrategy);
            case NONE:
                return new ForNone(queryDefinition, returnedFieldStrategy);
            default:
                throw new NotSupportedException("Strategy not supported" + returnedFieldStrategy.type());
        }
    }

    @Override
    protected ResultDefinition<ResultDocumentMap> newResultDefinition(ResultDocumentsBuilder resultDocumentsBuilder,
                                                                      List<ResultDocumentMap> documents) {
        return new ResultDefinition.WithMap(resultDocumentsBuilder, documents);
    }

    static final class ForRecord extends ResultDocumentsMap {

        private ForRecord(@NotNull final QueryDefinition queryDefinition,
                          @NotNull final ReturnedFieldStrategy returnedFieldStrategy) {
            super(queryDefinition, returnedFieldStrategy);
        }

        @Override
        protected ResultDocumentBuilder<ResultDocumentMap> newResultDocumentBuilder(final int absolutePos,
                                                                                    final ScoreDoc scoreDoc) {
            return new ResultDocumentMap.ForRecord(absolutePos, scoreDoc);
        }
    }

    static final class ForFields extends ResultDocumentsMap {

        private ForFields(final @NotNull QueryDefinition queryDefinition,
                          final @NotNull ReturnedFieldStrategy returnedFieldStrategy) {
            super(queryDefinition, returnedFieldStrategy);
        }

        @Override
        protected ResultDocumentBuilder<ResultDocumentMap> newResultDocumentBuilder(final int absolutePos,
                                                                                    final ScoreDoc scoreDoc) {
            return new ResultDocumentMap.ForFields(absolutePos, scoreDoc);
        }
    }

    static final class ForNone extends ResultDocumentsMap {

        private ForNone(final @NotNull QueryDefinition queryDefinition,
                        final @NotNull ReturnedFieldStrategy returnedFieldStrategy) {
            super(queryDefinition, returnedFieldStrategy);
        }

        @Override
        protected ResultDocumentBuilder<ResultDocumentMap> newResultDocumentBuilder(final int absolutePos,
                                                                                    final ScoreDoc scoreDoc) {
            return new ResultDocumentMap.ForNone(absolutePos, scoreDoc);
        }
    }
}
