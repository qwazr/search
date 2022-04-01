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
package com.qwazr.server.client;

import com.qwazr.utils.RandomArrayIterator;
import com.qwazr.utils.concurrent.FunctionEx;

import javax.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * This class represents a connection to a set of servers
 *
 * @param <T> The type of the class which handle the connection to one server
 */
public class MultiClient<T> implements Iterable<T> {

    private final ExecutorService executorService;
    private final T[] clients;

    /**
     * Create a new multi client with given clients
     *
     * @param clients         an array of client
     * @param executorService an externally maintained executor service
     */
    protected MultiClient(final T[] clients, final ExecutorService executorService) {
        this.clients = clients;
        this.executorService = executorService;
    }

    @Override
    final public Iterator<T> iterator() {
        return new RandomArrayIterator<>(clients);
    }

    private WebApplicationException ensureWebApplicationException(final Throwable t) {
        return t instanceof WebApplicationException ? (WebApplicationException) t : new WebApplicationException(t);
    }

    protected <R> R firstRandomSuccess(final FunctionEx<T, R, Exception> action,
                                       final Consumer<WebApplicationException> exceptions) {
        if (clients == null || clients.length == 0)
            return null;
        for (final T client : this) {
            try {
                final R result = action.apply(client);
                if (result != null)
                    return result;
            } catch (WebApplicationException e) {
                exceptions.accept(e);
            } catch (Exception e) {
                exceptions.accept(ensureWebApplicationException(e));
            }
        }
        return null;
    }

    protected <R> R firstRandomSuccess(final FunctionEx<T, R, Exception> action, final Logger logger) {
        final MultiWebApplicationException.Builder errors = MultiWebApplicationException.of(logger);
        final R result = firstRandomSuccess(action, errors::add);
        if (errors.isEmpty())
            return result;
        throw errors.build();
    }

    protected <R> List<R> forEachParallel(final FunctionEx<T, R, Exception> action,
                                          final Consumer<WebApplicationException> exceptions) {

        if (clients == null || clients.length == 0)
            return Collections.emptyList();

        // Start the parallel threads
        final List<Future<R>> futures = new ArrayList<>(clients.length);
        for (final T client : this)
            futures.add(executorService.submit(() -> action.apply(client)));

        // Get the results
        final List<R> results = new ArrayList<>(clients.length);
        for (Future<R> future : futures) {
            if (future == null)
                continue;
            try {
                results.add(future.get());
            } catch (ExecutionException e) {
                exceptions.accept(ensureWebApplicationException(e.getCause()));
            } catch (InterruptedException e) {
                exceptions.accept(new WebApplicationException(e));
            }
        }
        return results;
    }

    protected <R> List<R> forEachParallel(final FunctionEx<T, R, Exception> action, final Logger logger) {
        final MultiWebApplicationException.Builder errors = MultiWebApplicationException.of(logger);
        final List<R> results = forEachParallel(action, errors::add);
        if (errors.isEmpty())
            return results;
        throw errors.build();
    }
}
