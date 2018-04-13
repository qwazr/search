/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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
package com.qwazr.search.collector;

import java.util.Collection;

public interface NumericCollector<R extends Comparable<R>> extends ConcurrentCollector<R> {

    boolean keepNewValue(R previousValue, R newValue);

    default R getReducedResult(final Collection<BaseCollector<R>> collectorCollection) {
        R previousValue = null;
        for (BaseCollector<R> collector : collectorCollection) {
            if (collector == null)
                continue;
            final R newValue = collector.getResult();
            if (newValue == null)
                continue;
            if (previousValue == null) {
                previousValue = newValue;
                continue;
            }
            if (keepNewValue(previousValue, newValue))
                previousValue = newValue;
        }
        return previousValue;
    }
}
