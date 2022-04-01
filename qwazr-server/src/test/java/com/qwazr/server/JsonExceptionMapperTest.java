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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.qwazr.utils.ObjectMappers;
import com.qwazr.utils.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

public class JsonExceptionMapperTest {

    @Test
    public void testJsonErrorWithException() {
        final JsonExceptionMapper.Generic genericMapper = new JsonExceptionMapper.Generic();
        final long time = System.currentTimeMillis();
        final String message = RandomUtils.alphanumeric(8);
        final Exception exception = new Exception(message);
        final JsonExceptionMapper.JsonError jsonError = genericMapper.getJsonError(time, exception);
        Assert.assertEquals(jsonError.getMessage(), message);
        Assert.assertEquals(jsonError.getStatus(), 500);
        assertThat(jsonError.getTime(), greaterThanOrEqualTo(time));
    }

    @Test
    public void testJsonErrorWithWebApplicationException() {
        final JsonExceptionMapper.WebApplication webApplicationMapper = new JsonExceptionMapper.WebApplication();
        final long time = System.currentTimeMillis();
        final String message = RandomUtils.alphanumeric(8);
        final NotFoundException exception = new NotFoundException(message);
        final JsonExceptionMapper.JsonError jsonError = webApplicationMapper.getJsonError(time, exception);
        Assert.assertEquals(jsonError.getMessage(), message);
        Assert.assertEquals(jsonError.getStatus(), 404);
        assertThat(jsonError.getTime(), greaterThanOrEqualTo(time));
    }

    @Test
    public void testResponseWithException() throws JsonProcessingException {
        final JsonExceptionMapper.Generic genericMapper = new JsonExceptionMapper.Generic();
        final long time = System.currentTimeMillis();
        final String message = RandomUtils.alphanumeric(8);
        final Exception exception = new Exception(message);
        try (final Response response = genericMapper.toResponse(exception)) {
            assertThat(response.getStatus(), equalTo(500));
            assertThat(response.getMediaType(), equalTo(MediaType.APPLICATION_JSON_TYPE));
            final JsonExceptionMapper.JsonError jsonError =
                    ObjectMappers.JSON.readValue(response.getEntity().toString(), JsonExceptionMapper.JsonError.class);
            Assert.assertEquals(jsonError.getMessage(), message);
            Assert.assertEquals(jsonError.getStatus(), 500);
            assertThat(jsonError.getTime(), greaterThanOrEqualTo(time));
        }
    }

    @Test
    public void testResponseWithWebApplicationException() throws JsonProcessingException {
        final JsonExceptionMapper.WebApplication webApplicationMapper = new JsonExceptionMapper.WebApplication();
        final long time = System.currentTimeMillis();
        final String message = RandomUtils.alphanumeric(8);
        final NotAcceptableException exception = new NotAcceptableException(message);
        try (final Response response = webApplicationMapper.toResponse(exception)) {
            assertThat(response.getStatus(), equalTo(406));
            assertThat(response.getMediaType(), equalTo(MediaType.APPLICATION_JSON_TYPE));
            final JsonExceptionMapper.JsonError jsonError =
                    ObjectMappers.JSON.readValue(response.getEntity().toString(), JsonExceptionMapper.JsonError.class);
            Assert.assertEquals(jsonError.getMessage(), message);
            Assert.assertEquals(jsonError.getStatus(), 406);
            assertThat(jsonError.getTime(), greaterThanOrEqualTo(time));
        }
    }
}
