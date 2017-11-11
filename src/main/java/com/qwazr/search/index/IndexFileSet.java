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

package com.qwazr.search.index;

import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.analysis.CustomAnalyzer;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.utils.HashUtils;
import com.qwazr.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.UUID;

class IndexFileSet {

	final static String INDEX_DATA = "data";
	final static String INDEX_TAXONOMY = "taxonomy";
	final static String REPL_WORK = "repl_work";
	final static String UUID_FILE = "uuid";
	final static String UUID_MASTER_FILE = "uuid.master";
	final static String SETTINGS_FILE = "settings.json";
	final static String FIELDS_FILE = "fields.json";
	final static String ANALYZERS_FILE = "analyzers.json";
	final static String RESOURCES_DIR = "resources";

	final private File uuidFile;
	final File uuidMasterFile;
	final private File settingsFile;
	final File mainDirectory;
	final Path dataDirectory;
	final Path taxonomyDirectory;
	final private File analyzerMapFile;
	final Path resourcesDirectoryPath;
	final private File fieldMapFile;
	final Path replWorkPath;

	IndexFileSet(final Path mainDirectory) {
		this.uuidFile = mainDirectory.resolve(UUID_FILE).toFile();
		this.uuidMasterFile = mainDirectory.resolve(UUID_MASTER_FILE).toFile();
		this.mainDirectory = mainDirectory.toFile();
		this.dataDirectory = mainDirectory.resolve(INDEX_DATA);
		this.taxonomyDirectory = mainDirectory.resolve(INDEX_TAXONOMY);
		this.analyzerMapFile = mainDirectory.resolve(ANALYZERS_FILE).toFile();
		this.resourcesDirectoryPath = mainDirectory.resolve(RESOURCES_DIR);
		this.fieldMapFile = mainDirectory.resolve(FIELDS_FILE).toFile();
		this.settingsFile = mainDirectory.resolve(SETTINGS_FILE).toFile();
		this.replWorkPath = mainDirectory.resolve(REPL_WORK);
	}

	void checkIndexDirectory() throws IOException {
		if (!mainDirectory.exists())
			mainDirectory.mkdir();
		if (!mainDirectory.isDirectory())
			throw new IOException("This name is not valid. No directory exists for this location: " +
					mainDirectory.getAbsolutePath());
	}

	/**
	 * Manage the index UUID
	 *
	 * @return
	 */
	UUID checkUuid() throws IOException {
		final UUID indexUuid;
		if (uuidFile.exists()) {
			if (!uuidFile.isFile())
				throw new IOException("The UUID path is not a file: " + uuidFile);
			indexUuid = UUID.fromString(IOUtils.readFileAsString(uuidFile));
		} else {
			indexUuid = HashUtils.newTimeBasedUUID();
			IOUtils.writeStringAsFile(indexUuid.toString(), uuidFile);
		}
		return indexUuid;
	}

	IndexSettingsDefinition loadSettings() throws IOException {
		return IndexSettingsDefinition.load(settingsFile, () -> IndexSettingsDefinition.EMPTY);
	}

	void writeSettings(final IndexSettingsDefinition settings) throws IOException {
		IndexSettingsDefinition.save(settings, settingsFile);
	}

	LinkedHashMap<String, FieldDefinition> loadFieldMap() throws IOException {
		return FieldDefinition.loadMap(fieldMapFile, LinkedHashMap::new);
	}

	void writeFieldMap(final LinkedHashMap<String, FieldDefinition> fields) throws IOException {
		FieldDefinition.saveMap(fields, fieldMapFile);
	}

	LinkedHashMap<String, CustomAnalyzer.Factory> loadAnalyzerDefinitionMap() throws IOException {
		return CustomAnalyzer.createFactoryMap(AnalyzerDefinition.loadMap(analyzerMapFile, LinkedHashMap::new),
				LinkedHashMap::new);
	}

	void writeAnalyzerDefinitionMap(final LinkedHashMap<String, AnalyzerDefinition> definitionMap) throws IOException {
		AnalyzerDefinition.saveMap(definitionMap, analyzerMapFile);
	}

}
