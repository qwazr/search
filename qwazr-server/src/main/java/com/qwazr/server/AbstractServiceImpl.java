/*
 * Copyright 2014-2020 Emmanuel Keller / QWAZR
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

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

public abstract class AbstractServiceImpl implements ServiceInterface {

    @Context
    protected ServletContext context;

    protected <T> T getContextAttribute(final Class<T> clazz) {
        return GenericServer.getContextAttribute(context, clazz);
    }

}
