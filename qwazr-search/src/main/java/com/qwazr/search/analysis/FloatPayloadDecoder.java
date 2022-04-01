/*
 *  Copyright 2015-2020 Emmanuel Keller / QWAZR
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.qwazr.search.analysis;

import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.util.BytesRef;

public class FloatPayloadDecoder extends BasePayloadDecoder<FloatPayloadDecoder> {

    public static final FloatPayloadDecoder INSTANCE = new FloatPayloadDecoder();

    public FloatPayloadDecoder(final float defaultValue) {
        super(defaultValue, FloatPayloadDecoder.class);
    }

    public FloatPayloadDecoder() {
        this(1F);
    }

    @Override
    public float computePayloadFactor(final BytesRef payload) {
        if (payload == null)
            return defaultValue;
        return PayloadHelper.decodeFloat(payload.bytes, payload.offset);
    }

    @Override
    protected boolean isEqual(final FloatPayloadDecoder query) {
        return true;
    }
}
