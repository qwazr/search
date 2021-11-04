/*
 * Copyright 2016-2020 Emmanuel Keller / QWAZR
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

import com.fasterxml.jackson.databind.JsonNode;
import com.qwazr.search.query.FieldResolver;
import com.qwazr.utils.ObjectMappers;
import com.qwazr.utils.StringUtils;
import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.StoredFieldVisitor;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.uhighlight.DefaultPassageFormatter;
import org.apache.lucene.search.uhighlight.PassageFormatter;
import org.apache.lucene.search.uhighlight.UnifiedHighlighter;
import org.apache.lucene.search.uhighlight.WholeBreakIterator;

interface Highlighters {

    LinkedHashMap<String, String[]> highlights(final Query query, final TopDocs topDocs);

    static Highlighters of(final LinkedHashMap<String, HighlighterDefinition> definitions,
                           final QueryContextImpl queryContext) {
        if (queryContext.fieldMap.fieldsContext.recordField != null
            && !queryContext.fieldMap.fieldsContext.recordField.isEmpty())
            return new WithRecord(definitions, queryContext);
        else
            return new WithStore(definitions, queryContext);
    }

    abstract class Base implements Highlighters {

        private final LinkedHashMap<String, PerFieldBase> perFieldHighlighterMap;
        protected final QueryContextImpl queryContext;


        private Base(final LinkedHashMap<String, HighlighterDefinition> definitions,
                     final QueryContextImpl queryContext) {
            this.queryContext = queryContext;
            this.perFieldHighlighterMap = new LinkedHashMap<>();
            definitions.forEach((name, definition) -> perFieldHighlighterMap.put(name, newPerField(name, definition)));
        }

        protected abstract PerFieldBase newPerField(final String highlightName,
                                                    final HighlighterDefinition definition);

        @Override
        public final LinkedHashMap<String, String[]> highlights(final Query query, final TopDocs topDocs) {
            final LinkedHashMap<String, String[]> result = new LinkedHashMap<>();
            perFieldHighlighterMap.forEach((name, perField) -> {
                result.put(name, perField.highlight(query, topDocs));
            });
            return result;
        }

        protected abstract class PerFieldBase extends UnifiedHighlighter {

            private final HighlighterDefinition definition;

            private final int maxPassages;

            protected final String field;

            private final String indexField;

            private final PassageFormatter passageFormatter;

            private final BreakIterator breakIterator;

            private final String highlightName;

            private PerFieldBase(final String highlightName,
                                 final HighlighterDefinition definition,
                                 final String storedField) {
                super(queryContext.indexSearcher, queryContext.resolveQueryAnalyzer(definition.defaultAnalyzer));
                if (definition.maxLength != null)
                    setMaxLength(definition.maxLength);
                if (definition.highlightPhrasesStrictly != null)
                    setHighlightPhrasesStrictly(definition.highlightPhrasesStrictly);
                if (definition.maxNoHighlightPassages != null)
                    setMaxNoHighlightPassages(definition.maxNoHighlightPassages);
                this.highlightName = highlightName;
                this.definition = definition;
                this.field = definition.field == null ? highlightName : definition.field;
                this.indexField = FieldResolver.resolveFullTextField(queryContext.fieldMap, field, field, StringUtils.EMPTY);
                this.maxPassages = definition.maxPassages != null ? definition.maxPassages : 1;
                this.passageFormatter = new DefaultPassageFormatter(definition.preTag == null ? "<b>" : definition.preTag,
                    definition.postTag == null ? "</b>" : definition.postTag,
                    definition.ellipsis == null ? "… " : definition.ellipsis,
                    definition.escape != null && definition.escape);

                if (definition.breakIterator == null)
                    this.breakIterator = new WholeBreakIterator();
                else {
                    final Locale locale;
                    if (definition.breakIterator.language != null)
                        locale = Locale.forLanguageTag(definition.breakIterator.language);
                    else
                        locale = Locale.ROOT;
                    switch (definition.breakIterator.type) {
                        case character:
                            this.breakIterator = BreakIterator.getCharacterInstance(locale);
                            break;
                        case word:
                            this.breakIterator = BreakIterator.getWordInstance(locale);
                            break;
                        case line:
                            this.breakIterator = BreakIterator.getLineInstance(locale);
                            break;
                        default:
                        case sentence:
                            this.breakIterator = BreakIterator.getSentenceInstance(locale);
                            break;
                    }
                }
            }

            protected abstract List<CharSequence[]> loadFieldValues(final DocIdSetIterator docIter,
                                                                    final int cacheCharsThreshold) throws IOException;

            @Override
            final protected List<CharSequence[]> loadFieldValues(final String[] fields,
                                                                 final DocIdSetIterator docIter,
                                                                 final int cacheCharsThreshold) throws IOException {
                return loadFieldValues(docIter, cacheCharsThreshold);
            }

            final protected List<CharSequence[]> superLoadFieldValues(final String[] fields,
                                                                      final DocIdSetIterator docIter,
                                                                      final int cacheCharsThreshold) throws IOException {
                return super.loadFieldValues(fields, docIter, cacheCharsThreshold);
            }

            @Override
            protected PassageFormatter getFormatter(final String field) {
                return passageFormatter;
            }

            @Override
            protected BreakIterator getBreakIterator(final String field) {
                return breakIterator;
            }

            private String[] highlight(final Query query, final TopDocs topDocs) {
                final String[] highlights;
                try {
                    highlights = super.highlight(indexField, query, topDocs, maxPassages);
                } catch (IOException e) {
                    throw new RuntimeException("Highlighting failure: " + highlightName, e);
                }
                int i = 0;
                for (final String highlight : highlights) {
                    final String[] parts = StringUtils.split(highlight, MULTIVAL_SEP_CHAR);
                    highlights[i++] = StringUtils.join(parts, definition.multivaluedSeparator);
                }
                return highlights;
            }

        }
    }

    class WithStore extends Base {

        private WithStore(final LinkedHashMap<String, HighlighterDefinition> definitions, final QueryContextImpl queryContext) {
            super(definitions, queryContext);
        }

        @Override
        protected PerFieldBase newPerField(final String highlightName,
                                           final HighlighterDefinition definition) {
            final String unResolvedStoredField = definition.storedField == null ? definition.field : definition.storedField;
            final String storedField = FieldResolver.resolveStoredField(queryContext.fieldMap, unResolvedStoredField, unResolvedStoredField);
            return new PerFieldStore(highlightName, definition, storedField);
        }

        class PerFieldStore extends PerFieldBase {

            private final String[] storedFields;

            private PerFieldStore(String highlightName, HighlighterDefinition definition, String storedField) {
                super(highlightName, definition, storedField);
                this.storedFields = new String[]{storedField};
            }

            @Override
            protected List<CharSequence[]> loadFieldValues(final DocIdSetIterator docIter,
                                                           final int cacheCharsThreshold) throws IOException {
                return superLoadFieldValues(storedFields, docIter, cacheCharsThreshold);
            }
        }
    }

    class WithRecord extends Base {

        private final ConcurrentHashMap<Integer, JsonNode> recordCache = new ConcurrentHashMap<>();
        private final String recordField;

        private WithRecord(LinkedHashMap<String, HighlighterDefinition> definition, QueryContextImpl queryContext) {
            super(definition, queryContext);
            recordField = queryContext.fieldMap.fieldsContext.recordField;
        }

        @Override
        protected PerFieldBase newPerField(final String highlightName,
                                           final HighlighterDefinition definition) {
            final String storedField = definition.storedField == null ? definition.field : definition.storedField;
            return new PerFieldRecord(highlightName, definition, storedField);
        }

        private class PerFieldRecord extends PerFieldBase {

            private final String[] jsonFieldPath;

            private PerFieldRecord(final String highlightName,
                                   final HighlighterDefinition definition,
                                   final String storedField) {
                super(highlightName, definition, storedField);
                jsonFieldPath = StringUtils.split(field, '.');
            }

            private JsonNode loadRecord(int docId) {
                return recordCache.computeIfAbsent(docId, doc -> {
                    final AtomicReference<JsonNode> result = new AtomicReference<>();
                    try {
                        searcher.doc(doc, new StoredFieldVisitor() {
                            @Override
                            public Status needsField(final FieldInfo fieldInfo) {
                                return recordField.equals(fieldInfo.name) ? Status.YES : Status.NO;
                            }

                            public void binaryField(final FieldInfo fieldInfo, final byte[] value) throws IOException {
                                result.set(ObjectMappers.SMILE.readTree(value));
                            }
                        });
                    } catch (IOException e) {
                        throw new RuntimeException("Doc not found: " + docId);
                    }
                    return result.get();
                });
            }

            @Override
            protected List<CharSequence[]> loadFieldValues(final DocIdSetIterator docIter,
                                                           final int cacheCharsThreshold) throws IOException {
                final List<CharSequence[]> docListOfFields = new ArrayList<>(cacheCharsThreshold == 0 ? 1 : (int) Math.min(64L, docIter.cost()));
                final AtomicInteger sumChars = new AtomicInteger();
                do {
                    final int doc = docIter.nextDoc();
                    if (doc == DocIdSetIterator.NO_MORE_DOCS)
                        break;
                    JsonNode jsonNode = loadRecord(doc);
                    if (jsonNode == null)
                        break;
                    for (final String jsonField : jsonFieldPath) {
                        jsonNode = jsonNode.get(jsonField);
                        if (jsonNode == null)
                            break;
                    }
                    if (jsonNode == null || !jsonNode.isTextual())
                        break;
                    final String fieldValue = jsonNode.asText();
                    docListOfFields.add(new CharSequence[]{fieldValue});
                    sumChars.addAndGet(fieldValue.length());

                } while (sumChars.get() <= cacheCharsThreshold && cacheCharsThreshold != 0);
                if (docListOfFields.isEmpty())
                    docListOfFields.add(new CharSequence[]{null});
                return docListOfFields;
            }
        }
    }


}
