/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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

import com.qwazr.externalizor   .Externalizor;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class SerializationUtils {

	/**
	 * Build a compressed byte array from an serializable object using Externalizor
	 *
	 * @param object the object to serialize
	 * @return the serialized object as a byte array
	 * @throws IOException                  if any I/O error occurs
	 * @throws ReflectiveOperationException if the class cannot be constructed
	 * @see Externalizor
	 */
	public static byte[] toExternalizorBytes(final Serializable object)
			throws IOException, ReflectiveOperationException {
		try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			Externalizor.serialize(object, output);
			return output.toByteArray();
		}
	}

	/**
	 * Deserialize an object from a compressed byte array using Externalizor
	 *
	 * @param bytes the serialized bytes
	 * @param clazz the type of the container object
	 * @param <T>   the generic type of the container object
	 * @return the filled object
	 * @throws IOException                  if any I/O error occurs
	 * @throws ReflectiveOperationException if the object cannot be constructed
	 * @see Externalizor
	 */
	public static <T extends Serializable> T fromExternalizorBytes(final byte[] bytes,
			final Class<? extends Serializable> clazz) throws IOException, ReflectiveOperationException {
		try (final ByteArrayInputStream input = new ByteArrayInputStream(bytes)) {
			return Externalizor.deserialize(input, (Class<T>) clazz);
		}
	}

	/**
	 * Build a byte array using the standard Java serialization
	 *
	 * @param object the object to serialize
	 * @return the serialized object as a byte array
	 * @throws IOException if any I/O error occurs
	 */
	public static byte[] toDefaultBytes(final Serializable object) throws IOException {
		try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			try (final ObjectOutputStream objected = new ObjectOutputStream(output)) {
				objected.writeObject(object);
			}
			return output.toByteArray();
		}
	}

	/**
	 * Deserialize an object using Java default serialization
	 *
	 * @param bytes the serialized bytes
	 * @param <T>   the generic type of the container object
	 * @return the filled object
	 * @throws IOException            if any I/O error occurs
	 * @throws ClassNotFoundException if the object cannot be constructed
	 */

	public static <T extends Serializable> T fromDefaultBytes(final byte[] bytes)
			throws IOException, ClassNotFoundException {
		try (final ByteArrayInputStream input = new ByteArrayInputStream(bytes)) {
			try (final ObjectInputStream objected = new ObjectInputStream(input)) {
				return (T) objected.readObject();
			}
		}
	}

	/**
	 * Build a byte array using the standard Java serialization with GZIP compression
	 *
	 * @param object the object to serialize
	 * @return the serialized object as a byte array
	 * @throws IOException if any I/O error occurs
	 */
	public static byte[] toDefaultCompressedBytes(final Serializable object) throws IOException {
		try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			try (final GZIPOutputStream compressed = new GZIPOutputStream(output)) {
				try (final ObjectOutputStream objected = new ObjectOutputStream(compressed)) {
					objected.writeObject(object);
				}
			}
			return output.toByteArray();
		}
	}

	/**
	 * Deserialize an object using Java default serialization with GZIP compression
	 *
	 * @param bytes the serialized bytes
	 * @param <T>   the generic type of the container object
	 * @return the filled object
	 * @throws IOException            if any I/O error occurs
	 * @throws ClassNotFoundException if the object cannot be constructed
	 */
	public static <T extends Serializable> T fromDefaultCompressedBytes(final byte[] bytes)
			throws IOException, ClassNotFoundException {
		try (final ByteArrayInputStream input = new ByteArrayInputStream(bytes)) {
			try (final GZIPInputStream compressed = new GZIPInputStream(input)) {
				try (final ObjectInputStream objected = new ObjectInputStream(compressed)) {
					return (T) objected.readObject();
				}
			}
		}
	}

}
