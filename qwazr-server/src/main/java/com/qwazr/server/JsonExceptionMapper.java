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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.ObjectMappers;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface JsonExceptionMapper<T extends Exception> extends ExceptionMapper<T> {

    Logger getLogger();

    JsonError getJsonError(long time, T exception);

    @Override
    default Response toResponse(final T e) {
        final long errorTime = System.currentTimeMillis();
        getLogger().log(Level.WARNING, e, () -> errorTime + " - " + e.getMessage());
        final JsonError jsonError = getJsonError(errorTime, e);
        final Response.ResponseBuilder builder = Response.status(jsonError.status);
        try {
            builder.type(MediaType.APPLICATION_JSON).entity(ObjectMappers.JSON.writeValueAsString(jsonError));
        } catch (JsonProcessingException ex) {
            // Fallback in text plain if we failed convert it to json
            builder.type(MediaType.TEXT_PLAIN).entity(jsonError.message);
        }
        return builder.build();
    }

    @JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE,
            getterVisibility = JsonAutoDetect.Visibility.NONE,
            isGetterVisibility = JsonAutoDetect.Visibility.NONE,
            creatorVisibility = JsonAutoDetect.Visibility.NONE,
            fieldVisibility = JsonAutoDetect.Visibility.NONE)
    class JsonError {

        private final long time;

        private final int status;

        private final String message;

        @JsonCreator
        JsonError(@JsonProperty("time") final long time, @JsonProperty("status") final int status,
                  @JsonProperty("message") final String message) {
            this.time = time;
            this.status = status;
            this.message = message;
        }

        @JsonProperty
        public long getTime() {
            return time;
        }

        @JsonProperty
        public int getStatus() {
            return status;
        }

        @JsonProperty
        public String getMessage() {
            return message;
        }
    }

    class Generic implements JsonExceptionMapper<Exception>, ExceptionMapper<Exception> {

        private final static Logger LOGGER = LoggerUtils.getLogger(Generic.class);

        @Override
        public Logger getLogger() {
            return LOGGER;
        }

        @Override
        public JsonError getJsonError(final long errorTime, final Exception exception) {
            return new JsonError(errorTime, 500, exception.getMessage());
        }
    }

    class WebApplication
            implements JsonExceptionMapper<WebApplicationException>, ExceptionMapper<WebApplicationException> {

        private final static Logger LOGGER = LoggerUtils.getLogger(WebApplication.class);

        @Override
        public Logger getLogger() {
            return LOGGER;
        }

        @Override
        public JsonError getJsonError(final long errorTime, final WebApplicationException exception) {
            return new JsonError(errorTime, exception.getResponse().getStatus(), exception.getMessage());
        }
    }
}
