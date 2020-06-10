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
 */
package com.qwazr.search.index;

import com.qwazr.binder.FieldMapWrapper;
import org.apache.lucene.search.ScoreDoc;

import javax.validation.constraints.NotNull;
import javax.ws.rs.NotSupportedException;
import java.util.List;

abstract class ResultDocumentsObject<T> extends ResultDocumentsList<ResultDocumentObject<T>> {

    private ResultDocumentsObject(final QueryDefinition queryDefinition,
                                  final ReturnedFieldStrategy returnedFieldStrategy) {
        super(queryDefinition.getStartValue(), returnedFieldStrategy);
    }

    static <T> ResultDocumentsObject<T> of(@NotNull final QueryDefinition queryDefinition,
                                           @NotNull final ReturnedFieldStrategy returnedFieldStrategy,
                                           @NotNull final FieldMapWrapper<T> wrapper) {
        switch (returnedFieldStrategy.type()) {
            case FIELDS:
                return new ForFields<>(queryDefinition, returnedFieldStrategy, wrapper);
            case RECORD:
                return new ForRecord<>(queryDefinition, returnedFieldStrategy, wrapper.objectClass);
            case NONE:
                return new ForNone<>(queryDefinition, returnedFieldStrategy);
            default:
                throw new NotSupportedException("Strategy not supported" + returnedFieldStrategy.type());
        }
    }

    @Override
    final protected ResultDefinition<ResultDocumentObject<T>> newResultDefinition(final ResultDocumentsBuilder resultDocumentsBuilder,
                                                                                  final List<ResultDocumentObject<T>> documents) {
        return new ResultDefinition.WithObject<>(resultDocumentsBuilder, documents);
    }

    static final class ForRecord<T> extends ResultDocumentsObject<T> {

        private final Class<T> recordClass;

        private ForRecord(@NotNull final QueryDefinition queryDefinition,
                          @NotNull final ReturnedFieldStrategy returnedFieldStrategy,
                          @NotNull final Class<T> recordClass) {
            super(queryDefinition, returnedFieldStrategy);
            this.recordClass = recordClass;
        }

        @Override
        protected ResultDocumentBuilder<ResultDocumentObject<T>> newResultDocumentBuilder(final int absolutePos,
                                                                                          final ScoreDoc scoreDoc) {
            return new ResultDocumentObject.ForRecord<>(absolutePos, scoreDoc, recordClass);
        }
    }

    static final class ForFields<T> extends ResultDocumentsObject<T> {

        private final FieldMapWrapper<T> wrapper;

        private ForFields(final @NotNull QueryDefinition queryDefinition,
                          final @NotNull ReturnedFieldStrategy returnedFieldStrategy,
                          final @NotNull FieldMapWrapper<T> wrapper) {
            super(queryDefinition, returnedFieldStrategy);
            this.wrapper = wrapper;
        }

        @Override
        protected ResultDocumentBuilder<ResultDocumentObject<T>> newResultDocumentBuilder(final int absolutePos,
                                                                                          final ScoreDoc scoreDoc) {
            return new ResultDocumentObject.ForFields<>(absolutePos, scoreDoc, wrapper);
        }
    }

    static final class ForNone<T> extends ResultDocumentsObject<T> {

        private ForNone(@NotNull final QueryDefinition queryDefinition,
                        @NotNull final ReturnedFieldStrategy returnedFieldStrategy) {
            super(queryDefinition, returnedFieldStrategy);
        }

        @Override
        protected ResultDocumentBuilder<ResultDocumentObject<T>> newResultDocumentBuilder(final int absolutePos,
                                                                                          final ScoreDoc scoreDoc) {
            return new ResultDocumentObject.ForNone<>(absolutePos, scoreDoc);
        }
    }
}
