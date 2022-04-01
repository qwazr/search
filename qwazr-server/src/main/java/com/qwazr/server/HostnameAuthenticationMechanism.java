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
 **/
package com.qwazr.server;

import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.AuthenticationMechanismFactory;
import io.undertow.security.api.SecurityContext;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.ExternalCredential;
import io.undertow.security.idm.IdentityManager;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.servlet.api.DeploymentInfo;

import javax.ws.rs.NotSupportedException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class HostnameAuthenticationMechanism implements AuthenticationMechanism {

    @FunctionalInterface
    public interface PrincipalResolver {
        String get(InetAddress inetAddress);
    }

    static final String NAME = "HOSTNAME";

    private final IdentityManager identityManager;

    private final PrincipalResolver principalResolver;

    HostnameAuthenticationMechanism(IdentityManager identityManager, PrincipalResolver principalResolver) {
        this.identityManager = identityManager;
        this.principalResolver = principalResolver;
    }

    @Override
    public AuthenticationMechanismOutcome authenticate(HttpServerExchange exchange, SecurityContext securityContext) {
        final String principal = principalResolver.get(exchange.getSourceAddress().getAddress());
        if (principal == null)
            return AuthenticationMechanismOutcome.NOT_ATTEMPTED;
        final Account account = identityManager.verify(principal, ExternalCredential.INSTANCE);
        if (account == null)
            return AuthenticationMechanismOutcome.NOT_AUTHENTICATED;
        securityContext.authenticationComplete(account, NAME, false);
        return AuthenticationMechanismOutcome.AUTHENTICATED;
    }

    @Override
    public ChallengeResult sendChallenge(HttpServerExchange exchange, SecurityContext securityContext) {
        return ChallengeResult.NOT_SENT;
    }

    static void register(DeploymentInfo deploymentInfo, PrincipalResolver principalResolver) {
        deploymentInfo.addAuthenticationMechanism(NAME, new Factory(principalResolver));
    }

    public static final class Factory implements AuthenticationMechanismFactory {

        private final PrincipalResolver principalResolver;

        public Factory(final PrincipalResolver principalResolver) {
            this.principalResolver = principalResolver;
        }

        @Override
        @Deprecated
        public AuthenticationMechanism create(String mechanismName, FormParserFactory formParserFactory,
                                              Map<String, String> properties) {
            throw new NotSupportedException();
        }

        @Override
        public AuthenticationMechanism create(String mechanismName, IdentityManager identityManager,
                                              FormParserFactory formParserFactory, Map<String, String> properties) {
            return new HostnameAuthenticationMechanism(identityManager, principalResolver);
        }
    }

    public static class MapPrincipalResolver implements PrincipalResolver {

        private final Map<InetAddress, String> addressMap;

        protected MapPrincipalResolver(final Map<InetAddress, String> addressMap) {
            this.addressMap = addressMap;
        }

        public MapPrincipalResolver() {
            this(new ConcurrentHashMap<>());
        }

        public void put(final String host, final String principal) throws UnknownHostException {
            Objects.requireNonNull(host, "The host parameter is missing");
            Objects.requireNonNull(principal, "The principal parameter is missing");
            for (InetAddress inetAddress : InetAddress.getAllByName(host))
                addressMap.put(inetAddress, principal);
        }

        public void remove(final String host) throws UnknownHostException {
            Objects.requireNonNull(host, "The host parameter is missing");
            for (InetAddress inetAddress : InetAddress.getAllByName(host))
                addressMap.remove(inetAddress);
        }

        @Override
        public String get(final InetAddress inetAddress) {
            return addressMap.get(inetAddress);
        }
    }
}
