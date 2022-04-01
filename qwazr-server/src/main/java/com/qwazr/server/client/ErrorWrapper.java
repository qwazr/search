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
package com.qwazr.server.client;

import com.qwazr.server.ServerException;
import com.qwazr.utils.concurrent.CallableEx;

import javax.ws.rs.WebApplicationException;

public class ErrorWrapper {

    public static int noError(WebApplicationException e, int... statusCodes) {
        final int sc = e.getResponse().getStatus();
        for (int statusCode : statusCodes)
            if (sc == statusCode)
                return sc;
        throw e;
    }

    public static int noError(ServerException e, int... statusCodes) {
        final int sc = e.getStatusCode();
        for (int statusCode : statusCodes)
            if (sc == statusCode)
                return sc;
        throw e;
    }

    public static boolean noError(Runnable runnable, int... statusCode) {
        try {
            runnable.run();
            return true;
        } catch (WebApplicationException e) {
            noError(e, statusCode);
            return false;
        } catch (ServerException e) {
            noError(e, statusCode);
            return false;
        }
    }

    public static <T, E extends Exception> T bypass(CallableEx<T, E> callable, int... statusCode) throws E {
        try {
            return callable.call();
        } catch (WebApplicationException e) {
            noError(e, statusCode);
            return null;
        } catch (ServerException e) {
            noError(e, statusCode);
            return null;
        }
    }
}
