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

import com.qwazr.utils.LoggerUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;

@RolesAllowed("welcome")
@Path("/")
public class WelcomeShutdownService extends WelcomeService {

    static final private Logger LOGGER = LoggerUtils.getLogger(WelcomeShutdownService.class);

    @DELETE
    @Path("/shutdown")
    public void shutdown() {
        new ShutdownThread(getContextAttribute(GenericServer.class));
    }

    private static class ShutdownThread implements Runnable {

        private final GenericServer server;

        private ShutdownThread(GenericServer server) {
            this.server = server;
            new Thread(this).start();
        }

        @Override
        public void run() {
            try {
                Thread.sleep(5000);
                server.close();
            } catch (InterruptedException e) {
                LOGGER.log(Level.WARNING, e, e::getMessage);
            }
        }
    }
}
