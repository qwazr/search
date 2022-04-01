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
package com.qwazr.utils;

import com.qwazr.utils.concurrent.ConsumerEx;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AutoCloseWrapper<T> implements AutoCloseable {

    private final Logger logger;
    private final T instance;
    private final ConsumerEx<T, Exception> closeAction;

    public static <T> AutoCloseWrapper<T> of(final T instance, final Logger logger, final ConsumerEx<T, Exception> closeAction) {
        return new AutoCloseWrapper<>(instance, logger, closeAction);
    }

    public AutoCloseWrapper(final T instance, final Logger logger, final ConsumerEx<T, Exception> closeAction) {
        this.instance = instance;
        this.logger = logger;
        this.closeAction = closeAction;
    }

    public T get() {
        return instance;
    }

    @Override
    public void close() {
        try {
            closeAction.accept(instance);
        }
        catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }
}
