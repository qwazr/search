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

import com.qwazr.search.field.FieldDefinition;
import com.qwazr.utils.Equalizer;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import org.apache.commons.text.similarity.JaccardDistance;
import org.apache.commons.text.similarity.LongestCommonSubsequence;
import org.apache.lucene.facet.sortedset.DefaultSortedSetDocValuesReaderState;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.index.IndexReader;

class IndexUtils {

    static SortedSetDocValuesReaderState getNewFacetsState(final IndexReader indexReader, final String stateFacetField)
        throws IOException {
        try {
            return new DefaultSortedSetDocValuesReaderState(indexReader,
                stateFacetField == null ? FieldDefinition.DEFAULT_SORTEDSET_FACET_FIELD : stateFacetField);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("was not indexed with SortedSetDocValues"))
                return null;
            throw e;
        }
    }

    static void codeLookup(final String lookup,
                           final Map<String, String[]> candidates,
                           final Consumer<String> resultConsumer) {
        final LongestCommonSubsequence longestCommonSubsequence = new LongestCommonSubsequence();
        final JaccardDistance jacardDistance = new JaccardDistance();
        final Set<LookupItem> ordered = new TreeSet<>();
        final String lowerCaseLookup = lookup.toLowerCase();
        candidates.forEach((typeKey, camelWords) -> {
            final LookupItem lookupItem = new LookupItem(
                longestCommonSubsequence,
                jacardDistance, lowerCaseLookup,
                typeKey, camelWords
            );
            if (lookupItem.isMatch())
                ordered.add(lookupItem);
        });
        ordered.forEach(lookupItem -> resultConsumer.accept(lookupItem.typeKey));
    }

    private static final class LookupItem extends Equalizer.Immutable<LookupItem> implements Comparable<LookupItem> {

        private final double camelMatch;
        private final int longestCommon;
        private final double jacardScore;
        private final String typeKey;

        private LookupItem(final LongestCommonSubsequence longestCommonSubsequence,
                           final JaccardDistance jacardDistance,
                           final String lowerCaseLookup,
                           final String typeKey,
                           final String[] typeKeyKeywords) {
            super(LookupItem.class);
            this.typeKey = typeKey;
            final String lowercaseType = typeKey.toLowerCase();
            double match = 0;
            for (final String typeKeyKeyword : typeKeyKeywords) {
                if (typeKeyKeyword.length() > 1)
                    if (lowerCaseLookup.contains(typeKeyKeyword))
                        match++;
            }
            camelMatch = match == 0 ? 0 : match / typeKeyKeywords.length;
            longestCommon = longestCommonSubsequence.apply(lowerCaseLookup, lowercaseType);
            jacardScore = jacardDistance.apply(lowerCaseLookup, lowercaseType);
        }

        @Override
        protected int computeHashCode() {
            return Objects.hash(camelMatch, longestCommon, jacardScore);
        }

        @Override
        protected boolean isEqual(final LookupItem other) {
            return Objects.equals(camelMatch, other.camelMatch)
                && Objects.equals(longestCommon, other.longestCommon)
                && Objects.equals(jacardScore, other.jacardScore)
                && Objects.equals(typeKey, other.typeKey);
        }

        public boolean isMatch() {
            return camelMatch > 0 || longestCommon > 0 || jacardScore < 1;
        }

        @Override
        public int compareTo(final LookupItem other) {
            int f;
            if ((f = Double.compare(other.camelMatch, camelMatch)) != 0)
                return f;
            if ((f = Integer.compare(other.longestCommon, longestCommon)) != 0)
                return f;
            if ((f = Double.compare(jacardScore, other.jacardScore)) != 0)
                return f;
            return typeKey.compareTo(other.typeKey);
        }

    }

}
