/*
 * Copyright 2020 Emmanuel Keller / QWAZR
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
package com.qwazr.utils;

import com.qwazr.utils.concurrent.ConsumerEx;
import java.util.function.Consumer;

/**
 * The main difference with optional, is that no object is created.
 * The two sentences are equivalent:
 * IfNotNull.apply(anObject, o -> System.out.println(o + " is not null!");
 * Optional.ofNullable(anObject).ifPresent(o -> System.out.println(o + " is not null!"));
 */
public interface IfNotNull {

    static <T> void apply(final T object, final Consumer<T> consumer) {
        if (object != null)
            consumer.accept(object);
    }

    static <T, E extends Exception> void applyEx(final T object, final ConsumerEx<T, E> consumer) throws Exception {
        if (object != null)
            consumer.accept(object);
    }
}
