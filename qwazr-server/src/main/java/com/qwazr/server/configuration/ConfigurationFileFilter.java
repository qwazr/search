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
package com.qwazr.server.configuration;

import com.qwazr.utils.WildcardMatcher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

class ConfigurationFileFilter implements Predicate<Path> {

    private final List<Matcher> patterns;
    private final boolean noMatchResult;

    ConfigurationFileFilter(final String[] patternArray) {
        patterns = new ArrayList<>();
        if (patternArray == null) {
            noMatchResult = true;
            return;
        }
        int inclusionCount = 0;
        for (final String pattern : patternArray) {
            if (pattern.startsWith("!"))
                patterns.add(new Matcher(pattern.substring(1), false));
            else {
                patterns.add(new Matcher(pattern, true));
                inclusionCount++;
            }
        }
        noMatchResult = inclusionCount == 0;
    }

    @Override
    final public boolean test(final Path path) {
        if (!Files.isRegularFile(path))
            return false;
        if (patterns == null || patterns.isEmpty())
            return true;
        final Path fileNamePath = path.getFileName();
        if (fileNamePath == null)
            return false;
        final String fileName = fileNamePath.toString();
        for (Matcher matcher : patterns)
            if (matcher.match(fileName))
                return matcher.result;
        return noMatchResult;
    }

    private static class Matcher extends WildcardMatcher {

        private final boolean result;

        private Matcher(final String pattern, final boolean result) {
            super(pattern);
            this.result = result;
        }
    }
}
