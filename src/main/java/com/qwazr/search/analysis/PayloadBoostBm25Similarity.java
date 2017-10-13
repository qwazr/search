/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qwazr.search.analysis;

import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.util.BytesRef;

public class PayloadBoostBm25Similarity extends BM25Similarity {

	private final float[] boosts;

	public PayloadBoostBm25Similarity(float... boosts) {
		this.boosts = boosts;
	}

	protected float scorePayload(int doc, int start, int end, BytesRef payload) {
		if (payload == null)
			return 1F;
		final int pos = payload.bytes[payload.offset];
		return boosts[pos];
	}
}
