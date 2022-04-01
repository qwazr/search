/*
 * Copyright 2014-2016 Emmanuel Keller / QWAZR
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

import org.apache.commons.codec.binary.Base64;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class StringUtils extends org.apache.commons.lang3.StringUtils {

    public static String replaceConsecutiveSpaces(final String source, final String replace) {
        if (isEmpty(source))
            return source;
        StringBuilder target = new StringBuilder();
        int l = source.length();
        boolean consecutiveSpace = false;
        for (int i = 0; i < l; i++) {
            char c = source.charAt(i);
            if (Character.isWhitespace(c)) {
                if (!consecutiveSpace) {
                    if (replace != null)
                        target.append(replace);
                    consecutiveSpace = true;
                }
            } else {
                target.append(c);
                if (consecutiveSpace)
                    consecutiveSpace = false;
            }
        }
        return target.toString();
    }

    public static Pattern wildcardPattern(String s) {
        final CharSequence[] esc = {"\\", ".", "(", ")", "[", "]", "+", "?", "*"};
        final CharSequence[] replace = {"/", "\\.", "\\(", "\\)", "\\[", "\\]", "\\+", "\\?", ".*"};
        s = s.trim();
        int i = 0;
        for (CharSequence ch : esc)
            s = s.replace(ch, replace[i++]);
        return Pattern.compile(s);
    }

    /**
     * @param text the text to encode
     * @return a base64 encoded string
     * @throws UnsupportedEncodingException if the encoding is not supported
     */
    public static String base64encode(final String text) throws UnsupportedEncodingException {
        if (isEmpty(text))
            return null;
        return Base64.encodeBase64URLSafeString(text.getBytes("UTF-8"));
    }

    /**
     * @param base64String the base64 string to decode
     * @return a decoded string
     */
    public static String base64decode(final String base64String) {
        if (isEmpty(base64String))
            return null;
        return new String(Base64.decodeBase64(base64String), StandardCharsets.UTF_8);
    }

    public static int compareNullValues(final Object v1, final Object v2) {
        if (v1 == null) {
            if (v2 == null)
                return 0;
            return -1;
        }
        if (v2 == null)
            return 1;
        return 0;
    }

    public static int compareNullString(final String v1, final String v2) {
        if (v1 == null) {
            if (v2 == null)
                return 0;
            return -1;
        }
        if (v2 == null)
            return 1;
        return v1.compareTo(v2);
    }

    public static int compareNullHashCode(final Object o1, final Object o2) {
        if (o1 == null) {
            if (o2 == null)
                return 0;
            return -1;
        }
        if (o2 == null)
            return 1;
        return o2.hashCode() - o1.hashCode();
    }

    public static String leftPad(final int value, final int size) {
        return org.apache.commons.lang3.StringUtils.leftPad(Integer.toString(value), size, '0');
    }

    public static String leftPad(final long value, final int size) {
        return org.apache.commons.lang3.StringUtils.leftPad(Long.toString(value), size, '0');
    }

    public static String[] toStringArray(final Collection<? extends Object> collection, final boolean sort) {
        if (collection == null)
            return null;
        final String[] array = new String[collection.size()];
        int i = 0;
        for (Object o : collection)
            array[i++] = o.toString();
        if (sort)
            Arrays.sort(array);
        return array;
    }

    public static CharSequence fastConcatCharSequence(final CharSequence... charSeqs) {
        if (charSeqs == null)
            return null;
        if (charSeqs.length == 1)
            return charSeqs[0];
        StringBuilder sb = new StringBuilder();
        for (CharSequence charSeq : charSeqs)
            if (charSeq != null)
                sb.append(charSeq);
        return sb;
    }

    public static String fastConcat(final CharSequence... charSeqs) {
        CharSequence cs = fastConcatCharSequence(charSeqs);
        return cs == null ? null : cs.toString();
    }

    public static void appendArray(final StringBuilder sb, final Object[] array) {
        for (Object object : array)
            appendObject(sb, object);
    }

    public static void appendCollection(final StringBuilder sb, final Collection<?> collection) {
        for (Object object : collection)
            appendObject(sb, object);
    }

    public static void appendObject(StringBuilder sb, Object object) {
        if (object instanceof Collection<?>)
            appendCollection(sb, (Collection<?>) object);
        else if (object instanceof Object[])
            appendArray(sb, (Object[]) object);
        else
            sb.append(object.toString());
    }

    public static String joinWithSeparator(final char separator, final Object... objects) {
        if (objects == null)
            throw new IllegalArgumentException("Object varargs must not be null");
        final List<String> fragments = new ArrayList<>();
        for (final Object object : objects) {
            if (object == null)
                continue;
            final String[] stringArray = StringUtils.split(object.toString(), separator);
            if (stringArray == null)
                continue;
            for (String string : stringArray)
                if (string != null && !string.isEmpty())
                    fragments.add(string);
        }
        return StringUtils.join(fragments, separator);
    }

    public static CharSequence fastConcatCharSequence(final Object... objects) {
        if (objects == null)
            return null;
        if (objects.length == 1)
            return objects[0].toString();
        final StringBuilder sb = new StringBuilder();
        for (final Object object : objects) {
            if (object != null)
                appendObject(sb, object);
        }
        return sb;
    }

    public static String fastConcat(final Object... objects) {
        CharSequence cs = fastConcatCharSequence(objects);
        return cs == null ? null : cs.toString();
    }

    /**
     * Retrieve the lines found in the passed text
     *
     * @param text              a text
     * @param collectEmptyLines true if the empty lines should be collected
     * @param lineCollector     the collection filled with the found lines
     * @return the number of lines found
     * @throws IOException if any I/O error occurs
     */
    public static int linesCollector(final String text, final boolean collectEmptyLines,
                                     final Collection<String> lineCollector) throws IOException {
        if (text == null)
            return 0;
        int i = 0;
        try (StringReader sr = new StringReader(text)) {
            try (BufferedReader br = new BufferedReader(sr)) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (!collectEmptyLines && line.length() == 0)
                        continue;
                    lineCollector.add(line);
                    i++;
                }
            }
        }
        return i;
    }

    /**
     * Escape the chars
     *
     * @param source        the string to escape
     * @param escaped_chars a list of char to escape
     * @return the escaped string
     */
    public static String escapeChars(final String source, final char[] escaped_chars) {
        if (escaped_chars == null || escaped_chars.length == 0)
            return source;
        if (source == null || source.length() == 0)
            return source;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            for (char ec : escaped_chars)
                if (c == ec)
                    sb.append('\\');
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Split the string by line separator
     *
     * @param str the string to split
     * @return an array with one item per line
     */
    public static String[] splitLines(final String str) {
        return split(str, System.lineSeparator());
    }

    /**
     * Ensure the string ends with the given suffix
     *
     * @param str    the string to test
     * @param suffix the suffix to add
     * @return a string
     */
    public static String ensureSuffix(final String str, final String suffix) {
        if (suffix == null)
            return str;
        if (str == null)
            return null;
        if (str.endsWith(suffix))
            return str;
        return str + suffix;
    }

    /**
     * @param text         the string to check
     * @param replacements the replacement map
     * @return the new string with the replacements
     */
    public static String replaceEach(final String text, final Map<String, Object> replacements) {
        if (replacements == null)
            return text;
        String[] search = ArrayUtils.toArray(replacements.keySet());
        String[] replace = ArrayUtils.toStringArray(replacements.values());
        return replaceEach(text, search, replace);
    }

    /**
     * @param chars the analyzed char sequence
     * @return true if the char sequence contains any digit
     */
    public static boolean anyDigit(final CharSequence chars) {
        return chars != null && chars.chars().anyMatch(Character::isDigit);
    }

    /**
     * @param chars the analyzed char sequence
     * @return true if the char sequence contains any alphabetic character
     */
    public static boolean anyAlpha(final CharSequence chars) {
        return chars != null && chars.chars().anyMatch(Character::isAlphabetic);
    }

    /**
     * @param chars the analyzed char sequence
     * @return true if the char sequence contains any lowercase character
     */
    public static boolean anyLowercase(final CharSequence chars) {
        return chars != null && chars.chars().anyMatch(Character::isLowerCase);
    }

    /**
     * @param chars the analyzed char sequence
     * @return true if the char sequence contains any uppoercase character
     */
    public static boolean anyUpperCase(final CharSequence chars) {
        return chars != null && chars.chars().anyMatch(Character::isUpperCase);
    }

    /**
     * Compress a string using GZIP compression
     *
     * @param text    the text to compress
     * @param charset the charset to use
     * @return the compressed byte array
     * @throws IOException if any I/O error occurs
     */
    public static byte[] compressGzip(final String text, final Charset charset) throws IOException {
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            try (final GZIPOutputStream compressed = new GZIPOutputStream(output)) {
                IOUtils.write(text, compressed, charset);
            }
            return output.toByteArray();
        }
    }

    /**
     * Decompress a byte array to a string using GZIP compression
     *
     * @param bytes the compressed bytes
     * @return the uncompressed text
     * @throws IOException if any I/O error occurs
     */
    public static String decompressGzip(final byte[] bytes, final Charset charset) throws IOException {
        try (final ByteArrayInputStream input = new ByteArrayInputStream(bytes)) {
            try (final GZIPInputStream decompressed = new GZIPInputStream(input)) {
                return IOUtils.toString(decompressed, charset);
            }
        }
    }
}
