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

package com.qwazr.server;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.utils.FileUtils;
import com.qwazr.utils.RuntimeUtils;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

@JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class WelcomeStatus {

    public final TitleVendorVersion implementation;
    public final TitleVendorVersion specification;
    public final Set<String> webapp_endpoints;
    public final Set<String> webservice_endpoints;
    public final MemoryStatus memory;
    public final Map<String, DiskStatus> file_stores;
    public final RuntimeStatus runtime;
    public final SortedMap<String, Object> properties;
    public final SortedMap<String, String> env;

    @JsonCreator
    private WelcomeStatus(@JsonProperty("implementation") TitleVendorVersion implementation,
            @JsonProperty("specification") TitleVendorVersion specification,
            @JsonProperty("webapp_endpoints") Set<String> webapp_endpoints,
            @JsonProperty("webservice_endpoints") Set<String> webservice_endpoints,
            @JsonProperty("memory") MemoryStatus memory,
            @JsonProperty("file_stores") Map<String, DiskStatus> file_stores,
            @JsonProperty("runtime") RuntimeStatus runtime,
            @JsonProperty("properties") SortedMap<String, Object> properties,
            @JsonProperty("env") SortedMap<String, String> env) {
        this.implementation = implementation;
        this.specification = specification;
        this.webapp_endpoints = webapp_endpoints;
        this.webservice_endpoints = webservice_endpoints;
        this.memory = memory;
        this.file_stores = file_stores;
        this.runtime = runtime;
        this.properties = properties;
        this.env = env;
    }

    public WelcomeStatus(final GenericServer server, final Boolean showProperties, final Boolean showEnvVars)
            throws IOException {
        this.webapp_endpoints = server == null ? null : server.getWebAppEndPoints();
        this.webservice_endpoints = server == null ? null : server.getWebServiceEndPoints();
        final Package pkg = getClass().getPackage();
        implementation = new TitleVendorVersion(pkg.getImplementationTitle(), pkg.getImplementationVendor(),
                pkg.getImplementationVersion());
        specification = new TitleVendorVersion(pkg.getSpecificationTitle(), pkg.getSpecificationVendor(),
                pkg.getSpecificationVersion());
        memory = new MemoryStatus();
        file_stores = new LinkedHashMap<>();
        for (Path rootDir : FileSystems.getDefault().getRootDirectories()) {
            if (!Files.isReadable(rootDir))
                continue;
            final FileStore fileStore = Files.getFileStore(rootDir);
            if (fileStore.getTotalSpace() > 0)
                file_stores.put(rootDir.toString(), new DiskStatus(fileStore));
        }
        runtime = new RuntimeStatus();
        if (showProperties != null && showProperties) {
            properties = new TreeMap<>();
            System.getProperties().forEach((key, value) -> properties.put(key.toString(), value));
        } else
            properties = null;
        if (showEnvVars != null && showEnvVars)
            env = new TreeMap<>(System.getenv());
        else
            env = null;
    }

    @JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE,
            getterVisibility = JsonAutoDetect.Visibility.NONE,
            isGetterVisibility = JsonAutoDetect.Visibility.NONE,
            creatorVisibility = JsonAutoDetect.Visibility.NONE,
            fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TitleVendorVersion {

        public final String title;
        public final String vendor;
        public final String version;

        @JsonCreator
        TitleVendorVersion(@JsonProperty("title") final String title, @JsonProperty("vendor") final String vendor,
                @JsonProperty("version") final String version) {
            this.title = title;
            this.vendor = vendor;
            this.version = version;
        }
    }

    @JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE,
            getterVisibility = JsonAutoDetect.Visibility.NONE,
            isGetterVisibility = JsonAutoDetect.Visibility.NONE,
            creatorVisibility = JsonAutoDetect.Visibility.NONE,
            fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MemoryStatus {

        public final BytesValues free;
        public final BytesValues total;
        public final BytesValues max;
        public final BytesValues usage;

        @JsonCreator
        MemoryStatus(@JsonProperty("free") BytesValues free, @JsonProperty("total") BytesValues total,
                @JsonProperty("max") BytesValues max, @JsonProperty("usage") BytesValues usage) {
            this.free = free;
            this.total = total;
            this.max = max;
            this.usage = usage;
        }

        public MemoryStatus() {
            this(BytesValues.of(Runtime.getRuntime().freeMemory()), BytesValues.of(Runtime.getRuntime().totalMemory()),
                    BytesValues.of(Runtime.getRuntime().maxMemory()), BytesValues.of(RuntimeUtils.getMemoryUsage()));
        }
    }

    @JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE,
            getterVisibility = JsonAutoDetect.Visibility.NONE,
            isGetterVisibility = JsonAutoDetect.Visibility.NONE,
            creatorVisibility = JsonAutoDetect.Visibility.NONE,
            fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RuntimeStatus {

        public final Integer activeThreads;
        public final Long openFiles;

        @JsonCreator
        RuntimeStatus(@JsonProperty("activeThreads") Integer activeThreads, @JsonProperty("openFiles") Long openFiles) {
            this.activeThreads = activeThreads;
            this.openFiles = openFiles;
        }

        public RuntimeStatus() {
            this(RuntimeUtils.getActiveThreadCount(), RuntimeUtils.getOpenFileCount());
        }
    }

    @JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE,
            getterVisibility = JsonAutoDetect.Visibility.NONE,
            isGetterVisibility = JsonAutoDetect.Visibility.NONE,
            creatorVisibility = JsonAutoDetect.Visibility.NONE,
            fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DiskStatus {

        public final String type;
        public final BytesValues free;
        public final BytesValues total;
        public final BytesValues used;
        public final Float usage;

        @JsonCreator
        private DiskStatus(@JsonProperty("type") String type, @JsonProperty("free") BytesValues free,
                @JsonProperty("total") BytesValues total, @JsonProperty("max") BytesValues used,
                @JsonProperty("usage") Float usage) {
            this.type = type;
            this.free = free;
            this.total = total;
            this.used = used;
            this.usage = usage;
        }

        public DiskStatus(final FileStore fileStore) throws IOException {
            this(fileStore.type(), BytesValues.of(fileStore.getUsableSpace()),
                    BytesValues.of(fileStore.getTotalSpace()),
                    BytesValues.of(fileStore.getTotalSpace() - fileStore.getUnallocatedSpace()),
                    (float) (fileStore.getTotalSpace() - fileStore.getUnallocatedSpace()) / fileStore.getTotalSpace() *
                            100);
        }
    }

    @JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE,
            getterVisibility = JsonAutoDetect.Visibility.NONE,
            isGetterVisibility = JsonAutoDetect.Visibility.NONE,
            creatorVisibility = JsonAutoDetect.Visibility.NONE,
            fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BytesValues {

        public final Long bytes;
        public final String text;

        @JsonCreator
        private BytesValues(@JsonProperty("bytes") Long bytes, @JsonProperty("text") String text) {
            this.bytes = bytes;
            this.text = text;
        }

        public static BytesValues of(Long bytes) {
            return new BytesValues(bytes, FileUtils.byteCountToDisplaySize(bytes));
        }
    }

}
