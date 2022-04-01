/**
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

import com.qwazr.server.client.ErrorWrapper;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class ErrorWrapperTest {

	@Test
	public void checkWebApplicationExceptionRunnable() {
		Assert.assertFalse(ErrorWrapper.noError(() -> {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}, 404));
	}

	@Test
	public void checkServerExceptionRunnable() {
		Assert.assertFalse(ErrorWrapper.noError(() -> {
			throw new ServerException(Response.Status.NOT_FOUND);
		}, 404));
	}

	@Test
	public void checkNoExceptionRunnable() {
		Assert.assertTrue(ErrorWrapper.noError(() -> {
		}));
	}

	@Test
	public void checkWebApplicationExceptionCallable() {
		Assert.assertNull(ErrorWrapper.bypass(() -> {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}, 404));
	}

	@Test
	public void checkServerExceptionCallable() {
		Assert.assertNull(ErrorWrapper.bypass(() -> {
			throw new ServerException(Response.Status.NOT_FOUND);
		}, 404));
	}

	@Test
	public void checkNoExceptionCallable() {
		final Object obj = new Object();
		Assert.assertEquals(obj, ErrorWrapper.bypass(() -> obj));
	}
}
