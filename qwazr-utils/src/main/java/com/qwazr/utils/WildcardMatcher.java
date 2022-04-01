/*
 * Copyright 2014-2020 Emmanuel Keller / QWAZR
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

import java.util.ArrayList;
import java.util.Objects;
import java.util.Stack;
import org.apache.commons.io.IOCase;

/**
 * This class is inspired by  {@link org.apache.commons.io.FilenameUtils}
 */
public class WildcardMatcher extends Equalizer.Immutable<WildcardMatcher> {

    private final String[] wcs;

    private final String pattern;

    public WildcardMatcher(final String pattern) {
        super(WildcardMatcher.class);
        this.pattern = pattern;
        wcs = pattern == null ? null : splitOnTokens(pattern);
    }

    public String getPattern() {
        return pattern;
    }

    /**
     * Match the passed name with the current pattern
     *
     * @param name            the string to test
     * @param caseSensitivity enable or disable the case sentisitiviy
     * @return true if the name match the pattern
     */
    public boolean match(final String name, IOCase caseSensitivity) {
        if (name == null && wcs == null) {
            return true;
        }
        if (name == null || wcs == null) {
            return false;
        }
        if (caseSensitivity == null) {
            caseSensitivity = IOCase.SENSITIVE;
        }
        final int length = name.length();
        boolean anyChars = false;
        int textIdx = 0;
        int wcsIdx = 0;
        final Stack<int[]> backtrack = new Stack<>();

        // loop around a backtrack stack, to handle complex * matching
        do {
            if (backtrack.size() > 0) {
                final int[] array = backtrack.pop();
                wcsIdx = array[0];
                textIdx = array[1];
                anyChars = true;
            }

            // loop whilst tokens and text left to process
            while (wcsIdx < wcs.length) {

                if (wcs[wcsIdx].equals("?")) {
                    // ? so move to next text char
                    textIdx++;
                    if (textIdx > length) {
                        break;
                    }
                    anyChars = false;

                } else if (wcs[wcsIdx].equals("*")) {
                    // set any chars status
                    anyChars = true;
                    if (wcsIdx == wcs.length - 1) {
                        textIdx = length;
                    }

                } else {
                    // matching text token
                    if (anyChars) {
                        // any chars then try to locate text token
                        textIdx = caseSensitivity.checkIndexOf(name, textIdx, wcs[wcsIdx]);
                        if (textIdx == -1) {
                            // token not found
                            break;
                        }
                        int repeat = caseSensitivity.checkIndexOf(name, textIdx + 1, wcs[wcsIdx]);
                        if (repeat >= 0) {
                            backtrack.push(new int[]{wcsIdx, repeat});
                        }
                    } else {
                        // matching from current position
                        if (!caseSensitivity.checkRegionMatches(name, textIdx, wcs[wcsIdx])) {
                            // couldnt match token
                            break;
                        }
                    }

                    // matched text token, move text index to end of matched token
                    textIdx += wcs[wcsIdx].length();
                    anyChars = false;
                }

                wcsIdx++;
            }

            // full match
            if (wcsIdx == wcs.length && textIdx == length) {
                return true;
            }

        } while (backtrack.size() > 0);

        return false;
    }

    /**
     * Case insensitive match
     *
     * @param name the string to test
     * @return true if the name matches the pattern
     */
    public boolean match(final String name) {
        return match(name, IOCase.INSENSITIVE);
    }

    /**
     * Splits a string into a number of tokens.
     * The text is split by '?' and '*'.
     * Where multiple '*' occur consecutively they are collapsed into a single '*'.
     *
     * @param text the text to split
     * @return the array of tokens, never null
     */
    static String[] splitOnTokens(final String text) {
        // used by wildcardMatch
        // package level so a unit test may run on this

        if (text.indexOf('?') == -1 && text.indexOf('*') == -1) {
            return new String[]{text};
        }

        char[] array = text.toCharArray();
        final ArrayList<String> list = new ArrayList<>();
        final StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (array[i] == '?' || array[i] == '*') {
                if (buffer.length() != 0) {
                    list.add(buffer.toString());
                    buffer.setLength(0);
                }
                if (array[i] == '?') {
                    list.add("?");
                } else if (list.isEmpty() || i > 0 && !list.get(list.size() - 1).equals("*")) {
                    list.add("*");
                }
            } else {
                buffer.append(array[i]);
            }
        }
        if (buffer.length() != 0) {
            list.add(buffer.toString());
        }

        return list.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    @Override
    protected boolean isEqual(final WildcardMatcher query) {
        return Objects.equals(pattern, query.pattern);
    }

    @Override
    protected int computeHashCode() {
        return Objects.hashCode(pattern);
    }
}
