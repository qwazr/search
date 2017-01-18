/**
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
package com.qwazr.search.field;

import com.qwazr.server.ServerException;
import com.qwazr.utils.SerializationUtils;
import com.qwazr.utils.StringUtils;

import javax.ws.rs.core.Response;
import java.io.Externalizable;
import java.io.IOException;
import java.io.Serializable;

interface TypeUtils {

	static int getIntNumber(final String fieldName, final Object value) {
		if (value == null)
			throw new ServerException(Response.Status.NOT_ACCEPTABLE,
					"Cannot extract an integer from a null value for the field " + fieldName);
		return value instanceof Number ? ((Number) value).intValue() : Integer.valueOf(value.toString());
	}

	static long getLongNumber(final String fieldName, final Object value) {
		if (value == null)
			throw new ServerException(Response.Status.NOT_ACCEPTABLE,
					"Cannot extract an long from a null value for the field " + fieldName);
		return value instanceof Number ? ((Number) value).longValue() : Long.valueOf(value.toString());
	}

	static float getFloatNumber(final String fieldName, final Object value) {
		if (value == null)
			throw new ServerException(Response.Status.NOT_ACCEPTABLE,
					"Cannot extract an float from a null value for the field " + fieldName);
		return value instanceof Number ? ((Number) value).floatValue() : Float.valueOf(value.toString());
	}

	static double getDoubleNumber(final String fieldName, final Object value) {
		if (value == null)
			throw new ServerException(Response.Status.NOT_ACCEPTABLE,
					"Cannot extract an double from a null value for the field " + fieldName);
		return value instanceof Number ? ((Number) value).doubleValue() : Double.valueOf(value.toString());
	}

	static String[] getStringArray(String fieldName, Object[] values, int start) {
		if (values == null)
			throw new ServerException(Response.Status.NOT_ACCEPTABLE,
					"Cannot extract an array from a null value for the field " + fieldName);
		if (start > values.length)
			throw new ServerException(Response.Status.NOT_ACCEPTABLE,
					"Not enough value in the array for the field " + fieldName);
		final String[] array = new String[values.length - start];
		int j = 0;
		for (int i = start; i < values.length; i++)
			array[j++] = values[i].toString();
		return array;
	}

	static byte[] toBytes(final String fieldName, final Serializable value) {
		try {
			return SerializationUtils.toExternalizorBytes(value);
		} catch (IOException | ReflectiveOperationException e) {
			throw new ServerException(Response.Status.NOT_ACCEPTABLE,
					"Cannot serialize the value of the field " + fieldName, e);
		}
	}

	static byte[] toBytes(final String fieldName, final Externalizable value) {
		try {
			return SerializationUtils.toExternalizorBytes(value);
		} catch (IOException | ReflectiveOperationException e) {
			throw new ServerException(Response.Status.NOT_ACCEPTABLE,
					"Cannot serialize the value of the field " + fieldName, e);
		}
	}

	static <T> T notNull(final T value, final String fieldName, final String msg) {
		if (value != null)
			return value;
		throw new RuntimeException("Error on field: " + fieldName + " - " + msg == null ? StringUtils.EMPTY : msg);
	}

}
