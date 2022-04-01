/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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

import java.util.Arrays;
import java.util.Collection;

public class ArrayUtils extends org.apache.commons.lang3.ArrayUtils {

    public static boolean startsWith(final byte[] array, final byte[] prefix) {
        if (array == null)
            return false;
        if (prefix == null)
            return false;
        if (prefix.length > array.length)
            return false;
        for (int i = 0; i < prefix.length; i++)
            if (array[i] != prefix[i])
                return false;
        return true;
    }

    public static boolean[] toPrimitiveBoolean(Collection<Boolean> collection) {
        final boolean[] array = new boolean[collection.size()];
        int i = 0;
        for (boolean val : collection)
            array[i++] = val;
        return array;
    }

    public static byte[] toPrimitiveByte(Collection<? extends Number> collection) {
        final byte[] array = new byte[collection.size()];
        int i = 0;
        for (Number val : collection)
            array[i++] = val.byteValue();
        return array;
    }

    public static char[] toPrimitiveChar(Collection<Character> collection) {
        final char[] array = new char[collection.size()];
        int i = 0;
        for (char val : collection)
            array[i++] = val;
        return array;
    }

    public static int[] toPrimitiveInt(Collection<? extends Number> collection) {
        final int[] array = new int[collection.size()];
        int i = 0;
        for (Number val : collection)
            array[i++] = val == null ? 0 : val.intValue();
        return array;
    }

    public static short[] toPrimitiveShort(Collection<? extends Number> collection) {
        final short[] array = new short[collection.size()];
        int i = 0;
        for (Number val : collection)
            array[i++] = val == null ? 0 : val.shortValue();
        return array;
    }

    public static long[] toPrimitiveLong(Collection<? extends Number> collection) {
        final long[] array = new long[collection.size()];
        int i = 0;
        for (Number val : collection)
            array[i++] = val == null ? 0 : val.longValue();
        return array;
    }

    public static double[] toPrimitiveDouble(Collection<? extends Number> collection) {
        final double[] array = new double[collection.size()];
        int i = 0;
        for (Number val : collection)
            array[i++] = val == null ? 0 : val.doubleValue();
        return array;
    }

    public static float[] toPrimitiveFloat(Collection<? extends Number> collection) {
        final float[] array = new float[collection.size()];
        int i = 0;
        for (Number val : collection)
            array[i++] = val == null ? 0 : val.floatValue();
        return array;
    }

    public static String prettyPrint(Collection<?> collection) {
        if (collection == null)
            return null;
        return Arrays.toString(collection.toArray());
    }

    public static String[] toArray(Collection<String> collection) {
        if (collection == null)
            return null;
        final String[] array = new String[collection.size()];
        int i = 0;
        for (String val : collection)
            array[i++] = val;
        return array;
    }

    public static String[] toStringArray(Collection<Object> collection) {
        if (collection == null)
            return null;
        final String[] array = new String[collection.size()];
        int i = 0;
        for (Object val : collection)
            array[i++] = val == null ? null : val.toString();
        return array;
    }
}