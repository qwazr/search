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

import com.qwazr.utils.StringUtils;

import javax.ws.rs.WebApplicationException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MultiWebApplicationException extends WebApplicationException {

    private final Collection<WebApplicationException> causes;

    MultiWebApplicationException(final String message, final int status,
                                 final Collection<WebApplicationException> exceptions) {
        super(message, status);
        this.causes = exceptions == null ? null : Collections.unmodifiableCollection(exceptions);
    }

    public Collection<WebApplicationException> getCauses() {
        return causes;
    }

    public static Builder of(Logger logger) {
        return new Builder(logger);
    }

    public static class Builder {

        private final Logger logger;
        private Set<WebApplicationException> exceptions;
        private Set<String> messages;

        Builder(Logger logger) {
            this.logger = logger;
        }

        public Builder add(WebApplicationException exception) {
            if (exception == null)
                return this;
            if (logger != null)
                logger.log(Level.WARNING, exception, exception::getMessage);
            if (exceptions == null)
                exceptions = new HashSet<>();
            exceptions.add(exception);
            message(exception.getMessage());
            return this;
        }

        public Builder message(String message) {
            if (message == null || StringUtils.isBlank(message))
                return this;
            if (messages == null)
                messages = new HashSet<>();
            messages.add(message);
            return this;
        }

        public boolean isEmpty() {
            return exceptions == null || exceptions.isEmpty();
        }

        public MultiWebApplicationException build() {
            final String message = messages == null ? StringUtils.EMPTY : StringUtils.joinWith(" - ", messages);
            final int status;
            if (exceptions == null)
                status = 500;
            else {
                final Set<Integer> statusSet = new HashSet<>();
                int st = 500;
                for (WebApplicationException e : exceptions) {
                    st = e.getResponse().getStatus();
                    statusSet.add(st);
                }
                status = statusSet.size() == 1 ? st : 500;
            }
            return new MultiWebApplicationException(message, status, exceptions);
        }
    }
}
