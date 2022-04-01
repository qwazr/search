/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.qwazr.utils.json.JsonMapper;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ObjectMappers {

    public final static JsonMapper JSON;

    public final static ObjectMapper YAML;

    public final static XmlMapper XML;

    public final static ObjectMapper SMILE;

    static {
        JSON = new JsonMapper();
        YAML = new ObjectMapper(new YAMLFactory());
        XML = new XmlMapper();
        SMILE = new ObjectMapper(new SmileFactory());
    }

    private final Collection<Pair<String, ObjectMapper>> objectMappers;

    public ObjectMappers(final Collection<Pair<String, ObjectMapper>> mappers) {
        this.objectMappers = new ArrayList<>(mappers);
    }

    public <T> T readFileValue(Path directory, String fileBaseName, T defaultValue, Class<? extends T> classValue)
            throws IOException {
        for (final Pair<String, ObjectMapper> pair : objectMappers) {
            final Path file = directory.resolve(fileBaseName + '.' + pair.getKey());
            if (Files.exists(file))
                return pair.getValue().readValue(file.toFile(), classValue);
        }
        return defaultValue;
    }

    public static Builder of() {
        return new Builder();
    }

    public static class Builder {

        private List<Pair<String, ObjectMapper>> mappers;

        public Builder add(String extension, ObjectMapper mapper) {
            if (mappers == null)
                mappers = new ArrayList<>();
            mappers.add(Pair.of(extension, mapper));
            return this;
        }

        public ObjectMappers build() {
            return new ObjectMappers(mappers);
        }

    }
}
