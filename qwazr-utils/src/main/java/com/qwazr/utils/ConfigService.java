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
package com.qwazr.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ConfigService<Config> {

    /**
     * @return The current loaded configuration
     */
    Config getCurrent();

    /**
     * Reload the configuration.
     *
     * @throws IOException if any I/O error occurred
     */
    Config reload() throws IOException;

    class FileConfigService<FileConfig extends PropertiesConfig> implements ConfigService<FileConfig> {

        private final BiFunction<Properties, Instant, FileConfig> configProvider;
        private final Path configPath;
        private volatile FileConfig current;

        protected FileConfigService(final Path configPath,
                                    final BiFunction<Properties, Instant, FileConfig> configProvider) throws IOException {
            this.configPath = configPath;
            this.configProvider = configProvider;
            reload();
        }

        @Override
        public FileConfig getCurrent() {
            return current;
        }

        @Override
        public synchronized FileConfig reload() throws IOException {
            if (Files.exists(configPath)) {
                final Instant lastModified = Files.getLastModifiedTime(configPath).toInstant();
                if (current == null || !lastModified.equals(current.getCreationTime())) {
                    final Properties properties = new Properties();
                    try (final BufferedReader reader = java.nio.file.Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
                        properties.load(reader);
                    }
                    current = configProvider.apply(properties, lastModified);
                }
            } else {
                if (current == null)
                    current = configProvider.apply(new Properties(), Instant.now());
            }
            return current;
        }

    }

    /**
     * Read the configuration value from properties
     */
    class PropertiesConfig {

        private final Properties properties;
        private final Instant creationTime;

        protected PropertiesConfig(final Properties properties, final Instant creationTime) {
            this.properties = properties;
            this.creationTime = creationTime;
        }

        protected Instant getCreationTime() {
            return creationTime;
        }

        protected <T> T getProperty(final String key, final Function<String, T> converter, final Supplier<T> defaultSupplier) {
            final String value = properties.getProperty(key);
            return value == null ? defaultSupplier.get() : converter.apply(value);
        }

        protected String getStringProperty(final String key, final Supplier<String> defaultValue) {
            return getProperty(key, v -> v, defaultValue);
        }

        protected URI getUriProperty(final String key, final Supplier<URI> defaultValue) {
            return getProperty(key, URI::create, defaultValue);
        }

        protected Integer getIntegerProperty(final String key, final Supplier<Integer> defaultValue) {
            return getProperty(key, Integer::parseInt, defaultValue);
        }

        protected Long getLongProperty(final String key, final Supplier<Long> defaultValue) {
            return getProperty(key, Long::parseLong, defaultValue);
        }

        protected Boolean getBooleanProperty(final String key, final Supplier<Boolean> defaultValue) {
            return getProperty(key, Boolean::parseBoolean, defaultValue);
        }

        protected Path getPathProperty(final String key, final Supplier<Path> defaultValue) {
            return getProperty(key, Path::of, defaultValue);
        }

    }


}
