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
package com.qwazr.server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.qwazr.utils.ObjectMappers;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Date;

@JsonInclude(Include.NON_EMPTY)
public class JsonExceptionResponse {

	public final Date date;
	@JsonProperty("status_code")
	public final Integer statusCode;
	public final String message;
	public final String exception;
	@JsonProperty("stack_traces")
	public final String[] stackTraces;

	@JsonCreator
	JsonExceptionResponse(@JsonProperty("date") Date date, @JsonProperty("status_code") Integer statusCode,
			@JsonProperty("message") String message, @JsonProperty("exception") String exception,
			@JsonProperty("stack_traces") String[] stackTraces) {
		this.date = date;
		this.statusCode = statusCode;
		this.message = message;
		this.exception = exception;
		this.stackTraces = stackTraces;
	}

	JsonExceptionResponse(Builder builder) {
		this(builder.date, builder.statusCode, builder.message, builder.exception, builder.stackTraces);
	}

	public Response toJson() {
		try {
			final String jsonMessage = ObjectMappers.JSON.writeValueAsString(this);
			return Response.status(statusCode).type(MediaType.APPLICATION_JSON).entity(jsonMessage).build();
		} catch (JsonProcessingException e) {
			return Response.status(statusCode).type(MediaType.TEXT_PLAIN).entity(message).build();
		}
	}

	public static Builder of() {
		return new Builder();
	}

	public static class Builder {

		private final Date date;
		private Integer statusCode;
		private String message;
		private String exception;
		private String[] stackTraces;

		private Builder() {
			this.date = new Date();
		}

		public Builder status(Status status) {
			statusCode = status.getStatusCode();
			message = status.getReasonPhrase();
			return this;
		}

		public Builder status(int statusCode) {
			this.statusCode = statusCode;
			return this;
		}

		public Builder message(String message) {
			this.message = message;
			return this;
		}

		public Builder exception(Throwable throwable, boolean printStackTrace) {
			final Throwable cause = throwable == null ? null : ExceptionUtils.getRootCause(throwable);
			if (cause == null)
				return this;
			this.message = cause.getMessage();
			this.exception = cause.getClass().getName();
			if (printStackTrace)
				this.stackTraces = ExceptionUtils.getStackFrames(cause);
			return this;
		}

		public JsonExceptionResponse build() {
			return new JsonExceptionResponse(this);
		}
	}

}
