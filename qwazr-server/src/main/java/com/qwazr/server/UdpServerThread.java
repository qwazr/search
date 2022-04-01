/*
 * s * Copyright 2016-2017 Emmanuel Keller / QWAZR
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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UdpServerThread extends Thread {

    private static final Logger LOGGER = LoggerUtils.getLogger(UdpServerThread.class);

    private final PacketListener[] packetListeners;

    private final AtomicBoolean isShutdown;

    private final InetSocketAddress socketAddress;
    private final InetAddress multicastGroupAddress;
    private final Integer multicastPort;
    private volatile DatagramSocket datagramSocket;

    private UdpServerThread(final InetSocketAddress socketAddress, final InetAddress multicastGroupAddress,
            final Integer multicastPort, final Collection<PacketListener> packetListeners) {
        super();
        setName("UDP Server");
        setDaemon(true);
        this.isShutdown = new AtomicBoolean(false);
        this.socketAddress = socketAddress;
        this.multicastGroupAddress = multicastGroupAddress;
        this.multicastPort = multicastPort;
        this.packetListeners = packetListeners.toArray(new PacketListener[0]);
        this.datagramSocket = null;
    }

    UdpServerThread(final InetSocketAddress socketAddress, final Collection<PacketListener> packetListeners) {
        this(socketAddress, null, null, packetListeners);
    }

    UdpServerThread(final String multicastGroupAddress, final int multicastPort,
            final Collection<PacketListener> packetListeners) throws UnknownHostException {
        this(null, InetAddress.getByName(multicastGroupAddress), multicastPort, packetListeners);
    }

    @Override
    public void run() {
        try (final DatagramSocket socket = multicastGroupAddress != null ?
                new MulticastSocket(multicastPort) :
                new DatagramSocket(socketAddress)) {
            this.datagramSocket = socket;
            if (multicastGroupAddress != null)
                ((MulticastSocket) socket).joinGroup(multicastGroupAddress);
            LOGGER.info(() -> "UDP Server started: " + socket.getLocalSocketAddress());
            while (!isShutdown.get()) {
                final byte[] dataBuffer = new byte[65536];
                final DatagramPacket datagramPacket = new DatagramPacket(dataBuffer, dataBuffer.length);
                socket.receive(datagramPacket);
                for (PacketListener packetListener : packetListeners) {
                    try {
                        packetListener.acceptPacket(datagramPacket);
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, e.getMessage(), e);
                    }
                }
            }
        } catch (IOException e) {
            if (!isShutdown.get())
                throw new RuntimeException("Error on UDP server " + socketAddress, e);
        } finally {
            LOGGER.info(() -> "UDP Server exit: " + socketAddress);
        }
    }

    /**
     * Start or restart the thread if it is stopped
     */
    synchronized void checkStarted() {
        if (isAlive())
            return;
        this.start();
    }

    void shutdown() {
        isShutdown.set(true);
        if (datagramSocket != null) {
            if (multicastGroupAddress != null) {
                try {
                    ((MulticastSocket) datagramSocket).leaveGroup(multicastGroupAddress);
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, e.getMessage(), e);
                }
            }
            if (datagramSocket.isConnected())
                datagramSocket.disconnect();
            if (!datagramSocket.isClosed())
                datagramSocket.close();
            datagramSocket = null;
        }
    }

    public interface PacketListener {

        void acceptPacket(final DatagramPacket packet);
    }
}
