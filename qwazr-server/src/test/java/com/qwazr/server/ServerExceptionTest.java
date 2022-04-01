/*
 * Copyright 2017 Emmanuel Keller / QWAZR
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

import com.qwazr.utils.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ServerExceptionTest {

	private void check(ServerException exception, Response.Status status, String message, Throwable cause) {
		Assert.assertNotNull(exception);
		Assert.assertEquals(status.getStatusCode(), exception.getStatusCode());
		Assert.assertEquals(message, exception.getMessage());
		Assert.assertEquals(cause, exception.getCause());

		final WebApplicationException jsonException = ServerException.getJsonException(null, exception);
		Assert.assertNotNull(jsonException);
		Assert.assertEquals(message, jsonException.getMessage());
		Assert.assertEquals(status.getStatusCode(), jsonException.getResponse().getStatus());
		Assert.assertEquals(exception, jsonException.getCause());
		Assert.assertTrue(MediaType.APPLICATION_JSON_TYPE.isCompatible(jsonException.getResponse().getMediaType()));

		final WebApplicationException textException = ServerException.getTextException(null, exception);
		Assert.assertNotNull(textException);
		Assert.assertEquals(message, textException.getMessage());
		Assert.assertEquals(status.getStatusCode(), textException.getResponse().getStatus());
		Assert.assertEquals(exception, textException.getCause());
		Assert.assertTrue(MediaType.TEXT_PLAIN_TYPE.isCompatible(textException.getResponse().getMediaType()));

	}

	private String random() {
		return RandomUtils.alphanumeric(RandomUtils.nextInt(5, 15));
	}

	private Throwable cause() {
		return new Exception(random());
	}

	@Test
	public void withStatusWithMessage() {
		final String msg = random();
		check(new ServerException(Response.Status.NOT_FOUND, msg), Response.Status.NOT_FOUND, msg, null);
		check(ServerException.of(new ServerException(Response.Status.NOT_FOUND, msg)), Response.Status.NOT_FOUND, msg,
				null);
	}

	@Test
	public void withStatusWithMessageWithCause() {
		final String msg = random();
		final Throwable cause = cause();
		check(ServerException.of(msg, cause), Response.Status.INTERNAL_SERVER_ERROR, msg, cause);
	}

	@Test
	public void withStatusWithWebApplicationException() {
		final NotFoundException notFoundException = new NotFoundException(random());
		check(ServerException.of(notFoundException), Response.Status.NOT_FOUND, notFoundException.getMessage(),
				notFoundException);
	}

	@Test
	public void withMessage() {
		final String msg = random();
		check(new ServerException(msg), Response.Status.INTERNAL_SERVER_ERROR, msg, null);
	}

	@Test
	public void withStatus() {
		check(new ServerException(Response.Status.CONFLICT), Response.Status.CONFLICT,
				Response.Status.CONFLICT.getReasonPhrase(), null);
	}

	@Test
	public void withCause() {
		final Throwable cause = cause();
		check(ServerException.of(cause), Response.Status.INTERNAL_SERVER_ERROR, cause.getMessage(), cause);
	}

}
