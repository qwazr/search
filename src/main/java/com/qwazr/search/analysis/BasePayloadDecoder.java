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

import com.qwazr.utils.Equalizer;
import org.apache.lucene.queries.payloads.PayloadDecoder;

public abstract class BasePayloadDecoder<T extends BasePayloadDecoder<?>> extends Equalizer<T> implements PayloadDecoder {

    protected final float defaultValue;

    public BasePayloadDecoder(float defaultValue, final Class<T> ownClass) {
        super(ownClass);
        this.defaultValue = defaultValue;
    }

    @Override
    public int hashCode() {
        return Float.hashCode(defaultValue);
    }

    @Override
    protected boolean isEqual(final T query) {
        return query.defaultValue == defaultValue;
    }
}
