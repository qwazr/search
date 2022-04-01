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

package com.qwazr.server;

import com.qwazr.utils.CollectionsUtils;
import com.qwazr.utils.RandomUtils;
import io.undertow.servlet.api.SessionPersistenceManager;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class InFileSessionPersistenceManagerTest {

    private static InFileSessionPersistenceManager sessionManager;

    private final static String DEPLOYMENT_NAME = "test";

    @BeforeClass
    public static void setup() throws IOException {
        final Path sessionDir = Files.createTempDirectory("qwazrserver-infiletest");
        sessionManager = new InFileSessionPersistenceManager(sessionDir);
    }

    @AfterClass
    public static void cleanup() {
        sessionManager.clear(DEPLOYMENT_NAME);
    }

    void checkSession(Map<String, SessionPersistenceManager.PersistentSession> writeSessions,
            Map<String, SessionPersistenceManager.PersistentSession> readSessions) {
        Assert.assertEquals(writeSessions.size(), readSessions.size());
        writeSessions.forEach((sessionId, writeSession) -> {
            final SessionPersistenceManager.PersistentSession readSession = readSessions.get(sessionId);
            Assert.assertNotNull(readSession);
            Assert.assertEquals(writeSession.getExpiration(), readSession.getExpiration());
            Assert.assertTrue(CollectionsUtils.equals(writeSession.getSessionData(), readSession.getSessionData()));
        });
    }

    @Test
    public void sessionTest() {

        // Session holder
        final Map<String, SessionPersistenceManager.PersistentSession> writeSessions = new HashMap<>();

        // Valid session
        final Map<String, Object> validAttributes = new HashMap<>();
        validAttributes.put("user", RandomUtils.alphanumeric(10));
        validAttributes.put("id", RandomUtils.nextInt());
        validAttributes.put("date", RandomUtils.nextPastDate(1, 10));
        validAttributes.put("NonSerializable", new NonSerializable());
        validAttributes.put("SerializableWithBug", new SerializableWithBug());

        final String validSessionId = RandomUtils.alphanumeric(10);
        final Date validExpirationDate = RandomUtils.nextFutureDate(1, 10);
        writeSessions.put(validSessionId,
                new SessionPersistenceManager.PersistentSession(validExpirationDate, validAttributes));

        // Expired session
        final Map<String, Object> expiredAttributes = new HashMap<>();
        expiredAttributes.put("user", RandomUtils.alphanumeric(10));
        final String expiredSessionId = RandomUtils.alphanumeric(12);
        final Date expiredExpirationDate = RandomUtils.nextPastDate(1, 10);
        writeSessions.put(expiredSessionId,
                new SessionPersistenceManager.PersistentSession(expiredExpirationDate, expiredAttributes));

        // Write and read sessions
        sessionManager.persistSessions(DEPLOYMENT_NAME, writeSessions);

        final Map<String, SessionPersistenceManager.PersistentSession> readSessions =
                sessionManager.loadSessionAttributes(DEPLOYMENT_NAME, null);

        // Remove non serializable
        validAttributes.remove("NonSerializable");
        validAttributes.remove("SerializableWithBug");
        // Remove expired
        writeSessions.remove(expiredSessionId);

        checkSession(writeSessions, readSessions);
    }

    public static class NonSerializable {

    }

    public static class SerializableWithBug implements Serializable {

        final NonSerializable object = new NonSerializable();

    }
}
