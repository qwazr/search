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
package com.qwazr.server;

import com.qwazr.utils.StringUtils;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class ServerException extends RuntimeException {

    private static final long serialVersionUID = -6102827990391082335L;

    private final int statusCode;

    ServerException(final int statusCode, String message, final Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public ServerException(final Response.Status status, final String message, final Throwable cause) {
        super(message == null ? status.getReasonPhrase() : message, cause);
        this.statusCode = status.getStatusCode();
    }

    public ServerException(final Response.Status status, final String message) {
        this(status, message, null);
    }

    public ServerException(final String message) {
        super(message);
        this.statusCode = 500;
    }

    public ServerException(final Response.Status status) {
        super(status.getReasonPhrase());
        this.statusCode = status.getStatusCode();
    }

    public int getStatusCode() {
        return statusCode;
    }

    final public ServerException warnIfCause(final Logger logger) {
        final Throwable cause = getCause();
        if (cause == null)
            return this;
        if (logger != null)
            logger.log(Level.WARNING, cause, this::getMessage);
        return this;
    }

    public WebApplicationException getTextException(boolean withStackTrace) {
        final String message = getMessage();
        final StringBuilder sb = new StringBuilder(message);
        if (withStackTrace) {
            sb.append("\n");
            sb.append(ExceptionUtils.getStackTrace(this));
        }
        final Response response = Response.status(statusCode).type(MediaType.TEXT_PLAIN).entity(sb.toString()).build();
        return new WebApplicationException(message, this, response);
    }

    public WebApplicationException getHtmlException(boolean withStackTrace) {
        final String message = getMessage();
        final StringBuilder sb = new StringBuilder();
        sb.append("<html><body><h2>Error ");
        sb.append(statusCode);
        sb.append("</h2>\n<p><pre><code>\n");
        sb.append(message);
        sb.append("\n</code></pre></p>");
        if (withStackTrace) {
            sb.append("<p><pre><code>\n");
            sb.append(ExceptionUtils.getStackTrace(this));
            sb.append("\n</code></pre></p>\n</body></html>");
        }
        final Response response = Response.status(statusCode).type(MediaType.TEXT_HTML).entity(sb.toString()).build();
        return new WebApplicationException(message, this, response);
    }

    public WebApplicationException getJsonException(boolean withStackTrace) {
        final String message = getMessage();
        final Response response = JsonExceptionResponse.of()
                .status(statusCode)
                .exception(this, withStackTrace)
                .message(message)
                .build()
                .toJson();
        return new WebApplicationException(message, this, response);
    }

    public static Response toResponse(final HttpHeaders headers, final Exception exception) {
        if (headers != null) {
            final List<MediaType> mediaTypes = headers.getAcceptableMediaTypes();
            if (mediaTypes != null) {
                for (MediaType mediaType : mediaTypes) {
                    if (mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE))
                        return ServerException.getJsonException(null, exception).getResponse();
                    if (mediaType.isCompatible(MediaType.TEXT_HTML_TYPE))
                        return ServerException.getHtmlException(null, exception).getResponse();
                }
            }
        }
        return ServerException.getTextException(null, exception).getResponse();
    }

    public static ServerException of(final Throwable throwable) {
        return of(Objects.requireNonNull(throwable, "Throwable cannot be null").getMessage(), throwable);
    }

    public static ServerException of(String message, final Throwable throwable) {
        if (throwable instanceof ServerException)
            return (ServerException) throwable;

        int status = 500;

        final int serverExceptionPos = ExceptionUtils.indexOfType(throwable, ServerException.class);
        if (serverExceptionPos != -1)
            return (ServerException) ExceptionUtils.getThrowableList(throwable).get(serverExceptionPos);

        final int webApplicationExceptionPos = ExceptionUtils.indexOfType(throwable, WebApplicationException.class);
        if (webApplicationExceptionPos != -1)
            status = ((WebApplicationException) ExceptionUtils.getThrowableList(throwable)
                    .get(webApplicationExceptionPos)).getResponse().getStatus();

        if (StringUtils.isBlank(message)) {
            message = throwable.getMessage();
            if (StringUtils.isBlank(message))
                message = ExceptionUtils.getRootCauseMessage(throwable);
            if (StringUtils.isBlank(message))
                message = "Internal server error";
        }

        return new ServerException(status, message, throwable);
    }

    private static WebApplicationException checkCompatible(final Exception e, final MediaType expectedType) {
        if (!(e instanceof WebApplicationException))
            return null;
        final WebApplicationException wae = (WebApplicationException) e;
        final Response response = wae.getResponse();
        if (response == null)
            return null;
        if (!response.hasEntity())
            return null;
        final MediaType mediaType = response.getMediaType();
        if (mediaType == null)
            return null;
        if (!expectedType.isCompatible(mediaType))
            return null;
        return wae;
    }

    public static WebApplicationException getTextException(final Logger logger, final Exception e) {
        final WebApplicationException wae = checkCompatible(e, MediaType.TEXT_PLAIN_TYPE);
        if (wae != null)
            return wae;
        return of(e).warnIfCause(logger).getTextException(logger == null);
    }

    public static WebApplicationException getJsonException(final Logger logger, final Exception e) {
        final WebApplicationException wae = checkCompatible(e, MediaType.APPLICATION_JSON_TYPE);
        if (wae != null)
            return wae;
        return of(e).warnIfCause(logger).getJsonException(logger == null);
    }

    public static WebApplicationException getHtmlException(final Logger logger, final Exception e) {
        final WebApplicationException wae = checkCompatible(e, MediaType.TEXT_HTML_TYPE);
        if (wae != null)
            return wae;
        return of(e).warnIfCause(logger).getHtmlException(logger == null);
    }

    public static final String APPLICATION_JACKSON_SMILE = "application/x-jackson-smile";
    public static final MediaType APPLICATION_JACKSON_SMILE_TYPE = MediaType.valueOf(APPLICATION_JACKSON_SMILE);

    public static WebApplicationException from(final WebApplicationException webAppException) {
        final Response response = webAppException.getResponse();
        if (response == null)
            return webAppException;
        final MediaType type = response.getMediaType();
        if (type == null || !response.hasEntity())
            return webAppException;
        final String message;
        if (type.isCompatible(MediaType.TEXT_PLAIN_TYPE) || type.isCompatible(MediaType.TEXT_HTML_TYPE)) {
            message = response.readEntity(String.class);
        } else if (type.isCompatible(MediaType.APPLICATION_JSON_TYPE) ||
                type.isCompatible(APPLICATION_JACKSON_SMILE_TYPE)) {
            try {
                message = response.readEntity(JsonExceptionResponse.class).message;
            } catch (ProcessingException e) {
                return webAppException;
            }
        } else
            return webAppException;
        return StringUtils.isBlank(message) ?
                webAppException :
                new WebApplicationException(message, response.getStatus());
    }

}
