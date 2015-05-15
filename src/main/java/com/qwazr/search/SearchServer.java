/**
 * Copyright 2015 Emmanuel Keller / QWAZR
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
package com.qwazr.search;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import com.qwazr.cluster.ClusterServer;
import com.qwazr.cluster.service.ClusterServiceImpl;
import com.qwazr.search.index.IndexManager;
import com.qwazr.search.index.IndexServiceImpl;
import com.qwazr.utils.server.AbstractServer;
import com.qwazr.utils.server.RestApplication;
import com.qwazr.utils.server.ServletApplication;

public class SearchServer extends AbstractServer {

	public final static String SERVICE_NAME_INDEX = "index";

	private final static ServerDefinition serverDefinition = new ServerDefinition();
	static {
		serverDefinition.mainJarPath = "qwazr-search.jar";
		serverDefinition.defaultDataDirName = "qwazr";
		serverDefinition.defaultWebServiceTcpPort = 9091;

	}

	private SearchServer() {
		super(serverDefinition);
	}

	public static class SearchApplication extends RestApplication {

		@Override
		public Set<Class<?>> getClasses() {
			Set<Class<?>> classes = super.getClasses();
			classes.add(ClusterServiceImpl.class);
			classes.add(IndexServiceImpl.class);
			return classes;
		}
	}

	@Override
	public void commandLine(CommandLine cmd) throws IOException, ParseException {
	}

	public static void checkDirectoryExists(File directoryFile)
			throws IOException {
		if (!directoryFile.exists())
			directoryFile.mkdir();
		if (!directoryFile.isDirectory())
			throw new IOException(
					"This name is not valid. No directory exists for this location: "
							+ directoryFile.getName());
	}

	public static void loadIndexManager(File dataDirectory) throws IOException {
		File indexDir = new File(dataDirectory, SERVICE_NAME_INDEX);
		checkDirectoryExists(indexDir);
		IndexManager.load(indexDir);
	}

	@Override
	public void load() throws IOException {
		File currentDataDir = getCurrentDataDir();
		ClusterServer.load(getWebServicePublicAddress(), currentDataDir);
		loadIndexManager(currentDataDir);

	}

	@Override
	protected RestApplication getRestApplication() {
		return new SearchApplication();
	}

	@Override
	protected ServletApplication getServletApplication() {
		return null;
	}

	public static void main(String[] args) throws IOException, ParseException,
			ServletException {
		new SearchServer().start(args);
	}

}
