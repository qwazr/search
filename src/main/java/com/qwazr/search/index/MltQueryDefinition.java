/**
 * Copyright 2015-2016 Emmanuel Keller / QWAZR
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.index;

import com.qwazr.utils.StringUtils;
import com.qwazr.utils.json.JsonMapper;

import java.io.IOException;
import java.util.Set;

public class MltQueryDefinition extends BaseQueryDefinition {

    final public String document_query;
    final public String query_default_field;

    final public Boolean boost;

    final public Float boost_factor;

    final public String[] fieldnames;

    final public Integer max_doc_freq;

    final public Integer max_doc_freq_pct;

    final public Integer max_num_tokens_parsed;

    final public Integer max_query_terms;

    final public Integer max_word_len;

    final public Integer min_doc_freq;

    final public Integer min_term_freq;

    final public Integer min_word_len;

    final public Set<?> stop_words;

    public MltQueryDefinition() {
	document_query = null;
	query_default_field = null;
	boost_factor = null;
	fieldnames = null;
	boost = null;
	max_doc_freq = null;
	max_doc_freq_pct = null;
	max_num_tokens_parsed = null;
	max_query_terms = null;
	max_word_len = null;
	min_doc_freq = null;
	min_term_freq = null;
	min_word_len = null;
	stop_words = null;
    }

    public static MltQueryDefinition newQuery(String jsonString) throws IOException {
	if (StringUtils.isEmpty(jsonString))
	    return null;
	return JsonMapper.MAPPER.readValue(jsonString, MltQueryDefinition.class);
    }

}
