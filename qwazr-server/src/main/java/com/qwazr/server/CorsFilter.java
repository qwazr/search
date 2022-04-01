/*
 * Copyright 2015-2021 Emmanuel Keller / QWAZR
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

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;

public class CorsFilter implements ContainerResponseFilter {

    public static String DEFAULT_ORIGIN = "*";
    public static boolean DEFAULT_ALLOW_CREDENTIALS = true;
    public static String DEFAULT_ALLOW_HEADERS = "origin, content-type, accept, authorization";
    public static String DEFAULT_ALLOW_METHODS = "GET, POST, PUT, DELETE, OPTIONS, HEAD";

    private final String origin;
    private final boolean allowCredential;
    private final String allowHeaders;
    private final String allowMethods;

    private CorsFilter(Builder builder) {
        origin = builder.origin;
        allowCredential = builder.allowCredentials;
        allowHeaders = builder.allowHeaders;
        allowMethods = builder.allowMethods;
    }

    public CorsFilter() {
        this(of());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        final MultivaluedMap<String, Object> headers = responseContext.getHeaders();
        headers.add("Access-Control-Allow-Origin", origin);
        headers.add("Access-Control-Allow-Credentials", allowCredential);
        headers.add("Access-Control-Allow-Headers", allowHeaders);
        headers.add("Access-Control-Allow-Methods", allowMethods);
    }

    public static Builder of() {
        return new Builder();
    }

    public static class Builder {

        private String origin;
        private boolean allowCredentials;
        private String allowHeaders;
        private String allowMethods;

        public Builder() {
            withOrigin(DEFAULT_ORIGIN);
            withAllowCredential(DEFAULT_ALLOW_CREDENTIALS);
            withAllowHeaders(DEFAULT_ALLOW_HEADERS);
            withAllowMethods(DEFAULT_ALLOW_METHODS);
        }

        public Builder withOrigin(String origin) {
            this.origin = origin;
            return this;
        }

        public Builder withAllowCredential(boolean allowCredentials) {
            this.allowCredentials = allowCredentials;
            return this;
        }

        public Builder withAllowHeaders(String allowHeaders) {
            this.allowHeaders = allowHeaders;
            return this;
        }

        public Builder withAllowMethods(String allowMethods) {
            this.allowMethods = allowMethods;
            return this;
        }

        public CorsFilter build() {
            return new CorsFilter(this);
        }
    }
}
