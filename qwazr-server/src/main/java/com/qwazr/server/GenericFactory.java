/*
 * Copyright 2017-2020 Emmanuel Keller / QWAZR
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
package com.qwazr.server;

import com.qwazr.utils.reflection.ConstructorParameters;
import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.api.InstanceHandle;
import io.undertow.servlet.util.ImmediateInstanceHandle;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.function.Supplier;

public interface GenericFactory<T> extends InstanceFactory<T> {

    static <T> GenericFactory<T> fromInstance(final T instance) {
        return new FromInstance<>(instance);
    }

    static <T> GenericFactory<T> fromSupplier(final Supplier<T> supplier) {
        return new FromSupplier<>(supplier);
    }

    static <T> GenericFactory<T> fromConstructor(final ConstructorParameters constructorParameters, final Class<T> clazz) {
        return new FromConstructor<>(constructorParameters, clazz);
    }

    final class FromInstance<T> implements GenericFactory<T> {

        protected final T instance;

        private FromInstance(final T instance) {
            this.instance = instance;
        }

        @Override
        public InstanceHandle<T> createInstance() {
            return new ImmediateInstanceHandle<>(instance);
        }
    }

    final class FromSupplier<T> implements GenericFactory<T> {

        private final Supplier<T> supplier;

        private FromSupplier(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public InstanceHandle<T> createInstance() {
            return new ImmediateInstanceHandle<>(supplier.get());
        }
    }

    final class FromConstructor<T> implements GenericFactory<T> {

        private final ConstructorParameters constructorParameters;
        private final Class<T> clazz;

        private FromConstructor(final ConstructorParameters constructorParameters, final Class<T> clazz) {
            this.constructorParameters = constructorParameters;
            this.clazz = clazz;
        }

        @Override
        public ImmediateInstanceHandle<T> createInstance() throws InstantiationException {
            final T instance;
            try {
                final com.qwazr.utils.reflection.InstanceFactory<T> instanceFactory =
                        Objects.requireNonNull(constructorParameters.findBestMatchingConstructor(clazz),
                                () -> "No matching constructor found for class: " + clazz);
                instance = instanceFactory.newInstance();
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw ServerException.of(e);
            }
            return new ImmediateInstanceHandle<>(instance);
        }

    }
}
