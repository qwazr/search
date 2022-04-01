/*
 * Copyright 2016-2017 Emmanuel Keller / QWAZR
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.utils.LinkUtils;
import com.qwazr.utils.StringUtils;
import javax.ws.rs.core.MultivaluedMap;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RemoteService {

    final public static String TIMEOUT_PARAMETER = "timeout";

    /**
     * The protocol. Should be "http" or "https"
     */
    final public String scheme;

    /**
     * The hostname of the remote server. Localhost by default.
     */
    final public String host;

    /**
     * The root path of the service
     */
    final public String path;

    /**
     * the TCP port (9091 by default)
     */
    final public Integer port;

    /**
     * the default timeout (milliseconds)
     */
    final public Integer timeout;

    /**
     * the (optional) login (Basic HTTP authentication)
     */
    final public String username;

    /**
     * the (optional) password (Basic HTTP authentication)
     */
    final public String password;

    @JsonIgnore
    final public String serverAddress; // {scheme}://{host}:{port}

    @JsonIgnore
    final public String serviceAddress; // {scheme}://{host}:{port}:{path}

    @JsonIgnore
    private volatile int hashCode;

    @JsonCreator
    protected RemoteService(@JsonProperty("scheme") final String scheme, @JsonProperty("host") final String host,
                            @JsonProperty("port") Integer port, @JsonProperty("path") String path,
                            @JsonProperty("timeout") Integer timeout, @JsonProperty("username") String username,
                            @JsonProperty("password") String password) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.path = path;
        this.timeout = timeout;
        this.username = username;
        this.password = password;
        this.serverAddress = getServerAddress();
        this.serviceAddress = getServiceAddress();
    }

    private String getServerAddress() {
        return ((scheme == null ? "http" : scheme) + "://" + (host == null ? "localhost" : host) + ':' +
                (port == null || port == -1 ? 9091 : port)).intern();
    }

    private String getServiceAddress() {
        return (serverAddress + '/' + (path == null ? StringUtils.EMPTY : path)).intern();
    }

    protected RemoteService(final Builder builder) {
        this(builder.scheme, builder.host, builder.port, builder.getPathSegment(0), builder.timeout, builder.username,
                builder.password);
    }

    @Override
    public synchronized int hashCode() {
        if (hashCode == 0)
            hashCode = Objects.hash(host, port);
        return hashCode;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof RemoteService))
            return false;
        if (this == o)
            return true;
        final RemoteService rs = (RemoteService) o;
        return Objects.equals(serviceAddress, rs.serviceAddress) && Objects.equals(timeout, rs.timeout) &&
                Objects.equals(username, rs.username) && Objects.equals(password, rs.password);
    }

    @Override
    public String toString() {
        return serviceAddress;
    }

    @JsonIgnore
    public boolean isCredential() {
        return !StringUtils.isBlank(username) || !StringUtils.isBlank(password);
    }

    public static Builder of() {
        return new Builder();
    }

    public static Builder of(final URI uri) {
        return new Builder(uri);
    }

    public static Builder of(final String url) throws URISyntaxException {
        return new Builder(new URI(url));
    }

    public static class Builder {

        private String scheme;
        private String host;
        private String[] pathSegments;
        private Integer port;
        private Integer timeout;
        private String username;
        private String password;
        private MultivaluedMap<String, String> queryParams;

        private Builder() {
            scheme = null;
            host = null;
            pathSegments = null;
            port = null;
            timeout = null;
            username = null;
            password = null;
            queryParams = null;
        }

        private Builder(final URI uri) {
            setScheme(uri.getScheme());
            setHost(uri.getHost());
            setPath(uri.getPath());
            setPort(uri.getPort());
            setUserInfo(uri.getUserInfo());
            setQuery(uri.getQuery());
        }

        private Builder(final String url) throws URISyntaxException {
            this(new URI(url));
        }

        /**
         * @param scheme The protocol. Should be "http" or "https"
         * @return the current builder
         */
        public Builder setScheme(final String scheme) {
            this.scheme = scheme == null ? "http" : scheme;
            return this;
        }

        /**
         * @param host The hostname of the remote server. "localhost" by default.
         * @return the current builder
         */
        public Builder setHost(final String host) {
            this.host = host == null ? "localhost" : host;
            return this;
        }

        /**
         * @param path The root path of the service
         * @return the current builder
         */
        public Builder setPath(final String path) {
            pathSegments = path == null ? null : StringUtils.split(path, '/');
            return this;
        }

        private String getPathSegment(final int pos) {
            if (pathSegments == null)
                return null;
            return pathSegments.length > pos ? pathSegments[pos] : null;
        }

        public final static Integer DEFAULT_PORT = 9091;

        /**
         * @param port The TCP port (9091 by default)
         * @return the current builder
         */
        public Builder setPort(final Integer port) {
            this.port = port == null ? DEFAULT_PORT : port == -1 ? DEFAULT_PORT : port;
            return this;
        }

        /**
         * @param userInfo The username and password in the form {username}:{password}
         * @return the current builder
         */
        public Builder setUserInfo(final String userInfo) {
            final String[] s = userInfo != null ? StringUtils.split(userInfo, ':') : null;
            if (s != null) {
                this.username = s.length > 0 ? s[0] : null;
                this.password = s.length > 1 ? s[1] : null;
            } else {
                this.username = null;
                this.password = null;
            }
            return this;
        }

        /**
         * @param username The (optional) login (Basic HTTP authentication)
         * @return the current builder
         */
        public Builder setUsername(final String username) {
            this.username = username;
            return this;
        }

        /**
         * @param password The (optional) password (Basic HTTP authentication)
         * @return the current builder
         */
        public Builder setPassword(final String password) {
            this.password = password;
            return this;
        }

        /**
         * @param timeout The default timeout (milliseconds)
         * @return the current builder
         */
        public Builder setTimeout(final Integer timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Set the parameters by extracting the query parameters
         *
         * @param query the query string
         * @return a new Builder
         */
        public Builder setQuery(final String query) {
            try {
                queryParams = LinkUtils.getQueryParameters(query);
                if (queryParams == null)
                    return this;
                final String s = queryParams.getFirst(TIMEOUT_PARAMETER);
                if (s != null)
                    setTimeout(Integer.parseInt(s));
                return this;
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * @return a new RemoteService
         */
        public RemoteService build() {
            return new RemoteService(this);
        }

    }

    /**
     * Build a list of Builder filled with an array of URL.
     * The form of the URL should be:
     * {protocol}://{username:password@}{host}:{port}/{service_path}?timeout={timeout}
     *
     * @param remoteServiceURLs an array of URL
     * @return a list of RemoteService
     * @throws URISyntaxException if the URI is malformatted
     */
    public static List<Builder> builders(final String... remoteServiceURLs) throws URISyntaxException {

        if (remoteServiceURLs == null || remoteServiceURLs.length == 0)
            return null;

        final List<Builder> builderList = new ArrayList<>();
        for (String url : remoteServiceURLs)
            if (url != null && !url.isEmpty())
                builderList.add(new Builder(url));

        return builderList.isEmpty() ? null : builderList;
    }

    public static List<Builder> builders(final Collection<String> remoteServiceURLs) throws URISyntaxException {

        if (remoteServiceURLs == null || remoteServiceURLs.isEmpty())
            return null;

        final List<Builder> builderList = new ArrayList<>();
        for (String url : remoteServiceURLs)
            if (url != null && !url.isEmpty())
                builderList.add(new Builder(url));

        return builderList.isEmpty() ? null : builderList;
    }

    private static RemoteService[] fromBuilders(final Collection<Builder> builders) {
        if (builders == null || builders.isEmpty())
            return null;
        // Build the array
        int i = 0;
        RemoteService[] remotes = new RemoteService[builders.size()];
        for (Builder builder : builders)
            remotes[i++] = new RemoteService(builder);
        return remotes;
    }

    /**
     * Build an array of RemoteService filled with an array of URL.
     * The form of the URL should be:
     * {protocol}://{username:password@}{host}:{port}/{service_path}?timeout={timeout}
     *
     * @param remoteServiceURLs an array of URL
     * @return an array of RemoteService
     * @throws URISyntaxException if the URI is malformatted
     */
    public static RemoteService[] build(final String... remoteServiceURLs) throws URISyntaxException {
        return fromBuilders(builders(remoteServiceURLs));
    }

    /**
     * Build an array of RemoteService filled with an array of URL.
     * The form of the URL should be:
     * {protocol}://{username:password@}{host}:{port}/{service_path}?timeout={timeout}
     *
     * @param remoteServiceURLs an collection of URL
     * @return an array of RemoteService
     * @throws URISyntaxException if the URI is malformatted
     */
    public static RemoteService[] build(final Collection<String> remoteServiceURLs) throws URISyntaxException {
        return fromBuilders(builders(remoteServiceURLs));
    }

}
