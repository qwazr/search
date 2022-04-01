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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExpUtils {

    public static List<Matcher> getMatcherList(final Collection<String> patternList) {
        if (patternList == null || patternList.isEmpty())
            return null;
        final List<Matcher> matcherList = new ArrayList<>(patternList.size());
        for (String pattern : patternList)
            matcherList.add(Pattern.compile(pattern).matcher(StringUtils.EMPTY));
        return matcherList;
    }

    public static boolean anyMatch(final String value, final Collection<Matcher> matcherList) {
        for (final Matcher matcher : matcherList) {
            synchronized (matcher) {
                matcher.reset(value);
                if (matcher.find())
                    return true;
            }
        }
        return false;
    }

    public static String removeAllMatches(String value, final Collection<Matcher> matcherList) {
        for (final Matcher matcher : matcherList) {
            synchronized (matcher) {
                value = matcher.reset(value).replaceAll(StringUtils.EMPTY);
            }
        }
        return value;
    }
}
