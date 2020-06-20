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
package com.qwazr.search.index;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.server.RemoteService;
import com.qwazr.utils.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(creatorVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
public class RemoteIndex extends RemoteService {

    final public String index;

    @JsonCreator
    private RemoteIndex(@JsonProperty("scheme") String scheme,
                        @JsonProperty("host") String host,
                        @JsonProperty("port") Integer port,
                        @JsonProperty("path") String path,
                        @JsonProperty("timeout") Integer timeout,
                        @JsonProperty("username") String username,
                        @JsonProperty("password") String password,
                        @JsonProperty("index") String index) {
        super(scheme, host, port, path, timeout, username, password);
        this.index = index;
    }

    public RemoteIndex(final RemoteService.Builder builder, final String index) {
        super(builder);
        this.index = index;
    }

    RemoteIndex(String index) {
        this(null, null, null, null, null, null, null, index);
    }

    /**
     * Build a RemoteIndex using the given URL.
     * The form of the URL should be:
     * {protocol}://{username:password@}{host}:{port}/indexes/{schema}/{index}?timeout={timeout}
     *
     * @param remoteIndexPattern the URL of the remote index
     * @return an array of RemoteIndex
     * @throws URISyntaxException if the URI syntax is not correct
     */

    public static RemoteIndex build(final String remoteIndexPattern) throws URISyntaxException {

        if (StringUtils.isEmpty(remoteIndexPattern))
            return null;

        if (remoteIndexPattern.contains("://") || remoteIndexPattern.startsWith("//")) {

            final URI uri = URI.create(remoteIndexPattern);

            final String[] paths = StringUtils.split(uri.getPath(), '/');
            if (paths.length != 2 || !IndexServiceInterface.PATH.equals(paths[0]))
                throw new URISyntaxException(remoteIndexPattern,
                    "The URL form should be: http://hostname/" + IndexServiceInterface.PATH + "/{index}?" +
                        TIMEOUT_PARAMETER + "={timeout}");

            final RemoteService.Builder builder = RemoteService.of(uri);
            builder.setPath(null);
            return new RemoteIndex(builder, paths[1]);
        } else {
            final String[] paths = StringUtils.split(remoteIndexPattern, '/');
            if (paths.length == 1)
                return new RemoteIndex(paths[0]);
        }
        throw new IllegalArgumentException(
            "The index pattern should be in the local form {index}, or using an URL");
    }

}
