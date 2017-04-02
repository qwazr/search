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
import com.qwazr.utils.json.JsonMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.UUID;

public class IndexFileSet {

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
	final File dataDirectory;
	final File taxonomyDirectory;
	final private File analyzerMapFile;
	final File resourcesDirectory;
	final private File fieldMapFile;
	final Path replWorkPath;

	IndexFileSet(final File mainDirectory) {
		this.uuidFile = new File(mainDirectory, UUID_FILE);
		this.uuidMasterFile = new File(mainDirectory, UUID_MASTER_FILE);
		this.mainDirectory = mainDirectory;
		this.dataDirectory = new File(mainDirectory, INDEX_DATA);
		this.taxonomyDirectory = new File(mainDirectory, INDEX_TAXONOMY);
		this.analyzerMapFile = new File(mainDirectory, ANALYZERS_FILE);
		this.resourcesDirectory = new File(mainDirectory, RESOURCES_DIR);
		this.fieldMapFile = new File(mainDirectory, FIELDS_FILE);
		this.settingsFile = new File(mainDirectory, SETTINGS_FILE);
		this.replWorkPath = mainDirectory.toPath().resolve(REPL_WORK);
	}

	void checkIndexDirectory() throws IOException {
		if (!mainDirectory.exists())
			mainDirectory.mkdir();
		if (!mainDirectory.isDirectory())
			throw new IOException("This name is not valid. No directory exists for this location: "
					+ mainDirectory.getAbsolutePath());
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
		return settingsFile.exists() ?
				JsonMapper.MAPPER.readValue(settingsFile, IndexSettingsDefinition.class) :
				IndexSettingsDefinition.EMPTY;
	}

	void writeSettings(final IndexSettingsDefinition settings) throws IOException {
		if (settings == null)
			Files.deleteIfExists(settingsFile.toPath());
		else
			JsonMapper.MAPPER.writeValue(settingsFile, settings);
	}

	LinkedHashMap<String, FieldDefinition> loadFieldMap() throws IOException {
		return fieldMapFile.exists() ?
				JsonMapper.MAPPER.readValue(fieldMapFile, FieldDefinition.MapStringFieldTypeRef) :
				new LinkedHashMap<>();
	}

	void writeFieldMap(final LinkedHashMap<String, FieldDefinition> fieldMap) throws IOException {
		if (fieldMap == null)
			Files.deleteIfExists(fieldMapFile.toPath());
		else
			JsonMapper.MAPPER.writeValue(fieldMapFile, fieldMap);
	}

	LinkedHashMap<String, CustomAnalyzer.Factory> loadAnalyzerDefinitionMap() throws IOException {
		return analyzerMapFile.exists() ?
				CustomAnalyzer.createFactoryMap(
						JsonMapper.MAPPER.readValue(analyzerMapFile, AnalyzerDefinition.MapStringAnalyzerTypeRef)) :
				new LinkedHashMap<>();
	}

	void writeAnalyzerDefinitionMap(final LinkedHashMap<String, AnalyzerDefinition> definitionMap) throws IOException {
		if (definitionMap == null) {
			Files.deleteIfExists(analyzerMapFile.toPath());
			return;
		}
		JsonMapper.MAPPER.writeValue(analyzerMapFile, definitionMap);
	}

}
