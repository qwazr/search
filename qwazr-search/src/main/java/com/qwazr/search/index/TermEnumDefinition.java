/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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
import com.qwazr.search.field.FieldTypeInterface;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TermEnumDefinition {

    public final Object term;
    public final Integer doc_freq;
    public final Long total_term_freq;

    public TermEnumDefinition() {
        term = null;
        doc_freq = null;
        total_term_freq = null;
    }

    TermEnumDefinition(final FieldTypeInterface field, final TermsEnum termsEnum) throws IOException {
        term = field.toTerm(termsEnum.term());
        doc_freq = termsEnum.docFreq();
        total_term_freq = termsEnum.totalTermFreq();
    }

    static List<TermEnumDefinition> buildTermList(final FieldTypeInterface field, final TermsEnum termsEnum,
                                                  final String prefix, int start, int rows) throws IOException {
        final List<TermEnumDefinition> termList = new ArrayList<>();
        if (prefix != null) {
            TermsEnum.SeekStatus status = termsEnum.seekCeil(new BytesRef(prefix));
            if (status == null || status == TermsEnum.SeekStatus.END)
                return termList;
        } else if (termsEnum.next() == null)
            return termList;

        while (start-- > 0)
            if (termsEnum.next() == null)
                return termList;

        while (rows-- > 0) {
            termList.add(new TermEnumDefinition(field, termsEnum));
            if (termsEnum.next() == null)
                break;
        }
        return termList;
    }

    public final static TypeReference<List<TermEnumDefinition>> ListTermEnumDefinitionRef =
        new TypeReference<>() {
        };
}
