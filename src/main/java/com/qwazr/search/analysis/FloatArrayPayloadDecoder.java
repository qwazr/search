/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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

import com.qwazr.utils.Equalizer;
import org.apache.lucene.queries.payloads.PayloadDecoder;
import org.apache.lucene.util.BytesRef;

public class FloatArrayPayloadDecoder extends Equalizer<FloatArrayPayloadDecoder> implements PayloadDecoder {

    private final float[] boosts;

    public FloatArrayPayloadDecoder(float... boosts) {
        super(FloatArrayPayloadDecoder.class);
        this.boosts = boosts;
    }

    public float computePayloadFactor(BytesRef payload) {
        if (payload == null)
            return 1F;
        final int pos = payload.bytes[payload.offset];
        return boosts[pos];
    }

    @Override
    protected boolean isEqual(FloatArrayPayloadDecoder query) {
        return true;
    }
}
