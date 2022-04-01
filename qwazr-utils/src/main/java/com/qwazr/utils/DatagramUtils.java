/**
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
package com.qwazr.utils;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Objects;

public class DatagramUtils {

	/**
	 * Send a byte array to the given address using UDP (datagram) transport.
	 *
	 * @param data      the byte array to send
	 * @param offset    the position of the first byte to send
	 * @param length    the number of bytes to send
	 * @param addresses the recipients
	 * @throws IOException if any I/O error occurs
	 */
	public static void send(final byte[] data, final int offset, final int length, final SocketAddress... addresses)
			throws IOException {
		Objects.requireNonNull(data, "The data is null");
		Objects.requireNonNull(addresses, "No recipients: the addresses is null");
		try (final DatagramSocket clientSocket = new DatagramSocket()) {
			for (SocketAddress address : addresses)
				clientSocket.send(new DatagramPacket(data, offset, length, address));
		}
	}

	/**
	 * Send a byte array to the given address using UDP (datagram) transport.
	 *
	 * @param data      the byte array to send
	 * @param offset    the position of the first byte to send
	 * @param length    the number of bytes to send
	 * @param addresses the recipients
	 * @throws IOException if any I/O error occurs
	 */
	public static void send(final byte[] data, final int offset, final int length,
			final Collection<SocketAddress> addresses) throws IOException {
		Objects.requireNonNull(data, "Nothing to send: the data is null");
		Objects.requireNonNull(addresses, "No recipients: the addresses is null");
		try (final DatagramSocket clientSocket = new DatagramSocket()) {
			for (SocketAddress address : addresses)
				clientSocket.send(new DatagramPacket(data, offset, length, address));
		}
	}

	/**
	 * Send a byte array to the given address using UDP (datagram) transport.
	 *
	 * @param data      the byte array to send
	 * @param addresses the recipients
	 * @throws IOException if any I/O error occurs
	 */
	public static void send(final byte[] data, final SocketAddress... addresses) throws IOException {
		Objects.requireNonNull(data, "Nothing to send: the data is null");
		send(data, 0, data.length, addresses);
	}

	/**
	 * Send a byte array to the given address using UDP (datagram) transport.
	 *
	 * @param data      the byte array to send
	 * @param addresses the recipients
	 * @throws IOException if any I/O error occurs
	 */
	public static void send(final byte[] data, final Collection<SocketAddress> addresses) throws IOException {
		Objects.requireNonNull(data, "Nothing to send: the data is null");
		send(data, 0, data.length, addresses);
	}

	/**
	 * Send a serializable object to the given address using UDP (datagram) transport
	 *
	 * @param object    the object to send
	 * @param addresses the recipients
	 * @throws IOException if any I/O error occurs
	 */
	public static void send(final Serializable object, final Collection<SocketAddress> addresses) throws IOException {
		Objects.requireNonNull(object, "Nothing to send: the object is null.");
		send(SerializationUtils.toDefaultCompressedBytes(object), addresses);
	}

	/**
	 * Send a serializable object to the given addresses using UDP (datagram) transport
	 *
	 * @param object    the object to send
	 * @param addresses the recipients
	 * @throws IOException if any I/O error occurs
	 */
	public static void send(final Serializable object, final SocketAddress... addresses) throws IOException {
		Objects.requireNonNull(object, "Nothing to send: the object is null.");
		send(SerializationUtils.toDefaultCompressedBytes(object), addresses);
	}

}
