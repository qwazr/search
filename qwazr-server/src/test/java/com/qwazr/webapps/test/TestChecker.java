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
package com.qwazr.webapps.test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.Assert;

public interface TestChecker {

    MediaType MIME_TEXT_CSS = MediaType.valueOf("text/css");
    MediaType MIME_IMAGE_X_PNG = MediaType.valueOf("image/x-png");
    MediaType MIME_FAVICON = MediaType.valueOf("image/vnd.microsoft.icon");

    default Response checkResponse(Response response, int expectedStatusCode) {
        Assert.assertNotNull(response);
        Assert.assertEquals(expectedStatusCode, response.getStatus());
        return response;
    }

    default Response checkContentType(Response response, MediaType contentType) {
        Assert.assertTrue(contentType.isCompatible(response.getMediaType()));
        return response;
    }

    default String checkEntity(Response response, MediaType contentType) {
        return checkContentType(response, contentType).readEntity(String.class);
    }

    default void checkContains(String content, String... patterns) {
        for (String pattern : patterns)
            Assert.assertTrue(content.contains(pattern));
    }

}
