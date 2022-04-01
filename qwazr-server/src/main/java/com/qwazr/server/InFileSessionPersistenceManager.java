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

import com.qwazr.utils.FileUtils;
import com.qwazr.utils.LoggerUtils;
import io.undertow.servlet.api.SessionPersistenceManager;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.apache.commons.io.output.NullOutputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class InFileSessionPersistenceManager implements SessionPersistenceManager {

    private static final Logger LOGGER = LoggerUtils.getLogger(InFileSessionPersistenceManager.class);

    private final Path sessionDir;

    public InFileSessionPersistenceManager(Path sessionDir) {
        this.sessionDir = sessionDir;
    }

    @Override
    public void persistSessions(final String deploymentName, final Map<String, PersistentSession> sessionData) {
        if (sessionData == null)
            return;
        final Path deploymentDir = sessionDir.resolve(deploymentName);
        try {
            if (!Files.exists(deploymentDir))
                Files.createDirectory(deploymentDir);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e,
                    () -> "Cannot create the session directory " + deploymentDir + ": persistence aborted.");
            return;
        }
        sessionData.forEach(
                (sessionId, persistentSession) -> writeSession(deploymentDir, sessionId, persistentSession));
    }

    private void writeSession(final Path deploymentDir, final String sessionId,
                              final PersistentSession persistentSession) {
        final Date expDate = persistentSession.getExpiration();
        if (expDate == null)
            return; // No expiry date? no serialization
        final Map<String, Object> sessionData = persistentSession.getSessionData();
        if (sessionData == null)
            return; // No sessionData? no serialization
        final File sessionFile = deploymentDir.resolve(sessionId).toFile();
        try (final ObjectOutputStream draftOutputStream = new ObjectOutputStream(new NullOutputStream())) {
            try (final ObjectOutputStream sessionOutputStream = new ObjectOutputStream(
                    new FileOutputStream(sessionFile))) {
                sessionOutputStream.writeLong(expDate.getTime()); // The date is stored as long
                sessionData.forEach(
                        (attribute, object) -> writeSessionAttribute(draftOutputStream, sessionOutputStream, attribute,
                                object));
            }
        } catch (IOException | CancellationException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Cannot save sessions in " + sessionFile);
        }
    }

    private void writeSessionAttribute(final ObjectOutputStream draftOut, final ObjectOutputStream sessionOut,
                                       final String attribute, final Object object) {
        if (attribute == null || !(object instanceof Serializable))
            return;
        // First we try to write it to the draftOutputStream
        try {
            draftOut.writeObject(object);
        } catch (IOException e) {
            LOGGER.warning(() -> "Cannot write attribute session object (draft test) " + attribute + " - " +
                    object.getClass() + " - " + e.getMessage());
            return;
        }
        try {
            sessionOut.writeUTF(attribute); // Attribute name stored as string
            sessionOut.writeObject(object);
        } catch (IOException e) {
            // The attribute cannot be written, we abort
            throw new CancellationException(
                    "Cannot write session attribute " + attribute + ": persistence aborted - " + object.getClass() +
                            " - " + e.getMessage());
        }
    }

    @Override
    public Map<String, PersistentSession> loadSessionAttributes(final String deploymentName,
                                                                final ClassLoader classLoader) {
        final Path deploymentDir = sessionDir.resolve(deploymentName);
        if (!Files.exists(deploymentDir) || !Files.isDirectory(deploymentDir))
            return null;
        try {
            final long time = System.currentTimeMillis();
            final Map<String, PersistentSession> finalMap = new HashMap<>();
            try (final Stream<Path> stream = Files.list(deploymentDir)) {
                stream.filter(p -> Files.isRegularFile(p)).forEach(sessionPath -> {
                    final File sessionFile = sessionPath.toFile();
                    final PersistentSession persistentSession = readSession(sessionFile);
                    if (persistentSession != null && persistentSession.getExpiration().getTime() > time)
                        finalMap.put(sessionFile.getName(), persistentSession);
                    try {
                        FileUtils.deleteDirectory(sessionPath);
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, e, () -> "Cannot delete session file " + sessionFile);
                    }
                });
            }
            return finalMap.isEmpty() ? null : finalMap;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e, () -> "Cannot read sessions in " + deploymentDir);
            return null;
        }
    }

    private PersistentSession readSession(final File sessionFile) {
        try {
            try (final FileInputStream fileInputStream = new FileInputStream(sessionFile)) {
                try (final ObjectInputStream in = new ObjectInputStream(fileInputStream)) {
                    final Date expDate = new Date(in.readLong());
                    final HashMap<String, Object> sessionData = new HashMap<>();
                    while (true) {
                        try {
                            readSessionAttribute(in, sessionData);
                        } catch (EOFException e) {
                            // Ok we reached the end of the file
                            break;
                        }
                    }
                    return new PersistentSession(expDate, sessionData);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e, () -> "Cannot load sessions from " + sessionFile);
            return null;
        }
    }

    private void readSessionAttribute(final ObjectInputStream in, final Map<String, Object> sessionData)
            throws IOException {
        final String attribute = in.readUTF();
        try {
            sessionData.put(attribute, in.readObject());
        } catch (ClassNotFoundException | NotSerializableException e) {
            LOGGER.log(Level.WARNING, e, () -> "The attribute " + attribute + " cannot be de-serialized");
        }
    }

    @Override
    public void clear(final String deploymentName) {
        final Path deploymentDir = sessionDir.resolve(deploymentName);
        try {
            FileUtils.deleteDirectory(deploymentDir);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e, () -> "Session cleanup failure: " + deploymentDir);
        }
    }
}
