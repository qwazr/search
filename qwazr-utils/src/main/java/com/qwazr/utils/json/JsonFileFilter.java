/*
 * Copyright 2014-2018 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.utils.json;

import java.io.File;
import java.io.FileFilter;

public class JsonFileFilter implements FileFilter {

    public final static String EXT_JS = ".js";
    public final static String EXT_JSON = ".json";

    private final static String[] EXTENSIONS = {EXT_JS, EXT_JSON};

    public final static JsonFileFilter INSTANCE = new JsonFileFilter();

    @Override
    public boolean accept(final File file) {
        if (!file.isFile())
            return false;
        final String name = file.getName().toLowerCase();
        for (final String extension : EXTENSIONS)
            if (name.endsWith(extension))
                return true;
        return false;
    }

}
