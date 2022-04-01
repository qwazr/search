/*
 * Copyright 2016-2020 Emmanuel Keller / QWAZR
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

import com.qwazr.server.GenericServer;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.idm.PasswordCredential;
import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class TestIdentityProvider implements GenericServer.IdentityManagerProvider {

    public final static AtomicInteger authSuccessCount = new AtomicInteger();
    public final static AtomicInteger authCount = new AtomicInteger();
    public final static String TEST_USER = "user_" + System.currentTimeMillis();
    public final static String TEST_PASSWORD = "passwd" + System.currentTimeMillis();
    public final static String VALID_ROLE = "authenticated-user";

    @Override
    public IdentityManager getIdentityManager(String realm) {
        return new TestIdentityManager();
    }

    public static class TestIdentityManager implements IdentityManager {

        @Override
        public Account verify(Account account) {
            return account;
        }

        @Override
        public Account verify(String id, Credential credential) {
            authCount.incrementAndGet();
            if (!TEST_USER.equals(id))
                return null;
            final PasswordCredential passwordCredential = (PasswordCredential) credential;
            final String passwd = new String(passwordCredential.getPassword());
            if (!TEST_PASSWORD.equals(passwd))
                return null;
            authSuccessCount.incrementAndGet();
            return new Account() {
                @Override
                public Principal getPrincipal() {
                    return () -> id;
                }

                @Override
                public Set<String> getRoles() {
                    return new HashSet<>(Collections.singleton(VALID_ROLE));
                }
            };
        }

        @Override
        public Account verify(Credential credential) {
            return null;
        }
    }
}
