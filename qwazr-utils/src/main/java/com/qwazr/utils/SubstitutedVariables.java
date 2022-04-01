/*
 * Copyright 2016-2018 Emmanuel Keller / QWAZR
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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SubstitutedVariables {

    private final Map<String, String> variables;
    private final String prefix;
    private final String suffix;
    private volatile SubstituteList substituteList;

    public SubstitutedVariables(String prefix, String suffix) {
        this.prefix = prefix == null ? StringUtils.EMPTY : prefix;
        this.suffix = suffix == null ? StringUtils.EMPTY : suffix;
        variables = new HashMap<>();
        updateSubstituteList();
    }

    public void putAll(Map<String, String> source) {
        if (source == null)
            return;
        synchronized (variables) {
            source.forEach((key, value) -> variables.put(prefix + key + suffix, value));
            updateSubstituteList();
        }
    }

    public void putAll(Properties properties) {
        if (properties == null)
            return;
        synchronized (variables) {
            properties.forEach((key, value) -> variables
                    .put(prefix + key + suffix, value == null ? StringUtils.EMPTY : value.toString()));
            updateSubstituteList();
        }
    }

    public void clear() {
        synchronized (variables) {
            variables.clear();
            updateSubstituteList();
        }
    }

    private synchronized void updateSubstituteList() {
        substituteList = new SubstituteList(variables);
    }

    public String substitute(String source) {
        if (source == null)
            return null;
        return substituteList.substitute(source);
    }

    private static class SubstituteList {

        private final String[] searchList;
        private final String[] replacementList;

        private SubstituteList(Map<String, String> variables) {
            searchList = new String[variables.size()];
            replacementList = new String[searchList.length];
            int i = 0;
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                searchList[i] = entry.getKey();
                replacementList[i++] = entry.getValue();
            }
        }

        final public String substitute(String source) {
            if (source == null)
                return source;
            if (source.isEmpty())
                return source;
            if (searchList.length == 0)
                return source;
            return StringUtils.replaceEachRepeatedly(source, searchList, replacementList);
        }
    }

    private static volatile SubstitutedVariables environmentVariables = null;

    public synchronized static SubstitutedVariables getEnvironmentVariables() {
        if (environmentVariables != null)
            return environmentVariables;
        if (environmentVariables != null)
            return environmentVariables;
        environmentVariables = new SubstitutedVariables("${", "}");
        environmentVariables.putAll(System.getenv());
        return environmentVariables;
    }

    private static volatile SubstitutedVariables propertiesVariables = null;

    public synchronized static SubstitutedVariables getPropertiesVariables() {
        if (propertiesVariables != null)
            return propertiesVariables;
        if (propertiesVariables != null)
            return propertiesVariables;
        propertiesVariables = new SubstitutedVariables("${", "}");
        propertiesVariables.putAll(System.getProperties());
        return propertiesVariables;
    }

    public static String propertyAndEnvironmentSubstitute(String source) {
        source = getPropertiesVariables().substitute(source);
        return getEnvironmentVariables().substitute(source);
    }

}
